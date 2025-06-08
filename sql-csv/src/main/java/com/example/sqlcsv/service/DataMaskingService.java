package com.example.sqlcsv.service;

import com.example.sqlcsv.config.DataMaskingConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 数据脱敏服务
 */
@Service
public class DataMaskingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMaskingService.class);
    
    // 缓存编译后的正则表达式模式，提高性能
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    
    /**
     * 对数据行进行脱敏处理
     * 
     * @param row 数据行
     * @param columnNames 列名列表
     * @param maskingRules 脱敏规则列表
     * @return 脱敏后的数据行
     */
    public Map<String, Object> maskRowData(Map<String, Object> row, 
                                          List<String> columnNames, 
                                          List<DataMaskingConfig.FieldMaskingRule> maskingRules) {
        if (row == null || maskingRules == null || maskingRules.isEmpty()) {
            return row;
        }
        
        // 创建脱敏后的数据行副本
        Map<String, Object> maskedRow = new ConcurrentHashMap<>(row);
        
        for (DataMaskingConfig.FieldMaskingRule rule : maskingRules) {
            if (!rule.isEnabled() || StringUtils.isBlank(rule.getFieldName())) {
                continue;
            }
            
            String fieldName = rule.getFieldName();
            Object value = maskedRow.get(fieldName);
            
            if (value != null && value instanceof String) {
                String stringValue = (String) value;
                String maskedValue = maskValue(stringValue, rule);
                maskedRow.put(fieldName, maskedValue);
            }
        }
        
        return maskedRow;
    }
    
    /**
     * 对单个值进行脱敏处理
     * 
     * @param value 原始值
     * @param rule 脱敏规则
     * @return 脱敏后的值
     */
    public String maskValue(String value, DataMaskingConfig.FieldMaskingRule rule) {
        if (StringUtils.isBlank(value) || rule == null || !rule.isEnabled()) {
            return value;
        }
        
        try {
            String regex = rule.getEffectiveRegex();
            String replacement = rule.getEffectiveReplacement();
            
            if (StringUtils.isBlank(regex) || StringUtils.isBlank(replacement)) {
                logger.warn("脱敏规则配置不完整，字段: {}, 类型: {}", rule.getFieldName(), rule.getMaskingType());
                return value;
            }
            
            Pattern pattern = getOrCreatePattern(regex);
            return pattern.matcher(value).replaceAll(replacement);
            
        } catch (Exception e) {
            logger.error("数据脱敏处理失败，字段: {}, 值: {}, 错误: {}", 
                        rule.getFieldName(), value, e.getMessage(), e);
            return value; // 脱敏失败时返回原值
        }
    }
    
    /**
     * 获取或创建正则表达式模式（带缓存）
     * 
     * @param regex 正则表达式
     * @return 编译后的模式
     */
    private Pattern getOrCreatePattern(String regex) {
        return patternCache.computeIfAbsent(regex, Pattern::compile);
    }
    
    /**
     * 验证脱敏规则的有效性
     * 
     * @param rule 脱敏规则
     * @return 是否有效
     */
    public boolean validateMaskingRule(DataMaskingConfig.FieldMaskingRule rule) {
        if (rule == null || StringUtils.isBlank(rule.getFieldName())) {
            return false;
        }
        
        try {
            String regex = rule.getEffectiveRegex();
            if (StringUtils.isNotBlank(regex)) {
                Pattern.compile(regex); // 验证正则表达式是否有效
            }
            return true;
        } catch (Exception e) {
            logger.error("脱敏规则验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 清空模式缓存
     */
    public void clearPatternCache() {
        patternCache.clear();
    }
    
    /**
     * 获取缓存大小
     */
    public int getPatternCacheSize() {
        return patternCache.size();
    }
}