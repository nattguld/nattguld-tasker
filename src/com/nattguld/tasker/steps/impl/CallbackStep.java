package com.nattguld.tasker.steps.impl;

import com.nattguld.tasker.callbacks.CallbackResponse;
import com.nattguld.tasker.steps.Step;
import com.nattguld.tasker.steps.StepState;

/**
 * 
 * @author randqm
 *
 */

public abstract class CallbackStep<T extends Object> extends Step {

	/**
	 * The callback response.
	 */
	private final CallbackResponse<T> callbackResponse;
	
	
	/**
	 * Creates a new callback step.
	 * 
	 * @param callbackResponse The callback response.
	 */
	public CallbackStep(CallbackResponse<T> callbackResponse) {
		this("unnamed", callbackResponse);
	}
	
	/**
	 * Creates a new callback step.
	 * 
	 * @param name The name of the callback step.
	 * 
	 * @param callbackResponse The callback response.
	 */
	public CallbackStep(String name, CallbackResponse<T> callbackResponse) {
		super(name, true);
		
		this.callbackResponse = callbackResponse;
	}
	
	/**
	 * Retrieves the callback.
	 * 
	 * @return The callback.
	 */
	protected abstract T callback();
	
	
	@Override
	public StepState execute() {
		callbackResponse.assign(callback());
		return StepState.SUCCESS;
	}
	
	/**
	 * Retrieves the callback response.
	 * 
	 * @return The callback response.
	 */
	public CallbackResponse<T> getFlowCallback() {
		return callbackResponse;
	}

}
