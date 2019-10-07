package com.nattguld.tasker.tasks.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import com.nattguld.tasker.TaskPolicy;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class GUITask extends Task {

	
	/**
	 * Creates a new step task.
	 */
	public GUITask() {
		super();
	}
	
	/**
	 * Creates a new step task.
	 * 
	 * @param name The name of the task.
	 */
	public GUITask(String name) {
		super(name);
	}
	
	/**
	 * Attempts to update the GUI.
	 * 
	 * @return The response state.
	 */
	protected abstract TaskState updateGUI();
	
	@Override
	protected TaskState executeTask() throws Exception {
		AtomicBoolean updated = new AtomicBoolean();
		AtomicReference<TaskState> taskResp = new AtomicReference<TaskState>(TaskState.CANCEL);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					taskResp.set(updateGUI());
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				updated.set(true);
			}
		});
		int elapsed = 0;
		
		while (!updated.get()) {
			Misc.sleep(50);
			elapsed += 50;
			
			if (elapsed >= getTimeoutMs()) {
				taskResp.set(TaskState.CANCEL);
				updated.set(true);
				break;
			}
		}
		return taskResp.get();
	}
	
	/**
	 * Retrieves the timeout in milliseconds.
	 * 
	 * @return The timeout.
	 */
	protected int getTimeoutMs() {
		return 10 * 1000;
	}
	
	@Override
	public TaskPolicy getPolicy() {
		return TaskPolicy.FORCE;
	}
 
}
