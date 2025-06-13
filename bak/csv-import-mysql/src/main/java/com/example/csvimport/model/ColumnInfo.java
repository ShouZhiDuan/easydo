package com.example.csvimport.model;

import lombok.Data;

import java.sql.Types;

/**
 * 列信息模型
 */
@Data
public class ColumnInfo {
    private String name;
    private JavaType javaType;
    private String sqlType;
    private int sqlTypeCode;
    private int maxLength;
    
    public ColumnInfo(String name, JavaType javaType) {
        this.name = name;
        this.javaType = javaType;
        this.maxLength = 0;
        determineSqlType();
    }
    
    /**
     * 根据Java类型确定SQL类型
     */
    private void determineSqlType() {
        switch (javaType) {
            case INTEGER:
                this.sqlType = "INT";
                this.sqlTypeCode = Types.INTEGER;
                break;
            case LONG:
                this.sqlType = "BIGINT";
                this.sqlTypeCode = Types.BIGINT;
                break;
            case DOUBLE:
                this.sqlType = "DOUBLE";
                this.sqlTypeCode = Types.DOUBLE;
                break;
            case BOOLEAN:
                this.sqlType = "BOOLEAN";
                this.sqlTypeCode = Types.BOOLEAN;
                break;
            case DATE:
                this.sqlType = "DATE";
                this.sqlTypeCode = Types.DATE;
                break;
            case TIMESTAMP:
                this.sqlType = "TIMESTAMP";
                this.sqlTypeCode = Types.TIMESTAMP;
                break;
            default:
                // 默认为VARCHAR，长度根据实际数据动态调整
                this.sqlType = "VARCHAR(255)";
                this.sqlTypeCode = Types.VARCHAR;
                break;
        }
    }
    
    /**
     * 更新VARCHAR类型的长度
     */
    public void updateVarcharLength(int length) {
        if (javaType == JavaType.STRING && length > maxLength) {
            maxLength = Math.max(length, 50); // 最小长度50
            // 根据长度选择合适的类型
            if (maxLength <= 255) {
                this.sqlType = String.format("VARCHAR(%d)", Math.min(maxLength * 2, 255)); // 预留一些空间
            } else if (maxLength <= 65535) {
                this.sqlType = "TEXT";
                this.sqlTypeCode = Types.LONGVARCHAR;
            } else {
                this.sqlType = "LONGTEXT";
                this.sqlTypeCode = Types.LONGVARCHAR;
            }
        }
    }
    
    /**
     * Java数据类型枚举
     */
    public enum JavaType {
        STRING,
        INTEGER,
        LONG,
        DOUBLE,
        BOOLEAN,
        DATE,
        TIMESTAMP
    }
}