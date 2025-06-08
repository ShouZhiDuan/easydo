package com.example.sqlcsv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;

/**
 * SQL执行服务
 */
@Service
public class SqlExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlExecutionService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 线程池配置
    private final ExecutorService executorService = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );
    
    // SQL执行超时时间（秒）
    private static final int SQL_TIMEOUT_SECONDS = 300;
    
    /**
     * 批量执行SQL查询（串行执行）
     * 
     * @param sqlList SQL语句列表
     * @return 查询结果列表
     */
    @Transactional(readOnly = true)
    public List<List<Map<String, Object>>> executeBatchQueries(List<String> sqlList) {
        if (sqlList == null || sqlList.isEmpty()) {
            throw new IllegalArgumentException("SQL语句列表不能为空");
        }
        
        List<List<Map<String, Object>>> results = new ArrayList<>();
        
        for (int i = 0; i < sqlList.size(); i++) {
            String sql = sqlList.get(i);
            logger.info("开始执行第{}个SQL查询: {}", i + 1, truncateSql(sql));
            
            try {
                long startTime = System.currentTimeMillis();
                List<Map<String, Object>> result = executeSingleQuery(sql);
                long endTime = System.currentTimeMillis();
                
                results.add(result);
                logger.info("第{}个SQL查询执行完成，耗时: {}ms，结果行数: {}", 
                           i + 1, endTime - startTime, result.size());
                           
            } catch (Exception e) {
                logger.error("第{}个SQL查询执行失败: {}, SQL: {}", i + 1, e.getMessage(), truncateSql(sql), e);
                // 添加空结果，保持索引一致性
                results.add(new ArrayList<>());
            }
        }
        
        return results;
    }
    
    /**
     * 批量执行SQL查询（并行执行）
     * 
     * @param sqlList SQL语句列表
     * @return 查询结果列表
     */
    public List<List<Map<String, Object>>> executeBatchQueriesParallel(List<String> sqlList) {
        if (sqlList == null || sqlList.isEmpty()) {
            throw new IllegalArgumentException("SQL语句列表不能为空");
        }
        
        List<Future<List<Map<String, Object>>>> futures = new ArrayList<>();
        
        // 提交所有查询任务
        for (int i = 0; i < sqlList.size(); i++) {
            final int index = i;
            final String sql = sqlList.get(i);
            
            Future<List<Map<String, Object>>> future = executorService.submit(() -> {
                try {
                    logger.info("开始并行执行第{}个SQL查询: {}", index + 1, truncateSql(sql));
                    long startTime = System.currentTimeMillis();
                    
                    List<Map<String, Object>> result = executeSingleQuery(sql);
                    
                    long endTime = System.currentTimeMillis();
                    logger.info("第{}个SQL查询并行执行完成，耗时: {}ms，结果行数: {}", 
                               index + 1, endTime - startTime, result.size());
                    
                    return result;
                } catch (Exception e) {
                    logger.error("第{}个SQL查询并行执行失败: {}, SQL: {}", 
                                index + 1, e.getMessage(), truncateSql(sql), e);
                    return new ArrayList<>();
                }
            });
            
            futures.add(future);
        }
        
        // 收集结果
        List<List<Map<String, Object>>> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                List<Map<String, Object>> result = futures.get(i).get(SQL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                results.add(result);
            } catch (TimeoutException e) {
                logger.error("第{}个SQL查询执行超时", i + 1);
                results.add(new ArrayList<>());
            } catch (Exception e) {
                logger.error("第{}个SQL查询结果获取失败: {}", i + 1, e.getMessage(), e);
                results.add(new ArrayList<>());
            }
        }
        
        return results;
    }
    
    /**
     * 执行单个SQL查询
     * 
     * @param sql SQL语句
     * @return 查询结果
     */
    private List<Map<String, Object>> executeSingleQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        
        // 验证SQL是否为查询语句
        if (!isSelectStatement(sql)) {
            throw new IllegalArgumentException("只支持SELECT查询语句");
        }
        
        try {
            // 设置查询超时
            jdbcTemplate.setQueryTimeout(SQL_TIMEOUT_SECONDS);
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            // 处理结果，确保所有值都是可序列化的
            return processQueryResult(result);
            
        } catch (DataAccessException e) {
            logger.error("SQL执行失败: {}, SQL: {}", e.getMessage(), truncateSql(sql));
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理查询结果，确保数据类型兼容
     */
    private List<Map<String, Object>> processQueryResult(List<Map<String, Object>> rawResult) {
        if (rawResult == null || rawResult.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> processedResult = new ArrayList<>();
        
        for (Map<String, Object> row : rawResult) {
            Map<String, Object> processedRow = new LinkedHashMap<>();
            
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 处理特殊数据类型
                if (value instanceof java.sql.Clob) {
                    try {
                        java.sql.Clob clob = (java.sql.Clob) value;
                        value = clob.getSubString(1, (int) clob.length());
                    } catch (Exception e) {
                        logger.warn("CLOB数据处理失败: {}", e.getMessage());
                        value = "[CLOB数据]";
                    }
                } else if (value instanceof java.sql.Blob) {
                    value = "[BLOB数据]";
                } else if (value instanceof java.sql.Timestamp) {
                    value = ((java.sql.Timestamp) value).toLocalDateTime();
                } else if (value instanceof java.sql.Date) {
                    value = ((java.sql.Date) value).toLocalDate();
                } else if (value instanceof java.sql.Time) {
                    value = ((java.sql.Time) value).toLocalTime();
                }
                
                processedRow.put(key, value);
            }
            
            processedResult.add(processedRow);
        }
        
        return processedResult;
    }
    
    /**
     * 验证是否为SELECT语句
     */
    private boolean isSelectStatement(String sql) {
        if (sql == null) {
            return false;
        }
        
        String trimmedSql = sql.trim().toLowerCase();
        return trimmedSql.startsWith("select") || 
               trimmedSql.startsWith("with") || // CTE查询
               (trimmedSql.startsWith("(") && trimmedSql.contains("select")); // 子查询
    }
    
    /**
     * 截断SQL用于日志输出
     */
    private String truncateSql(String sql) {
        if (sql == null) {
            return "null";
        }
        
        String cleanSql = sql.replaceAll("\\s+", " ").trim();
        if (cleanSql.length() > 100) {
            return cleanSql.substring(0, 100) + "...";
        }
        return cleanSql;
    }
    
    /**
     * 验证SQL语句的安全性（基础检查）
     */
    public boolean validateSqlSafety(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        
        String lowerSql = sql.toLowerCase().trim();
        
        // 检查危险关键字
        String[] dangerousKeywords = {
            "drop", "delete", "update", "insert", "create", "alter", 
            "truncate", "exec", "execute", "sp_", "xp_", "--", "/*"
        };
        
        for (String keyword : dangerousKeywords) {
            if (lowerSql.contains(keyword)) {
                logger.warn("SQL包含危险关键字: {}", keyword);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 批量验证SQL安全性
     */
    public List<String> validateBatchSqlSafety(List<String> sqlList) {
        List<String> errors = new ArrayList<>();
        
        if (sqlList == null || sqlList.isEmpty()) {
            errors.add("SQL列表不能为空");
            return errors;
        }
        
        for (int i = 0; i < sqlList.size(); i++) {
            String sql = sqlList.get(i);
            if (!validateSqlSafety(sql)) {
                errors.add(String.format("第%d个SQL语句不安全: %s", i + 1, truncateSql(sql)));
            }
        }
        
        return errors;
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}