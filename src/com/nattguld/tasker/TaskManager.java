package com.nattguld.tasker;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.nattguld.tasker.callbacks.ICallback;
import com.nattguld.tasker.cfg.TaskConfig;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskFuture;
import com.nattguld.tasker.tasks.TaskPoolExecutor;
import com.nattguld.tasker.tasks.TaskProperty;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public class TaskManager {
	
	/**
	 * The pending tasks update cycle delay.
	 */
	private static final int PENDING_UPDATE_DELAY = 10000;
	
	/**
	 * Holds tasks delayed tasks before becoming active.
	 */
	private static List<Task> delayed = new CopyOnWriteArrayList<>();
	
	/**
	 * Holds the currently active tasks.
	 */
    private static Map<Task, Future<?>> active = new ConcurrentHashMap<>();
    
	/**
	 * Holds the tasks that turned inactive.
	 */
	private static List<Task> inactive = new CopyOnWriteArrayList<>();
	
    /**
     * The executor service for threading.
     */
    private static TaskPoolExecutor executorService;
    
    /**
     * The executor service for threading.
     */
    private static ExecutorService alternateExecutorService = Executors.newCachedThreadPool();

    
    static {
    	RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
				try {
					TaskFuture<?> tf = (TaskFuture<?>)runnable;
					Task task = tf.getTask();

					switch (task.getPolicy()) {
					case DEFAULT:
					case SINGLE:
						if (!delayed.contains(task)) {
							delayed.add(task);
							System.err.println(task.getName() + " has been delayed [Policy: " + task.getPolicy().getName() + "]");
						}
						return;
						
					case FORCE:
						executeAlternatively(task);
						break;
						
					case OPTIONAL:
						System.err.println(task.getName() + " has been rejected and ignored [Policy: " + task.getPolicy().getName() + "]");
						return;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
    	/*executorService = new ThreadPoolExecutor(TaskConfig.getConfig().getMaxParallel(), TaskConfig.getConfig().getMaxParallel(), 60L, TimeUnit.SECONDS
    			, new LinkedBlockingQueue<Runnable>(), rejectedExecutionHandler);*/
		executorService = new TaskPoolExecutor(TaskConfig.getConfig().getMaxParallel(), TaskConfig.getConfig().getMaxParallel(), TaskConfig.getConfig().getMaxQueueSize(), rejectionHandler);
    	//executorService = new TaskPoolExecutor(1, 2, 1, rejectionHandler);
    	executorService.allowCoreThreadTimeOut(true);
    	
    	processTasks();
    }
	
    /**
     * Processes the tasks.
     */
    private static void processTasks() {
    	alternateExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (active.isEmpty() && delayed.isEmpty()) {
							Misc.sleep(PENDING_UPDATE_DELAY);
							continue;
						}
						for (Task task : active.keySet()) {
							boolean timedOut = task.isTimedOut();
							
							if (task.isActive() && !timedOut) {
								continue;
							}
							if (timedOut) {
								System.err.println(task.getName() + " timed out.");
							}
							remove(task);
							
							if (task.hasProperty(TaskProperty.KEEP_ALIVE)) {
								executeAlternatively(task);
							}
						}
						if (executorService.getQueue().size() < executorService.getMaxQueueSize()) {
							for (Task delayedTask : delayed) {
								if (delayedTask.getPolicy() == TaskPolicy.SINGLE 
										&& !taskByClassIsActive(delayedTask.getClass().getSimpleName()).isEmpty()) {
									continue;
								}
								async(delayedTask);
								
								if (executorService.getQueue().size() >= executorService.getMaxQueueSize()) {
									break;
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					Misc.sleep(100);
				}
			}
    	});
    }
    
    /**
     * Updates the max parallel threads count.
     * 
     * @param maxParallel The new count.
     */
	public static void updateMaxParallel(int maxParallel) {
		executorService.setMaximumPoolSize(maxParallel);
	}
    
	/**
	 * Removes a task.
	 * 
	 * @param task The task to remove.
	 */
	public static void remove(Task task) {
		if (active.containsKey(task)) {
			stop(task);
		}
		if (delayed.contains(task)) {
			delayed.remove(task);
		}
		if (inactive.contains(task)) {
			inactive.remove(task);
		}
	}
	
	/**
	 * Stops a task.
	 * 
	 * @param task The task to stop.
	 */
	private static void stop(Task task) {
		if (active.containsKey(task)) {
			stop(active.get(task));
			
			active.remove(task);
			
			if (!task.hasProperty(TaskProperty.DAEMON) 
					&& task.getState() != TaskState.FINISHED
					&& task.getState() != TaskState.CANCEL) {
				inactive.add(task);
			}
		}
	}
	
	/**
	 * Stops a future.
	 * 
	 * @param future The future to stop.
	 */
	private static void stop(Future<?> future) {
		if (Objects.nonNull(future)) {
			future.cancel(true);
		}
	}
	
    /**
     * Executes a task alternatively ignoring defaults.
     * 
     * @param task The task to execute.
     */
    public static void executeAlternatively(Task task) {
    	Future<?> sf = alternateExecutorService.submit(task);
    	active.put(task, sf);
    }

    /**
     * Submits a task in an asynchronous matter.
     * 
     * @param task The task.
     */
	public static void async(Task task) {
		if (Objects.isNull(task)) {
			System.err.println("Received nulled task");
    		return;
    	}
		if (task.getPolicy() == TaskPolicy.FORCE || task.hasProperty(TaskProperty.KEEP_ALIVE)) {
			executeAlternatively(task);
			return;
		}
		if (task.getPolicy() == TaskPolicy.SINGLE
				&& !taskByClassIsActive(task.getClass().getSimpleName()).isEmpty()) {
			if (!delayed.contains(task)) {
				delayed.add(task);
			}
			return;
		}
		if (delayed.contains(task)) {
			delayed.remove(task);
		}
		Future<?> sf = executorService.submit(task);
    	active.put(task, sf);
	}
	
	/**
	 * Submits a task in a synchronous matter.
	 * 
	 * @param task The task.
	 * 
	 * @return The task state.
	 */
	public static TaskState sync(Task task) {
		async(task);
		
		while (task.isActive()) {
			Misc.sleep(Task.DEFAULT_REPEAT_DELAY);
		}
		return task.getState();
	}
	
	/**
	 * Submits a task and waits for it's callback.
	 * 
	 * @param task the task.

	 * @return The callback.
	 */
	public static Object callback(Task task) {
		if (!(task instanceof ICallback<?>)) {
			System.err.println("Unable to get response from " + task.getName() + " as it's not a callback task.");
			return task.getState();
		}
		sync(task);
		
		return ((ICallback<?>)task).getCallbackResponse().getResponse();
	}
	
	/**
	 * Retries a task.
	 * 
	 * @param task The task to retry.
	 * 
	 * @return The submitted task.
	 */
	public static void retry(Task task) {
		task.reset();
		async(task);
	}
	
	
	/**
	 * Disposes the task manager.
	 */
	public static void dispose() {
		inactive.clear();
		delayed.clear();
		
		for (Task task : active.keySet()) {
			stop(task);
		}
		active.clear();
		executorService.shutdownNow();
		alternateExecutorService.shutdownNow();
	}
	
	/**
	 * Retrieves the active tasks by a given class name.
	 * 
	 * @param className The class name.
	 * 
	 * @return The name.
	 */
	public static List<Task> taskByClassIsActive(String className) {
		return getActiveTasks().stream()
				.filter(t -> t.getClass().getSimpleName().equals(className))
				.collect(Collectors.toList());
	}
	
	/**
	 * Retrieves the pending tasks.
	 * 
	 * @return The pending tasks.
	 */
	public static int getQueueSize() {
		return executorService.getQueue().size() + delayed.size();
	}
	
	/**
	 * Retrieves the delayed tasks.
	 * 
	 * @return The delayed tasks.
	 */
	public static List<Task> getDelayedTasks() {
		return delayed;
	}
	
	/**
	 * Retrieves the active tasks.
	 * 
	 * @return The active tasks.
	 */
	public static Set<Task> getActiveTasks() {
		return active.keySet();
	}
	
	/**
	 * Retrieves the inactive tasks.
	 * 
	 * @return The inactive tasks.
	 */
	public static List<Task> getInactiveTasks() {
		return inactive;
	}
	
	/**
	 * Retrieves whether a task is delayed or not.
	 * 
	 * @param task The task.
	 * 
	 * @return The result.
	 */
	public static boolean isInDelayed(Task task) {
		return delayed.contains(task);
	}
	
	/**
	 * Retrieves the amount of delayed tasks.
	 * 
	 * @return The result.
	 */
	public static int getDelayedCount() {
		return delayed.size();
	}
	
	/**
	 * Retrieves whether a task is queued or not.
	 * 
	 * @param task The task.
	 * 
	 * @return The result.
	 */
	public static boolean isInQueue(Task task) {
		return executorService.getQueue().contains(task);
	}
	
	/**
	 * Retrieves the amount of queued tasks.
	 * 
	 * @return The result.
	 */
	public static int getQueuedCount() {
		return executorService.getQueue().size();
	}
	
	/**
	 * Retrieves the active task count as string.
	 * 
	 * @return The result.
	 */
	public static String getActive() {
		return executorService.getActiveCount() + "/" + executorService.getMaximumPoolSize();
	}
	
	/**
	 * Retrieves the amount of completed tasks.
	 * 
	 * @return The result.
	 */
	public static long getCompleted() {
		return executorService.getCompletedTaskCount();
	}

}
