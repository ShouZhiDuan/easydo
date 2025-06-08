package com.example.csvimport.config;

import lombok.Data;

/**
 * 数据库连接配置
 */
@Data
public class DatabaseConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    
    // 连接池配置
    private int maximumPoolSize = 20;
    private int minimumIdle = 5;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
    
    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}