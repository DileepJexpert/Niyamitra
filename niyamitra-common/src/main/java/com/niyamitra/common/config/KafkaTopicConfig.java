package com.niyamitra.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_UPLOADED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic documentExtractedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_EXTRACTED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic tenantOnboardedTopic() {
        return TopicBuilder.name(KafkaTopics.TENANT_ONBOARDED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic tasksGeneratedTopic() {
        return TopicBuilder.name(KafkaTopics.TASKS_GENERATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic gazetteFoundTopic() {
        return TopicBuilder.name(KafkaTopics.GAZETTE_FOUND).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic whatsappReceivedTopic() {
        return TopicBuilder.name(KafkaTopics.WHATSAPP_RECEIVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic whatsappSendTopic() {
        return TopicBuilder.name(KafkaTopics.WHATSAPP_SEND).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic expiryApproachingTopic() {
        return TopicBuilder.name(KafkaTopics.EXPIRY_APPROACHING).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic escalationTriggeredTopic() {
        return TopicBuilder.name(KafkaTopics.ESCALATION_TRIGGERED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic portalCheckRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.PORTAL_CHECK_REQUESTED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic portalCheckCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.PORTAL_CHECK_COMPLETED).partitions(3).replicas(1).build();
    }
}
