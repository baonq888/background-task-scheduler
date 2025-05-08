package com.runner.task_scheduler;

import com.runner.task_scheduler.taskrunner.command.TaskCommand;
import com.runner.task_scheduler.taskrunner.command.impl.LogTaskCommand;
import com.runner.task_scheduler.taskrunner.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TaskSchedulerApplication implements CommandLineRunner {
	private final TaskExecutorService taskExecutorService;

	public static void main(String[] args) {
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		log.info("Submitting tasks...");

		TaskCommand task1 = new LogTaskCommand("Processing data batch", 3000);
		TaskCommand task2 = new LogTaskCommand("Sending notification email", 1000);
		TaskCommand task3 = new LogTaskCommand("Generating report", 5000);
		TaskCommand task4 = new LogTaskCommand("Short cleanup task", 500);

		taskExecutorService.submitTask(task1);
		taskExecutorService.submitTask(task2);
		taskExecutorService.submitTask(task3);
		taskExecutorService.submitTask(task4);

		log.info("Task submission complete. Waiting for scheduled tasks.");
	}
}
