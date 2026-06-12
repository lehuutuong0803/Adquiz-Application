package com.adquiz.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConfigurationProperties(prefix = "ai-generation")
public class AIGenerationProperties {

    private String serviceUrl;
    private int questionThreshold;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public int getQuestionThreshold() {
        return questionThreshold;
    }

    public void setQuestionThreshold(int questionThreshold) {
        this.questionThreshold = questionThreshold;
    }
}
