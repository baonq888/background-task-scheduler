package com.runner.task_scheduler.taskrunner.service;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.observer.TaskEvent;
import com.runner.task_scheduler.taskrunner.observer.TaskSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskExecutorService {
    private final AsyncTaskExecutor taskExecutor;
    private final TaskSubject taskSubject;

    @Autowired
    public TaskExecutorService(@Qualifier("taskExecutor") AsyncTaskExecutor taskExecutor, TaskSubject taskSubject) {
        this.taskExecutor = taskExecutor;
        this.taskSubject = taskSubject;
    }

    public void submitTask(TaskCommand command) {
        if (command == null) {
            log.warn("Attempted to submit a null task.");
            return;
        }

        String taskId = command.getTaskId();
        taskSubject.notifyObservers(new TaskEvent(taskId, TaskEvent.Status.SUBMITTED));

        taskExecutor.submit(() -> {
            try {
                taskSubject.notifyObservers(new TaskEvent(taskId, TaskEvent.Status.STARTING));
                log.debug("Task {} starting execution in thread: {}", taskId, Thread.currentThread().getName());

                command.execute();

                taskSubject.notifyObservers(new TaskEvent(taskId, TaskEvent.Status.COMPLETED));
                log.debug("Task {} completed successfully.", taskId);

            } catch (Exception e) {
                taskSubject.notifyObservers(new TaskEvent(taskId, TaskEvent.Status.FAILED, e.getMessage(), e));
                log.error("Task {} failed execution.", taskId, e);
            }
        });
    }
}