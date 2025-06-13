package com.example.csvimport.service;

import com.example.csvimport.model.ColumnInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * CSV处理服务
 */
@Slf4j
@Service
public class CsvService {
    
    // 常用日期格式
    private static final List<SimpleDateFormat> DATE_FORMATS = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy/MM/dd"),
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("MM/dd/yyyy"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
    );
    
    // 数字模式
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern LONG_PATTERN = Pattern.compile("^-?\\d{10,}$");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^-?\\d*\\.\\d+$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false|yes|no|1|0)$", Pattern.CASE_INSENSITIVE);
    
    /**
     * 解析CSV文件头部并推断列类型
     */
    public List<ColumnInfo> analyzeColumns(String csvFilePath, int sampleSize) {
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            throw new RuntimeException("CSV文件不存在: " + csvFilePath);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            Map<String, String> headers = parser.getHeaderMap();
            List<ColumnInfo> columns = new ArrayList<>();
            
            // 初始化列信息
            for (String header : headers.keySet()) {
                columns.add(new ColumnInfo(header, ColumnInfo.JavaType.STRING));
            }
            
            log.info("CSV文件头部解析完成，列数: {}", columns.size());
            log.info("列名: {}", headers.keySet());
            
            // 分析样本数据以推断类型
            analyzeSampleData(parser, columns, sampleSize);
            
            return columns;
            
        } catch (IOException e) {
            log.error("解析CSV文件失败: {}", csvFilePath, e);
            throw new RuntimeException("解析CSV文件失败", e);
        }
    }
    
    /**
     * 分析样本数据推断列类型
     */
    private void analyzeSampleData(CSVParser parser, List<ColumnInfo> columns, int sampleSize) {
        Map<Integer, Map<ColumnInfo.JavaType, Integer>> typeCounters = new HashMap<>();
        Map<Integer, Integer> maxLengths = new HashMap<>();
        
        // 初始化计数器
        for (int i = 0; i < columns.size(); i++) {
            typeCounters.put(i, new HashMap<>());
            maxLengths.put(i, 0);
        }
        
        int recordCount = 0;
        for (CSVRecord record : parser) {
            if (recordCount >= sampleSize) {
                break;
            }
            
            for (int i = 0; i < Math.min(record.size(), columns.size()); i++) {
                String value = record.get(i);
                if (value != null && !value.trim().isEmpty()) {
                    ColumnInfo.JavaType detectedType = detectDataType(value.trim());
                    typeCounters.get(i).merge(detectedType, 1, Integer::sum);
                    maxLengths.put(i, Math.max(maxLengths.get(i), value.length()));
                }
            }
            recordCount++;
        }
        
        // 根据统计结果确定最终类型
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo column = columns.get(i);
            Map<ColumnInfo.JavaType, Integer> typeCounts = typeCounters.get(i);
            
            if (!typeCounts.isEmpty()) {
                // 选择出现频率最高的类型
                ColumnInfo.JavaType mostFrequentType = typeCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(ColumnInfo.JavaType.STRING);
                
                // 如果大部分值都是同一类型，则使用该类型
                int totalSamples = typeCounts.values().stream().mapToInt(Integer::intValue).sum();
                int mostFrequentCount = typeCounts.get(mostFrequentType);
                
                if (mostFrequentCount >= totalSamples * 0.8) { // 80%以上的值是同一类型
                    column.setJavaType(mostFrequentType);
                } else {
                    column.setJavaType(ColumnInfo.JavaType.STRING); // 类型不一致，使用字符串
                }
            }
            
            // 更新VARCHAR长度
            column.updateVarcharLength(maxLengths.get(i));
            
            log.debug("列 '{}' 类型推断结果: {} (样本数: {})", 
                    column.getName(), column.getJavaType(), typeCounts.values().stream().mapToInt(Integer::intValue).sum());
        }
        
        log.info("数据类型推断完成，分析了 {} 行样本数据", recordCount);
    }
    
    /**
     * 检测数据类型
     */
    private ColumnInfo.JavaType detectDataType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ColumnInfo.JavaType.STRING;
        }
        
        value = value.trim();
        
        // 检测布尔值
        if (BOOLEAN_PATTERN.matcher(value).matches()) {
            return ColumnInfo.JavaType.BOOLEAN;
        }
        
        // 检测整数
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return ColumnInfo.JavaType.INTEGER;
                } else {
                    return ColumnInfo.JavaType.LONG;
                }
            } catch (NumberFormatException e) {
                // 继续检测其他类型
            }
        }
        
        // 检测长整数
        if (LONG_PATTERN.matcher(value).matches()) {
            try {
                Long.parseLong(value);
                return ColumnInfo.JavaType.LONG;
            } catch (NumberFormatException e) {
                // 继续检测其他类型
            }
        }
        
        // 检测浮点数
        if (DOUBLE_PATTERN.matcher(value).matches()) {
            try {
                Double.parseDouble(value);
                return ColumnInfo.JavaType.DOUBLE;
            } catch (NumberFormatException e) {
                // 继续检测其他类型
            }
        }
        
        // 检测日期
        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                format.parse(value);
                if (value.contains(":")) {
                    return ColumnInfo.JavaType.TIMESTAMP;
                } else {
                    return ColumnInfo.JavaType.DATE;
                }
            } catch (ParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        return ColumnInfo.JavaType.STRING;
    }
    
    /**
     * 流式处理CSV数据
     */
    public void processDataStream(String csvFilePath, List<ColumnInfo> columns, 
                                  int batchSize, Consumer<List<List<String>>> batchProcessor) {
        Path path = Paths.get(csvFilePath);
        
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            List<List<String>> batch = new ArrayList<>();
            long totalRecords = 0;
            
            log.info("开始流式处理CSV数据，批次大小: {}", batchSize);
            
            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                
                // 确保行数据与列数匹配
                for (int i = 0; i < columns.size(); i++) {
                    if (i < record.size()) {
                        row.add(record.get(i));
                    } else {
                        row.add(""); // 缺失的列用空字符串填充
                    }
                }
                
                batch.add(row);
                totalRecords++;
                
                // 当批次达到指定大小时处理
                if (batch.size() >= batchSize) {
                    batchProcessor.accept(new ArrayList<>(batch));
                    batch.clear();
                    
                    if (totalRecords % (batchSize * 10) == 0) {
                        log.info("已读取 {} 行数据", totalRecords);
                    }
                }
            }
            
            // 处理剩余的数据
            if (!batch.isEmpty()) {
                batchProcessor.accept(batch);
            }
            
            log.info("CSV数据流式处理完成，总共处理 {} 行数据", totalRecords);
            
        } catch (IOException e) {
            log.error("流式处理CSV数据失败: {}", csvFilePath, e);
            throw new RuntimeException("流式处理CSV数据失败", e);
        }
    }
    
    /**
     * 从文件路径提取表名
     */
    public String extractTableName(String csvFilePath) {
        Path path = Paths.get(csvFilePath);
        String fileName = path.getFileName().toString();
        
        // 移除文件扩展名
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileName = fileName.substring(0, lastDotIndex);
        }
        
        // 清理表名，确保符合MySQL命名规范
        String tableName = fileName.replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");
        
        // 确保表名不为空且不以数字开头
        if (tableName.isEmpty() || Character.isDigit(tableName.charAt(0))) {
            tableName = "table_" + tableName;
        }
        
        log.info("从文件路径 '{}' 提取表名: '{}'", csvFilePath, tableName);
        return tableName;
    }
}