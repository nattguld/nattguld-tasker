package com.nattguld.tasker.tasks.impl;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.nattguld.tasker.steps.Step;
import com.nattguld.tasker.steps.StepState;
import com.nattguld.tasker.steps.impl.TaskStep;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 *
 * @param <T>
 */

public abstract class StepTask<T> extends Task<T> {
	
	/**
	 * Holds the task steps.
	 */
	private final List<Step> steps = new LinkedList<>();
	
	/**
	 * Holds the step deque for task execution.
	 */
	private final Deque<Step> stepDeque = new ConcurrentLinkedDeque<>();

	
	@Override
	protected TaskState executeTask() throws Exception {
		stepDeque.clear();
		stepDeque.addAll(steps);
		
		while (!stepDeque.isEmpty()) {
			Step step = stepDeque.poll();
			
			step.setState(StepState.IN_PROGRESS);
			
			try {
				while (step.getState() == StepState.IN_PROGRESS) {
					step.setState(step.execute());
					Misc.sleep(getStepDelay());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				step.setState(StepState.EXCEPTION);
				
				if (step.isCritical()) {
					return TaskState.EXCEPTION;
				}
			}
			if (step.getState() == StepState.CANCEL) {
				return TaskState.CANCEL;
			}
			if (step.getState() == StepState.INTERRUPT) {
				break;
			}
			
			Misc.sleep(getStepDelay());
		}
		
		return null;
	}
	
	/*
	 * IN_QUEUE,
	IN_PROGRESS,
	FAILED,
	SUCCESS,
	EXCEPTION,
	INTERRUPT,
	CANCEL;
	 */
	
	private void executeStep(TaskStep step) {
		
	}
	
	/**
	 * Retrieves the delay between steps.
	 * 
	 * @return The delay.
	 */
	protected int getStepDelay() {
		return 100;
	}

}
