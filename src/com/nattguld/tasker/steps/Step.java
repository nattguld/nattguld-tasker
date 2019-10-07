package com.nattguld.tasker.steps;

import com.nattguld.tasker.TaskManager;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class Step {
	
	/**
	 * The step name.
	 */
	private final String name;
	
	/**
	 * Whether the step is critical or not.
	 */
	private final boolean critical;
	
	/**
	 * The current step state.
	 */
	private StepState state;
	
	/**
	 * The current step status.
	 */
	private String status;
	
	
	/**
	 * Creates a new step.
	 */
	public Step() {
		this("unnamed");
	}
	
	/**
	 * Creates a new step.
	 * 
	 * @param critical Whether the step is critical or not.
	 */
	public Step(boolean critical) {
		this("unnamed", critical);
	}
	
	/**
	 * Creates a new step.
	 * 
	 * @param name The step name.
	 */
	public Step(String name) {
		this(name, true);
	}
	
	/**
	 * Creates a new step.
	 * 
	 * @param name The step name.
	 * 
	 * @param critical Whether the step is critical or not.
	 */
	public Step(String name, boolean critical) {
		this.name = name;
		this.critical = critical;
		this.state = StepState.IN_QUEUE;
		this.status = "Idle";
	}
	
	/**
	 * Attempts to execute the step.
	 * 
	 * @return The step execution response.
	 */
	public abstract StepState execute();
	
	
	/**
	 * Runs an external task within the step.
	 * 
	 * @param external The external task.
	 * 
	 * @return The finish task state.
	 */
	protected TaskState runExternalTask(Task external) {
		TaskManager.executeForcefully(external);
		
		while (external.isActive()) {
			setStatus(external.getStatus());
			Misc.sleep(200);
		}
		return external.getState();
	}
	
	/**
	 * Retrieves the step name.
	 * 
	 * @return The name of the step.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Retrieves whether the step is critical or not.
	 * 
	 * @return The result.
	 */
	public boolean isCritical() {
		return critical;
	}
	
	/**
	 * Modifies the step state.
	 * 
	 * @param state The new state.
	 * 
	 * @return The step.
	 */
	public Step setState(StepState state) {
		this.state = state;
		return this;
	}
	
	/**
	 * Retrieves the current step state.
	 * 
	 * @return The state.
	 */
	public StepState getState() {
		return state;
	}
	
	/**
	 * Modifies the status.
	 * 
	 * @param status The new status.
	 */
	protected void setStatus(String status) {
		if (getStatus().equals(status)) {
			return;
		}
		this.status = status;
	}
	
	/**
	 * Retrieves the current step status.
	 * 
	 * @return The status.
	 */
	public String getStatus() {
		return getName() + ": " + status;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
