package com.nattguld.tasker.tasks;

/**
 * 
 * @author randqm
 *
 */

public enum TaskState {
	
	IN_QUEUE("In Queue"),
	RUNNING("Running"),
	PAUSED("Paused"),
	ERROR("Error"),
	CANCEL("Cancelling"),
	FINISHED("Finished"),
	EXCEPTION("Exception");
	
	
	/**
	 * The name.
	 */
	private final String name;
	
	
	/**
	 * Creates a new task state.
	 * 
	 * @param name The name.
	 */
	private TaskState(String name) {
		this.name = name;
	}
	
	/**
	 * Retrieves the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
