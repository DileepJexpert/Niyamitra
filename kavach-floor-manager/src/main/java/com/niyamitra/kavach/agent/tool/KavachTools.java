package com.niyamitra.kavach.agent.tool;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.EscalationTriggeredEvent;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class KavachTools {

    private final RestClient taskServiceClient;
    private final RestClient ruleServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KavachTools(
            @Value("${kavach.services.task-url:http://localhost:8084}") String taskUrl,
            @Value("${kavach.services.rule-url:http://localhost:8082}") String ruleUrl,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.taskServiceClient = RestClient.builder().baseUrl(taskUrl).build();
        this.ruleServiceClient = RestClient.builder().baseUrl(ruleUrl).build();
        this.kafkaTemplate = kafkaTemplate;
    }

    @Tool("Fetch Anupalan compliance tasks due within N days for a user")
    public String getUpcomingTasks(String userId, int days) {
        log.info("Kavach tool: getUpcomingTasks for user={}, days={}", userId, days);
        try {
            return taskServiceClient.get()
                    .uri("/api/v1/tasks?tenantId={tenantId}&status=PENDING", userId)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Unable to fetch tasks: " + e.getMessage();
        }
    }

    @Tool("Get full task details including linked documents")
    public String getTaskDetails(String taskId) {
        log.info("Kavach tool: getTaskDetails for task={}", taskId);
        try {
            return taskServiceClient.get()
                    .uri("/api/v1/tasks/{taskId}", taskId)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Unable to fetch task details: " + e.getMessage();
        }
    }

    @Tool("Postpone an Anupalan task deadline with reason")
    public String rescheduleTask(String taskId, String newDate, String reason) {
        log.info("Kavach tool: rescheduleTask task={}, newDate={}, reason={}", taskId, newDate, reason);
        try {
            return taskServiceClient.put()
                    .uri("/api/v1/tasks/{taskId}/reschedule?newDate={newDate}&reason={reason}",
                            taskId, newDate, reason)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Unable to reschedule task: " + e.getMessage();
        }
    }

    @Tool("Mark that a document photo has been received via Kavach WhatsApp")
    public String markDocumentReceived(String taskId) {
        log.info("Kavach tool: markDocumentReceived for task={}", taskId);
        try {
            return taskServiceClient.post()
                    .uri("/api/v1/tasks/{taskId}/acknowledge", taskId)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Unable to mark document received: " + e.getMessage();
        }
    }

    @Tool("Kavach escalation: send notification to the factory owner")
    public String escalateToOwner(String taskId, String reason) {
        log.info("Kavach tool: escalateToOwner task={}, reason={}", taskId, reason);
        try {
            EscalationTriggeredEvent event = new EscalationTriggeredEvent(
                    UUID.randomUUID(),
                    UUID.fromString(taskId),
                    null, // tenantId resolved by notification service
                    null, // escalateToUserId resolved by notification service
                    reason,
                    Instant.now(),
                    UUID.randomUUID(),
                    "KAVACH"
            );
            kafkaTemplate.send(KafkaTopics.ESCALATION_TRIGGERED, taskId, event);
            return "Kavach escalation sent to factory owner for task " + taskId;
        } catch (Exception e) {
            return "Escalation failed: " + e.getMessage();
        }
    }

    @Tool("Search the Anupalan regulatory database for regulation information")
    public String searchAnupalanRules(String query) {
        log.info("Kavach tool: searchAnupalanRules query={}", query);
        try {
            return ruleServiceClient.get()
                    .uri("/api/v1/anupalan/rules/applicable?nicCode={nic}&state=UP", query)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Unable to search rules: " + e.getMessage();
        }
    }
}
