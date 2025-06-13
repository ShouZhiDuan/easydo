package com.example.excelviewer.model;

import java.util.List;
import java.util.Map;

/**
 * Excel数据模型
 */
public class ExcelData {
    private String fileName;
    private List<SheetData> sheets;
    private String message;
    
    public ExcelData() {}
    
    public ExcelData(String fileName, List<SheetData> sheets) {
        this.fileName = fileName;
        this.sheets = sheets;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public List<SheetData> getSheets() {
        return sheets;
    }
    
    public void setSheets(List<SheetData> sheets) {
        this.sheets = sheets;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 工作表数据
     */
    public static class SheetData {
        private String sheetName;
        private List<String> headers;
        private List<Map<String, Object>> rows;
        private int totalRows;
        
        public SheetData() {}
        
        public SheetData(String sheetName, List<String> headers, List<Map<String, Object>> rows) {
            this.sheetName = sheetName;
            this.headers = headers;
            this.rows = rows;
            this.totalRows = rows.size();
        }
        
        public String getSheetName() {
            return sheetName;
        }
        
        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }
        
        public List<String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }
        
        public List<Map<String, Object>> getRows() {
            return rows;
        }
        
        public void setRows(List<Map<String, Object>> rows) {
            this.rows = rows;
            this.totalRows = rows != null ? rows.size() : 0;
        }
        
        public int getTotalRows() {
            return totalRows;
        }
        
        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }
    }
} 