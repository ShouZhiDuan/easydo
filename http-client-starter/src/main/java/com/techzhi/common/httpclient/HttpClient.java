package com.techzhi.common.httpclient;

import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP客户端接口
 * 提供同步和异步的HTTP请求方法
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public interface HttpClient {

    /**
     * 执行同步HTTP请求
     * 
     * @param request HTTP请求对象
     * @return HTTP响应对象
     */
    HttpResponse<String> execute(HttpRequest request);

    /**
     * 执行同步HTTP请求，并将响应体转换为指定类型
     * 
     * @param request HTTP请求对象
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType);

    /**
     * 执行异步HTTP请求
     * 
     * @param request HTTP请求对象
     * @return CompletableFuture包装的HTTP响应对象
     */
    CompletableFuture<HttpResponse<String>> executeAsync(HttpRequest request);

    /**
     * 执行异步HTTP请求，并将响应体转换为指定类型
     * 
     * @param request HTTP请求对象
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return CompletableFuture包装的HTTP响应对象
     */
    <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpRequest request, Class<T> responseType);

    /**
     * GET请求的便捷方法
     * 
     * @param url 请求URL
     * @return HTTP响应对象
     */
    default HttpResponse<String> get(String url) {
        return execute(HttpRequest.get(url));
    }

    /**
     * GET请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    default <T> HttpResponse<T> get(String url, Class<T> responseType) {
        return execute(HttpRequest.get(url), responseType);
    }

    /**
     * POST请求的便捷方法
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return HTTP响应对象
     */
    default HttpResponse<String> post(String url, Object body) {
        return execute(HttpRequest.post(url).jsonBody(body));
    }

    /**
     * POST请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    default <T> HttpResponse<T> post(String url, Object body, Class<T> responseType) {
        return execute(HttpRequest.post(url).jsonBody(body), responseType);
    }

    /**
     * PUT请求的便捷方法
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return HTTP响应对象
     */
    default HttpResponse<String> put(String url, Object body) {
        return execute(HttpRequest.put(url).jsonBody(body));
    }

    /**
     * PUT请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    default <T> HttpResponse<T> put(String url, Object body, Class<T> responseType) {
        return execute(HttpRequest.put(url).jsonBody(body), responseType);
    }

    /**
     * DELETE请求的便捷方法
     * 
     * @param url 请求URL
     * @return HTTP响应对象
     */
    default HttpResponse<String> delete(String url) {
        return execute(HttpRequest.delete(url));
    }

    /**
     * DELETE请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    default <T> HttpResponse<T> delete(String url, Class<T> responseType) {
        return execute(HttpRequest.delete(url), responseType);
    }

    /**
     * PATCH请求的便捷方法
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return HTTP响应对象
     */
    default HttpResponse<String> patch(String url, Object body) {
        return execute(HttpRequest.patch(url).jsonBody(body));
    }

    /**
     * PATCH请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return HTTP响应对象
     */
    default <T> HttpResponse<T> patch(String url, Object body, Class<T> responseType) {
        return execute(HttpRequest.patch(url).jsonBody(body), responseType);
    }

    /**
     * 异步GET请求的便捷方法
     * 
     * @param url 请求URL
     * @return CompletableFuture包装的HTTP响应对象
     */
    default CompletableFuture<HttpResponse<String>> getAsync(String url) {
        return executeAsync(HttpRequest.get(url));
    }

    /**
     * 异步GET请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return CompletableFuture包装的HTTP响应对象
     */
    default <T> CompletableFuture<HttpResponse<T>> getAsync(String url, Class<T> responseType) {
        return executeAsync(HttpRequest.get(url), responseType);
    }

    /**
     * 异步POST请求的便捷方法
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return CompletableFuture包装的HTTP响应对象
     */
    default CompletableFuture<HttpResponse<String>> postAsync(String url, Object body) {
        return executeAsync(HttpRequest.post(url).jsonBody(body));
    }

    /**
     * 异步POST请求的便捷方法，支持类型转换
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param responseType 响应体类型
     * @param <T> 响应体类型泛型
     * @return CompletableFuture包装的HTTP响应对象
     */
    default <T> CompletableFuture<HttpResponse<T>> postAsync(String url, Object body, Class<T> responseType) {
        return executeAsync(HttpRequest.post(url).jsonBody(body), responseType);
    }

    // =============== 文件操作相关方法 ===============

    /**
     * 上传单个文件
     * 
     * @param url 上传URL
     * @param fieldName 字段名
     * @param file 要上传的文件
     * @return HTTP响应对象
     */
    default HttpResponse<String> uploadFile(String url, String fieldName, File file) {
        return execute(HttpRequest.post(url).multipartFile(fieldName, file));
    }

    /**
     * 上传单个文件，带进度回调
     * 
     * @param url 上传URL
     * @param fieldName 字段名
     * @param file 要上传的文件
     * @param progressCallback 进度回调
     * @return HTTP响应对象
     */
    default HttpResponse<String> uploadFile(String url, String fieldName, File file, FileProgressCallback progressCallback) {
        return execute(HttpRequest.post(url).multipartFile(fieldName, file).progressCallback(progressCallback));
    }

    /**
     * 上传多个文件
     * 
     * @param url 上传URL
     * @param files 要上传的文件列表
     * @return HTTP响应对象
     */
    default HttpResponse<String> uploadFiles(String url, List<MultipartFile> files) {
        return execute(HttpRequest.post(url).multipartFiles(files));
    }

    /**
     * 上传多个文件，带进度回调
     * 
     * @param url 上传URL
     * @param files 要上传的文件列表
     * @param progressCallback 进度回调
     * @return HTTP响应对象
     */
    default HttpResponse<String> uploadFiles(String url, List<MultipartFile> files, FileProgressCallback progressCallback) {
        return execute(HttpRequest.post(url).multipartFiles(files).progressCallback(progressCallback));
    }

    /**
     * 上传文件和表单数据
     * 
     * @param url 上传URL
     * @param files 要上传的文件列表
     * @param formFields 表单字段
     * @return HTTP响应对象
     */
    default HttpResponse<String> uploadFilesWithForm(String url, List<MultipartFile> files, Map<String, String> formFields) {
        return execute(HttpRequest.post(url).multipartFiles(files).formFields(formFields));
    }

    /**
     * 下载文件到指定路径
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @return 是否下载成功
     */
    boolean downloadFile(String url, String savePath);

    /**
     * 下载文件到指定路径，带进度回调
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @param progressCallback 进度回调
     * @return 是否下载成功
     */
    boolean downloadFile(String url, String savePath, FileProgressCallback progressCallback);

    /**
     * 下载文件到指定路径，带请求头
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @param headers 请求头
     * @return 是否下载成功
     */
    boolean downloadFile(String url, String savePath, Map<String, String> headers);

    /**
     * 下载文件到指定路径，带请求头和进度回调
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @param headers 请求头
     * @param progressCallback 进度回调
     * @return 是否下载成功
     */
    boolean downloadFile(String url, String savePath, Map<String, String> headers, FileProgressCallback progressCallback);

    /**
     * 获取文件流
     * 
     * @param url 文件URL
     * @return 文件输入流
     */
    InputStream getFileStream(String url);

    /**
     * 获取文件流，带请求头
     * 
     * @param url 文件URL
     * @param headers 请求头
     * @return 文件输入流
     */
    InputStream getFileStream(String url, Map<String, String> headers);

    /**
     * 获取文件字节数组
     * 
     * @param url 文件URL
     * @return 文件字节数组
     */
    default byte[] getFileBytes(String url) {
        HttpResponse<String> response = execute(HttpRequest.get(url));
        return response.isSuccessful() ? response.getRawBody().getBytes() : null;
    }

    /**
     * 获取文件字节数组，带请求头
     * 
     * @param url 文件URL
     * @param headers 请求头
     * @return 文件字节数组
     */
    default byte[] getFileBytes(String url, Map<String, String> headers) {
        HttpResponse<String> response = execute(HttpRequest.get(url).headers(headers));
        return response.isSuccessful() ? response.getRawBody().getBytes() : null;
    }

    /**
     * 异步上传文件
     * 
     * @param url 上传URL
     * @param fieldName 字段名
     * @param file 要上传的文件
     * @return CompletableFuture包装的HTTP响应对象
     */
    default CompletableFuture<HttpResponse<String>> uploadFileAsync(String url, String fieldName, File file) {
        return executeAsync(HttpRequest.post(url).multipartFile(fieldName, file));
    }

    /**
     * 异步上传文件，带进度回调
     * 
     * @param url 上传URL
     * @param fieldName 字段名
     * @param file 要上传的文件
     * @param progressCallback 进度回调
     * @return CompletableFuture包装的HTTP响应对象
     */
    default CompletableFuture<HttpResponse<String>> uploadFileAsync(String url, String fieldName, File file, FileProgressCallback progressCallback) {
        return executeAsync(HttpRequest.post(url).multipartFile(fieldName, file).progressCallback(progressCallback));
    }

    /**
     * 异步下载文件
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @return CompletableFuture包装的下载结果
     */
    CompletableFuture<Boolean> downloadFileAsync(String url, String savePath);

    /**
     * 异步下载文件，带进度回调
     * 
     * @param url 下载URL
     * @param savePath 保存路径
     * @param progressCallback 进度回调
     * @return CompletableFuture包装的下载结果
     */
    CompletableFuture<Boolean> downloadFileAsync(String url, String savePath, FileProgressCallback progressCallback);

    /**
     * 关闭HTTP客户端，释放资源
     */
    void close();
} 