package com.example.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 服务生产者Feign客户端
 */
@FeignClient(name = "service-provider", fallbackFactory = GlobalFeignFallbackFactory.class)
public interface ProviderFeignClient {

    /**
     * 获取服务信息
     */
    @GetMapping("/provider/info")
    String getInfo();

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/provider/user/{id}")
    String getUserById(@PathVariable("id") String id);

    /**
     * 健康检查
     */
    @GetMapping("/provider/health")
    String health();

}