package com.runner.task_scheduler.taskrunner.observer.impl;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.observer.TaskEvent;
import com.runner.task_scheduler.taskrunner.observer.TaskObserver;
import com.runner.task_scheduler.taskrunner.observer.TaskSubject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class TaskManager implements TaskSubject {
    private final AsyncTaskExecutor taskExecutor;
    private final List<TaskObserver> observers = new CopyOnWriteArrayList<>(); // To avoid race conditions
    private final ApplicationContext context; // To find observer beans

    public TaskManager(
            @Qualifier("taskExecutor") AsyncTaskExecutor taskExecutor,
            ApplicationContext context) {
        this.taskExecutor = taskExecutor;
        this.context = context;
    }

    @PostConstruct
    public void registerObservers() {
        Map<String, TaskObserver> observerMap = context.getBeansOfType(TaskObserver.class);
        log.info("Found {} observers to register.", observerMap.size());
        observerMap.values().forEach(this::attach);
    }

    @Override
    public void attach(TaskObserver observer) {
        if (observers.contains(observer)) {
            observers.add(observer);
            log.info("Attached observer: {}", observer.getClass().getSimpleName());

        }
    }

    @Override
    public void detach(TaskObserver observer) {
        observers.remove(observer);
        log.info("Detached observer: {}", observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers(TaskEvent event) {
        log.debug("Notifying {} observers about event: {}", observers.size(), event);
        for (TaskObserver observer : observers) {
            try {
                observer.update(event);
            } catch (Exception e) {
                // Continues notifying to observers even if one failed
                log.error("Observer {} failed during update for event {}",
                        observer.getClass().getSimpleName(), event.getTaskId(), e);
            }
        }
    }

    public void submitTask(TaskCommand command) {
        if (command == null) {
            log.warn("Attempted to submit a null task.");
            return;
        }
        String taskId = command.getTaskId();
        log.info("Submitting task {} for asynchronous execution.", taskId);
        notifyObservers(new TaskEvent(taskId, TaskEvent.Status.SUBMITTED));

        // Submit the task execution to the thread pool
        taskExecutor.submit(() -> {
            try {
                log.debug("Task {} starting execution in thread: {}", taskId, Thread.currentThread().getName());
                notifyObservers(new TaskEvent(taskId, TaskEvent.Status.STARTING));

                // Execute the scheduled task
                command.execute();

                log.debug("Task {} completed successfully.", taskId);
                notifyObservers(new TaskEvent(taskId, TaskEvent.Status.COMPLETED));

            } catch (Exception e) {
                log.error("Task {} failed execution.", taskId, e);
                notifyObservers(new TaskEvent(taskId, TaskEvent.Status.FAILED, e.getMessage(), e));

            }
        });
    }
}

