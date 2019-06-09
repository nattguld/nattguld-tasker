package com.nattguld.tasker.callbacks;

import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public class CallbackResponse<T> {
	
	/**
	 * The response.
	 */
	private T response;
	
	/**
	 * Whether a response has been assigned or not.
	 */
	private boolean assigned;
	
	
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
	public T getResponseNow() {
		return response;
	}
	
	/**
	 * Retrieves the response or waits for it if not assigned yet.
	 * 
	 * @param delay The delay to re-check if a response has been assigned or not.
	 * 
	 * @param timeout The timeout if no response has been received.
	 * 
	 * @return The response.
	 */
	public T waitAndGetResponse(int delay, int timeout) {
		int elapsed = 0;
		
		while (!assigned && elapsed < timeout) {
			Misc.sleep(delay);
			elapsed += delay;
		}
		return response;
	}
	
	/**
	 * Retrieves the response or waits for it if not assigned yet.
	 * 
	 * @return The response.
	 */
	public T waitAndGetResponse() {
		return waitAndGetResponse(500, 30 * 60 * 1000);
	}

}
