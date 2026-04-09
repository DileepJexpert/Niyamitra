package com.niyamitra.task.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.enums.ComplianceCategory;
import com.niyamitra.common.enums.TaskStatus;
import com.niyamitra.common.event.DocumentExtractedEvent;
import com.niyamitra.common.event.TasksGeneratedEvent;
import com.niyamitra.task.model.ComplianceTask;
import com.niyamitra.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventConsumer {

    private final TaskRepository taskRepository;

    @KafkaListener(topics = KafkaTopics.TASKS_GENERATED, groupId = "niyamitra-task-service")
    public void handleTasksGenerated(TasksGeneratedEvent event) {
        log.info("Creating {} compliance tasks for tenant {} from Anupalan rules",
                event.tasksCreated(), event.tenantId());

        // Tasks will be created based on rule IDs from Anupalan service
        // In production, this would call Anupalan Rule Service to get rule details
        for (UUID ruleId : event.applicableRuleIds()) {
            ComplianceTask task = ComplianceTask.builder()
                    .tenantId(event.tenantId())
                    .ruleId(ruleId)
                    .title("Compliance task for rule " + ruleId)
                    .description("Auto-generated from Anupalan rules on tenant onboarding")
                    .category(ComplianceCategory.POLLUTION)
                    .status(TaskStatus.PENDING)
                    .dueDate(LocalDate.now().plusMonths(1))
                    .build();
            taskRepository.save(task);
        }

        log.info("Created {} tasks for tenant {}", event.applicableRuleIds().size(), event.tenantId());
    }

    @KafkaListener(topics = KafkaTopics.DOCUMENT_EXTRACTED, groupId = "niyamitra-task-service")
    public void handleDocumentExtracted(DocumentExtractedEvent event) {
        log.info("Processing document extraction for doc={}, task={}", event.documentId(), event.taskId());

        if (event.taskId() == null) {
            log.info("No task linked to document {}, skipping task update", event.documentId());
            return;
        }

        taskRepository.findById(event.taskId()).ifPresent(task -> {
            Map<String, Object> data = event.extractedData();

            // Update task with extracted expiry date
            if (data != null && data.containsKey("expiryDate")) {
                String expiryStr = (String) data.get("expiryDate");
                try {
                    task.setDueDate(LocalDate.parse(expiryStr));
                } catch (Exception e) {
                    log.warn("Could not parse expiry date: {}", expiryStr);
                }
            }

            // If document was successfully extracted, mark task as in progress
            if (task.getStatus() == TaskStatus.PENDING) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }

            taskRepository.save(task);
            log.info("Updated task {} with extraction data from document {}", task.getId(), event.documentId());
        });
    }
}
