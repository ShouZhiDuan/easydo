package com.example.httpclient.config;

import com.example.httpclient.service.HttpClientService;
import com.example.httpclient.service.impl.OkHttpHttpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpClientService httpClientService(HttpClientProperties properties, ObjectMapper objectMapper) {
        return new OkHttpHttpClientService(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(); // Provide a default ObjectMapper if none exists
    }
}