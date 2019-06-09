package com.nattguld.tasker.callbacks;

/**
 * 
 * @author randqm
 *
 */

public interface ICallback<T extends Object> {
	
	
	/**
	 * Retrieves the callback response.
	 * 
	 * @return The callback response.
	 */
	public CallbackResponse<T> getCallbackResponse();

}
