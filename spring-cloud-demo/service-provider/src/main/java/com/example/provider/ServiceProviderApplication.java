package com.example.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务生产者启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceProviderApplication {


    public static void main(String[] args) {
        SpringApplication.run(ServiceProviderApplication.class, args);
    }

}