package com.runner.task_scheduler.taskrunner.command.impl;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LogTaskCommand implements TaskCommand {
    private final String taskId;
    private final String message;
    private final long durationMillis;

    public LogTaskCommand(String message, long durationMillis) {
        this.taskId = UUID.randomUUID().toString();
        this.message = message;
        this.durationMillis = durationMillis;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void execute() {
        log.info("Task {} Starting: {}. Will run for {} ms.", taskId, message, durationMillis);
        try {
            TimeUnit.MILLISECONDS.sleep(durationMillis);
            log.info("Task {} Finished: {}", taskId, message);
        } catch (InterruptedException e) {
            log.error("Task {} Interrupted: {}", taskId, message, e);
            Thread.currentThread().interrupt(); // Preserve interrupt status
        } catch (Exception e) {
            log.error("Task {} Failed: {}", taskId, message, e);
            throw new RuntimeException("Task execution failed for task " + taskId, e);
        }

    }
}
