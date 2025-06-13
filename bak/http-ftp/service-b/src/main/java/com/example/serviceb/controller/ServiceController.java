package com.example.serviceb.controller;

import com.example.serviceb.service.FtpListenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServiceController {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    
    @Autowired
    private FtpListenerService ftpListenerService;
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "HTTP-FTP Service B",
            "ftpListener", ftpListenerService.isRunning() ? "RUNNING" : "STOPPED",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * 获取服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "service", "HTTP-FTP Service B",
            "version", "1.0.0",
            "ftpListener", Map.of(
                "running", ftpListenerService.isRunning(),
                "status", ftpListenerService.isRunning() ? "ACTIVE" : "INACTIVE"
            ),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * 启动FTP监听服务
     */
    @PostMapping("/listener/start")
    public ResponseEntity<Map<String, Object>> startListener() {
        try {
            ftpListenerService.start();
            logger.info("FTP Listener service started via API");
            
            return ResponseEntity.ok(Map.of(
                "message", "FTP Listener service started successfully",
                "status", "RUNNING",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("Failed to start FTP Listener service", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to start FTP Listener service",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 停止FTP监听服务
     */
    @PostMapping("/listener/stop")
    public ResponseEntity<Map<String, Object>> stopListener() {
        try {
            ftpListenerService.stop();
            logger.info("FTP Listener service stopped via API");
            
            return ResponseEntity.ok(Map.of(
                "message", "FTP Listener service stopped successfully",
                "status", "STOPPED",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("Failed to stop FTP Listener service", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to stop FTP Listener service",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 获取FTP监听服务状态
     */
    @GetMapping("/listener/status")
    public ResponseEntity<Map<String, Object>> getListenerStatus() {
        return ResponseEntity.ok(Map.of(
            "running", ftpListenerService.isRunning(),
            "status", ftpListenerService.isRunning() ? "RUNNING" : "STOPPED",
            "description", "FTP request listener service",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * 处理GET请求 - 通过FTP转发
     */
    @GetMapping("/request")
    public ResponseEntity<Map<String, Object>> handleGetRequest(@RequestParam Map<String, String> params) {
        try {
            logger.info("Received GET request with params: {}", params);
            
            // 模拟处理请求并返回响应
            Map<String, Object> response = Map.of(
                "status", 200,
                "message", "GET request processed successfully",
                "service", "HTTP-FTP Service B",
                "receivedParams", params,
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("GET request processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "service", "HTTP-FTP Service B",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 处理POST请求 - 通过FTP转发
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> handlePostRequest(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("Received POST request with data: {}", requestData);
            
            // 模拟处理请求并返回响应
            Map<String, Object> response = Map.of(
                "status", 200,
                "message", "POST request processed successfully",
                "service", "HTTP-FTP Service B",
                "receivedData", requestData,
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("POST request processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "service", "HTTP-FTP Service B",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}