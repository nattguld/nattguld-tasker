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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.nattguld.tasker.callbacks.ICallback;
import com.nattguld.tasker.cfg.TaskConfig;
import com.nattguld.tasker.tasks.Task;
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
    private static ThreadPoolExecutor executorService;
    
    /**
     * The executor service for threading.
     */
    private static ExecutorService alternateExecutorService = Executors.newCachedThreadPool();
    
    
    static {
    	RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
				Task task = (Task)runnable;
				
				if (task.getPolicy() == TaskPolicy.OPTIONAL) {
					System.err.println(task.getName() + " has been rejected and ignored [Policy: " + task.getPolicy().getName() + "]");
					return;
				}
				try {
		            Future<?> sf = alternateExecutorService.submit(task);
		            active.put(task, sf);
		            
		            System.err.println(task.getName() + " has been rejected but re-executed through alternate executor [Policy: " + task.getPolicy().getName() + "]");
		            
		        } catch(Exception e) {
		        	System.err.println(task.getName() + " has been rejected and failed to re-execute [Policy: " + task.getPolicy().getName() + "]");
		        }
			}
    	};
    	executorService = new ThreadPoolExecutor(TaskConfig.getConfig().getMaxParallel(), TaskConfig.getConfig().getMaxParallel(), 60L, TimeUnit.SECONDS
    			, new LinkedBlockingQueue<Runnable>(), rejectedExecutionHandler);
    	executorService.allowCoreThreadTimeOut(true);
    	
    	processTasks();
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
     * Processes the tasks.
     */
    private static void processTasks() {
    	executorService.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (active.isEmpty() && delayed.isEmpty()) {
							Misc.sleep(PENDING_UPDATE_DELAY);
							continue;
						}
						for (Task task : active.keySet()) {
							if (task.isActive()) {
								continue;
							}
							remove(task);
						}
						if (executorService.getQueue().size() < executorService.getMaximumPoolSize()) {
							for (Task delayedTask : delayed) {
								if (delayedTask.getPolicy() == TaskPolicy.SINGLE 
										&& !taskByClassIsActive(delayedTask.getClass().getSimpleName()).isEmpty()) {
									continue;
								}
								async(delayedTask);
								
								if (executorService.getQueue().size() >= executorService.getMaximumPoolSize()) {
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
     * Submits a task in an asynchronous matter.
     * 
     * @param task The task.
     */
	public static void async(Task task) {
		if (Objects.isNull(task)) {
			System.err.println("Received nulled task");
    		return;
    	}
		if (task.getPolicy() == TaskPolicy.FORCE) {
			executeForcefully(task);
			return;
		}
		if (task.getPolicy() == TaskPolicy.SINGLE
				&& !taskByClassIsActive(task.getClass().getSimpleName()).isEmpty()) {
			delayed.add(task);
			return;
		}
		if (delayed.contains(task)) {
			delayed.remove(task);
		}
		Future<?> sf = executorService.submit(task);
    	active.put(task, sf);
	}
	
	/**
	 * Executes a task forcefully.
	 * 
	 * @param task The task to execute.
	 */
	public static void executeForcefully(Task task) {
		alternateExecutorService.execute(task);
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
		async(task);
		return waitAndGetResponse(task);
	}
	
	/**
	 * Retrieves the task response or waits for it if not assigned yet.
	 * 
	 * @param task The task.
	 * 
	 * @param delay The delay to re-check if a response has been assigned or not.
	 * 
	 * @param timeout The timeout if no response has been received.
	 * 
	 * @return The response.
	 */
	public static Object waitAndGetResponse(Task task) {
		if (!(task instanceof ICallback<?>)) {
			System.err.println("Unable to get response from " + task.getName() + " as it's not a callback task.");
			return task.getState();
		}
		ICallback<?> callback = (ICallback<?>)task;

		while (task.isActive()) {
			Misc.sleep(Task.DEFAULT_REPEAT_DELAY);
		}
		return callback.getCallbackResponse().getResponse();
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
