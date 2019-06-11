package com.nattguld.tasker.tasks.impl;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.nattguld.tasker.steps.Step;
import com.nattguld.tasker.steps.StepState;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.util.Misc;

/**
 * 
 * @author randqm
 * 
 */

public abstract class StepTask extends Task {
	
	/**
	 * Holds the task steps.
	 */
	private final List<Step> steps = new LinkedList<>();
	
	/**
	 * Holds the step deque for task execution.
	 */
	private final Deque<Step> stepDeque = new ConcurrentLinkedDeque<>();
	
	/**
	 * The current step.
	 */
	private Step currentStep;

	
	/**
	 * Creates a new step task.
	 */
	public StepTask() {
		super();
	}
	
	/**
	 * Creates a new step task.
	 * 
	 * @param name The name of the task.
	 */
	public StepTask(String name) {
		super(name);
	}
	
	/**
	 * Builds the step flow.
	 * 
	 * @param steps The steps.
	 */
	protected abstract void buildStepFlow(List<Step> steps);
	
	@Override
	protected TaskState executeTask() throws Exception {
		steps.clear();
		stepDeque.clear();
		
		buildStepFlow(steps);
		
		if (steps.isEmpty()) {
			System.err.println("Empty step flow");
			return TaskState.ERROR;
		}
		stepDeque.addAll(steps);
		
		while (!stepDeque.isEmpty()) {
			currentStep = stepDeque.poll();
			
			setStatus(currentStep.getName() + ": Executing");
			currentStep.setState(StepState.IN_PROGRESS);
			
			try {
				while (currentStep.getState() == StepState.IN_PROGRESS) {
					currentStep.setState(currentStep.execute());
					Misc.sleep(getStepDelay());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				currentStep.setState(StepState.EXCEPTION);
			}
			if (currentStep.getState() == StepState.CANCEL) {
				setStatus(currentStep.getName() + ": Cancelled Flow");
				return TaskState.CANCEL;
			}
			if (currentStep.getState() == StepState.INTERRUPT) {
				setStatus(currentStep.getName() + ": Interrupted Flow");
				break;
			}
			if (currentStep.getState() == StepState.EXCEPTION || currentStep.getState() == StepState.FAILED) {
				setStatus(currentStep.getName() + ": Failed to execute");
				
				if (currentStep.isCritical()) {
					return currentStep.getState() == StepState.EXCEPTION ? TaskState.EXCEPTION : TaskState.ERROR;
				}
			} else {
				setStatus(currentStep.getName() + ": Executed successfully");
			}
			Misc.sleep(getStepDelay());
		}
		return TaskState.FINISHED;
	}
	
	/**
	 * Retrieves the status of the current step.
	 * 
	 * @return The status.
	 */
	public String getCurrentStepStatus() {
		return Objects.isNull(currentStep) ? "Waiting..." : currentStep.getStatus();
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
