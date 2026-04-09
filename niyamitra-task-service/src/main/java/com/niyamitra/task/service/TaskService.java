package com.niyamitra.task.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.enums.TaskStatus;
import com.niyamitra.common.event.EscalationTriggeredEvent;
import com.niyamitra.common.event.ExpiryApproachingEvent;
import com.niyamitra.common.exception.ResourceNotFoundException;
import com.niyamitra.task.model.ComplianceTask;
import com.niyamitra.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ComplianceTask createTask(ComplianceTask task) {
        log.info("Creating compliance task: {} for tenant {}", task.getTitle(), task.getTenantId());
        return taskRepository.save(task);
    }

    public List<ComplianceTask> getTasksByTenant(UUID tenantId, TaskStatus status) {
        if (status != null) {
            return taskRepository.findByTenantIdAndStatus(tenantId, status);
        }
        return taskRepository.findByTenantId(tenantId);
    }

    public ComplianceTask getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    @Transactional
    public ComplianceTask updateStatus(UUID taskId, TaskStatus newStatus) {
        ComplianceTask task = getTask(taskId);
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDate.now());
        }
        return taskRepository.save(task);
    }

    @Transactional
    public ComplianceTask reschedule(UUID taskId, LocalDate newDate, String reason) {
        ComplianceTask task = getTask(taskId);
        log.info("Rescheduling task {} from {} to {}: {}", taskId, task.getDueDate(), newDate, reason);
        task.setDueDate(newDate);
        return taskRepository.save(task);
    }

    @Transactional
    public ComplianceTask acknowledge(UUID taskId) {
        ComplianceTask task = getTask(taskId);
        task.setAcknowledged(true);
        return taskRepository.save(task);
    }

    public void checkAndPublishExpiryAlerts() {
        LocalDate today = LocalDate.now();

        // Check T-30, T-15, T-5 day alerts
        for (int daysAhead : new int[]{30, 15, 5}) {
            LocalDate targetDate = today.plusDays(daysAhead);
            List<ComplianceTask> tasks = taskRepository.findTasksDueOn(targetDate);

            for (ComplianceTask task : tasks) {
                ExpiryApproachingEvent event = new ExpiryApproachingEvent(
                        UUID.randomUUID(), task.getId(), task.getTenantId(),
                        task.getAssignedTo(), daysAhead,
                        Instant.now(), UUID.randomUUID(), "KAVACH"
                );
                kafkaTemplate.send(KafkaTopics.EXPIRY_APPROACHING, task.getId().toString(), event);
                log.info("Published expiry alert: task={}, daysRemaining={}", task.getId(), daysAhead);
            }
        }

        // Check for T-5 unacknowledged tasks that need escalation
        LocalDate fiveDaysOut = today.plusDays(5);
        List<ComplianceTask> escalationCandidates = taskRepository.findTasksDueOn(fiveDaysOut);
        for (ComplianceTask task : escalationCandidates) {
            if (!task.isAcknowledged() && task.getEscalationLevel() == 0) {
                task.setEscalationLevel(1);
                task.setStatus(TaskStatus.ESCALATED);
                taskRepository.save(task);

                EscalationTriggeredEvent escalation = new EscalationTriggeredEvent(
                        UUID.randomUUID(), task.getId(), task.getTenantId(),
                        null, // escalateToUserId — owner lookup needed
                        "T-5 deadline approaching, floor manager unresponsive",
                        Instant.now(), UUID.randomUUID(), "KAVACH"
                );
                kafkaTemplate.send(KafkaTopics.ESCALATION_TRIGGERED, task.getId().toString(), escalation);
                log.info("Kavach escalation triggered for task {}", task.getId());
            }
        }

        // Mark overdue tasks
        List<ComplianceTask> overdueTasks = taskRepository.findTasksDueOn(today.minusDays(1));
        for (ComplianceTask task : overdueTasks) {
            task.setStatus(TaskStatus.OVERDUE);
            taskRepository.save(task);
            log.info("Task {} marked as OVERDUE", task.getId());
        }
    }
}
