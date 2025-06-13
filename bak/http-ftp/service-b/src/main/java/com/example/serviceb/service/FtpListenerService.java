package com.example.serviceb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FtpListenerService {
    
    private static final Logger logger = LoggerFactory.getLogger(FtpListenerService.class);
    
    @Autowired
    private FtpClientService ftpClientService;
    
    @Autowired
    private RequestProcessorService requestProcessorService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private volatile boolean isRunning = false;
    
    @PostConstruct
    public void init() {
        logger.info("FTP Listener Service initialized");
        isRunning = true;
    }
    
    /**
     * 定时检查FTP服务器上的新请求文件
     */
    @Scheduled(fixedDelay = 2000) // 每2秒检查一次
    public void checkForNewRequests() {
        if (!isRunning) {
            return;
        }
        
        try {
            FTPFile[] requestFiles = ftpClientService.listRequestFiles();
            
            if (requestFiles.length > 0) {
                logger.info("Found {} request files to process", requestFiles.length);
                
                for (FTPFile file : requestFiles) {
                    if (file.isFile() && file.getName().startsWith("request_") && file.getName().endsWith(".json")) {
                        processRequestFileAsync(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error checking for new requests", e);
        }
    }
    
    /**
     * 异步处理请求文件
     */
    @Async
    public CompletableFuture<Void> processRequestFileAsync(String fileName) {
        try {
            logger.info("Processing request file: {}", fileName);
            
            // 下载请求文件
            String requestContent = ftpClientService.downloadRequest(fileName);
            
            // 处理请求
            Map<String, Object> response = requestProcessorService.processRequest(requestContent);
            
            // 提取请求ID
            String requestId = extractRequestId(fileName);
            
            // 上传响应
            String responseJson = objectMapper.writeValueAsString(response);
            ftpClientService.uploadResponse(requestId, responseJson);
            
            logger.info("Request processed successfully: {}", fileName);
            
        } catch (Exception e) {
            logger.error("Error processing request file: {}", fileName, e);
            
            try {
                // 发送错误响应
                String requestId = extractRequestId(fileName);
                Map<String, Object> errorResponse = Map.of(
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                );
                
                String responseJson = objectMapper.writeValueAsString(errorResponse);
                ftpClientService.uploadResponse(requestId, responseJson);
                
            } catch (Exception uploadError) {
                logger.error("Failed to upload error response", uploadError);
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 从文件名中提取请求ID
     */
    private String extractRequestId(String fileName) {
        // 文件名格式: request_{requestId}.json
        if (fileName.startsWith("request_") && fileName.endsWith(".json")) {
            return fileName.substring(8, fileName.length() - 5);
        }
        return "unknown";
    }
    
    /**
     * 停止监听服务
     */
    public void stop() {
        logger.info("Stopping FTP Listener Service");
        isRunning = false;
    }
    
    /**
     * 启动监听服务
     */
    public void start() {
        logger.info("Starting FTP Listener Service");
        isRunning = true;
    }
    
    /**
     * 获取服务状态
     */
    public boolean isRunning() {
        return isRunning;
    }
}