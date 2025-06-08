package com.example.servicea.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class HttpFtpClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpFtpClientService.class);
    
    @Autowired
    private FtpClientService ftpClientService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送GET请求
     */
    public Map<String, Object> sendGetRequest(String path, Map<String, String> params) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("method", "GET");
        request.put("path", path);
        request.put("params", params);
        request.put("timestamp", System.currentTimeMillis());
        
        return sendRequest(request);
    }
    
    /**
     * 发送POST请求
     */
    public Map<String, Object> sendPostRequest(String path, Object body) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("method", "POST");
        request.put("path", path);
        request.put("body", body);
        request.put("timestamp", System.currentTimeMillis());
        
        return sendRequest(request);
    }
    
    /**
     * 发送请求并等待响应
     */
    private Map<String, Object> sendRequest(Map<String, Object> request) throws IOException {
        try {
            // 将请求序列化为JSON
            String requestJson = objectMapper.writeValueAsString(request);
            logger.info("Sending request: {}", requestJson);
            
            // 上传请求到远程FTP服务器
            String requestId = ftpClientService.uploadRequest(requestJson);
            
            // 等待响应（30秒超时）
            String responseJson = ftpClientService.downloadResponse(requestId, 30000);
            logger.info("Received response: {}", responseJson);
            
            // 解析响应
            @SuppressWarnings("unchecked")
            Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
            return response;
            
        } catch (Exception e) {
            logger.error("Error sending request via FTP", e);
            
            // 返回错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }
}