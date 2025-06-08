package com.example.sqlcsv.controller;

import com.example.sqlcsv.dto.SqlExportRequest;
import com.example.sqlcsv.dto.SqlExportResponse;
import com.example.sqlcsv.service.SqlToExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * SQL导出控制器
 */
@RestController
@RequestMapping("/api/sql-export")
@Validated
public class SqlExportController {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlExportController.class);
    
    @Autowired
    private SqlToExcelService sqlToExcelService;
    
    /**
     * 执行SQL查询并导出到Excel
     * 
     * @param request 导出请求
     * @return 导出响应
     */
    @PostMapping("/export")
    public ResponseEntity<SqlExportResponse> exportSqlToExcel(@Valid @RequestBody SqlExportRequest request) {
        logger.info("收到SQL导出请求: {}", request);
        
        try {
            SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                HttpStatus status = response.getCode() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
            }
            
        } catch (Exception e) {
            logger.error("SQL导出处理异常: {}", e.getMessage(), e);
            SqlExportResponse errorResponse = SqlExportResponse.error("服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 下载导出的Excel文件
     * 
     * @param filePath 文件路径
     * @return 文件资源
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath) {
        try {
            File file = new File(filePath);
            
            if (!file.exists() || !file.isFile()) {
                logger.warn("请求下载的文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 安全检查：确保文件在允许的目录内
            String canonicalPath = file.getCanonicalPath();
            if (!isFilePathSafe(canonicalPath)) {
                logger.warn("不安全的文件路径访问: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, 
                       "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            
            logger.info("开始下载文件: {}, 大小: {} bytes", fileName, file.length());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取导出统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = sqlToExcelService.getExportStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("获取统计信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 清理过期文件
     * 
     * @param daysToKeep 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupFiles(
            @RequestParam(value = "daysToKeep", defaultValue = "7") @Min(1) int daysToKeep) {
        try {
            int deletedCount = sqlToExcelService.cleanupExpiredFiles(daysToKeep);
            
            Map<String, Object> result = Map.of(
                "success", true,
                "message", "清理完成",
                "deletedCount", deletedCount,
                "daysToKeep", daysToKeep
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("清理文件失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = Map.of(
                "success", false,
                "message", "清理失败: " + e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "SQL Export Service",
            "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * 检查文件路径安全性
     */
    private boolean isFilePathSafe(String canonicalPath) {
        try {
            // 这里可以添加更严格的路径检查逻辑
            // 例如：检查是否在允许的导出目录内
            return canonicalPath.contains("exports") && canonicalPath.endsWith(".xlsx");
        } catch (Exception e) {
            logger.error("文件路径安全检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SqlExportResponse> handleException(Exception e) {
        logger.error("控制器异常: {}", e.getMessage(), e);
        SqlExportResponse response = SqlExportResponse.error("请求处理失败: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<SqlExportResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errorMessage.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; ")
        );
        
        logger.warn("参数验证失败: {}", errorMessage.toString());
        SqlExportResponse response = SqlExportResponse.badRequest(errorMessage.toString());
        return ResponseEntity.badRequest().body(response);
    }
}