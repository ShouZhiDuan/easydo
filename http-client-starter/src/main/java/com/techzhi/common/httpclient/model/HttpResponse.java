package com.techzhi.common.httpclient.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP响应实体
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class HttpResponse<T> {
    
    private int statusCode;
    private String reasonPhrase;
    private Map<String, List<String>> headers;
    private T body;
    private String rawBody;
    private Duration responseTime;
    private long contentLength;
    private String contentType;
    private String contentEncoding;
    private boolean successful;
    private Exception exception;

    public HttpResponse() {
        this.headers = new HashMap<>();
    }

    public HttpResponse(int statusCode) {
        this();
        this.statusCode = statusCode;
        this.successful = statusCode >= 200 && statusCode < 300;
    }

    public HttpResponse(int statusCode, T body) {
        this(statusCode);
        this.body = body;
    }

    // Convenience methods
    public boolean isSuccessful() {
        return successful;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }

    public boolean isInformational() {
        return statusCode >= 100 && statusCode < 200;
    }

    public String getHeaderValue(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getHeaderValues(String name) {
        return headers.get(name);
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    // Builder pattern methods
    public HttpResponse<T> statusCode(int statusCode) {
        this.statusCode = statusCode;
        this.successful = statusCode >= 200 && statusCode < 300;
        return this;
    }

    public HttpResponse<T> reasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    public HttpResponse<T> header(String name, String value) {
        this.headers.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(value);
        return this;
    }

    public HttpResponse<T> headers(Map<String, List<String>> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
        return this;
    }

    public HttpResponse<T> body(T body) {
        this.body = body;
        return this;
    }

    public HttpResponse<T> rawBody(String rawBody) {
        this.rawBody = rawBody;
        return this;
    }

    public HttpResponse<T> responseTime(Duration responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public HttpResponse<T> contentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public HttpResponse<T> contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpResponse<T> contentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public HttpResponse<T> exception(Exception exception) {
        this.exception = exception;
        this.successful = false;
        return this;
    }

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { 
        this.statusCode = statusCode; 
        this.successful = statusCode >= 200 && statusCode < 300;
    }
    public String getReasonPhrase() { return reasonPhrase; }
    public void setReasonPhrase(String reasonPhrase) { this.reasonPhrase = reasonPhrase; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public void setHeaders(Map<String, List<String>> headers) { this.headers = headers; }
    public T getBody() { return body; }
    public void setBody(T body) { this.body = body; }
    public String getRawBody() { return rawBody; }
    public void setRawBody(String rawBody) { this.rawBody = rawBody; }
    public Duration getResponseTime() { return responseTime; }
    public void setResponseTime(Duration responseTime) { this.responseTime = responseTime; }
    public long getContentLength() { return contentLength; }
    public void setContentLength(long contentLength) { this.contentLength = contentLength; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContentEncoding() { return contentEncoding; }
    public void setContentEncoding(String contentEncoding) { this.contentEncoding = contentEncoding; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    public Exception getException() { return exception; }
    public void setException(Exception exception) { 
        this.exception = exception; 
        this.successful = false;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", headers=" + headers +
                ", contentLength=" + contentLength +
                ", contentType='" + contentType + '\'' +
                ", responseTime=" + responseTime +
                ", successful=" + successful +
                '}';
    }
} 