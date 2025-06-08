package com.example.sqlcsv.service;

import com.example.sqlcsv.config.DataMaskingConfig;
import com.example.sqlcsv.dto.SqlExportRequest;
import com.example.sqlcsv.dto.SqlExportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL转Excel主服务
 */
@Service
public class SqlToExcelService {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlToExcelService.class);
    
    @Autowired
    private SqlExecutionService sqlExecutionService;
    
    @Autowired
    private ExcelExportService excelExportService;
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    @Value("${app.export.output-directory:./exports}")
    private String outputDirectory;
    
    @Value("${app.export.max-sql-count:50}")
    private int maxSqlCount;
    
    @Value("${app.export.max-records-per-query:100000}")
    private long maxRecordsPerQuery;
    
    /**
     * 执行SQL查询并导出到Excel
     * 
     * @param request 导出请求
     * @return 导出响应
     */
    public SqlExportResponse exportSqlToExcel(SqlExportRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 参数验证
            SqlExportResponse validationResult = validateRequest(request);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            // 2. SQL安全性验证
            if (request.isValidateSqlSafety()) {
                List<String> sqlErrors = sqlExecutionService.validateBatchSqlSafety(request.getSqlList());
                if (!sqlErrors.isEmpty()) {
                    SqlExportResponse response = SqlExportResponse.badRequest("SQL安全性验证失败");
                    response.setErrors(sqlErrors);
                    return response;
                }
            }
            
            // 3. 验证脱敏规则
            List<String> maskingErrors = validateMaskingRules(request.getMaskingRules());
            if (!maskingErrors.isEmpty()) {
                SqlExportResponse response = SqlExportResponse.badRequest("数据脱敏规则验证失败");
                response.setErrors(maskingErrors);
                return response;
            }
            
            // 4. 执行SQL查询
            logger.info("开始执行批量SQL查询，SQL数量: {}, 并行执行: {}", 
                       request.getSqlList().size(), request.isParallelExecution());
            
            List<List<Map<String, Object>>> queryResults;
            if (request.isParallelExecution()) {
                queryResults = sqlExecutionService.executeBatchQueriesParallel(request.getSqlList());
            } else {
                queryResults = sqlExecutionService.executeBatchQueries(request.getSqlList());
            }
            
            // 5. 检查查询结果
            List<String> warnings = new ArrayList<>();
            List<Long> recordCounts = new ArrayList<>();
            long totalRecords = 0;
            
            for (int i = 0; i < queryResults.size(); i++) {
                List<Map<String, Object>> result = queryResults.get(i);
                long recordCount = result.size();
                recordCounts.add(recordCount);
                totalRecords += recordCount;
                
                if (recordCount == 0) {
                    warnings.add(String.format("第%d个SQL查询返回空结果", i + 1));
                } else if (recordCount > maxRecordsPerQuery) {
                    warnings.add(String.format("第%d个SQL查询返回记录数(%d)超过建议上限(%d)", 
                                              i + 1, recordCount, maxRecordsPerQuery));
                }
            }
            
            // 6. 生成输出文件路径
            String outputPath = generateOutputPath(request.getFileName());
            
            // 7. 导出到Excel
            logger.info("开始导出Excel文件: {}", outputPath);
            excelExportService.exportToExcel(
                queryResults, 
                request.getSheetNames(), 
                request.getMaskingRules(), 
                request.getSqlMaskingRules(),
                outputPath
            );
            
            // 8. 构建响应
            long endTime = System.currentTimeMillis();
            SqlExportResponse response = SqlExportResponse.success(outputPath);
            response.setSqlCount(request.getSqlList().size());
            response.setTotalRecords(totalRecords);
            response.setRecordCounts(recordCounts);
            response.setProcessingTimeMs(endTime - startTime);
            response.setWarnings(warnings.isEmpty() ? null : warnings);
            
            // 设置文件大小
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                response.setFileSize(outputFile.length());
            }
            
            logger.info("SQL导出Excel完成，文件: {}, 总记录数: {}, 耗时: {}ms", 
                       outputPath, totalRecords, endTime - startTime);
            
            return response;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("SQL导出Excel失败: {}, 耗时: {}ms", e.getMessage(), endTime - startTime, e);
            
            SqlExportResponse response = SqlExportResponse.error("导出失败: " + e.getMessage());
            response.setProcessingTimeMs(endTime - startTime);
            return response;
        }
    }
    
    /**
     * 验证请求参数
     */
    private SqlExportResponse validateRequest(SqlExportRequest request) {
        if (request == null) {
            return SqlExportResponse.badRequest("请求参数不能为空");
        }
        
        if (request.getSqlList() == null || request.getSqlList().isEmpty()) {
            return SqlExportResponse.badRequest("SQL语句列表不能为空");
        }
        
        if (request.getSqlList().size() > maxSqlCount) {
            return SqlExportResponse.badRequest(
                String.format("SQL语句数量(%d)超过最大限制(%d)", request.getSqlList().size(), maxSqlCount)
            );
        }
        
        // 检查SQL语句是否为空
        for (int i = 0; i < request.getSqlList().size(); i++) {
            String sql = request.getSqlList().get(i);
            if (sql == null || sql.trim().isEmpty()) {
                return SqlExportResponse.badRequest(
                    String.format("第%d个SQL语句不能为空", i + 1)
                );
            }
        }
        
        // 检查Sheet名称数量
        if (request.getSheetNames() != null && 
            request.getSheetNames().size() != request.getSqlList().size()) {
            return SqlExportResponse.badRequest(
                "Sheet名称数量必须与SQL语句数量一致，或者不提供Sheet名称"
            );
        }
        
        return SqlExportResponse.success(null);
    }
    
    /**
     * 验证脱敏规则
     */
    private List<String> validateMaskingRules(List<DataMaskingConfig.FieldMaskingRule> maskingRules) {
        List<String> errors = new ArrayList<>();
        
        if (maskingRules == null || maskingRules.isEmpty()) {
            return errors;
        }
        
        for (int i = 0; i < maskingRules.size(); i++) {
            DataMaskingConfig.FieldMaskingRule rule = maskingRules.get(i);
            if (!dataMaskingService.validateMaskingRule(rule)) {
                errors.add(String.format("第%d个脱敏规则无效: %s", i + 1, rule.getFieldName()));
            }
        }
        
        return errors;
    }
    
    /**
     * 生成输出文件路径
     */
    private String generateOutputPath(String fileName) {
        // 确保输出目录存在
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // 生成文件名
        String actualFileName;
        if (fileName != null && !fileName.trim().isEmpty()) {
            actualFileName = fileName.trim();
            if (!actualFileName.toLowerCase().endsWith(".xlsx")) {
                actualFileName += ".xlsx";
            }
        } else {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            actualFileName = "sql_export_" + timestamp + ".xlsx";
        }
        
        // 处理文件名中的非法字符
        actualFileName = actualFileName.replaceAll("[\\/:*?\"<>|]", "_");
        
        return new File(outputDir, actualFileName).getAbsolutePath();
    }
    
    /**
     * 获取导出统计信息
     */
    public Map<String, Object> getExportStatistics() {
        File outputDir = new File(outputDirectory);
        
        Map<String, Object> stats = Map.of(
            "outputDirectory", outputDirectory,
            "maxSqlCount", maxSqlCount,
            "maxRecordsPerQuery", maxRecordsPerQuery,
            "outputDirectoryExists", outputDir.exists(),
            "exportedFilesCount", outputDir.exists() ? 
                (outputDir.listFiles(f -> f.getName().endsWith(".xlsx")) != null ? 
                 outputDir.listFiles(f -> f.getName().endsWith(".xlsx")).length : 0) : 0
        );
        
        return stats;
    }
    
    /**
     * 清理过期的导出文件
     */
    public int cleanupExpiredFiles(int daysToKeep) {
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;
        
        File[] files = outputDir.listFiles(f -> f.getName().endsWith(".xlsx"));
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                        logger.info("删除过期文件: {}", file.getName());
                    } else {
                        logger.warn("删除文件失败: {}", file.getName());
                    }
                }
            }
        }
        
        logger.info("清理完成，删除了{}个过期文件", deletedCount);
        return deletedCount;
    }
}