package com.nattguld.tasker.tasks;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author randqm
 *
 */

public class TaskPoolExecutor extends ThreadPoolExecutor {

	/**
	 * The maximum queue size.
	 */
	private final int maxQueueSize;
	
	
	/**
	 * Creates a new task pool executor.
	 * 
	 * @param corePoolSize The core pool size.
	 * 
	 * @param maxPoolSize The maximum pool size.
	 * 
	 * @param maxQueueSize The maximum queue size.
	 * 
	 * @param rejectionHandler The task rejection handler.
	 */
	public TaskPoolExecutor(int corePoolSize, int maxPoolSize, int maxQueueSize, RejectedExecutionHandler rejectionHandler) {
		super(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(maxQueueSize), rejectionHandler);
		
		this.maxQueueSize = maxQueueSize;
	}
	
	@Override
    protected <V> RunnableFuture<V> newTaskFor(Runnable runnable, V result) {
		return (RunnableFuture<V>)new TaskFuture<V>((Task)runnable, result);   
    }
	
	/**
	 * Retrieves the maximum queue size.
	 * 
	 * @return The maximum queue size.
	 */
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
 
}
