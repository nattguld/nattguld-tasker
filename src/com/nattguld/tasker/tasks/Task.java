package com.nattguld.tasker.tasks;

import java.util.Objects;

import com.nattguld.tasker.callbacks.CallbackResponse;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class Task<T> {
	
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
	 * The execution response.
	 */
	private CallbackResponse<T> response;
	
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
		this("unnamed");
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param name The name of the task.
	 */
	public Task(String name) {
		this.name = name;
		this.state = TaskState.IN_QUEUE;
		this.status = "In queue";
		this.repeatDelay = 1000;
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
	 * 
	 * @throws Exception
	 */
	public boolean handleTask() throws Exception {
		if (getState() == TaskState.CANCEL) {
			return true;
		}
		if (getState() == TaskState.PAUSED) {
			Misc.sleep(2000);
			return false;
		}
		attempts++;
		setState(TaskState.RUNNING);
		TaskState respState = TaskState.RUNNING;
		
		try {
			respState = executeTask();

		} catch (Exception ex) {
			ex.printStackTrace();
			respState = TaskState.EXCEPTION;
		}
		if (getState() != TaskState.CANCEL) {
			setState(respState);
		}
		if (respState == TaskState.RUNNING) {
			throw new Exception("Task state is still running after task execution.");
		}
		if (!hasProperty(TaskProperty.REPEAT)) {
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
				return true;
			}
		}
		Misc.sleep(getRepeatDelay());
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
	public Task<T> cancel() {
		setState(TaskState.CANCEL);
		return this;
	}
	
	/**
	 * Pauses the task.
	 * 
	 * @return The task.
	 */
	public Task<T> pause() {
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
	public Task<T> unpause() {
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
	protected Task<T> setState(TaskState state) {
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
		return true;
	}
	
	/**
	 * Retrieves the task's status.
	 * 
	 * @return The status.
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Modifies the repeat delay of the task.
	 * 
	 * @param repeatDelay The new repeat delay.
	 * 
	 * @return The task.
	 */
	public Task<T> setRepeatDelay(int repeatDelay) {
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
	 * Retrieves the response.
	 * 
	 * @return The response.
	 */
	public CallbackResponse<T> getResponse() {
		return response;
	}
	
	/**
	 * Retrieves whether a given task property is assigned to the task or not.
	 * 
	 * @param prop The task property.
	 * 
	 * @return The result.
	 */
	protected boolean hasProperty(TaskProperty prop) {
		if (Objects.isNull(getProperties()) || getProperties().length <= 0) {
			return false;
		}
		for (TaskProperty tp : getProperties()) {
			if (tp == prop) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieves the custom task properties.
	 * 
	 * @return The custom task properties if any.
	 */
	protected TaskProperty[] getProperties() {
		return null;
	}
	
	/**
	 * The maximum amount of attempts allowed to execute the task successfully.
	 * 
	 * @return The amount.
	 */
	protected int getMaxAttempts() {
		return 1;
	}

}
