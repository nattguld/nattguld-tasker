# nattguld-tasker

## Simple example task
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

## Example task with callback
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

## Example step task
```java
public StepTaskExample extends StepTask {
  
  public SimpleExample() {
    super("Step example");
  }
  
  @Override
  protected void builStepFlow(List<Step> steps) {
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

## Example step task with callback
```java
public StepTaskExample extends StepTask implements ICallback<T> {
  
  private final CallbackResponse<T> callbackResponse;
  
  
  public SimpleExample() {
    super("Step callback example");
    this.callbackResponse = new CallbackResponse<T>(fallback);
  }
  
  @Override
  protected void builStepFlow(List<Step> steps) {
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
