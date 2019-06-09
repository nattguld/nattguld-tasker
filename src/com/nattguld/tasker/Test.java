package com.nattguld.tasker;

import java.util.List;

import com.nattguld.tasker.callbacks.CallbackResponse;
import com.nattguld.tasker.callbacks.ICallback;
import com.nattguld.tasker.steps.Step;
import com.nattguld.tasker.steps.StepState;
import com.nattguld.tasker.steps.impl.CallbackStep;
import com.nattguld.tasker.tasks.Task;
import com.nattguld.tasker.tasks.TaskProperty;
import com.nattguld.tasker.tasks.TaskState;
import com.nattguld.tasker.tasks.impl.StepTask;

public class Test {
	
	
	public static void main(String[] args) {
		Tol t = new Tol();
		
		//System.out.println("State: " + TaskManager.sync(t).getName());
		System.out.println("async--");
		
		//TaskManager.async(t);
		System.out.println(TaskManager.callback(t));
	}
	
	
	public static class Tol extends StepTask implements ICallback<Boolean> {

		private final CallbackResponse<Boolean> r;
		
		
		public Tol() {
			this.r = new CallbackResponse<Boolean>(false);
		}
		
		@Override
		protected void buildStepFlow(List<Step> steps) {
			steps.add(new CallbackStep<Boolean>(r) {

				@Override
				protected Boolean callback() {
					System.err.println("arr");
					return true;
				}
				
			});
		}
		
		@Override
		public CallbackResponse<Boolean> getCallbackResponse() {
			return r;
		}
		
	}

}