package com.nattguld.tasker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
	 * Holds tasks pending to become active.
	 */
	private static List<Task> pending = new CopyOnWriteArrayList<>();
	
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
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * The task configurations.
     */
    private static TaskConfig taskConfig;
	
    
    static {
    	taskConfig = new TaskConfig();
    	
    	processPending();
    	processActive();
    }
    
    /**
     * Processes the pending tasks.
     */
    private static void processPending() {
    	executorService.submit(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (pending.isEmpty()) {
							Misc.sleep(PENDING_UPDATE_DELAY);
							continue;
						}
						for (Task task : pending) {
							if (active.size() >= getTaskConfig().getMaxParallel()) {
								continue;
							}
							async(task);
							pending.remove(task);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
    	});
    }
    
    /**
     * Processes the active tasks.
     */
    private static void processActive() {
    	executorService.submit(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (active.isEmpty()) {
							Misc.sleep(PENDING_UPDATE_DELAY);
							continue;
						}
						for (Task task : active.keySet()) {
							if (!task.isActive()) {
								remove(task);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
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
		if (pending.contains(task)) {
			pending.remove(task);
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
		if (active.size() >= getTaskConfig().getMaxParallel()) {
			pending.add(task);
			return;
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
		pending.clear();
		inactive.clear();
		
		for (Task task : active.keySet()) {
			stop(task);
		}
		active.clear();
    	executorService.shutdownNow();
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
	 * Retrieves the tasks with a given class.
	 * 
	 * @param c The class.
	 * 
	 * @return The tasks.
	 */
	public static List<Task> getTasksForClass(Class<?> c) {
		return getAllTasks().stream()
				.filter(t -> t.getClass().isInstance(c))
				.collect(Collectors.toList());
	}
	
	/**
	 * Modifies the task configurations.
	 * 
	 * @param loadedConfig The loaded configurations.
	 */
	public static void setTaskConfig(TaskConfig loadedConfig) {
		taskConfig = loadedConfig;
	}
	
	/**
	 * Retrieves the task configurations.
	 * 
	 * @return The task configurations.
	 */
	public static TaskConfig getTaskConfig() {
		return taskConfig;
	}
	
	/**
	 * Retrieves the pending tasks.
	 * 
	 * @return The pending tasks.
	 */
	public static List<Task> getPendingTasks() {
		return pending;
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
	 * Retrieves all the tasks.
	 * 
	 * @return The tasks.
	 */
	public static List<Task> getAllTasks() {
		List<Task> all = new ArrayList<>();
		
		all.addAll(getActiveTasks());
		all.addAll(getPendingTasks());
		all.addAll(getInactiveTasks());
		
		return all;
	}

}
