# Nattguld Tasker

## About
Easy to use library usesful for any multi threaded applications.
Run asynchonous, synchronous and callback tasks anywhere in your application.
Use step tasks to build your task flows step by step and stay in full control of what's going on.
Or keep your users updated on whats going on in your application into detail.
_Licensed under the MIT license._

## General Configurations
```java
//Whether to keep failed tasks available in a list for later review or not.
TaskManager.getTaskConfig().setRemoveFailed(boolean value);

//Limits the amount of parallell tasks being active (40 * processor cores by default)
TaskManager.getTaskConfig().setMaxParallel(int value);

//Load a task configuration you saved
TaskManager.setTaskConfig(TaskConfig loadedConfig);
```

## Usage Examples
### Simple example task
```java
public SimpleExample extends Task {
  
  public SimpleExample() {
    super("Simple example");
  }
  
  @Override
  public TaskState executeTask() throws Exception {
    System.out.println("Hello world");
    return TaskState.FINISHED;
  }
}
TaskManager.async(new SimpleExample()); //Asynchronous
TaskState responseState = TaskManager.sync(new SimpleExample()); //Synchronous
```

### Example task with callback
```java
public CallbackExample extends Task implements ICallback<T> {
  
  private final CallbackResponse<T> callbackResponse;
  
  
  public CallbackExample() {
    super("Callback example");
    this.callbackResponse = new CallbackResponse<T>(fallback);
  }
  
  @Override
  public TaskState executeTask() throws Exception {
    System.out.println("Hello world");
    callbackResponse.assign(myValue);
    return TaskState.FINISHED;
  }
  
  @Override
  public CallbackResponse<T> getCallbackResponse() {
    return callbackResponse;
  }
}
//Asynchronous
CallbackExample task = new CallbackExample();
TaskManager.async(task);
T cb = (T)TaskManager.waitAndGetResponse(task);

//Synchronous
CallbackExample task = new CallbackExample();
T cb = (T)TaskManager.callback(task);
```

### Example step task
```java
public StepTaskExample extends StepTask {
  
  public SimpleExample() {
    super("Step example");
  }
  
  @Override
  protected void buildStepFlow(List<Step> steps) {
    steps.add(new Step("Im a critical step")) {
      @Override
      public StepState execute() {
        System.out.println("Hello I'm step 1");
        return StepState.SUCCESS;
      }
    }
    steps.add(new Step("Im a non-critical step", false)) {
      @Override
      public StepState execute() {
        System.out.println("Hello I'm step 2");
        return StepState.SUCCESS;
      }
    }
  }
}
```

### Example step task with callback
```java
public StepTaskExample extends StepTask implements ICallback<T> {
  
  private final CallbackResponse<T> callbackResponse;
  
  
  public SimpleExample() {
    super("Step callback example");
    this.callbackResponse = new CallbackResponse<T>(fallback);
  }
  
  @Override
  protected void buildStepFlow(List<Step> steps) {
    steps.add(new CallbackStep<T>("Im a callbackstep", callbackResponse)) {
      @Override
      public T callback() {
        return myValue;
      }
    }
  }
  
  @Override
  public CallbackResponse<T> getCallbackResponse() {
    return callbackResponse;
  }
}
```
