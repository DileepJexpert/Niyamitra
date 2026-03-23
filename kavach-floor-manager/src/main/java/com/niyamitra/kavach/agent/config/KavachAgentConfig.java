package com.niyamitra.kavach.agent.config;

import com.niyamitra.kavach.agent.service.KavachAgent;
import com.niyamitra.kavach.agent.tool.KavachTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KavachAgentConfig {

    @Value("${kavach.ai.anthropic-api-key:}")
    private String anthropicApiKey;

    @Value("${kavach.ai.model:claude-haiku-4-5-20251001}")
    private String modelName;

    @Bean
    public ChatLanguageModel kavachChatModel() {
        return AnthropicChatModel.builder()
                .apiKey(anthropicApiKey.isEmpty() ? "demo" : anthropicApiKey)
                .modelName(modelName)
                .maxTokens(512)
                .build();
    }

    @Bean
    public KavachAgent kavachAgent(ChatLanguageModel kavachChatModel, KavachTools kavachTools) {
        return AiServices.builder(KavachAgent.class)
                .chatLanguageModel(kavachChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(kavachTools)
                .build();
    }
}
