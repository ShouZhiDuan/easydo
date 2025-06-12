package com.techzhi.common.httpclient;

import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP客户端模板类
 * 提供更方便的HTTP请求方法
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class HttpClientTemplate {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientTemplate.class);

    private final HttpClient httpClient;

    public HttpClientTemplate(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * GET请求
     */
    public HttpResponse<String> get(String url) {
        return httpClient.get(url);
    }

    /**
     * GET请求，带查询参数
     */
    public HttpResponse<String> get(String url, Map<String, String> queryParams) {
        return httpClient.execute(HttpRequest.get(url).queryParams(queryParams));
    }

    /**
     * GET请求，带请求头
     */
    public HttpResponse<String> get(String url, Map<String, String> queryParams, Map<String, String> headers) {
        return httpClient.execute(HttpRequest.get(url).queryParams(queryParams).headers(headers));
    }

    /**
     * GET请求，支持类型转换
     */
    public <T> HttpResponse<T> get(String url, Class<T> responseType) {
        return httpClient.get(url, responseType);
    }

    /**
     * GET请求，带查询参数，支持类型转换
     */
    public <T> HttpResponse<T> get(String url, Map<String, String> queryParams, Class<T> responseType) {
        return httpClient.execute(HttpRequest.get(url).queryParams(queryParams), responseType);
    }

    /**
     * POST请求
     */
    public HttpResponse<String> post(String url, Object body) {
        return httpClient.post(url, body);
    }

    /**
     * POST请求，带请求头
     */
    public HttpResponse<String> post(String url, Object body, Map<String, String> headers) {
        return httpClient.execute(HttpRequest.post(url).jsonBody(body).headers(headers));
    }

    /**
     * POST请求，支持类型转换
     */
    public <T> HttpResponse<T> post(String url, Object body, Class<T> responseType) {
        return httpClient.post(url, body, responseType);
    }

    /**
     * POST请求，带请求头，支持类型转换
     */
    public <T> HttpResponse<T> post(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return httpClient.execute(HttpRequest.post(url).jsonBody(body).headers(headers), responseType);
    }

    /**
     * PUT请求
     */
    public HttpResponse<String> put(String url, Object body) {
        return httpClient.put(url, body);
    }

    /**
     * PUT请求，带请求头
     */
    public HttpResponse<String> put(String url, Object body, Map<String, String> headers) {
        return httpClient.execute(HttpRequest.put(url).jsonBody(body).headers(headers));
    }

    /**
     * PUT请求，支持类型转换
     */
    public <T> HttpResponse<T> put(String url, Object body, Class<T> responseType) {
        return httpClient.put(url, body, responseType);
    }

    /**
     * DELETE请求
     */
    public HttpResponse<String> delete(String url) {
        return httpClient.delete(url);
    }

    /**
     * DELETE请求，带请求头
     */
    public HttpResponse<String> delete(String url, Map<String, String> headers) {
        return httpClient.execute(HttpRequest.delete(url).headers(headers));
    }

    /**
     * DELETE请求，支持类型转换
     */
    public <T> HttpResponse<T> delete(String url, Class<T> responseType) {
        return httpClient.delete(url, responseType);
    }

    /**
     * PATCH请求
     */
    public HttpResponse<String> patch(String url, Object body) {
        return httpClient.patch(url, body);
    }

    /**
     * PATCH请求，支持类型转换
     */
    public <T> HttpResponse<T> patch(String url, Object body, Class<T> responseType) {
        return httpClient.patch(url, body, responseType);
    }

    /**
     * 表单POST请求
     */
    public HttpResponse<String> postForm(String url, Map<String, String> formData) {
        return httpClient.execute(HttpRequest.post(url).formBody(formData));
    }

    /**
     * 表单POST请求，支持类型转换
     */
    public <T> HttpResponse<T> postForm(String url, Map<String, String> formData, Class<T> responseType) {
        return httpClient.execute(HttpRequest.post(url).formBody(formData), responseType);
    }

    /**
     * 带Bearer Token的GET请求
     */
    public HttpResponse<String> getWithToken(String url, String token) {
        return httpClient.execute(HttpRequest.get(url).bearerToken(token));
    }

    /**
     * 带Bearer Token的GET请求，支持类型转换
     */
    public <T> HttpResponse<T> getWithToken(String url, String token, Class<T> responseType) {
        return httpClient.execute(HttpRequest.get(url).bearerToken(token), responseType);
    }

    /**
     * 带Bearer Token的POST请求
     */
    public HttpResponse<String> postWithToken(String url, Object body, String token) {
        return httpClient.execute(HttpRequest.post(url).jsonBody(body).bearerToken(token));
    }

    /**
     * 带Bearer Token的POST请求，支持类型转换
     */
    public <T> HttpResponse<T> postWithToken(String url, Object body, String token, Class<T> responseType) {
        return httpClient.execute(HttpRequest.post(url).jsonBody(body).bearerToken(token), responseType);
    }

    /**
     * 基本认证GET请求
     */
    public HttpResponse<String> getWithBasicAuth(String url, String username, String password) {
        return httpClient.execute(HttpRequest.get(url).basicAuth(username, password));
    }

    /**
     * 基本认证POST请求
     */
    public HttpResponse<String> postWithBasicAuth(String url, Object body, String username, String password) {
        return httpClient.execute(HttpRequest.post(url).jsonBody(body).basicAuth(username, password));
    }

    // 异步方法

    /**
     * 异步GET请求
     */
    public CompletableFuture<HttpResponse<String>> getAsync(String url) {
        return httpClient.getAsync(url);
    }

    /**
     * 异步GET请求，支持类型转换
     */
    public <T> CompletableFuture<HttpResponse<T>> getAsync(String url, Class<T> responseType) {
        return httpClient.getAsync(url, responseType);
    }

    /**
     * 异步POST请求
     */
    public CompletableFuture<HttpResponse<String>> postAsync(String url, Object body) {
        return httpClient.postAsync(url, body);
    }

    /**
     * 异步POST请求，支持类型转换
     */
    public <T> CompletableFuture<HttpResponse<T>> postAsync(String url, Object body, Class<T> responseType) {
        return httpClient.postAsync(url, body, responseType);
    }

    /**
     * 执行自定义请求
     */
    public HttpResponse<String> execute(HttpRequest request) {
        return httpClient.execute(request);
    }

    /**
     * 执行自定义请求，支持类型转换
     */
    public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
        return httpClient.execute(request, responseType);
    }

    /**
     * 异步执行自定义请求
     */
    public CompletableFuture<HttpResponse<String>> executeAsync(HttpRequest request) {
        return httpClient.executeAsync(request);
    }

    /**
     * 异步执行自定义请求，支持类型转换
     */
    public <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpRequest request, Class<T> responseType) {
        return httpClient.executeAsync(request, responseType);
    }

    // =============== 文件操作相关方法 ===============

    /**
     * 上传单个文件
     */
    public HttpResponse<String> uploadFile(String url, String fieldName, File file) {
        return httpClient.uploadFile(url, fieldName, file);
    }

    /**
     * 上传单个文件，带进度回调
     */
    public HttpResponse<String> uploadFile(String url, String fieldName, File file, FileProgressCallback progressCallback) {
        return httpClient.uploadFile(url, fieldName, file, progressCallback);
    }

    /**
     * 上传多个文件
     */
    public HttpResponse<String> uploadFiles(String url, List<MultipartFile> files) {
        return httpClient.uploadFiles(url, files);
    }

    /**
     * 上传多个文件，带进度回调
     */
    public HttpResponse<String> uploadFiles(String url, List<MultipartFile> files, FileProgressCallback progressCallback) {
        return httpClient.uploadFiles(url, files, progressCallback);
    }

    /**
     * 上传文件和表单数据
     */
    public HttpResponse<String> uploadFilesWithForm(String url, List<MultipartFile> files, Map<String, String> formFields) {
        return httpClient.uploadFilesWithForm(url, files, formFields);
    }

    /**
     * 下载文件
     */
    public boolean downloadFile(String url, String savePath) {
        return httpClient.downloadFile(url, savePath);
    }

    /**
     * 下载文件，带进度回调
     */
    public boolean downloadFile(String url, String savePath, FileProgressCallback progressCallback) {
        return httpClient.downloadFile(url, savePath, progressCallback);
    }

    /**
     * 下载文件，带请求头
     */
    public boolean downloadFile(String url, String savePath, Map<String, String> headers) {
        return httpClient.downloadFile(url, savePath, headers);
    }

    /**
     * 下载文件，带请求头和进度回调
     */
    public boolean downloadFile(String url, String savePath, Map<String, String> headers, FileProgressCallback progressCallback) {
        return httpClient.downloadFile(url, savePath, headers, progressCallback);
    }

    /**
     * 获取文件流
     */
    public InputStream getFileStream(String url) {
        return httpClient.getFileStream(url);
    }

    /**
     * 获取文件流，带请求头
     */
    public InputStream getFileStream(String url, Map<String, String> headers) {
        return httpClient.getFileStream(url, headers);
    }

    /**
     * 获取文件字节数组
     */
    public byte[] getFileBytes(String url) {
        return httpClient.getFileBytes(url);
    }

    /**
     * 获取文件字节数组，带请求头
     */
    public byte[] getFileBytes(String url, Map<String, String> headers) {
        return httpClient.getFileBytes(url, headers);
    }

    /**
     * 异步上传文件
     */
    public CompletableFuture<HttpResponse<String>> uploadFileAsync(String url, String fieldName, File file) {
        return httpClient.uploadFileAsync(url, fieldName, file);
    }

    /**
     * 异步上传文件，带进度回调
     */
    public CompletableFuture<HttpResponse<String>> uploadFileAsync(String url, String fieldName, File file, FileProgressCallback progressCallback) {
        return httpClient.uploadFileAsync(url, fieldName, file, progressCallback);
    }

    /**
     * 异步下载文件
     */
    public CompletableFuture<Boolean> downloadFileAsync(String url, String savePath) {
        return httpClient.downloadFileAsync(url, savePath);
    }

    /**
     * 异步下载文件，带进度回调
     */
    public CompletableFuture<Boolean> downloadFileAsync(String url, String savePath, FileProgressCallback progressCallback) {
        return httpClient.downloadFileAsync(url, savePath, progressCallback);
    }

    /**
     * 获取底层HTTP客户端
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        httpClient.close();
    }
} 