package com.niyamitra.task.scheduler;

import com.niyamitra.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiryCheckScheduler {

    private final TaskService taskService;

    @Scheduled(cron = "0 0 6 * * *") // 06:00 IST daily
    public void checkExpiries() {
        log.info("Running daily Kavach expiry check...");
        taskService.checkAndPublishExpiryAlerts();
    }
}
