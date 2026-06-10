package com.adquiz.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
