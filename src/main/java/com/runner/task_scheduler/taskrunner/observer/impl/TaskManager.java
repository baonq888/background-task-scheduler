package com.runner.task_scheduler.taskrunner.observer.impl;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.observer.TaskEvent;
import com.runner.task_scheduler.taskrunner.observer.TaskObserver;
import com.runner.task_scheduler.taskrunner.observer.TaskSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class TaskManager implements TaskSubject, ApplicationListener<ContextRefreshedEvent> {
    private final AsyncTaskExecutor taskExecutor;
    private final List<TaskObserver> observers = new CopyOnWriteArrayList<>(); // To avoid race conditions

    @Autowired
    public TaskManager(
            @Qualifier("taskExecutor") AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, TaskObserver> observerBeans = applicationContext.getBeansOfType(TaskObserver.class);
        log.info("Found {} TaskObserver beans to register.", observerBeans.size());
        observerBeans.values().forEach(this::attach);
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

