package com.nattguld.tasker.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.nattguld.tasker.TaskPolicy;
import com.nattguld.tasker.cfg.TaskConfig;
import com.nattguld.tasker.util.Attributes;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class Task implements Runnable {
	
	/**
	 * The default task repeat delay in milliseconds.
	 */
	public static final int DEFAULT_REPEAT_DELAY = 1000;
	
	/**
	 * The task properties.
	 */
	private final List<TaskProperty> props;
	
	/**
	 * The task attributes.
	 */
	private final Attributes attributes;
	
	/**
	 * The name of the task.
	 */
	private final String name;
	
	/**
	 * The current state of the task.
	 */
	private TaskState state;
	
	/**
	 * The current status of the task.
	 */
	private String status;
	
	/**
	 * The repeat delay.
	 */
	private int repeatDelay;
	
	/**
	 * The amount of attempts made to execute the task successfully.
	 */
	private int attempts;
	
	
	/**
	 * Creates a new task.
	 */
	public Task() {
		this(null);
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param name The name of the task.
	 */
	public Task(String name) {
		this.name = Objects.isNull(name) ? getClass().getName() : name;
		this.state = TaskState.IN_QUEUE;
		this.status = "In queue";
		this.repeatDelay = DEFAULT_REPEAT_DELAY;
		this.attributes = new Attributes();
		this.props = new ArrayList<>();
	}
	
	/**
	 * Whether the conditions for executing the task flow are met or not.
	 * 
	 * @return The result.
	 */
	protected boolean preConditionsMet() {
		//TODO override when required
		return true;
	}
	
	/**
	 * Executes when the flow is starting.
	 */
	protected void onStart() {
		reset();
	}
	
	@Override
	public void run() {
		if (!preConditionsMet()) {
			setState(TaskState.ERROR);
			return;
		}
		onStart();
		
		try {
			while (!handleTask()) {
				Misc.sleep(getRepeatDelay());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		onFinish();
	}
	
	/**
	 * Executes when the flow is finished.
	 */
	protected void onFinish() {
		//TODO override when required
	}
	
	/**
	 * Attempts to execute the task.
	 * 
	 * @return The task state.
	 * 
	 * @throws Exception
	 */
	protected abstract TaskState executeTask() throws Exception;
	
	/**
	 * Handles execution of the task.
	 * 
	 * @return Whether the task finished execution or not.
	 */
	public boolean handleTask() {
		if (getState() == TaskState.CANCEL) {
			return true;
		}
		if (getState() == TaskState.PAUSED) {
			Misc.sleep(2000);
			return false;
		}
		attempts++;
		setState(TaskState.RUNNING);
		setStatus("Running");
		TaskState respState = TaskState.RUNNING;
		
		try {
			setStatus("Executing " + getName());
			respState = executeTask();

		} catch (Exception ex) {
			ex.printStackTrace();
			respState = TaskState.EXCEPTION;
		}
		setStatus("Finished with response [" + respState.getName() + ": " + getStatus() + "]");
		
		if (respState == TaskState.CANCEL || getState() == TaskState.CANCEL) {
			setState(TaskState.FINISHED);
			return true;
		}
		if (respState == TaskState.RETRY) {
			setState(TaskState.IN_QUEUE);
			reset();
			onStart();
			return false;
		}
		if (!hasProperty(TaskProperty.REPEAT)) {
			if (respState == TaskState.RUNNING) {
				setStatus("Fatal Error, task state is still running after task execution");
				setState(TaskState.EXCEPTION);
				return true;
			}
			setState(respState);
			
			if (respState == TaskState.FINISHED) {
				return true;
			}
			return attempts >= getMaxAttempts();
		}
		if (respState == TaskState.ERROR || respState == TaskState.EXCEPTION) {
			if (attempts < getMaxAttempts()) {
				return false;
			}
			if (!hasProperty(TaskProperty.IGNORE_CRITICAL)) {
				setState(respState);
				return true;
			}
			setState(TaskState.IN_QUEUE);
			return false;
		}
		setState(TaskState.IN_QUEUE);
		return false;
	}
	
	/**
	 * Resets the task.
	 */
	public void reset() {
		setState(TaskState.IN_QUEUE);
		
		this.attempts = 0;
		this.status = "In queue";
	}
	
	/**
	 * Cancels the task.
	 * 
	 * @return the task.
	 */
	public Task cancel() {
		setState(TaskState.CANCEL);
		return this;
	}
	
	/**
	 * Pauses the task.
	 * 
	 * @return The task.
	 */
	public Task pause() {
		if (getState() == TaskState.CANCEL || getState() == TaskState.ERROR 
				|| getState() == TaskState.EXCEPTION) {
			return this;
		}
		return setState(TaskState.PAUSED);
	}
	
	/**
	 * Unpauses the task if it was paused.
	 * 
	 * @return The task.
	 */
	public Task unpause() {
		if (getState() != TaskState.PAUSED) {
			return this;
		}
		return setState(TaskState.RUNNING);
	}
	
	/**
	 * Retrieves the name of the task.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Modifies the state of the task.
	 * 
	 * @param state The new state.
	 * 
	 * @return The task.
	 */
	protected Task setState(TaskState state) {
		this.state = state;
		return this;
	}
	
	/**
	 * Retrieves the current state of the task.
	 * 
	 * @return The state.
	 */
	public TaskState getState() {
		return state;
	}
	
	/**
	 * Retrieves whether the task is active or not.
	 * 
	 * @return The result.
	 */
	public boolean isActive() {
		return getState() == TaskState.IN_QUEUE || getState() == TaskState.RUNNING 
				|| getState() == TaskState.PAUSED;
	}
	
	/**
	 * Modifies the current task status.
	 * 
	 * @param status The new status.
	 * 
	 * @return Whether the status changed or not.
	 */
	public boolean setStatus(String status) {
		if (getStatus().equals(status)) {
			return false;
		}
		this.status = status;
		
		if (!hasProperty(TaskProperty.DAEMON) || TaskConfig.getConfig().isDebug()) {
			System.out.println("[" + getName() + "][" + getState().getName() + "]: " + status);
		}
		return true;
	}
	
	/**
	 * Retrieves the task's status.
	 * 
	 * @return The status.
	 */
	public String getStatus() {
		return Objects.isNull(status) ? "" : status;
	}
	
	/**
	 * Modifies the repeat delay of the task.
	 * 
	 * @param repeatDelay The new repeat delay.
	 * 
	 * @return The task.
	 */
	public Task setRepeatDelay(int repeatDelay) {
		this.repeatDelay = repeatDelay;
		return this;
	}
	
	/**
	 * Retrieves the repeat delay of the task.
	 * 
	 * @return The repeat delay.
	 */
	public int getRepeatDelay() {
		return repeatDelay;
	}
	
	/**
	 * Retrieves whether the task has a given property or not.
	 * 
	 * @param prop The property.
	 * 
	 * @return The result.
	 */
	public boolean hasProperty(TaskProperty prop) {
		return props.contains(prop);
	}
	
	/**
	 * Sets a task property.
	 * 
	 * @param prop The property.
	 * 
	 * @param enabled Whether to enabled the property or not.
	 * 
	 * @return The task.
	 */
	public Task setProperty(TaskProperty prop, boolean enabled) {
		if (!enabled && hasProperty(prop)) {
			props.remove(prop);
			return this;
		}
		if (enabled && !hasProperty(prop)) {
			props.add(prop);
			return this;
		}
		return this;
	}
	
	/**
	 * Retrieves the attributes.
	 * 
	 * @return The attributes.
	 */
	protected Attributes getAttributes() {
		return attributes;
	}
	
	/**
	 * The maximum amount of attempts allowed to execute the task successfully.
	 * 
	 * @return The amount.
	 */
	protected int getMaxAttempts() {
		return 1;
	}
	
	/**
	 * Retrieves the task policy.
	 * 
	 * @return The task policy.
	 */
	public TaskPolicy getPolicy() {
		return TaskPolicy.DEFAULT;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
