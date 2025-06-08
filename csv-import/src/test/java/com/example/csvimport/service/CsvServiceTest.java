package com.example.csvimport.service;

import com.example.csvimport.model.ColumnInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvServiceTest {
    
    private CsvService csvService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        csvService = new CsvService();
    }
    
    @Test
    void testAnalyzeColumns() throws IOException {
        // 创建测试CSV文件
        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = "id,name,age,salary,is_active,created_date\n" +
                           "1,John Doe,25,50000.50,true,2023-01-01\n" +
                           "2,Jane Smith,30,60000.75,false,2023-01-02\n" +
                           "3,Bob Johnson,35,70000.00,true,2023-01-03";
        Files.write(csvFile, csvContent.getBytes());
        
        // 分析列
        List<ColumnInfo> columns = csvService.analyzeColumns(csvFile.toString(), 100);
        
        // 验证结果
        assertEquals(6, columns.size());
        
        // 验证列名
        assertEquals("id", columns.get(0).getName());
        assertEquals("name", columns.get(1).getName());
        assertEquals("age", columns.get(2).getName());
        assertEquals("salary", columns.get(3).getName());
        assertEquals("is_active", columns.get(4).getName());
        assertEquals("created_date", columns.get(5).getName());
        
        // 验证数据类型推断
        assertEquals(ColumnInfo.JavaType.INTEGER, columns.get(0).getJavaType()); // id
        assertEquals(ColumnInfo.JavaType.STRING, columns.get(1).getJavaType());  // name
        assertEquals(ColumnInfo.JavaType.INTEGER, columns.get(2).getJavaType()); // age
        assertEquals(ColumnInfo.JavaType.DOUBLE, columns.get(3).getJavaType());  // salary
        assertEquals(ColumnInfo.JavaType.BOOLEAN, columns.get(4).getJavaType()); // is_active
        assertEquals(ColumnInfo.JavaType.DATE, columns.get(5).getJavaType());    // created_date
    }
    
    @Test
    void testExtractTableName() {
        // 测试正常文件名
        assertEquals("users", csvService.extractTableName("/path/to/users.csv"));
        assertEquals("order_data", csvService.extractTableName("/path/to/order-data.csv"));
        assertEquals("product_info", csvService.extractTableName("/path/to/product info.csv"));
        
        // 测试特殊字符
        assertEquals("test_file", csvService.extractTableName("/path/to/test@file#.csv"));
        
        // 测试以数字开头的文件名
        assertEquals("table_123data", csvService.extractTableName("/path/to/123data.csv"));
        
        // 测试没有扩展名的文件
        assertEquals("datafile", csvService.extractTableName("/path/to/datafile"));
    }
    
    @Test
    void testProcessDataStream() throws IOException {
        // 创建测试CSV文件
        Path csvFile = tempDir.resolve("stream_test.csv");
        String csvContent = "col1,col2,col3\n" +
                           "value1,value2,value3\n" +
                           "value4,value5,value6\n" +
                           "value7,value8,value9";
        Files.write(csvFile, csvContent.getBytes());
        
        // 准备列信息
        List<ColumnInfo> columns = List.of(
                new ColumnInfo("col1", ColumnInfo.JavaType.STRING),
                new ColumnInfo("col2", ColumnInfo.JavaType.STRING),
                new ColumnInfo("col3", ColumnInfo.JavaType.STRING)
        );
        
        // 收集处理的数据
        List<List<List<String>>> batches = new ArrayList<>();
        
        // 处理数据流
        csvService.processDataStream(csvFile.toString(), columns, 2, batches::add);
        
        // 验证结果
        assertEquals(2, batches.size()); // 3行数据，批次大小2，应该有2个批次
        assertEquals(2, batches.get(0).size()); // 第一个批次2行
        assertEquals(1, batches.get(1).size()); // 第二个批次1行
        
        // 验证数据内容
        assertEquals("value1", batches.get(0).get(0).get(0));
        assertEquals("value2", batches.get(0).get(0).get(1));
        assertEquals("value3", batches.get(0).get(0).get(2));
    }
    
    @Test
    void testAnalyzeColumnsWithMixedTypes() throws IOException {
        // 创建包含混合类型的测试CSV文件
        Path csvFile = tempDir.resolve("mixed_types.csv");
        String csvContent = "mixed_col\n" +
                           "123\n" +
                           "abc\n" +
                           "456\n" +
                           "def";
        Files.write(csvFile, csvContent.getBytes());
        
        // 分析列
        List<ColumnInfo> columns = csvService.analyzeColumns(csvFile.toString(), 100);
        
        // 验证结果 - 混合类型应该被识别为STRING
        assertEquals(1, columns.size());
        assertEquals("mixed_col", columns.get(0).getName());
        assertEquals(ColumnInfo.JavaType.STRING, columns.get(0).getJavaType());
    }
    
    @Test
    void testAnalyzeColumnsWithEmptyValues() throws IOException {
        // 创建包含空值的测试CSV文件
        Path csvFile = tempDir.resolve("empty_values.csv");
        String csvContent = "id,name,age\n" +
                           "1,John,25\n" +
                           "2,,30\n" +
                           "3,Jane,";
        Files.write(csvFile, csvContent.getBytes());
        
        // 分析列
        List<ColumnInfo> columns = csvService.analyzeColumns(csvFile.toString(), 100);
        
        // 验证结果
        assertEquals(3, columns.size());
        assertEquals(ColumnInfo.JavaType.INTEGER, columns.get(0).getJavaType()); // id
        assertEquals(ColumnInfo.JavaType.STRING, columns.get(1).getJavaType());  // name
        assertEquals(ColumnInfo.JavaType.INTEGER, columns.get(2).getJavaType()); // age
    }
}