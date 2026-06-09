package com.adquiz.aigeneration.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    // ChatClient is Spring AI's main interface for interacting with LLMs
    // builder() is auto-configured by Spring AI using values from application.yaml
    // (api-key, model, temperature)
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
