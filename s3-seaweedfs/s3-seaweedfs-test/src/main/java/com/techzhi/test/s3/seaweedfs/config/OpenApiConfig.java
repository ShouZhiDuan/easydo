package com.techzhi.test.s3.seaweedfs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 * 用于生成API文档
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("S3 SeaweedFS Test API")
                        .description("SeaweedFS S3协议测试应用API文档")
                        .version("1.0.0"));
    }
}