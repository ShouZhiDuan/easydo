package com.techzhi.common.httpclient.model;

import java.io.File;
import java.io.InputStream;

/**
 * 多部分文件上传信息
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class MultipartFile {
    
    private String fieldName;
    private String fileName;
    private String contentType;
    private File file;
    private InputStream inputStream;
    private byte[] content;
    private long contentLength;

    public MultipartFile() {}

    public MultipartFile(String fieldName, File file) {
        this.fieldName = fieldName;
        this.file = file;
        this.fileName = file.getName();
        this.contentLength = file.length();
        this.contentType = guessContentType(fileName);
    }

    public MultipartFile(String fieldName, String fileName, InputStream inputStream) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.contentType = guessContentType(fileName);
        this.contentLength = -1; // Unknown length
    }

    public MultipartFile(String fieldName, String fileName, byte[] content) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.content = content;
        this.contentLength = content.length;
        this.contentType = guessContentType(fileName);
    }

    public MultipartFile(String fieldName, String fileName, byte[] content, String contentType) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.content = content;
        this.contentLength = content.length;
        this.contentType = contentType;
    }

    /**
     * 根据文件名猜测内容类型
     */
    private String guessContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String lowercaseFileName = fileName.toLowerCase();
        if (lowercaseFileName.endsWith(".jpg") || lowercaseFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowercaseFileName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowercaseFileName.endsWith(".json")) {
            return "application/json";
        } else if (lowercaseFileName.endsWith(".xml")) {
            return "application/xml";
        } else if (lowercaseFileName.endsWith(".zip")) {
            return "application/zip";
        } else {
            return "application/octet-stream";
        }
    }

    // Static factory methods
    public static MultipartFile of(String fieldName, File file) {
        return new MultipartFile(fieldName, file);
    }

    public static MultipartFile of(String fieldName, String fileName, InputStream inputStream) {
        return new MultipartFile(fieldName, fileName, inputStream);
    }

    public static MultipartFile of(String fieldName, String fileName, byte[] content) {
        return new MultipartFile(fieldName, fileName, content);
    }

    public static MultipartFile of(String fieldName, String fileName, byte[] content, String contentType) {
        return new MultipartFile(fieldName, fileName, content, contentType);
    }

    // Getters and Setters
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }
    public InputStream getInputStream() { return inputStream; }
    public void setInputStream(InputStream inputStream) { this.inputStream = inputStream; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    public long getContentLength() { return contentLength; }
    public void setContentLength(long contentLength) { this.contentLength = contentLength; }

    @Override
    public String toString() {
        return "MultipartFile{" +
                "fieldName='" + fieldName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
} 