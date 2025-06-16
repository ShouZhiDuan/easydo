package com.techzhi.harbor.config;

import com.techzhi.harbor.client.HarborClient;
import com.techzhi.harbor.service.DockerImageService;
import com.techzhi.harbor.service.HarborImageService;
import com.techzhi.harbor.util.HarborUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Harbor自动配置类
 * 
 * @author techzhi
 */
@Configuration
@EnableConfigurationProperties(HarborProperties.class)
public class HarborAutoConfiguration {

    /**
     * 创建Harbor客户端Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public HarborClient harborClient(HarborProperties properties) {
        return new HarborClient(properties);
    }

    /**
     * 创建Harbor镜像服务Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public HarborImageService harborImageService(HarborClient harborClient, HarborProperties properties) {
        return new HarborImageService(harborClient, properties);
    }

    /**
     * 创建Docker镜像服务Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public DockerImageService dockerImageService(HarborProperties properties) {
        return new DockerImageService(properties);
    }

    /**
     * 创建Harbor工具类Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public HarborUtil harborUtil(HarborImageService harborImageService, DockerImageService dockerImageService) {
        return new HarborUtil(harborImageService, dockerImageService);
    }
}