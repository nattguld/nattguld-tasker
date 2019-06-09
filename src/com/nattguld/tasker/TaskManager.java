package com.nattguld.tasker;

import com.nattguld.tasker.tasks.Task;

/**
 * 
 * @author randqm
 *
 */

public class TaskManager {
	
	
	
	public static void processTask(Task<?> task) {
		if (task.handleTask()) {
			return;
		}
	}

}
