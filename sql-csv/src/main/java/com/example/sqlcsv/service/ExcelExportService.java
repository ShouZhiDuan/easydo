package com.example.sqlcsv.service;

import com.example.sqlcsv.config.DataMaskingConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Excel导出服务
 */
@Service
public class ExcelExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    // 每个Sheet最大行数（Excel限制）
    private static final int MAX_ROWS_PER_SHEET = 1048576;
    // 内存中保持的行数（SXSSFWorkbook配置）
    private static final int ROWS_IN_MEMORY = 1000;
    
    /**
     * 导出查询结果到Excel文件
     * 
     * @param queryResults 查询结果列表
     * @param sheetNames Sheet名称列表
     * @param maskingRules 全局脱敏规则列表
     * @param outputPath 输出文件路径
     * @throws IOException IO异常
     */
    public void exportToExcel(List<List<Map<String, Object>>> queryResults,
                             List<String> sheetNames,
                             List<DataMaskingConfig.FieldMaskingRule> maskingRules,
                             String outputPath) throws IOException {
        exportToExcel(queryResults, sheetNames, maskingRules, null, outputPath);
    }
    
    /**
     * 导出查询结果到Excel文件（支持每个SQL独立脱敏规则）
     * 
     * @param queryResults 查询结果列表
     * @param sheetNames Sheet名称列表
     * @param globalMaskingRules 全局脱敏规则列表
     * @param sqlMaskingRules 每个SQL对应的脱敏规则列表
     * @param outputPath 输出文件路径
     * @throws IOException IO异常
     */
    public void exportToExcel(List<List<Map<String, Object>>> queryResults,
                             List<String> sheetNames,
                             List<DataMaskingConfig.FieldMaskingRule> globalMaskingRules,
                             List<List<DataMaskingConfig.FieldMaskingRule>> sqlMaskingRules,
                             String outputPath) throws IOException {
        
        if (queryResults == null || queryResults.isEmpty()) {
            throw new IllegalArgumentException("查询结果不能为空");
        }
        
        // 使用SXSSFWorkbook以支持大数据量导出
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROWS_IN_MEMORY)) {
            
            for (int i = 0; i < queryResults.size(); i++) {
                List<Map<String, Object>> resultSet = queryResults.get(i);
                String sheetName = getSheetName(sheetNames, i);
                
                // 确定当前SQL使用的脱敏规则
                List<DataMaskingConfig.FieldMaskingRule> currentMaskingRules = getCurrentMaskingRules(
                    globalMaskingRules, sqlMaskingRules, i);
                
                if (resultSet != null && !resultSet.isEmpty()) {
                    createSheet(workbook, resultSet, sheetName, currentMaskingRules);
                } else {
                    // 创建空Sheet
                    createEmptySheet(workbook, sheetName);
                }
            }
            
            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
                logger.info("Excel文件导出成功: {}", outputPath);
            }
            
        } catch (Exception e) {
            logger.error("Excel导出失败: {}", e.getMessage(), e);
            throw new IOException("Excel导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取当前SQL对应的脱敏规则
     * 
     * @param globalMaskingRules 全局脱敏规则
     * @param sqlMaskingRules 每个SQL对应的脱敏规则列表
     * @param sqlIndex SQL索引
     * @return 当前SQL使用的脱敏规则
     */
    private List<DataMaskingConfig.FieldMaskingRule> getCurrentMaskingRules(
            List<DataMaskingConfig.FieldMaskingRule> globalMaskingRules,
            List<List<DataMaskingConfig.FieldMaskingRule>> sqlMaskingRules,
            int sqlIndex) {
        
        // 优先使用SQL特定的脱敏规则
        if (sqlMaskingRules != null && sqlIndex < sqlMaskingRules.size()) {
            List<DataMaskingConfig.FieldMaskingRule> sqlSpecificRules = sqlMaskingRules.get(sqlIndex);
            if (sqlSpecificRules != null && !sqlSpecificRules.isEmpty()) {
                logger.debug("使用第{}个SQL的专用脱敏规则，规则数量: {}", sqlIndex + 1, sqlSpecificRules.size());
                return sqlSpecificRules;
            }
        }
        
        // 回退到全局脱敏规则
        if (globalMaskingRules != null && !globalMaskingRules.isEmpty()) {
            logger.debug("使用全局脱敏规则，规则数量: {}", globalMaskingRules.size());
            return globalMaskingRules;
        }
        
        // 没有脱敏规则
        logger.debug("第{}个SQL没有配置脱敏规则", sqlIndex + 1);
        return null;
    }
    
    /**
     * 创建Sheet并填充数据
     */
    private void createSheet(SXSSFWorkbook workbook, 
                           List<Map<String, Object>> resultSet, 
                           String sheetName,
                           List<DataMaskingConfig.FieldMaskingRule> maskingRules) {
        
        Sheet sheet = workbook.createSheet(sheetName);
        
        if (resultSet.isEmpty()) {
            return;
        }
        
        // 获取列名
        Map<String, Object> firstRow = resultSet.get(0);
        List<String> columnNames = List.copyOf(firstRow.keySet());
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnNames.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnNames.get(i));
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowIndex = 1;
        for (Map<String, Object> dataRow : resultSet) {
            if (rowIndex >= MAX_ROWS_PER_SHEET) {
                logger.warn("Sheet {} 数据行数超过Excel限制，部分数据将被截断", sheetName);
                break;
            }
            
            // 应用数据脱敏
            Map<String, Object> maskedRow = dataMaskingService.maskRowData(dataRow, columnNames, maskingRules);
            
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = maskedRow.get(columnNames.get(i));
                setCellValue(cell, value);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 自动调整列宽（仅对前10列，避免性能问题）
        int maxColumns = Math.min(columnNames.size(), 10);
        for (int i = 0; i < maxColumns; i++) {
            try {
                sheet.autoSizeColumn(i);
                // 设置最大列宽，避免过宽
                int columnWidth = sheet.getColumnWidth(i);
                if (columnWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            } catch (Exception e) {
                logger.warn("自动调整列宽失败: {}", e.getMessage());
            }
        }
        
        logger.info("Sheet {} 创建完成，数据行数: {}", sheetName, resultSet.size());
    }
    
    /**
     * 创建空Sheet
     */
    private void createEmptySheet(SXSSFWorkbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("无数据");
        
        CellStyle style = createHeaderStyle(workbook);
        cell.setCellStyle(style);
        
        logger.info("空Sheet {} 创建完成", sheetName);
    }
    
    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else if (value instanceof java.time.LocalDateTime) {
            cell.setCellValue(((java.time.LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else if (value instanceof java.time.LocalDate) {
            cell.setCellValue(((java.time.LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        // 设置对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 获取Sheet名称
     */
    private String getSheetName(List<String> sheetNames, int index) {
        if (sheetNames != null && index < sheetNames.size() && sheetNames.get(index) != null) {
            String name = sheetNames.get(index).trim();
            // Excel Sheet名称限制：不能超过31个字符，不能包含特殊字符
            name = name.replaceAll("[\\/:*?\"<>|]", "_");
            if (name.length() > 31) {
                name = name.substring(0, 31);
            }
            return name.isEmpty() ? "Sheet" + (index + 1) : name;
        }
        return "Sheet" + (index + 1);
    }
}