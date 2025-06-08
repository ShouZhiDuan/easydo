package com.example.sqlcsv.config;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 数据脱敏配置类
 */
public class DataMaskingConfig {
    
    /**
     * 脱敏类型枚举
     */
    public enum MaskingType {
        PHONE("手机号", "(\\d{3})\\d{4}(\\d{4})", "$1****$2"),
        ID_CARD("身份证", "(\\d{6})\\d{8}(\\d{4})", "$1********$2"),
        EMAIL("邮箱", "(\\w{1,3})\\w*@(\\w+\\.\\w+)", "$1***@$2"),
        BANK_CARD("银行卡", "(\\d{4})\\d*(\\d{4})", "$1****$2"),
        NAME("姓名", "(.).*(.)", "$1*$2"),
        CUSTOM("自定义", "", "");
        
        private final String description;
        private final String regex;
        private final String replacement;
        
        MaskingType(String description, String regex, String replacement) {
            this.description = description;
            this.regex = regex;
            this.replacement = replacement;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getRegex() {
            return regex;
        }
        
        public String getReplacement() {
            return replacement;
        }
    }
    
    /**
     * 字段脱敏配置
     */
    public static class FieldMaskingRule {
        private String fieldName;
        private MaskingType maskingType;
        private String customRegex;
        private String customReplacement;
        private boolean enabled = true;
        
        public FieldMaskingRule() {}
        
        public FieldMaskingRule(String fieldName, MaskingType maskingType) {
            this.fieldName = fieldName;
            this.maskingType = maskingType;
        }
        
        public FieldMaskingRule(String fieldName, String customRegex, String customReplacement) {
            this.fieldName = fieldName;
            this.maskingType = MaskingType.CUSTOM;
            this.customRegex = customRegex;
            this.customReplacement = customReplacement;
        }
        
        // Getters and Setters
        public String getFieldName() {
            return fieldName;
        }
        
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        
        public MaskingType getMaskingType() {
            return maskingType;
        }
        
        public void setMaskingType(MaskingType maskingType) {
            this.maskingType = maskingType;
        }
        
        public String getCustomRegex() {
            return customRegex;
        }
        
        public void setCustomRegex(String customRegex) {
            this.customRegex = customRegex;
        }
        
        public String getCustomReplacement() {
            return customReplacement;
        }
        
        public void setCustomReplacement(String customReplacement) {
            this.customReplacement = customReplacement;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        /**
         * 获取实际使用的正则表达式
         */
        public String getEffectiveRegex() {
            if (maskingType == MaskingType.CUSTOM) {
                return customRegex;
            }
            return maskingType.getRegex();
        }
        
        /**
         * 获取实际使用的替换模式
         */
        public String getEffectiveReplacement() {
            if (maskingType == MaskingType.CUSTOM) {
                return customReplacement;
            }
            return maskingType.getReplacement();
        }
    }
}