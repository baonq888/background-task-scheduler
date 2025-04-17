package com.runner.task_scheduler.taskrunner.observer;

public interface TaskSubject {
    void attach(TaskObserver observer);
    void detach(TaskObserver observer);
    void notifyObservers(TaskEvent event);
}
