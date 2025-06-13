package com.techzhi.sample.service;

import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FileOperationDemoService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileOperationDemoService.class);
    
    @Autowired
    private HttpClient httpClient;
    
    // 文件上传服务地址
    private static final String UPLOAD_URL = "https://httpbin.org/post";
    // 文件下载示例地址
    private static final String DOWNLOAD_URL = "https://httpbin.org/uuid";
    
    /**
     * 场景1: 单个文件上传
     */
    public void singleFileUploadExample() {
        logger.info("=== 场景1: 单个文件上传示例 ===");
        
        try {
            // 创建一个测试文件
            File testFile = createTestFile("test-upload.txt", "这是一个测试上传文件的内容\n文件上传功能测试");
            
            // 上传文件
            HttpResponse<String> response = httpClient.uploadFile(
                UPLOAD_URL, 
                "file", 
                testFile
            );
            
            if (response.isSuccessful()) {
                logger.info("文件上传成功，响应: {}", response.getBody());
            } else {
                logger.error("文件上传失败，状态码: {}", response.getStatusCode());
            }
            
            // 清理测试文件
            testFile.delete();
            
        } catch (Exception e) {
            logger.error("单个文件上传示例失败", e);
        }
    }
    
    /**
     * 场景2: 带进度回调的文件上传
     */
    public void fileUploadWithProgressExample() {
        logger.info("=== 场景2: 带进度回调的文件上传示例 ===");
        
        try {
            // 创建一个较大的测试文件
            File testFile = createTestFile("large-test-upload.txt", generateLargeContent());
            
            // 创建进度回调
            FileProgressCallback progressCallback = new FileProgressCallback() {
                @Override
                public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                    logger.info("上传进度: {}/{} bytes ({}%)", 
                              bytesTransferred, totalBytes, String.format("%.2f", percentage));
                }
                
                @Override
                public void onComplete(long totalBytes) {
                    logger.info("文件上传完成，总共上传: {} bytes", totalBytes);
                }
                
                @Override
                public void onError(Exception exception) {
                    logger.error("文件上传出错", exception);
                }
            };
            
            // 带进度回调的文件上传
            HttpResponse<String> response = httpClient.uploadFile(
                UPLOAD_URL, 
                "file", 
                testFile, 
                progressCallback
            );
            
            if (response.isSuccessful()) {
                logger.info("带进度回调的文件上传成功");
            } else {
                logger.error("带进度回调的文件上传失败，状态码: {}", response.getStatusCode());
            }
            
            // 清理测试文件
            testFile.delete();
            
        } catch (Exception e) {
            logger.error("带进度回调的文件上传示例失败", e);
        }
    }
    
    /**
     * 场景3: 多文件上传
     */
    public void multipleFilesUploadExample() {
        logger.info("=== 场景3: 多文件上传示例 ===");
        
        try {
            // 创建多个测试文件
            File file1 = createTestFile("upload-file1.txt", "第一个上传文件的内容");
            File file2 = createTestFile("upload-file2.txt", "第二个上传文件的内容");
            File file3 = createTestFile("upload-file3.txt", "第三个上传文件的内容");
            
            List<MultipartFile> files = Arrays.asList(
                MultipartFile.of("file1", file1),
                MultipartFile.of("file2", file2),
                MultipartFile.of("file3", file3)
            );
            
            // 上传多个文件
            HttpResponse<String> response = httpClient.uploadFiles(UPLOAD_URL, files);
            
            if (response.isSuccessful()) {
                logger.info("多文件上传成功，响应: {}", response.getBody());
            } else {
                logger.error("多文件上传失败，状态码: {}", response.getStatusCode());
            }
            
            // 清理测试文件
            file1.delete();
            file2.delete();
            file3.delete();
            
        } catch (Exception e) {
            logger.error("多文件上传示例失败", e);
        }
    }
    
    /**
     * 场景4: 文件上传与表单数据结合
     */
    public void fileUploadWithFormExample() {
        logger.info("=== 场景4: 文件上传与表单数据结合示例 ===");
        
        try {
            // 创建测试文件
            File testFile = createTestFile("form-upload.txt", "文件上传与表单数据结合示例");
            
            List<MultipartFile> files = Arrays.asList(
                MultipartFile.of("attachment", testFile)
            );
            
            Map<String, String> formFields = new HashMap<>();
            formFields.put("title", "文档标题");
            formFields.put("description", "文档描述");
            formFields.put("category", "测试文档");
            formFields.put("author", "TechZhi");
            
            // 上传文件和表单数据
            HttpResponse<String> response = httpClient.uploadFilesWithForm(
                UPLOAD_URL, 
                files, 
                formFields
            );
            
            if (response.isSuccessful()) {
                logger.info("文件与表单数据上传成功，响应: {}", response.getBody());
            } else {
                logger.error("文件与表单数据上传失败，状态码: {}", response.getStatusCode());
            }
            
            // 清理测试文件
            testFile.delete();
            
        } catch (Exception e) {
            logger.error("文件上传与表单数据结合示例失败", e);
        }
    }
    
    /**
     * 场景5: 异步文件上传
     */
    public void asyncFileUploadExample() {
        logger.info("=== 场景5: 异步文件上传示例 ===");
        
        try {
            // 创建测试文件
            File testFile = createTestFile("async-upload.txt", "异步文件上传测试内容");
            
            // 异步上传文件
            CompletableFuture<HttpResponse<String>> future = httpClient.uploadFileAsync(
                UPLOAD_URL, 
                "file", 
                testFile
            );
            
            future.thenAccept(response -> {
                if (response.isSuccessful()) {
                    logger.info("异步文件上传成功，响应: {}", response.getBody());
                } else {
                    logger.error("异步文件上传失败，状态码: {}", response.getStatusCode());
                }
                
                // 清理测试文件
                testFile.delete();
            }).exceptionally(throwable -> {
                logger.error("异步文件上传异常", throwable);
                testFile.delete();
                return null;
            });
            
            // 等待异步操作完成（实际应用中不需要）
            Thread.sleep(3000);
            
        } catch (Exception e) {
            logger.error("异步文件上传示例失败", e);
        }
    }
    
    /**
     * 场景6: 文件下载
     */
    public void fileDownloadExample() {
        logger.info("=== 场景6: 文件下载示例 ===");
        
        try {
            String downloadPath = "downloaded-file.json";
            
            // 下载文件
            boolean success = httpClient.downloadFile(DOWNLOAD_URL, downloadPath);
            
            if (success) {
                logger.info("文件下载成功，保存路径: {}", downloadPath);
                
                // 检查文件是否存在
                File downloadedFile = new File(downloadPath);
                if (downloadedFile.exists()) {
                    logger.info("下载文件大小: {} bytes", downloadedFile.length());
                    
                    // 清理下载的文件
                    downloadedFile.delete();
                }
            } else {
                logger.error("文件下载失败");
            }
            
        } catch (Exception e) {
            logger.error("文件下载示例失败", e);
        }
    }
    
    /**
     * 场景7: 带进度回调的文件下载
     */
    public void fileDownloadWithProgressExample() {
        logger.info("=== 场景7: 带进度回调的文件下载示例 ===");
        
        try {
            String downloadPath = "downloaded-with-progress.json";
            
            // 创建进度回调
            FileProgressCallback progressCallback = new FileProgressCallback() {
                @Override
                public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                    logger.info("下载进度: {}/{} bytes ({}%)", 
                              bytesTransferred, totalBytes, String.format("%.2f", percentage));
                }
                
                @Override
                public void onComplete(long totalBytes) {
                    logger.info("文件下载完成，总共下载: {} bytes", totalBytes);
                }
                
                @Override
                public void onError(Exception exception) {
                    logger.error("文件下载出错", exception);
                }
            };
            
            // 带进度回调的文件下载
            boolean success = httpClient.downloadFile(DOWNLOAD_URL, downloadPath, progressCallback);
            
            if (success) {
                logger.info("带进度回调的文件下载成功");
                
                // 清理下载的文件
                File downloadedFile = new File(downloadPath);
                if (downloadedFile.exists()) {
                    downloadedFile.delete();
                }
            } else {
                logger.error("带进度回调的文件下载失败");
            }
            
        } catch (Exception e) {
            logger.error("带进度回调的文件下载示例失败", e);
        }
    }
    
    /**
     * 场景8: 异步文件下载
     */
    public void asyncFileDownloadExample() {
        logger.info("=== 场景8: 异步文件下载示例 ===");
        
        try {
            String downloadPath = "async-downloaded.json";
            
            // 异步下载文件
            CompletableFuture<Boolean> future = httpClient.downloadFileAsync(DOWNLOAD_URL, downloadPath);
            
            future.thenAccept(success -> {
                if (success) {
                    logger.info("异步文件下载成功，保存路径: {}", downloadPath);
                    
                    // 清理下载的文件
                    File downloadedFile = new File(downloadPath);
                    if (downloadedFile.exists()) {
                        downloadedFile.delete();
                    }
                } else {
                    logger.error("异步文件下载失败");
                }
            }).exceptionally(throwable -> {
                logger.error("异步文件下载异常", throwable);
                return null;
            });
            
            // 等待异步操作完成（实际应用中不需要）
            Thread.sleep(3000);
            
        } catch (Exception e) {
            logger.error("异步文件下载示例失败", e);
        }
    }
    
    /**
     * 运行所有文件操作示例
     */
    public void runAllFileOperationExamples() {
        logger.info("开始运行所有文件操作示例...");
        
        singleFileUploadExample();
        fileUploadWithProgressExample();
        multipleFilesUploadExample();
        fileUploadWithFormExample();
        asyncFileUploadExample();
        fileDownloadExample();
        fileDownloadWithProgressExample();
        asyncFileDownloadExample();
        
        logger.info("所有文件操作示例运行完成！");
    }
    
    /**
     * 创建测试文件
     */
    private File createTestFile(String filename, String content) throws IOException {
        File file = new File(filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
    
    /**
     * 生成较大的文件内容用于测试进度回调
     */
    private String generateLargeContent() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content.append("这是第").append(i + 1).append("行内容，用于测试文件上传进度回调功能。\n");
        }
        return content.toString();
    }
} 