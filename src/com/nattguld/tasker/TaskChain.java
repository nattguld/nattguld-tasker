package com.nattguld.tasker;

import java.util.ArrayDeque;
import java.util.Deque;

import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskProperty;
import com.nattguld.tasker.tasks.TaskState;

/**
 * 
 * @author randqm
 *
 */

public class TaskChain extends Task {
	
	
	/**
	 * The tasks deque
	 */
	private final Deque<Task> chain;
	
	
	/**
	 * Creates a new task chain.
	 * 
	 * @param name The task name.
	 */
	public TaskChain(String name) {
		this(name, new ArrayDeque<>());
	}
	
	/**
	 * Creates a new task chain.
	 * 
	 * @param name The task name.
	 * 
	 * @param chain The tasks deque.
	 */
	public TaskChain(String name, Deque<Task> chain) {
		super(name);
		
		this.chain = chain;
		
		super.setProperty(TaskProperty.REPEAT, true)
			.setProperty(TaskProperty.IGNORE_CRITICAL, true)
			.setProperty(TaskProperty.DAEMON, true);
	}
	
	/**
	 * Adds a new task to the chain.
	 * 
	 * @param task The task to add.
	 * 
	 * @return The task chain instance.
	 */
	public TaskChain addTask(Task task) {
		chain.add(task);
		return this;
	}

	@Override
	protected TaskState executeTask() throws Exception {
		try {
			TaskManager.sync(chain.poll());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chain.isEmpty() ? TaskState.CANCEL : TaskState.FINISHED;
	}

}
