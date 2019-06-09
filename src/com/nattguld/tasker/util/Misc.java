package com.nattguld.tasker.util;

/**
 * 
 * @author randqm
 *
 */

public class Misc {
	
	
	/**
	 * Makes the current thread sleep for a given amount of time.
	 * 
	 * @param ms The milliseconds to sleep.
	 */
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
