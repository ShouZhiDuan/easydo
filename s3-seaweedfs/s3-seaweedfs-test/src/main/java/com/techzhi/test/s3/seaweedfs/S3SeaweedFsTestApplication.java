package com.techzhi.test.s3.seaweedfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * S3 SeaweedFS 测试应用启动类
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.techzhi.test.s3.seaweedfs", "com.techzhi.common.s3.seaweedfs"})
public class S3SeaweedFsTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3SeaweedFsTestApplication.class, args);
    }
}