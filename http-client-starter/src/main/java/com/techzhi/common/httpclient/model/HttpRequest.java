package com.techzhi.common.httpclient.model;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求实体
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class HttpRequest {
    
    private HttpMethod method;
    private URI uri;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Object body;
    private String contentType;
    private Duration timeout;
    private int retryCount;
    private boolean followRedirects;
    
    // 文件相关字段
    private List<MultipartFile> multipartFiles;
    private Map<String, String> formFields;
    private FileProgressCallback progressCallback;

    public HttpRequest() {
        this.method = HttpMethod.GET;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.contentType = "application/json";
        this.followRedirects = true;
        this.retryCount = 0;
        this.multipartFiles = new ArrayList<>();
        this.formFields = new HashMap<>();
    }

    public HttpRequest(HttpMethod method, String url) {
        this();
        this.method = method;
        this.uri = URI.create(url);
    }

    public HttpRequest(HttpMethod method, URI uri) {
        this();
        this.method = method;
        this.uri = uri;
    }

    // Builder pattern
    public static HttpRequest get(String url) {
        return new HttpRequest(HttpMethod.GET, url);
    }

    public static HttpRequest post(String url) {
        return new HttpRequest(HttpMethod.POST, url);
    }

    public static HttpRequest put(String url) {
        return new HttpRequest(HttpMethod.PUT, url);
    }

    public static HttpRequest delete(String url) {
        return new HttpRequest(HttpMethod.DELETE, url);
    }

    public static HttpRequest patch(String url) {
        return new HttpRequest(HttpMethod.PATCH, url);
    }

    public static HttpRequest head(String url) {
        return new HttpRequest(HttpMethod.HEAD, url);
    }

    public static HttpRequest options(String url) {
        return new HttpRequest(HttpMethod.OPTIONS, url);
    }

    public HttpRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public HttpRequest headers(Map<String, String> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
        return this;
    }

    public HttpRequest queryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    public HttpRequest queryParams(Map<String, String> queryParams) {
        if (queryParams != null) {
            this.queryParams.putAll(queryParams);
        }
        return this;
    }

    public HttpRequest body(Object body) {
        this.body = body;
        return this;
    }

    public HttpRequest body(Object body, String contentType) {
        this.body = body;
        this.contentType = contentType;
        return this;
    }

    public HttpRequest jsonBody(Object body) {
        this.body = body;
        this.contentType = "application/json";
        return this;
    }

    public HttpRequest xmlBody(Object body) {
        this.body = body;
        this.contentType = "application/xml";
        return this;
    }

    public HttpRequest formBody(Map<String, String> formData) {
        this.body = formData;
        this.contentType = "application/x-www-form-urlencoded";
        return this;
    }

    public HttpRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpRequest timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequest retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public HttpRequest followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public HttpRequest bearerToken(String token) {
        return header("Authorization", "Bearer " + token);
    }

    public HttpRequest basicAuth(String username, String password) {
        String auth = username + ":" + password;
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
        return header("Authorization", "Basic " + encodedAuth);
    }

    public HttpRequest userAgent(String userAgent) {
        return header("User-Agent", userAgent);
    }

    // 文件上传相关方法
    public HttpRequest multipartFile(String fieldName, File file) {
        this.multipartFiles.add(new MultipartFile(fieldName, file));
        this.contentType = "multipart/form-data";
        return this;
    }

    public HttpRequest multipartFile(String fieldName, String fileName, InputStream inputStream) {
        this.multipartFiles.add(new MultipartFile(fieldName, fileName, inputStream));
        this.contentType = "multipart/form-data";
        return this;
    }

    public HttpRequest multipartFile(String fieldName, String fileName, byte[] content) {
        this.multipartFiles.add(new MultipartFile(fieldName, fileName, content));
        this.contentType = "multipart/form-data";
        return this;
    }

    public HttpRequest multipartFile(String fieldName, String fileName, byte[] content, String contentType) {
        this.multipartFiles.add(new MultipartFile(fieldName, fileName, content, contentType));
        this.contentType = "multipart/form-data";
        return this;
    }

    public HttpRequest multipartFile(MultipartFile multipartFile) {
        this.multipartFiles.add(multipartFile);
        this.contentType = "multipart/form-data";
        return this;
    }

    public HttpRequest multipartFiles(List<MultipartFile> multipartFiles) {
        if (multipartFiles != null) {
            this.multipartFiles.addAll(multipartFiles);
            this.contentType = "multipart/form-data";
        }
        return this;
    }

    public HttpRequest formField(String name, String value) {
        this.formFields.put(name, value);
        if (this.multipartFiles.isEmpty()) {
            this.contentType = "application/x-www-form-urlencoded";
        } else {
            this.contentType = "multipart/form-data";
        }
        return this;
    }

    public HttpRequest formFields(Map<String, String> formFields) {
        if (formFields != null) {
            this.formFields.putAll(formFields);
            if (this.multipartFiles.isEmpty()) {
                this.contentType = "application/x-www-form-urlencoded";
            } else {
                this.contentType = "multipart/form-data";
            }
        }
        return this;
    }

    public HttpRequest progressCallback(FileProgressCallback callback) {
        this.progressCallback = callback;
        return this;
    }

    public boolean hasMultipartFiles() {
        return !this.multipartFiles.isEmpty();
    }

    public boolean hasFormFields() {
        return !this.formFields.isEmpty();
    }

    // Getters and Setters
    public HttpMethod getMethod() { return method; }
    public void setMethod(HttpMethod method) { this.method = method; }
    public URI getUri() { return uri; }
    public void setUri(URI uri) { this.uri = uri; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public void setQueryParams(Map<String, String> queryParams) { this.queryParams = queryParams; }
    public Object getBody() { return body; }
    public void setBody(Object body) { this.body = body; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public boolean isFollowRedirects() { return followRedirects; }
    public void setFollowRedirects(boolean followRedirects) { this.followRedirects = followRedirects; }
    public List<MultipartFile> getMultipartFiles() { return multipartFiles; }
    public void setMultipartFiles(List<MultipartFile> multipartFiles) { this.multipartFiles = multipartFiles; }
    public Map<String, String> getFormFields() { return formFields; }
    public void setFormFields(Map<String, String> formFields) { this.formFields = formFields; }
    public FileProgressCallback getProgressCallback() { return progressCallback; }
    public void setProgressCallback(FileProgressCallback progressCallback) { this.progressCallback = progressCallback; }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method=" + method +
                ", uri=" + uri +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", contentType='" + contentType + '\'' +
                ", timeout=" + timeout +
                ", retryCount=" + retryCount +
                ", followRedirects=" + followRedirects +
                '}';
    }
} 