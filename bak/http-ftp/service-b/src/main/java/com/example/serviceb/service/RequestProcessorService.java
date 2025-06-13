package com.example.serviceb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RequestProcessorService {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessorService.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理接收到的请求
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> processRequest(String requestJson) {
        try {
            Map<String, Object> request = objectMapper.readValue(requestJson, Map.class);
            
            String method = (String) request.get("method");
            String path = (String) request.get("path");
            
            logger.info("Processing {} request for path: {}", method, path);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "HTTP-FTP Service B");
            
            if ("GET".equals(method)) {
                return processGetRequest(path, request, response);
            } else if ("POST".equals(method)) {
                return processPostRequest(path, request, response);
            } else {
                response.put("status", 405);
                response.put("error", "Method Not Allowed");
                response.put("message", "Unsupported method: " + method);
                return response;
            }
            
        } catch (Exception e) {
            logger.error("Error processing request", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return errorResponse;
        }
    }
    
    /**
     * 处理GET请求
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processGetRequest(String path, Map<String, Object> request, Map<String, Object> response) {
        Map<String, String> params = (Map<String, String>) request.get("params");
        
        if (path.startsWith("/users/")) {
            String userId = params != null ? params.get("id") : null;
            if (userId == null && path.length() > 7) {
                userId = path.substring(7); // Extract from path
            }
            
            if (userId != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("name", "User " + userId);
                userData.put("email", "user" + userId + "@example.com");
                userData.put("status", "active");
                
                response.put("status", 200);
                response.put("data", userData);
                response.put("message", "User retrieved successfully");
            } else {
                response.put("status", 400);
                response.put("error", "Bad Request");
                response.put("message", "User ID is required");
            }
        } else {
            response.put("status", 404);
            response.put("error", "Not Found");
            response.put("message", "Endpoint not found: " + path);
        }
        
        return response;
    }
    
    /**
     * 处理POST请求
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processPostRequest(String path, Map<String, Object> request, Map<String, Object> response) {
        Object body = request.get("body");
        
        if ("/users".equals(path)) {
            if (body instanceof Map) {
                Map<String, Object> userData = (Map<String, Object>) body;
                
                // 模拟创建用户
                String userId = "user_" + System.currentTimeMillis();
                userData.put("id", userId);
                userData.put("createdAt", System.currentTimeMillis());
                userData.put("status", "active");
                
                response.put("status", 201);
                response.put("data", userData);
                response.put("message", "User created successfully");
            } else {
                response.put("status", 400);
                response.put("error", "Bad Request");
                response.put("message", "Invalid request body");
            }
        } else if ("/default".equals(path)) {
            // 处理通用请求
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("received", body);
            responseData.put("processed", true);
            responseData.put("processedAt", System.currentTimeMillis());
            
            response.put("status", 200);
            response.put("data", responseData);
            response.put("message", "Request processed successfully");
        } else {
            response.put("status", 404);
            response.put("error", "Not Found");
            response.put("message", "Endpoint not found: " + path);
        }
        
        return response;
    }
}