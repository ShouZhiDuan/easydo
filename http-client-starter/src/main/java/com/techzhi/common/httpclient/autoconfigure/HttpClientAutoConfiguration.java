package com.techzhi.common.httpclient.autoconfigure;

import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.HttpClientTemplate;
import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.impl.SimpleApacheHttpClientImpl;
import com.techzhi.common.httpclient.impl.OkHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP客户端自动配置类
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
@ConditionalOnProperty(prefix = "techzhi.http-client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HttpClientAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientAutoConfiguration.class);

    /**
     * Apache HttpClient 5配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "techzhi.http-client", name = "client-type", havingValue = "APACHE_HTTP_CLIENT")
    static class ApacheHttpClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HttpClient apacheHttpClient(HttpClientProperties properties) {
            logger.info("Creating Apache HttpClient 5-based HTTP client with properties: {}", properties);
            return new SimpleApacheHttpClientImpl(properties);
        }
    }

    /**
     * OkHttp客户端配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "techzhi.http-client", name = "client-type", havingValue = "OK_HTTP", matchIfMissing = true)
    static class OkHttpClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HttpClient okHttpClient(HttpClientProperties properties) {
            logger.info("Creating OkHttp-based HTTP client with properties: {}", properties);
            return new OkHttpClientImpl(properties);
        }
    }

    /**
     * 默认HTTP客户端配置（当没有其他实现可用时）
     */
    @Configuration
    @ConditionalOnMissingBean(HttpClient.class)
    static class DefaultHttpClientConfiguration {

        @Bean
        public HttpClient defaultHttpClient(HttpClientProperties properties) {
            logger.info("Creating default HTTP client (OkHttp) with properties: {}", properties);
            return new OkHttpClientImpl(properties);
        }
    }

    /**
     * HTTP客户端模板Bean
     * 提供一个便捷的HTTP客户端模板
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpClientTemplate httpClientTemplate(HttpClient httpClient) {
        return new HttpClientTemplate(httpClient);
    }
} 