package com.runner.task_scheduler.taskrunner.scheduler;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.command.impl.LogTaskCommand;
import com.runner.task_scheduler.taskrunner.observer.impl.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTaskSubmitter {
    private final TaskManager taskManager;

    public ScheduledTaskSubmitter(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Runs every minute at second 0
    @Scheduled(cron = "0 * * * * ?")
    public void scheduleRegularLogTask() {
        log.info("CRON TRIGGERED: Submitting a scheduled log task.");
        TaskCommand scheduledCommand = new LogTaskCommand("Executed via CRON schedule", 500);
        taskManager.submitTask(scheduledCommand);
    }
}
