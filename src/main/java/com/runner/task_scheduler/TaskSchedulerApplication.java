package com.runner.task_scheduler;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.command.impl.LogTaskCommand;
import com.runner.task_scheduler.taskrunner.observer.impl.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@Slf4j
@EnableScheduling
public class TaskSchedulerApplication implements CommandLineRunner {
	private final TaskManager taskManager;

    public TaskSchedulerApplication(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public static void main(String[] args) {
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		log.info("Application started, submitting example tasks...");

		if (taskManager == null) {
			log.error("TaskManager is null! Check configuration and cycle resolution.");
			// Optionally wait or fetch context manually - but ideally @Lazy handles this timing
			return;
		}

		TaskCommand task1 = new LogTaskCommand("Processing data batch", 3000);
		TaskCommand task2 = new LogTaskCommand("Sending notification email", 1000);
		TaskCommand task3 = new LogTaskCommand("Generating report", 5000);
		TaskCommand task4 = new LogTaskCommand("Short cleanup task", 500);

		taskManager.submitTask(task1);
		taskManager.submitTask(task2);
		taskManager.submitTask(task3);
		taskManager.submitTask(task4);

		log.info("Task submission complete. Waiting for scheduled tasks...");
	}
}
