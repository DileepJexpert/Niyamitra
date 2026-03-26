package com.niyamitra.anupalan.service;

import com.niyamitra.anupalan.model.AnupalanRule;
import com.niyamitra.anupalan.repository.AnupalanRuleRepository;
import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.TasksGeneratedEvent;
import com.niyamitra.common.event.TenantOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantOnboardedConsumer {

    private final AnupalanRuleRepository ruleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.TENANT_ONBOARDED, groupId = "anupalan-rule-service")
    public void handleTenantOnboarded(TenantOnboardedEvent event) {
        log.info("Anupalan processing tenant onboarded: tenantId={}, nicCode={}, state={}",
                event.tenantId(), event.nicCode(), event.state());

        try {
            List<AnupalanRule> applicableRules = ruleRepository.findApplicableRules(
                    event.nicCode(), event.state());

            if (applicableRules.isEmpty()) {
                log.warn("No Anupalan rules found for NIC={}, state={}", event.nicCode(), event.state());
                return;
            }

            List<UUID> ruleIds = applicableRules.stream().map(AnupalanRule::getId).toList();

            TasksGeneratedEvent tasksEvent = new TasksGeneratedEvent(
                    UUID.randomUUID(),
                    event.tenantId(),
                    ruleIds,
                    ruleIds.size(),
                    Instant.now(),
                    event.correlationId(),
                    "ANUPALAN"
            );

            kafkaTemplate.send(KafkaTopics.TASKS_GENERATED, event.tenantId().toString(), tasksEvent);
            log.info("Anupalan generated {} task rules for tenant {}", ruleIds.size(), event.tenantId());

        } catch (Exception e) {
            log.error("Failed to process tenant onboarded event for {}: {}", event.tenantId(), e.getMessage(), e);
        }
    }
}
