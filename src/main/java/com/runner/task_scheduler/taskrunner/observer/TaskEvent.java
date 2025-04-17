package com.runner.task_scheduler.taskrunner.observer;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
public class TaskEvent {
    public enum Status { SUBMITTED, STARTING, COMPLETED, FAILED, CANCELLED }

    private String taskId;
    private Status status;
    private  Instant timestamp;
    private String message;
    private Throwable error;

    public TaskEvent(String taskId, Status status, String message, Throwable error) {
        this.taskId = taskId;
        this.status = status;
        this.timestamp = Instant.now();
        this.message = message;
        this.error = error;
    }

    public TaskEvent(String taskId, Status status) {
        this.taskId = taskId;
        this.status = status;
    }

    public TaskEvent(String taskId, Status status, String message) {
        this.taskId = taskId;
        this.status = status;
        this.message = message;
    }
}
