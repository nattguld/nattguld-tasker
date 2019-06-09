package com.nattguld.tasker.cfg;

/**
 * 
 * @author randqm
 *
 */

public class TaskConfig {
	
	/**
	 * Whether to remove failed tasks or not.
	 */
	private boolean removeFailed = true;
	
	/**
	 * The maximum amount of parallel tasks allowed.
	 */
	private int maxParallel = 40 * Runtime.getRuntime().availableProcessors();

	
	/**
	 * Modifies whether to remove failed tasks or not.
	 * 
	 * @param removeFailed The new state.
	 */
	public void setRemoveFailed(boolean removeFailed) {
		this.removeFailed = removeFailed;
	}
	
	/**
	 * Retrieves whether to remove failed tasks or not.
	 * 
	 * @return The result.
	 */
	public boolean isRemoveFailed() {
		return removeFailed;
	}
	
	/**
	 * Modifies the maximum amount of parallel tasks allowed.
	 * 
	 * @param maxParallel The new amount.
	 */
	public void setMaxParallel(int maxParallel) {
		this.maxParallel = maxParallel;
	}
	
	/**
	 * Retrieves the maximum amount of parallel tasks allowed.
	 * 
	 * @return The amount.
	 */
	public int getMaxParallel() {
		return maxParallel;
	}

}
