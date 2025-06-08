package com.example.csvview.model;

import java.util.List;

public class CsvData {
    private List<String> headers;
    private List<List<String>> rows;
    private String fileName;
    private int totalRows;

    public CsvData() {}

    public CsvData(List<String> headers, List<List<String>> rows, String fileName) {
        this.headers = headers;
        this.rows = rows;
        this.fileName = fileName;
        this.totalRows = rows != null ? rows.size() : 0;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
        this.totalRows = rows != null ? rows.size() : 0;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
}