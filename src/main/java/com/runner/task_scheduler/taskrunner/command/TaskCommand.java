package com.runner.task_scheduler.taskrunner.command;

public interface TaskCommand {
    String getTaskId();
    void execute();
}
