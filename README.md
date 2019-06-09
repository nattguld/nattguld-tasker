# nattguld-tasker

Simple example task:
```
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

Example task with callback:
```
public CallbackExample extends Task implements ICallback<T> {
  
  private final CallbackResponse<T> callback;
  
  
  public CallbackExample() {
    super("Callback example");
    this.callback = new CallbackResponse<T>(fallback);
  }
  
  @Override
  public TaskState executeTask() throws Exception {
    System.out.println("Hello world");
    return TaskState.FINISHED;
  }
  
  @Override
  public CallbackResponse<T> getCallbackResponse() {
    return callback;
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
