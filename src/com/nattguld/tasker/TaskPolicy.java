package com.nattguld.tasker;

/**
 * 
 * @author randqm
 *
 */

public enum TaskPolicy {
	
	FORCE("Force"),
	DEFAULT("Default"),
	OPTIONAL("Optional"),
	SINGLE("Single by class");
	
	
	/**
	 * The name.
	 */
	private final String name;
	
	
	/**
	 * Creates a new task policy.
	 * 
	 * @param name The name.
	 */
	private TaskPolicy(String name) {
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
