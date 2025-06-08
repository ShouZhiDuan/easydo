package com.example.csvimport.example;

import com.example.csvimport.config.DatabaseConfig;
import com.example.csvimport.config.DynamicJdbcTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DynamicJdbcTemplateExample {
    
    @Autowired
    private DynamicJdbcTemplateConfig.DynamicJdbcTemplateFactory jdbcTemplateFactory;
    
    public void example() {
        // 创建数据库配置
        DatabaseConfig config = new DatabaseConfig(
            "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC",
            "username",
            "password"
        );
        
        // 动态创建JdbcTemplate
        JdbcTemplate jdbcTemplate = jdbcTemplateFactory.createJdbcTemplate(config);
        
        // 使用JdbcTemplate执行SQL
        String result = jdbcTemplate.queryForObject("SELECT 'Hello World'", String.class);
        System.out.println("查询结果: " + result);
        
        // 关闭连接（可选，程序结束时会自动关闭）
        jdbcTemplateFactory.closeDataSource(config);
    }
}