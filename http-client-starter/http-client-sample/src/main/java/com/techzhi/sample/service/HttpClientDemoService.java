package com.techzhi.sample.service;

import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.HttpClientTemplate;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.sample.model.Post;
import com.techzhi.sample.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class HttpClientDemoService {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientDemoService.class);
    
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private HttpClientTemplate httpClientTemplate;
    
    // 基础API地址
    private static final String JSONPLACEHOLDER_BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String HTTPBIN_BASE_URL = "https://httpbin.org";
    
    /**
     * 场景1: 基本GET请求
     */
    public void basicGetExample() {
        logger.info("=== 场景1: 基本GET请求示例 ===");
        
        try {
            // 简单的GET请求
            HttpResponse<String> response = httpClient.get(JSONPLACEHOLDER_BASE_URL + "/posts/1");
            
            logger.info("响应状态码: {}", response.getStatusCode());
            logger.info("响应内容: {}", response.getBody());
            
        } catch (Exception e) {
            logger.error("基本GET请求失败", e);
        }
    }
    
    /**
     * 场景2: 带类型转换的GET请求
     */
    public void getWithTypeConversionExample() {
        logger.info("=== 场景2: 带类型转换的GET请求示例 ===");
        
        try {
            // 获取单个用户，自动转换为User对象
            HttpResponse<User> userResponse = httpClient.get(
                JSONPLACEHOLDER_BASE_URL + "/users/1", 
                User.class
            );
            
                         if (userResponse.isSuccessful()) {
                User user = userResponse.getBody();
                logger.info("获取用户成功: {}", user);
            } else {
                logger.error("获取用户失败，状态码: {}", userResponse.getStatusCode());
            }
            
            // 获取用户列表（简化处理，直接获取字符串响应）
            HttpResponse<String> usersResponse = httpClient.get(
                JSONPLACEHOLDER_BASE_URL + "/users"
            );
            
                         if (usersResponse.isSuccessful()) {
                String usersJson = usersResponse.getBody();
                logger.info("获取用户列表成功，响应: {}", usersJson.substring(0, Math.min(200, usersJson.length())) + "...");
            }
            
        } catch (Exception e) {
            logger.error("类型转换GET请求失败", e);
        }
    }
    
    /**
     * 场景3: POST请求示例
     */
    public void postExample() {
        logger.info("=== 场景3: POST请求示例 ===");
        
        try {
            // 创建新的Post
            Post newPost = new Post(1L, "示例标题", "这是一个示例内容");
            
            HttpResponse<Post> postResponse = httpClient.post(
                JSONPLACEHOLDER_BASE_URL + "/posts",
                newPost,
                Post.class
            );
            
            if (postResponse.isSuccessful()) {
                Post createdPost = postResponse.getBody();
                logger.info("创建Post成功: {}", createdPost);
            } else {
                logger.error("创建Post失败，状态码: {}", postResponse.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("POST请求失败", e);
        }
    }
    
    /**
     * 场景4: 自定义请求头和查询参数
     */
    public void customRequestExample() {
        logger.info("=== 场景4: 自定义请求头和查询参数示例 ===");
        
        try {
            HttpRequest request = HttpRequest.get(HTTPBIN_BASE_URL + "/get")
                .header("X-Custom-Header", "CustomValue")
                .header("User-Agent", "TechZhi-HttpClient/1.0")
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("search", "test");
            
            HttpResponse<String> response = httpClient.execute(request);
            
            if (response.isSuccessful()) {
                logger.info("自定义请求成功，响应: {}", response.getBody());
            } else {
                logger.error("自定义请求失败，状态码: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("自定义请求失败", e);
        }
    }
    
    /**
     * 场景5: 异步请求示例
     */
    public void asyncRequestExample() {
        logger.info("=== 场景5: 异步请求示例 ===");
        
        try {
            // 异步GET请求
            CompletableFuture<HttpResponse<String>> future = httpClient.getAsync(
                JSONPLACEHOLDER_BASE_URL + "/posts/1"
            );
            
            future.thenAccept(response -> {
                if (response.isSuccessful()) {
                    logger.info("异步请求成功，响应: {}", response.getBody());
                } else {
                    logger.error("异步请求失败，状态码: {}", response.getStatusCode());
                }
            }).exceptionally(throwable -> {
                logger.error("异步请求异常", throwable);
                return null;
            });
            
            // 等待异步请求完成（实际应用中不需要）
            Thread.sleep(2000);
            
        } catch (Exception e) {
            logger.error("异步请求设置失败", e);
        }
    }
    
    /**
     * 场景6: 表单提交示例
     */
    public void formSubmitExample() {
        logger.info("=== 场景6: 表单提交示例 ===");
        
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("title", "表单标题");
            formData.put("body", "表单内容");
            formData.put("userId", "1");
            
            HttpRequest request = HttpRequest.post(HTTPBIN_BASE_URL + "/post")
                .formBody(formData);
            
            HttpResponse<String> response = httpClient.execute(request);
            
            if (response.isSuccessful()) {
                logger.info("表单提交成功，响应: {}", response.getBody());
            } else {
                logger.error("表单提交失败，状态码: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("表单提交失败", e);
        }
    }
    
    /**
     * 场景7: HttpClientTemplate使用示例
     */
    public void httpClientTemplateExample() {
        logger.info("=== 场景7: HttpClientTemplate使用示例 ===");
        
        try {
            // 使用模板发送请求（假设有token认证）
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
            
            HttpResponse<String> response = httpClientTemplate.getWithToken(
                HTTPBIN_BASE_URL + "/bearer",
                token
            );
            
            if (response.isSuccessful()) {
                logger.info("Token认证请求成功，响应: {}", response.getBody());
            } else {
                logger.error("Token认证请求失败，状态码: {}", response.getStatusCode());
            }
            
            // Basic认证示例
            HttpResponse<String> basicAuthResponse = httpClientTemplate.getWithBasicAuth(
                HTTPBIN_BASE_URL + "/basic-auth/user/pass",
                "user",
                "pass"
            );
            
            if (basicAuthResponse.isSuccessful()) {
                logger.info("Basic认证请求成功，响应: {}", basicAuthResponse.getBody());
            } else {
                logger.error("Basic认证请求失败，状态码: {}", basicAuthResponse.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("HttpClientTemplate请求失败", e);
        }
    }
    
    /**
     * 场景8: 错误处理示例
     */
    public void errorHandlingExample() {
        logger.info("=== 场景8: 错误处理示例 ===");
        
        try {
            // 请求不存在的资源（404错误）
            HttpResponse<String> notFoundResponse = httpClient.get(
                JSONPLACEHOLDER_BASE_URL + "/posts/999999"
            );
            
            if (notFoundResponse.isSuccessful()) {
                logger.info("请求成功: {}", notFoundResponse.getBody());
            } else {
                logger.warn("请求失败，状态码: {}, 错误信息: {}", 
                          notFoundResponse.getStatusCode(), 
                          notFoundResponse.getReasonPhrase());
            }
            
            // 请求无效的URL（连接错误）
            try {
                HttpResponse<String> invalidUrlResponse = httpClient.get("http://invalid-url-that-does-not-exist.com");
            } catch (Exception e) {
                logger.error("连接失败（预期错误）: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("错误处理示例失败", e);
        }
    }
    
    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        logger.info("开始运行HTTP Client所有示例...");
        
        basicGetExample();
        getWithTypeConversionExample();
        postExample();
        customRequestExample();
        asyncRequestExample();
        formSubmitExample();
        httpClientTemplateExample();
        errorHandlingExample();
        
        logger.info("所有示例运行完成！");
    }
} 