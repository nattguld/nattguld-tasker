package com.nattguld.tasker.tasks.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

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
	 * @return Whether updating was successfull or not.
	 */
	protected abstract boolean updateGUI();
	
	@Override
	protected TaskState executeTask() throws Exception {
		AtomicBoolean updated = new AtomicBoolean();
		AtomicBoolean success = new AtomicBoolean();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					success.set(updateGUI());
					
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
				success.set(false);
				updated.set(true);
				break;
			}
		}
		return success.get() ? TaskState.FINISHED : TaskState.ERROR;
	}
	
	/**
	 * Retrieves the timeout in milliseconds.
	 * 
	 * @return The timeout.
	 */
	protected int getTimeoutMs() {
		return 10 * 1000;
	}
 
}
