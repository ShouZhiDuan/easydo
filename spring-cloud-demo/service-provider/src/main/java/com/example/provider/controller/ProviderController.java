package com.example.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务生产者控制器
 */
@RestController
@RequestMapping("/provider")
@RefreshScope  // 支持配置动态刷新
public class ProviderController {

    @Value("${server.port}")
    private String port;

    @Value("${spring.application.name}")
    private String serviceName;
    
    // 从nacos配置中心获取的配置
    @Value("${app.name}")
    private String appName;
    
    @Value("${mysql.db}")
    private String mysqlDb;


    @PostConstruct
    public void init() {
        System.out.println("======>appName: " + appName);
        System.out.println("======>mysqlDb: " + mysqlDb);
    }
    


    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public String getInfo() {
        return String.format("服务名称: %s, 端口: %s, 时间: %s", 
                serviceName, port, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable String id) {
        return String.format("用户ID: %s, 来自服务: %s, 端口: %s", id, serviceName, port);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "服务运行正常";
    }
    
    /**
     * 获取从nacos配置中心加载的配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("mysql.db", mysqlDb);
        config.put("app.name", appName);
        // config.put("应用版本", appVersion);
        // config.put("应用描述", appDescription);
        // config.put("数据库URL", databaseUrl);
        // config.put("数据库用户名", databaseUsername);
        config.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return config;
    }

}