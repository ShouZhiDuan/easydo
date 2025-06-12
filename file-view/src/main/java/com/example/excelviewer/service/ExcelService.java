package com.example.excelviewer.service;

import com.example.excelviewer.model.ExcelData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelService {
    
    private static final int MAX_ROWS_PER_SHEET = 1000; // 限制每个工作表最大行数
    
    /**
     * 解析Excel文件
     * @param file 上传的Excel文件
     * @return Excel数据
     */
    public ExcelData parseExcelFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (!isExcelFile(fileName)) {
            throw new IllegalArgumentException("文件格式不正确，仅支持.xlsx和.xls格式");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, fileName);
            List<ExcelData.SheetData> sheets = parseWorkbook(workbook);
            return new ExcelData(fileName, sheets);
        }
    }
    
    /**
     * 从文件路径解析Excel文件
     * @param filePath 文件路径
     * @return Excel数据
     */
    public ExcelData parseExcelFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("文件不存在: " + filePath);
            }
            
            Workbook workbook = createWorkbook(inputStream, filePath);
            List<ExcelData.SheetData> sheets = parseWorkbook(workbook);
            return new ExcelData(filePath, sheets);
        }
    }
    
    /**
     * 检查是否为Excel文件
     */
    private boolean isExcelFile(String fileName) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls");
    }
    
    /**
     * 创建工作簿对象
     */
    private Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }
    
    /**
     * 解析工作簿
     */
    private List<ExcelData.SheetData> parseWorkbook(Workbook workbook) {
        List<ExcelData.SheetData> sheets = new ArrayList<>();
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            ExcelData.SheetData sheetData = parseSheet(sheet);
            sheets.add(sheetData);
        }
        
        return sheets;
    }
    
    /**
     * 解析工作表
     */
    private ExcelData.SheetData parseSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        Iterator<Row> rowIterator = sheet.iterator();
        boolean isFirstRow = true;
        int rowCount = 0;
        
        while (rowIterator.hasNext() && rowCount < MAX_ROWS_PER_SHEET) {
            Row row = rowIterator.next();
            
            if (isFirstRow) {
                // 解析表头
                headers = parseHeaders(row);
                isFirstRow = false;
            } else {
                // 解析数据行
                Map<String, Object> rowData = parseRow(row, headers);
                rows.add(rowData);
            }
            rowCount++;
        }
        
        return new ExcelData.SheetData(sheetName, headers, rows);
    }
    
    /**
     * 解析表头
     */
    private List<String> parseHeaders(Row row) {
        List<String> headers = new ArrayList<>();
        
        for (Cell cell : row) {
            String header = getCellStringValue(cell);
            headers.add(header.isEmpty() ? "Column" + (cell.getColumnIndex() + 1) : header);
        }
        
        return headers;
    }
    
    /**
     * 解析数据行
     */
    private Map<String, Object> parseRow(Row row, List<String> headers) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Cell cell = row.getCell(i);
            Object value = getCellValue(cell);
            rowData.put(header, value);
        }
        
        return rowData;
    }
    
    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，返回整数类型
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    }
                    return numericValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
    
    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
} 