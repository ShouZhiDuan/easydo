package com.example.servicea.controller;

import com.example.servicea.service.HttpFtpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HttpFtpController {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpFtpController.class);
    
    @Autowired
    private HttpFtpClientService httpFtpClientService;
    
    /**
     * GET请求示例
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String id, @RequestParam Map<String, String> params) {
        try {
            logger.info("Sending GET request for user: {}", id);
            params.put("id", id);
            
            Map<String, Object> response = httpFtpClientService.sendGetRequest("/users/" + id, params);
            
            Integer status = (Integer) response.get("status");
            if (status != null && status == 200) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(status != null ? status : 500).body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * POST请求示例
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            logger.info("Sending POST request to create user: {}", userData);
            
            Map<String, Object> response = httpFtpClientService.sendPostRequest("/users", userData);
            
            Integer status = (Integer) response.get("status");
            if (status != null && status == 201) {
                return ResponseEntity.status(201).body(response);
            } else {
                return ResponseEntity.status(status != null ? status : 500).body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 通用POST请求
     */
    @PostMapping("/proxy/**")
    public ResponseEntity<Map<String, Object>> proxyPost(@RequestBody Map<String, Object> requestData, 
                                                         @RequestParam(required = false) String path) {
        try {
            String targetPath = path != null ? path : "/default";
            logger.info("Sending proxy POST request to path: {}", targetPath);
            
            Map<String, Object> response = httpFtpClientService.sendPostRequest(targetPath, requestData);
            
            Integer status = (Integer) response.get("status");
            return ResponseEntity.status(status != null ? status : 200).body(response);
        } catch (Exception e) {
            logger.error("Error processing proxy POST request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 通用GET请求处理
     */
    @GetMapping("/request")
    public ResponseEntity<Map<String, Object>> handleGetRequest(@RequestParam Map<String, String> params) {
        try {
            logger.info("Handling GET request with params: {}", params);
            
            Map<String, Object> response = httpFtpClientService.sendGetRequest("/api/request", params);
            
            Integer status = (Integer) response.get("status");
            return ResponseEntity.status(status != null ? status : 200).body(response);
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 通用POST请求处理
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> handlePostRequest(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("Handling POST request with data: {}", requestData);
            
            Map<String, Object> response = httpFtpClientService.sendPostRequest("/api/request", requestData);
            
            Integer status = (Integer) response.get("status");
            return ResponseEntity.status(status != null ? status : 200).body(response);
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "HTTP-FTP Service A",
            "timestamp", System.currentTimeMillis()
        ));
    }
}