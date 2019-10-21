package com.nattguld.tasker.tasks;

import java.util.concurrent.FutureTask;

/**
 * 
 * @author randqm
 *
 */

public class TaskFuture<V> extends FutureTask<V> {
	
	/**
	 * The task.
	 */
	private final Task task;
	
	
	/**
	 * Creates a new task future.
	 * 
	 * @param task The task.
	 * 
	 * @param result The future result.
	 */
	public TaskFuture(Task task, V result) {
		super(task, result);
		
		this.task = task;
	}
	
	/**
	 * Retrieves the task.
	 * 
	 * @return The task.
	 */
	public Task getTask() {
		return task;
	}

}
