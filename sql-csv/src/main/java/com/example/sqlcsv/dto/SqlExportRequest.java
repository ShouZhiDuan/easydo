package com.example.sqlcsv.dto;

import com.example.sqlcsv.config.DataMaskingConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * SQL导出请求DTO
 */
public class SqlExportRequest {
    
    /**
     * SQL语句列表
     */
    @NotNull(message = "SQL语句列表不能为空")
    @NotEmpty(message = "SQL语句列表不能为空")
    @JsonProperty("sqlList")
    private List<String> sqlList;
    
    /**
     * Sheet名称列表（可选）
     */
    @JsonProperty("sheetNames")
    private List<String> sheetNames;
    
    /**
     * 输出文件名（可选，默认生成时间戳文件名）
     */
    @JsonProperty("fileName")
    private String fileName;
    
    /**
     * 数据脱敏规则列表（可选）
     * 全局脱敏规则，应用于所有SQL查询结果
     */
    @JsonProperty("maskingRules")
    private List<DataMaskingConfig.FieldMaskingRule> maskingRules;
    
    /**
     * 每个SQL对应的脱敏规则列表（可选）
     * 索引与sqlList对应，如果某个索引为null或空，则使用全局maskingRules
     * 优先级高于全局maskingRules
     */
    @JsonProperty("sqlMaskingRules")
    private List<List<DataMaskingConfig.FieldMaskingRule>> sqlMaskingRules;
    
    /**
     * 是否并行执行SQL（默认false）
     */
    @JsonProperty("parallelExecution")
    private boolean parallelExecution = false;
    
    /**
     * 是否验证SQL安全性（默认true）
     */
    @JsonProperty("validateSqlSafety")
    private boolean validateSqlSafety = true;
    
    // 构造函数
    public SqlExportRequest() {}
    
    public SqlExportRequest(List<String> sqlList) {
        this.sqlList = sqlList;
    }
    
    // Getters and Setters
    public List<String> getSqlList() {
        return sqlList;
    }
    
    public void setSqlList(List<String> sqlList) {
        this.sqlList = sqlList;
    }
    
    public List<String> getSheetNames() {
        return sheetNames;
    }
    
    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public List<DataMaskingConfig.FieldMaskingRule> getMaskingRules() {
        return maskingRules;
    }
    
    public void setMaskingRules(List<DataMaskingConfig.FieldMaskingRule> maskingRules) {
        this.maskingRules = maskingRules;
    }
    
    public List<List<DataMaskingConfig.FieldMaskingRule>> getSqlMaskingRules() {
        return sqlMaskingRules;
    }
    
    public void setSqlMaskingRules(List<List<DataMaskingConfig.FieldMaskingRule>> sqlMaskingRules) {
        this.sqlMaskingRules = sqlMaskingRules;
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }
    
    public boolean isValidateSqlSafety() {
        return validateSqlSafety;
    }
    
    public void setValidateSqlSafety(boolean validateSqlSafety) {
        this.validateSqlSafety = validateSqlSafety;
    }
    
    @Override
    public String toString() {
        return "SqlExportRequest{" +
                "sqlCount=" + (sqlList != null ? sqlList.size() : 0) +
                ", sheetNames=" + sheetNames +
                ", fileName='" + fileName + '\'' +
                ", maskingRulesCount=" + (maskingRules != null ? maskingRules.size() : 0) +
                ", sqlMaskingRulesCount=" + (sqlMaskingRules != null ? sqlMaskingRules.size() : 0) +
                ", parallelExecution=" + parallelExecution +
                ", validateSqlSafety=" + validateSqlSafety +
                '}';
    }
}