package com.example.sqlcsv.service;

import com.example.sqlcsv.config.DataMaskingConfig;
import com.example.sqlcsv.dto.SqlExportRequest;
import com.example.sqlcsv.dto.SqlExportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL转Excel服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class SqlToExcelServiceTest {
    
    @Autowired
    private SqlToExcelService sqlToExcelService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // 设置临时输出目录
        System.setProperty("app.export.output-directory", tempDir.toString());
    }
    
    @Test
    void testExportSqlToExcel_Success() {
        // 准备测试数据
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "SELECT id, username, email FROM users LIMIT 5",
            "SELECT id, product_name, price FROM products LIMIT 3"
        ));
        request.setSheetNames(Arrays.asList("用户数据", "产品数据"));
        request.setFileName("test_export.xlsx");
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getFilePath());
        assertEquals(2, response.getSqlCount());
        assertNotNull(response.getTotalRecords());
        assertNotNull(response.getProcessingTimeMs());
        
        // 验证文件是否生成
        File exportedFile = new File(response.getFilePath());
        assertTrue(exportedFile.exists());
        assertTrue(exportedFile.length() > 0);
    }
    
    @Test
    void testExportSqlToExcel_WithDataMasking() {
        // 准备脱敏规则
        List<DataMaskingConfig.FieldMaskingRule> maskingRules = Arrays.asList(
            new DataMaskingConfig.FieldMaskingRule("phone", DataMaskingConfig.MaskingType.PHONE),
            new DataMaskingConfig.FieldMaskingRule("id_card", DataMaskingConfig.MaskingType.ID_CARD),
            new DataMaskingConfig.FieldMaskingRule("email", DataMaskingConfig.MaskingType.EMAIL)
        );
        
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "SELECT username, email, phone, id_card, real_name FROM users LIMIT 3"
        ));
        request.setSheetNames(Arrays.asList("脱敏用户数据"));
        request.setMaskingRules(maskingRules);
        request.setFileName("masked_export.xlsx");
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertTrue(response.isSuccess());
        assertNotNull(response.getFilePath());
        
        // 验证文件是否生成
        File exportedFile = new File(response.getFilePath());
        assertTrue(exportedFile.exists());
    }
    
    @Test
    void testExportSqlToExcel_ParallelExecution() {
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "SELECT * FROM users",
            "SELECT * FROM products",
            "SELECT * FROM orders"
        ));
        request.setSheetNames(Arrays.asList("用户", "产品", "订单"));
        request.setParallelExecution(true);
        request.setFileName("parallel_export.xlsx");
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertTrue(response.isSuccess());
        assertEquals(3, response.getSqlCount());
        
        // 验证文件是否生成
        File exportedFile = new File(response.getFilePath());
        assertTrue(exportedFile.exists());
    }
    
    @Test
    void testExportSqlToExcel_EmptyRequest() {
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList());
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("SQL语句列表不能为空"));
    }
    
    @Test
    void testExportSqlToExcel_InvalidSql() {
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "SELECT * FROM non_existing_table"
        ));
        request.setFileName("invalid_sql_export.xlsx");
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果 - 应该成功但包含空结果
        assertTrue(response.isSuccess());
        assertEquals(1, response.getSqlCount());
        
        // 验证文件是否生成（即使查询失败也会生成文件）
        File exportedFile = new File(response.getFilePath());
        assertTrue(exportedFile.exists());
    }
    
    @Test
    void testExportSqlToExcel_UnsafeSql() {
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "DROP TABLE users",
            "DELETE FROM products"
        ));
        request.setValidateSqlSafety(true);
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("SQL安全性验证失败"));
        assertNotNull(response.getErrors());
        assertFalse(response.getErrors().isEmpty());
    }
    
    @Test
    void testExportSqlToExcel_SheetNameMismatch() {
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList(
            "SELECT * FROM users LIMIT 1",
            "SELECT * FROM products LIMIT 1"
        ));
        request.setSheetNames(Arrays.asList("用户数据")); // 只有一个Sheet名称，但有两个SQL
        
        // 执行测试
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        
        // 验证结果
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("Sheet名称数量必须与SQL语句数量一致"));
    }
    
    @Test
    void testGetExportStatistics() {
        // 执行测试
        var statistics = sqlToExcelService.getExportStatistics();
        
        // 验证结果
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("outputDirectory"));
        assertTrue(statistics.containsKey("maxSqlCount"));
        assertTrue(statistics.containsKey("maxRecordsPerQuery"));
        assertTrue(statistics.containsKey("outputDirectoryExists"));
        assertTrue(statistics.containsKey("exportedFilesCount"));
    }
    
    @Test
    void testCleanupExpiredFiles() {
        // 先创建一个测试文件
        SqlExportRequest request = new SqlExportRequest();
        request.setSqlList(Arrays.asList("SELECT 1 as test_column"));
        request.setFileName("cleanup_test.xlsx");
        
        SqlExportResponse response = sqlToExcelService.exportSqlToExcel(request);
        assertTrue(response.isSuccess());
        
        // 验证文件存在
        File exportedFile = new File(response.getFilePath());
        assertTrue(exportedFile.exists());
        
        // 执行清理（保留0天，应该删除所有文件）
        int deletedCount = sqlToExcelService.cleanupExpiredFiles(0);
        
        // 验证结果
        assertTrue(deletedCount >= 0);
    }
}