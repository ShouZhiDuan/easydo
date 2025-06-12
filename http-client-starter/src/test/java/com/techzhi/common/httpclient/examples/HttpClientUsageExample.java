package com.techzhi.common.httpclient.examples;

import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.HttpClientTemplate;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP客户端使用示例
 */
@Service
public class HttpClientUsageExample {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private HttpClientTemplate httpClientTemplate;

    /**
     * 基本GET请求示例
     */
    public void basicGetExample() {
        HttpResponse<String> response = httpClient.get("https://httpbin.org/get");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }

    /**
     * POST请求示例
     */
    public void postExample() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "test");
        requestBody.put("value", "example");

        HttpResponse<String> response = httpClient.post("https://httpbin.org/post", requestBody);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }

    /**
     * 带请求头的请求示例
     */
    public void requestWithHeadersExample() {
        HttpRequest request = HttpRequest.get("https://httpbin.org/headers")
                .header("Accept", "application/json")
                .header("User-Agent", "TechZhi-HttpClient/1.0");

        HttpResponse<String> response = httpClient.execute(request);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }
} 