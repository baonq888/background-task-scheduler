package com.runner.task_scheduler.taskrunner.observer.impl;

import com.runner.task_scheduler.taskrunner.observer.TaskEvent;
import com.runner.task_scheduler.taskrunner.observer.TaskObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogTaskObserver implements TaskObserver {
    @Override
    public void update(TaskEvent event) {
        // Log the event details
        log.info("Observer Received Event: {}", event.toString());
        if (event.getStatus() == TaskEvent.Status.FAILED && event.getError() != null) {
            log.error("Task {} failed with error: {}", event.getTaskId(), event.getError().getMessage(), event.getError());
        }
    }
}
