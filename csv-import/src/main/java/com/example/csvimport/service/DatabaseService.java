package com.example.csvimport.service;

import com.example.csvimport.config.DatabaseConfig;
import com.example.csvimport.model.ColumnInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库操作服务
 */
@Slf4j
@Service
public class DatabaseService {
    
    private JdbcTemplate jdbcTemplate;
    private final DynamicJdbcTemplateConfig.DynamicJdbcTemplateFactory jdbcTemplateFactory;
    
    public DatabaseService(DynamicJdbcTemplateConfig.DynamicJdbcTemplateFactory jdbcTemplateFactory) {
        this.jdbcTemplateFactory = jdbcTemplateFactory;
    }
    
    /**
     * 创建数据库连接
     */
    public void createConnection(DatabaseConfig config) {
        try {
            // 使用动态工厂创建JdbcTemplate
            this.jdbcTemplate = jdbcTemplateFactory.createJdbcTemplate(config);
            log.info("数据库连接创建成功: {}", config.getUrl());
        } catch (Exception e) {
            log.error("创建数据库连接失败", e);
            throw new RuntimeException("创建数据库连接失败", e);
        }
    }
    
    /**
     * 关闭当前数据库连接
     */
    public void closeConnection(DatabaseConfig config) {
        jdbcTemplateFactory.closeDataSource(config);
        this.jdbcTemplate = null;
    }
    
    /**
     * 创建表
     */
    public void createTable(String tableName, List<ColumnInfo> columns) {
        try {
            // 先删除表（如果存在）
            String dropSql = String.format("DROP TABLE IF EXISTS `%s`", tableName);
            jdbcTemplate.execute(dropSql);
            log.info("删除已存在的表: {}", tableName);
            
            // 构建创建表的SQL
            StringBuilder createSql = new StringBuilder();
            createSql.append(String.format("CREATE TABLE `%s` (", tableName));
            
            String columnDefinitions = columns.stream()
                    .map(col -> String.format("`%s` %s", col.getName(), col.getSqlType()))
                    .collect(Collectors.joining(", "));
            
            createSql.append(columnDefinitions);
            createSql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            
            jdbcTemplate.execute(createSql.toString());
            log.info("表创建成功: {} (列数: {})", tableName, columns.size());
            
        } catch (Exception e) {
            log.error("创建表失败: {}", tableName, e);
            throw new RuntimeException("创建表失败: " + tableName, e);
        }
    }
    
    /**
     * 批量插入数据
     */
    public void batchInsert(String tableName, List<ColumnInfo> columns, List<List<String>> dataRows, int batchSize) {
        if (dataRows.isEmpty()) {
            log.warn("没有数据需要插入");
            return;
        }
        
        try {
            // 构建插入SQL
            String placeholders = columns.stream()
                    .map(col -> "?")
                    .collect(Collectors.joining(", "));
            
            String insertSql = String.format("INSERT INTO `%s` (%s) VALUES (%s)",
                    tableName,
                    columns.stream().map(col -> "`" + col.getName() + "`").collect(Collectors.joining(", ")),
                    placeholders);
            
            log.info("开始批量插入数据，总行数: {}, 批次大小: {}", dataRows.size(), batchSize);
            
            // 分批插入
            int totalRows = dataRows.size();
            int processedRows = 0;
            
            for (int i = 0; i < totalRows; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalRows);
                List<List<String>> batch = dataRows.subList(i, endIndex);
                
                jdbcTemplate.batchUpdate(insertSql, batch, batchSize, (ps, row) -> {
                    for (int j = 0; j < row.size() && j < columns.size(); j++) {
                        String value = row.get(j);
                        ColumnInfo column = columns.get(j);
                        setParameterValue(ps, j + 1, value, column);
                    }
                });
                
                processedRows += batch.size();
                if (processedRows % (batchSize * 10) == 0 || processedRows == totalRows) {
                    log.info("已处理 {}/{} 行数据 ({:.1f}%)", 
                            processedRows, totalRows, (double) processedRows / totalRows * 100);
                }
            }
            
            log.info("数据插入完成，共插入 {} 行", totalRows);
            
        } catch (Exception e) {
            log.error("批量插入数据失败", e);
            throw new RuntimeException("批量插入数据失败", e);
        }
    }
    
    /**
     * 设置PreparedStatement参数值
     */
    private void setParameterValue(PreparedStatement ps, int parameterIndex, String value, ColumnInfo column) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(parameterIndex, column.getSqlTypeCode());
            return;
        }
        
        try {
            switch (column.getJavaType()) {
                case INTEGER:
                    ps.setInt(parameterIndex, Integer.parseInt(value.trim()));
                    break;
                case LONG:
                    ps.setLong(parameterIndex, Long.parseLong(value.trim()));
                    break;
                case DOUBLE:
                    ps.setDouble(parameterIndex, Double.parseDouble(value.trim()));
                    break;
                case BOOLEAN:
                    ps.setBoolean(parameterIndex, Boolean.parseBoolean(value.trim()));
                    break;
                case DATE:
                    ps.setDate(parameterIndex, java.sql.Date.valueOf(value.trim()));
                    break;
                case TIMESTAMP:
                    ps.setTimestamp(parameterIndex, java.sql.Timestamp.valueOf(value.trim()));
                    break;
                default:
                    ps.setString(parameterIndex, value);
                    break;
            }
        } catch (Exception e) {
            // 如果类型转换失败，则作为字符串处理
            ps.setString(parameterIndex, value);
        }
    }
    
    /**
     * 创建索引
     */
    public void createIndexes(String tableName, List<String> indexColumns) {
        if (indexColumns == null || indexColumns.isEmpty()) {
            log.info("没有指定索引列");
            return;
        }
        
        try {
            for (String columnName : indexColumns) {
                String indexName = String.format("idx_%s_%s", tableName, columnName);
                String createIndexSql = String.format("CREATE INDEX `%s` ON `%s` (`%s`)", 
                        indexName, tableName, columnName);
                
                jdbcTemplate.execute(createIndexSql);
                log.info("索引创建成功: {} on {}.{}", indexName, tableName, columnName);
            }
        } catch (Exception e) {
            log.error("创建索引失败", e);
            throw new RuntimeException("创建索引失败", e);
        }
    }
    
    /**
     * 关闭连接
     */
    public void closeConnection() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                log.info("数据库连接已关闭");
            }
        } catch (Exception e) {
            log.error("关闭数据库连接失败", e);
        }
    }
    
    /**
     * 获取JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}