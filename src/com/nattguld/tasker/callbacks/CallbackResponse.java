package com.nattguld.tasker.callbacks;

/**
 * 
 * @author randqm
 *
 */

public class CallbackResponse<T extends Object> {
	
	/**
	 * The response.
	 */
	private T response;
	
	/**
	 * Whether a response has been assigned or not.
	 */
	private boolean assigned;
	
	
	/**
	 * Creates a new callback response.
	 * 
	 * @param fallback The fallback value.
	 */
	public CallbackResponse(T fallback) {
		this.response = fallback;
	}
	
	/**
	 * Assigns a response.
	 * 
	 * @param response The response.
	 * 
	 * @return The response.
	 */
	public CallbackResponse<T> assign(T response) {
		this.response = response;
		this.assigned = true;
		return this;
	}
	
	/**
	 * Retrieves whether a response has been assigned or not.
	 * 
	 * @return The result.
	 */
	public boolean isAssigned() {
		return assigned;
	}
	
	/**
	 * Retrieves the response without checking is one has been assigned or not.
	 * 
	 * @return The response.
	 */
	public T getResponse() {
		return response;
	}

}
