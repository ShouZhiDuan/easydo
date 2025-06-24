package com.example.consumer.config;

import feign.codec.ErrorDecoder;
import feign.Response;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局Feign配置类
 * 统一配置Feign的错误处理、重试策略等
 */
@Configuration
public class GlobalFeignConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalFeignConfiguration.class);
    
    /**
     * 自定义错误解码器
     * 根据HTTP状态码返回不同的异常类型
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new GlobalFeignErrorDecoder();
    }
    
    /**
     * 自定义重试策略
     * 配置重试间隔和最大重试次数
     */
    @Bean
    public Retryer retryer() {
        // 初始间隔1秒，最大间隔3秒，最大重试3次
        return new Retryer.Default(1000, 3000, 3);
    }
    
    /**
     * 全局Feign错误解码器
     */
    public static class GlobalFeignErrorDecoder implements ErrorDecoder {
        
        private final ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();
        
        @Override
        public Exception decode(String methodKey, Response response) {
            logger.error("Feign调用失败，方法：{}，状态码：{}，原因：{}", 
                methodKey, response.status(), response.reason());
            
            switch (response.status()) {
                case 400:
                    return new IllegalArgumentException("请求参数错误");
                case 401:
                    return new SecurityException("认证失败");
                case 403:
                    return new SecurityException("权限不足");
                case 404:
                    return new NotFoundException("资源未找到");
                case 500:
                    return new InternalServerErrorException("服务内部错误");
                case 502:
                case 503:
                case 504:
                    return new ServiceUnavailableException("服务不可用");
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
    
    /**
     * 资源未找到异常
     */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * 服务内部错误异常
     */
    public static class InternalServerErrorException extends RuntimeException {
        public InternalServerErrorException(String message) {
            super(message);
        }
    }
    
    /**
     * 服务不可用异常
     */
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}