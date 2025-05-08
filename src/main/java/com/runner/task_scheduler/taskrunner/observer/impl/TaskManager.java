package com.runner.task_scheduler.taskrunner.observer.impl;

import com.runner.task_scheduler.taskrunner.observer.TaskEvent;
import com.runner.task_scheduler.taskrunner.observer.TaskObserver;
import com.runner.task_scheduler.taskrunner.observer.TaskSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class TaskManager implements TaskSubject, ApplicationListener<ContextRefreshedEvent> {
    private final List<TaskObserver> observers = new CopyOnWriteArrayList<>(); // To avoid race conditions


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



}

