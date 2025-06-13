package com.techzhi.sample.controller;

import com.techzhi.sample.service.FileOperationDemoService;
import com.techzhi.sample.service.HttpClientDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    
    @Autowired
    private HttpClientDemoService httpClientDemoService;
    
    @Autowired
    private FileOperationDemoService fileOperationDemoService;
    
    /**
     * 运行所有HTTP客户端示例
     */
    @PostMapping("/http-client/all")
    public Map<String, String> runAllHttpClientExamples() {
        try {
            httpClientDemoService.runAllExamples();
            return Collections.singletonMap("message", "所有HTTP客户端示例运行完成，请查看日志");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行HTTP客户端示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行基本GET请求示例
     */
    @PostMapping("/http-client/basic-get")
    public Map<String, String> runBasicGetExample() {
        try {
            httpClientDemoService.basicGetExample();
            return Collections.singletonMap("message", "基本GET请求示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行基本GET请求示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行类型转换GET请求示例
     */
    @PostMapping("/http-client/type-conversion")
    public Map<String, String> runTypeConversionExample() {
        try {
            httpClientDemoService.getWithTypeConversionExample();
            return Collections.singletonMap("message", "类型转换GET请求示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行类型转换示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行POST请求示例
     */
    @PostMapping("/http-client/post")
    public Map<String, String> runPostExample() {
        try {
            httpClientDemoService.postExample();
            return Collections.singletonMap("message", "POST请求示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行POST请求示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行自定义请求示例
     */
    @PostMapping("/http-client/custom-request")
    public Map<String, String> runCustomRequestExample() {
        try {
            httpClientDemoService.customRequestExample();
            return Collections.singletonMap("message", "自定义请求示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行自定义请求示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行异步请求示例
     */
    @PostMapping("/http-client/async")
    public Map<String, String> runAsyncRequestExample() {
        try {
            httpClientDemoService.asyncRequestExample();
            return Collections.singletonMap("message", "异步请求示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行异步请求示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行表单提交示例
     */
    @PostMapping("/http-client/form-submit")
    public Map<String, String> runFormSubmitExample() {
        try {
            httpClientDemoService.formSubmitExample();
            return Collections.singletonMap("message", "表单提交示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行表单提交示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行HttpClientTemplate示例
     */
    @PostMapping("/http-client/template")
    public Map<String, String> runHttpClientTemplateExample() {
        try {
            httpClientDemoService.httpClientTemplateExample();
            return Collections.singletonMap("message", "HttpClientTemplate示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行HttpClientTemplate示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行错误处理示例
     */
    @PostMapping("/http-client/error-handling")
    public Map<String, String> runErrorHandlingExample() {
        try {
            httpClientDemoService.errorHandlingExample();
            return Collections.singletonMap("message", "错误处理示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行错误处理示例失败: " + e.getMessage());
        }
    }
    
    // =============== 文件操作示例接口 ===============
    
    /**
     * 运行所有文件操作示例
     */
    @PostMapping("/file-operations/all")
    public Map<String, String> runAllFileOperationExamples() {
        try {
            fileOperationDemoService.runAllFileOperationExamples();
            return Collections.singletonMap("message", "所有文件操作示例运行完成，请查看日志");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行文件操作示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行单个文件上传示例
     */
    @PostMapping("/file-operations/single-upload")
    public Map<String, String> runSingleFileUploadExample() {
        try {
            fileOperationDemoService.singleFileUploadExample();
            return Collections.singletonMap("message", "单个文件上传示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行单个文件上传示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行带进度回调的文件上传示例
     */
    @PostMapping("/file-operations/upload-with-progress")
    public Map<String, String> runFileUploadWithProgressExample() {
        try {
            fileOperationDemoService.fileUploadWithProgressExample();
            return Collections.singletonMap("message", "带进度回调的文件上传示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行带进度回调的文件上传示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行多文件上传示例
     */
    @PostMapping("/file-operations/multiple-upload")
    public Map<String, String> runMultipleFilesUploadExample() {
        try {
            fileOperationDemoService.multipleFilesUploadExample();
            return Collections.singletonMap("message", "多文件上传示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行多文件上传示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行文件上传与表单数据结合示例
     */
    @PostMapping("/file-operations/upload-with-form")
    public Map<String, String> runFileUploadWithFormExample() {
        try {
            fileOperationDemoService.fileUploadWithFormExample();
            return Collections.singletonMap("message", "文件上传与表单数据结合示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行文件上传与表单数据结合示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行异步文件上传示例
     */
    @PostMapping("/file-operations/async-upload")
    public Map<String, String> runAsyncFileUploadExample() {
        try {
            fileOperationDemoService.asyncFileUploadExample();
            return Collections.singletonMap("message", "异步文件上传示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行异步文件上传示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行文件下载示例
     */
    @PostMapping("/file-operations/download")
    public Map<String, String> runFileDownloadExample() {
        try {
            fileOperationDemoService.fileDownloadExample();
            return Collections.singletonMap("message", "文件下载示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行文件下载示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行带进度回调的文件下载示例
     */
    @PostMapping("/file-operations/download-with-progress")
    public Map<String, String> runFileDownloadWithProgressExample() {
        try {
            fileOperationDemoService.fileDownloadWithProgressExample();
            return Collections.singletonMap("message", "带进度回调的文件下载示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行带进度回调的文件下载示例失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行异步文件下载示例
     */
    @PostMapping("/file-operations/async-download")
    public Map<String, String> runAsyncFileDownloadExample() {
        try {
            fileOperationDemoService.asyncFileDownloadExample();
            return Collections.singletonMap("message", "异步文件下载示例运行完成");
        } catch (Exception e) {
            return Collections.singletonMap("error", "运行异步文件下载示例失败: " + e.getMessage());
        }
    }
    
    // =============== 信息接口 ===============
    
    /**
     * 获取可用的示例接口列表
     */
    @GetMapping("/endpoints")
    public Map<String, Object> getAvailableEndpoints() {
        return Map.of(
            "http_client_examples", Map.of(
                "all", "POST /api/demo/http-client/all - 运行所有HTTP客户端示例",
                "basic_get", "POST /api/demo/http-client/basic-get - 基本GET请求示例",
                "type_conversion", "POST /api/demo/http-client/type-conversion - 类型转换GET请求示例",
                "post", "POST /api/demo/http-client/post - POST请求示例",
                "custom_request", "POST /api/demo/http-client/custom-request - 自定义请求示例",
                "async", "POST /api/demo/http-client/async - 异步请求示例",
                "form_submit", "POST /api/demo/http-client/form-submit - 表单提交示例",
                "template", "POST /api/demo/http-client/template - HttpClientTemplate示例",
                "error_handling", "POST /api/demo/http-client/error-handling - 错误处理示例"
            ),
            "file_operations_examples", Map.of(
                "all", "POST /api/demo/file-operations/all - 运行所有文件操作示例",
                "single_upload", "POST /api/demo/file-operations/single-upload - 单个文件上传示例",
                "upload_with_progress", "POST /api/demo/file-operations/upload-with-progress - 带进度回调的文件上传示例",
                "multiple_upload", "POST /api/demo/file-operations/multiple-upload - 多文件上传示例",
                "upload_with_form", "POST /api/demo/file-operations/upload-with-form - 文件上传与表单数据结合示例",
                "async_upload", "POST /api/demo/file-operations/async-upload - 异步文件上传示例",
                "download", "POST /api/demo/file-operations/download - 文件下载示例",
                "download_with_progress", "POST /api/demo/file-operations/download-with-progress - 带进度回调的文件下载示例",
                "async_download", "POST /api/demo/file-operations/async-download - 异步文件下载示例"
            ),
            "info", Map.of(
                "endpoints", "GET /api/demo/endpoints - 获取可用的示例接口列表"
            )
        );
    }
} 