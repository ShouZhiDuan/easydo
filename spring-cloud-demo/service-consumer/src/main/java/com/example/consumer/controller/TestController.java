package com.example.consumer.controller;

import com.example.consumer.feign.ProviderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于演示全局降级处理器的效果
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private ProviderFeignClient providerFeignClient;
    
    /**
     * 测试获取服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> testGetInfo() {
        Map<String, Object> result = new HashMap<>();
        String info = providerFeignClient.getInfo();
        
        // 检查是否为降级响应
        if (isFallbackResponse(info)) {
            result.put("success", false);
            result.put("data", null);
            result.put("message", info);
        } else {
            result.put("success", true);
            result.put("data", info);
            result.put("message", "调用成功");
        }
        return result;
    }
    
    /**
     * 测试根据ID获取用户信息
     */
    @GetMapping("/user/{id}")
    public Map<String, Object> testGetUserById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        String userInfo = providerFeignClient.getUserById(id);
        
        // 检查是否为降级响应
        if (isFallbackResponse(userInfo)) {
            result.put("success", false);
            result.put("data", null);
            result.put("message", userInfo);
        } else {
            result.put("success", true);
            result.put("data", userInfo);
            result.put("message", "调用成功");
        }
        return result;
    }
    
    /**
     * 测试健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> testHealth() {
        Map<String, Object> result = new HashMap<>();
        String health = providerFeignClient.health();
        
        // 检查是否为降级响应
        if (isFallbackResponse(health)) {
            result.put("success", false);
            result.put("data", null);
            result.put("message", health);
        } else {
            result.put("success", true);
            result.put("data", health);
            result.put("message", "调用成功");
        }
        return result;
    }
    
    /**
     * 判断是否为降级响应
     * 根据降级工厂返回的特定消息模式来判断
     */
    private boolean isFallbackResponse(String response) {
        if (response == null) {
            return false;
        }
        // 检查是否包含降级响应的关键词
        return response.contains("服务调用失败") ||
               response.contains("服务暂时不可用") || 
               response.contains("服务不可用") ||
               response.contains("服务健康检查失败") ||
               response.contains("用户服务暂时不可用") ||
               response.contains("用户不存在") ||
               response.contains("服务内部错误") ||
               response.contains("请求的资源不存在");
    }
    
    /**
     * 测试所有接口
     */
    @GetMapping("/all")
    public Map<String, Object> testAll() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        
        // 测试info接口
        try {
            String info = providerFeignClient.getInfo();
            if (isFallbackResponse(info)) {
                results.put("info", Map.of("success", false, "message", info));
            } else {
                results.put("info", Map.of("success", true, "data", info));
            }
        } catch (Exception e) {
            results.put("info", Map.of("success", false, "error", e.getMessage()));
        }
        
        // 测试user接口
        try {
            String userInfo = providerFeignClient.getUserById("123");
            if (isFallbackResponse(userInfo)) {
                results.put("user", Map.of("success", false, "message", userInfo));
            } else {
                results.put("user", Map.of("success", true, "data", userInfo));
            }
        } catch (Exception e) {
            results.put("user", Map.of("success", false, "error", e.getMessage()));
        }
        
        // 测试health接口
        try {
            String health = providerFeignClient.health();
            if (isFallbackResponse(health)) {
                results.put("health", Map.of("success", false, "message", health));
            } else {
                results.put("health", Map.of("success", true, "data", health));
            }
        } catch (Exception e) {
            results.put("health", Map.of("success", false, "error", e.getMessage()));
        }
        
        result.put("results", results);
        result.put("message", "批量测试完成");
        return result;
    }
}