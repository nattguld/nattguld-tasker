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
	 * Adds a step in the first position.
	 * 
	 * @param step The step.
	 */
	protected void addFirst(Step step) {
		steps.add(0, step);
	}
	
	/**
	 * Adds a step.
	 * 
	 * @param step The step to add.
	 */
	protected void add(Step step) {
		steps.add(step);
	}
	
	@Override
	protected void onStart() {
		steps.clear();
		
		super.onStart();
	}
	
	@Override
	protected void onFinish() {
		if (isTimedOut()) {
			setStatus("Timed out at " + (Objects.nonNull(currentStep) 
					? (currentStep.getName() + ": " + currentStep.getStatus()) : "NA"));
		}
		super.onFinish();
	}
	
	/**
	 * Builds the step flow.
	 */
	protected abstract void buildStepFlow();
	
	@Override
	protected TaskState executeTask() throws Exception {
		if (stepDeque.isEmpty()) {
			buildStepFlow();

			stepDeque.addAll(steps);

			if (stepDeque.isEmpty()) {
				System.err.println("[" + getName() + "]: Empty step flow");
				return TaskState.ERROR;
			}
		}
		while (!stepDeque.isEmpty()) {
			if (getState() == TaskState.CANCEL) {
				setStatus("Task has been cancelled");
				return TaskState.CANCEL;
			}
			currentStep = stepDeque.poll();
			
			setStatus(currentStep.getName() + ": Executing");
			currentStep.setState(StepState.IN_PROGRESS);
			
			while (currentStep.getState() == StepState.IN_PROGRESS) {
				try {
					currentStep.setState(currentStep.execute());
					
				} catch (Exception ex) {
					ex.printStackTrace();
					onException(currentStep, ex);
					currentStep.setState(StepState.EXCEPTION);
					break;
				}
				Misc.sleep(getStepDelay());
			}
			refreshStartTime();
			
			if (currentStep.getState() == StepState.CANCEL) {
				setStatus(currentStep.getName() + ": Cancelled Flow");
				return TaskState.CANCEL;
			}
			if (currentStep.getState() == StepState.INTERRUPT) {
				setStatus(currentStep.getName() + ": Interrupted Flow");
				break;
			}
			if (currentStep.getState() == StepState.RETRY) {
				setStatus(currentStep.getName() + ": Interrupted Flow for Retry");
				return TaskState.RETRY;
			}
			if (currentStep.getState() == StepState.EXCEPTION || currentStep.getState() == StepState.FAILED) {
				setStatus(currentStep.getName() + ": Failed to execute");
				onStepFail(currentStep);
				
				if (currentStep.isCritical()) {
					return currentStep.getState() == StepState.EXCEPTION ? TaskState.EXCEPTION : TaskState.ERROR;
				}
			}
			setStatus(currentStep.getName() + ": Executed successfully");
			Misc.sleep(getStepDelay());
		}
		setStatus("Successfully executed step task " + getName());
		return TaskState.FINISHED;
	}
	
	@Override
	public void reset() {
		stepDeque.clear();
		
		super.reset();
	}
	
	/**
	 * Executed when a step fails.
	 * 
	 * @param step The step.
	 */
	protected void onStepFail(Step step) {
		System.err.println("[" + getName() + "] Failed [" + step.getState() + "] to execute flow step " + step.getName() + ": " + step.getStatus());
	}
	
	/**
	 * Executed when the flow encounters an exception.
	 * 
	 * @param step The flow step.
	 * 
	 * @param ex The exception.
	 */
	protected void onException(Step step, Exception ex) {
		System.err.println("[" + getName() + "] Failed [" + step.getState() + "] to execute flow step " + step.getName() + " due an exception.");
	}
	
	@Override
	public String getStatus() {
		return Objects.isNull(currentStep) ? super.getStatus() : currentStep.getStatus();
	}
	
	/**
	 * Retrieves the delay between steps.
	 * 
	 * @return The delay.
	 */
	protected int getStepDelay() {
		return 100;
	}
	
	/**
	 * Retrieves the task's steps.
	 * 
	 * @return The steps.
	 */
	public List<Step> getSteps() {
		synchronized (steps) {
			return steps;
		}
	}

}
