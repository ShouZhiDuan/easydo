package com.example.sqlcsv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SQL导出响应DTO
 */
public class SqlExportResponse {
    
    /**
     * 响应状态码
     */
    @JsonProperty("code")
    private int code;
    
    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * 是否成功
     */
    @JsonProperty("success")
    private boolean success;
    
    /**
     * 生成的文件路径
     */
    @JsonProperty("filePath")
    private String filePath;
    
    /**
     * 文件大小（字节）
     */
    @JsonProperty("fileSize")
    private Long fileSize;
    
    /**
     * 处理的SQL数量
     */
    @JsonProperty("sqlCount")
    private Integer sqlCount;
    
    /**
     * 总记录数
     */
    @JsonProperty("totalRecords")
    private Long totalRecords;
    
    /**
     * 每个查询的记录数
     */
    @JsonProperty("recordCounts")
    private List<Long> recordCounts;
    
    /**
     * 处理耗时（毫秒）
     */
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    /**
     * 错误信息列表
     */
    @JsonProperty("errors")
    private List<String> errors;
    
    /**
     * 警告信息列表
     */
    @JsonProperty("warnings")
    private List<String> warnings;
    
    // 构造函数
    public SqlExportResponse() {
        this.createdAt = LocalDateTime.now();
    }
    
    // 静态工厂方法
    public static SqlExportResponse success(String filePath) {
        SqlExportResponse response = new SqlExportResponse();
        response.setCode(200);
        response.setMessage("导出成功");
        response.setSuccess(true);
        response.setFilePath(filePath);
        return response;
    }
    
    public static SqlExportResponse error(int code, String message) {
        SqlExportResponse response = new SqlExportResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
    
    public static SqlExportResponse error(String message) {
        return error(500, message);
    }
    
    public static SqlExportResponse badRequest(String message) {
        return error(400, message);
    }
    
    // Getters and Setters
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Integer getSqlCount() {
        return sqlCount;
    }
    
    public void setSqlCount(Integer sqlCount) {
        this.sqlCount = sqlCount;
    }
    
    public Long getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public List<Long> getRecordCounts() {
        return recordCounts;
    }
    
    public void setRecordCounts(List<Long> recordCounts) {
        this.recordCounts = recordCounts;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    @Override
    public String toString() {
        return "SqlExportResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", sqlCount=" + sqlCount +
                ", totalRecords=" + totalRecords +
                ", processingTimeMs=" + processingTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}