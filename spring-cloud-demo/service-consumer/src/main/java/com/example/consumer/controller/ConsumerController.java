package com.example.consumer.controller;

import com.example.consumer.feign.ProviderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 服务消费者控制器
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ProviderFeignClient providerFeignClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${server.port}")
    private String port;

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 使用Feign调用服务生产者获取信息
     */
    @GetMapping("/info")
    public String getProviderInfo() {
        String result = providerFeignClient.getInfo();
        return String.format("消费者[%s:%s] 调用结果: %s", serviceName, port, result);
    }

    /**
     * 使用Feign调用服务生产者获取用户信息
     */
    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable String id) {
        String result = providerFeignClient.getUserById(id);
        return String.format("消费者[%s:%s] 调用结果: %s", serviceName, port, result);
    }

    /**
     * 获取服务实例列表
     */
    @GetMapping("/instances")
    public List<ServiceInstance> getServiceInstances() {
        return discoveryClient.getInstances("service-provider");
    }

    /**
     * 获取所有服务列表
     */
    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        String providerHealth = providerFeignClient.health();
        return String.format("消费者服务正常, 生产者状态: %s", providerHealth);
    }

}