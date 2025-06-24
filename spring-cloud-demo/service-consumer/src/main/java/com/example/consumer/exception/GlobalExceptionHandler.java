package com.example.consumer.exception;

import com.example.consumer.config.GlobalFeignConfiguration;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理Feign调用异常和其他业务异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理Feign异常
     */
    @ExceptionHandler(FeignException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException e) {
        logger.error("Feign调用异常：{}", e.getMessage(), e);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "服务调用失败：" + e.getMessage());
        result.put("code", e.status());
        result.put("timestamp", System.currentTimeMillis());
        
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        if (e.status() == 404) {
            status = HttpStatus.NOT_FOUND;
        } else if (e.status() == 500) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return ResponseEntity.status(status).body(result);
    }
    
    /**
     * 处理重试异常
     */
    @ExceptionHandler(RetryableException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRetryableException(RetryableException e) {
        logger.error("服务重试失败：{}", e.getMessage(), e);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "服务重试失败，请稍后再试");
        result.put("code", 503);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }
    
    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(GlobalFeignConfiguration.NotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleNotFoundException(GlobalFeignConfiguration.NotFoundException e) {
        logger.warn("资源未找到：{}", e.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("code", 404);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    
    /**
     * 处理服务内部错误异常
     */
    @ExceptionHandler(GlobalFeignConfiguration.InternalServerErrorException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleInternalServerErrorException(GlobalFeignConfiguration.InternalServerErrorException e) {
        logger.error("服务内部错误：{}", e.getMessage(), e);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("code", 500);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    
    /**
     * 处理服务不可用异常
     */
    @ExceptionHandler(GlobalFeignConfiguration.ServiceUnavailableException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleServiceUnavailableException(GlobalFeignConfiguration.ServiceUnavailableException e) {
        logger.error("服务不可用：{}", e.getMessage(), e);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("code", 503);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }
    
    /**
     * 处理安全异常
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        logger.warn("安全异常：{}", e.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("code", 403);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常：{}", e.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", e.getMessage());
        result.put("code", 400);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        logger.error("未知异常：{}", e.getMessage(), e);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "系统异常，请联系管理员");
        result.put("code", 500);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}