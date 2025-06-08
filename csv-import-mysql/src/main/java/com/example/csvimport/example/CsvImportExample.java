package com.example.csvimport.example;

import com.example.csvimport.config.DatabaseConfig;
import com.example.csvimport.service.CsvImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * CSV导入使用示例
 */
@Slf4j
@Component
public class CsvImportExample {
    
    @Autowired
    private CsvImportService csvImportService;
    
    /**
     * 基本使用示例
     */
    public void basicExample() {
        try {
            // 1. 配置数据库连接
            DatabaseConfig databaseConfig = new DatabaseConfig(
                    "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC",
                    "root",
                    "password"
            );
            
            // 2. 指定CSV文件路径
            String csvFilePath = "/path/to/your/data.csv";
            
            // 3. 执行导入（使用默认参数）
            csvImportService.importCsv(csvFilePath, databaseConfig);
            
            log.info("基本导入示例完成");
            
        } catch (Exception e) {
            log.error("基本导入示例失败", e);
        } finally {
            csvImportService.closeConnection();
        }
    }
    
    /**
     * 高级使用示例
     */
    public void advancedExample() {
        try {
            // 1. 配置数据库连接（包含连接池参数）
            DatabaseConfig databaseConfig = new DatabaseConfig(
                    "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true",
                    "root",
                    "password"
            );
            
            // 自定义连接池参数
            databaseConfig.setMaximumPoolSize(30);
            databaseConfig.setMinimumIdle(10);
            databaseConfig.setConnectionTimeout(60000);
            
            // 2. 指定CSV文件路径
            String csvFilePath = "/path/to/your/large_data.csv";
            
            // 3. 指定需要创建索引的列
            List<String> indexColumns = Arrays.asList("id", "user_id", "created_date");
            
            // 4. 自定义批次大小和样本大小
            int batchSize = 5000;  // 大批次提高性能
            int sampleSize = 2000; // 更大样本提高类型推断准确性
            
            // 5. 执行导入
            csvImportService.importCsv(csvFilePath, databaseConfig, indexColumns, batchSize, sampleSize);
            
            log.info("高级导入示例完成");
            
        } catch (Exception e) {
            log.error("高级导入示例失败", e);
        } finally {
            csvImportService.closeConnection();
        }
    }
    
    /**
     * 批量处理多个CSV文件示例
     */
    public void batchProcessExample() {
        DatabaseConfig databaseConfig = new DatabaseConfig(
                "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC",
                "root",
                "password"
        );
        
        List<String> csvFiles = Arrays.asList(
                "/path/to/users.csv",
                "/path/to/orders.csv",
                "/path/to/products.csv"
        );
        
        try {
            for (String csvFile : csvFiles) {
                log.info("开始处理文件: {}", csvFile);
                
                // 根据文件名设置不同的索引策略
                List<String> indexColumns = null;
                if (csvFile.contains("users")) {
                    indexColumns = Arrays.asList("id", "email");
                } else if (csvFile.contains("orders")) {
                    indexColumns = Arrays.asList("id", "user_id", "order_date");
                } else if (csvFile.contains("products")) {
                    indexColumns = Arrays.asList("id", "category_id");
                }
                
                csvImportService.importCsv(csvFile, databaseConfig, indexColumns);
                log.info("文件处理完成: {}", csvFile);
            }
            
            log.info("批量处理示例完成");
            
        } catch (Exception e) {
            log.error("批量处理示例失败", e);
        } finally {
            csvImportService.closeConnection();
        }
    }
    
    /**
     * 性能优化示例
     */
    public void performanceOptimizedExample() {
        try {
            // 1. 高性能数据库配置
            DatabaseConfig databaseConfig = new DatabaseConfig(
                    "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC" +
                    "&rewriteBatchedStatements=true" +
                    "&cachePrepStmts=true" +
                    "&prepStmtCacheSize=250" +
                    "&prepStmtCacheSqlLimit=2048" +
                    "&useServerPrepStmts=true",
                    "root",
                    "password"
            );
            
            // 2. 优化连接池配置
            databaseConfig.setMaximumPoolSize(50);
            databaseConfig.setMinimumIdle(20);
            databaseConfig.setConnectionTimeout(30000);
            databaseConfig.setIdleTimeout(300000);
            
            // 3. 大文件处理配置
            String csvFilePath = "/path/to/very_large_data.csv";
            List<String> indexColumns = Arrays.asList("id");
            
            // 4. 大批次处理
            int batchSize = 10000;  // 大批次减少网络开销
            int sampleSize = 5000;  // 大样本提高准确性
            
            // 5. 执行导入
            long startTime = System.currentTimeMillis();
            csvImportService.importCsv(csvFilePath, databaseConfig, indexColumns, batchSize, sampleSize);
            long endTime = System.currentTimeMillis();
            
            log.info("性能优化导入完成，耗时: {} 秒", (endTime - startTime) / 1000.0);
            
        } catch (Exception e) {
            log.error("性能优化示例失败", e);
        } finally {
            csvImportService.closeConnection();
        }
    }
}