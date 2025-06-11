package com.example.filejson.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseData {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("data")
    private Object data;
    
    public ResponseData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ResponseData(String message, String status) {
        this();
        this.message = message;
        this.status = status;
    }
    
    public ResponseData(String message, String status, Object data) {
        this(message, status);
        this.data = data;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}