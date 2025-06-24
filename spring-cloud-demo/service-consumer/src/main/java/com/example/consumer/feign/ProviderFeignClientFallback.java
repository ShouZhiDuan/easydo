package com.example.consumer.feign;

import org.springframework.stereotype.Component;

/**
 * 服务生产者Feign客户端降级处理
 */
@Component
public class ProviderFeignClientFallback implements ProviderFeignClient {

    @Override
        public String getInfo() {
        return "服务暂时不可用，请稍后重试";
    }

    @Override
    public String getUserById(String id) {
        return String.format("用户ID: %s, 服务暂时不可用", id);
    }

    @Override
    public String health() {
        return "服务健康检查失败";
    }

}