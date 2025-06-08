package com.example.csvimport.service;

import com.example.csvimport.config.DatabaseConfig;
import com.example.csvimport.model.ColumnInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CSV导入主服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {
    
    private final DatabaseService databaseService;
    private final CsvService csvService;
    
    /**
     * 执行完整的CSV导入流程
     * 
     * @param csvFilePath CSV文件路径
     * @param databaseConfig 数据库配置
     * @param indexColumns 需要创建索引的列名
     * @param batchSize 批量插入大小
     * @param sampleSize 类型推断样本大小
     */
    public void importCsv(String csvFilePath, DatabaseConfig databaseConfig, 
                         List<String> indexColumns, int batchSize, int sampleSize) {
        
        long startTime = System.currentTimeMillis();
        log.info("开始CSV导入流程: {}", csvFilePath);
        
        try {
            // 1. 创建数据库连接
            log.info("步骤1: 创建数据库连接");
            databaseService.createConnection(databaseConfig);
            
            // 2. 分析CSV文件结构和数据类型
            log.info("步骤2: 分析CSV文件结构 (样本大小: {})", sampleSize);
            List<ColumnInfo> columns = csvService.analyzeColumns(csvFilePath, sampleSize);
            
            // 3. 提取表名
            String tableName = csvService.extractTableName(csvFilePath);
            log.info("目标表名: {}", tableName);
            
            // 4. 创建表
            log.info("步骤3: 创建数据库表");
            databaseService.createTable(tableName, columns);
            
            // 5. 流式导入数据
            log.info("步骤4: 开始流式数据导入 (批次大小: {})", batchSize);
            csvService.processDataStream(csvFilePath, columns, batchSize, 
                    dataRows -> databaseService.batchInsert(tableName, columns, dataRows, batchSize));
            
            // 6. 创建索引
            if (indexColumns != null && !indexColumns.isEmpty()) {
                log.info("步骤5: 创建索引");
                databaseService.createIndexes(tableName, indexColumns);
            } else {
                log.info("跳过索引创建 - 未指定索引列");
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("CSV导入完成! 表名: {}, 耗时: {} ms ({} 秒)", 
                    tableName, duration, duration / 1000.0);
            
        } catch (Exception e) {
            log.error("CSV导入失败", e);
            throw new RuntimeException("CSV导入失败", e);
        }
    }
    
    /**
     * 使用默认参数导入CSV
     */
    public void importCsv(String csvFilePath, DatabaseConfig databaseConfig) {
        importCsv(csvFilePath, databaseConfig, null, 1000, 1000);
    }
    
    /**
     * 使用默认参数导入CSV，并创建指定索引
     */
    public void importCsv(String csvFilePath, DatabaseConfig databaseConfig, List<String> indexColumns) {
        importCsv(csvFilePath, databaseConfig, indexColumns, 1000, 1000);
    }
    
    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        databaseService.closeConnection();
    }
}