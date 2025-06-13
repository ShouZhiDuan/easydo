package com.techzhi.sample.runner;

import com.techzhi.sample.service.HttpClientDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DemoRunner.class);
    
    @Autowired
    private HttpClientDemoService httpClientDemoService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=================================================");
        logger.info("       TechZhi HTTP Client Starter 示例应用");
        logger.info("=================================================");
        logger.info("应用已启动，开始运行HTTP客户端示例...");
        logger.info("");
        
        // 运行几个基本示例
        try {
            logger.info("正在运行基本GET请求示例...");
            httpClientDemoService.basicGetExample();
            
            Thread.sleep(1000); // 稍微等待一下
            
            logger.info("正在运行类型转换GET请求示例...");
            httpClientDemoService.getWithTypeConversionExample();
            
            Thread.sleep(1000);
            
            logger.info("正在运行POST请求示例...");
            httpClientDemoService.postExample();
            
        } catch (Exception e) {
            logger.error("运行启动示例时发生错误", e);
        }
        
        logger.info("");
        logger.info("=================================================");
        logger.info("              示例运行完成！");
        logger.info("=================================================");
        logger.info("您可以通过以下方式运行更多示例：");
        logger.info("");
        logger.info("1. 使用HTTP API接口：");
        logger.info("   - 获取所有可用接口: GET http://localhost:8080/api/demo/endpoints");
        logger.info("   - 运行所有HTTP客户端示例: POST http://localhost:8080/api/demo/http-client/all");
        logger.info("   - 运行所有文件操作示例: POST http://localhost:8080/api/demo/file-operations/all");
        logger.info("");
        logger.info("2. 查看详细的示例代码：");
        logger.info("   - HttpClientDemoService.java - HTTP客户端使用示例");
        logger.info("   - FileOperationDemoService.java - 文件操作示例");
        logger.info("");
        logger.info("3. 配置说明：");
        logger.info("   - 查看 application.yml 中的HTTP客户端配置");
        logger.info("   - 修改配置来测试不同的客户端行为");
        logger.info("");
        logger.info("=================================================");
        logger.info("             欢迎使用 TechZhi HTTP Client Starter!");
        logger.info("=================================================");
    }
} 