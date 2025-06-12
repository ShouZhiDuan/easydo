package com.techzhi.common.httpclient.demo;

import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.impl.OkHttpClientImpl;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件操作演示类
 * 展示HTTP客户端的文件上传、下载等功能
 */
public class FileOperationDemo {

    public static void main(String[] args) {
        // 创建HTTP客户端
        HttpClientProperties properties = new HttpClientProperties();
        HttpClient httpClient = new OkHttpClientImpl(properties);

        System.out.println("=== TechZhi HTTP Client 文件操作演示 ===\n");

        try {
            // 1. 演示文件上传
            demonstrateFileUpload(httpClient);

            // 2. 演示文件下载
            demonstrateFileDownload(httpClient);

            // 3. 演示获取文件流
            demonstrateFileStream(httpClient);

            // 4. 演示多文件上传
            demonstrateMultipleFileUpload(httpClient);

        } catch (Exception e) {
            System.err.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭客户端
            httpClient.close();
        }

        System.out.println("\n=== 文件操作演示完成 ===");
    }

    private static void demonstrateFileUpload(HttpClient httpClient) {
        System.out.println("1. 文件上传演示");
        System.out.println("---------------");

        try {
            // 创建测试文件
            File testFile = new File("test-file.txt");
            if (!testFile.exists()) {
                System.out.println("测试文件不存在，跳过文件上传演示");
                return;
            }

            // 带进度回调的文件上传
            FileProgressCallback progressCallback = new FileProgressCallback() {
                @Override
                public void onStart() {
                    System.out.println("开始上传文件...");
                }

                @Override
                public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                    System.out.printf("上传进度: %d/%d bytes (%.2f%%)\n", 
                        bytesTransferred, totalBytes, percentage);
                }

                @Override
                public void onComplete(long totalBytes) {
                    System.out.println("文件上传完成！总共上传: " + totalBytes + " bytes");
                }

                @Override
                public void onError(Exception exception) {
                    System.err.println("文件上传失败: " + exception.getMessage());
                }
            };

            HttpResponse<String> response = httpClient.uploadFile(
                "https://httpbin.org/post",
                "file",
                testFile,
                progressCallback
            );

            System.out.println("上传响应状态码: " + response.getStatusCode());
            if (response.isSuccessful()) {
                System.out.println("✅ 文件上传成功");
            } else {
                System.out.println("❌ 文件上传失败");
            }

        } catch (Exception e) {
            System.err.println("文件上传演示失败: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateFileDownload(HttpClient httpClient) {
        System.out.println("2. 文件下载演示");
        System.out.println("---------------");

        try {
            FileProgressCallback downloadCallback = new FileProgressCallback() {
                @Override
                public void onStart() {
                    System.out.println("开始下载文件...");
                }

                @Override
                public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                    if (percentage >= 0) {
                        System.out.printf("下载进度: %d/%d bytes (%.2f%%)\n", 
                            bytesTransferred, totalBytes, percentage);
                    } else {
                        System.out.printf("已下载: %d bytes\n", bytesTransferred);
                    }
                }

                @Override
                public void onComplete(long totalBytes) {
                    System.out.println("文件下载完成！总共下载: " + totalBytes + " bytes");
                }

                @Override
                public void onError(Exception exception) {
                    System.err.println("文件下载失败: " + exception.getMessage());
                }
            };

            boolean success = httpClient.downloadFile(
                "https://httpbin.org/json",
                "downloaded-sample.json",
                downloadCallback
            );

            if (success) {
                System.out.println("✅ 文件下载成功，保存为: downloaded-sample.json");
            } else {
                System.out.println("❌ 文件下载失败");
            }

        } catch (Exception e) {
            System.err.println("文件下载演示失败: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateFileStream(HttpClient httpClient) {
        System.out.println("3. 文件流获取演示");
        System.out.println("----------------");

        try {
            // 获取文件字节数组
            byte[] fileBytes = httpClient.getFileBytes("https://httpbin.org/uuid");
            if (fileBytes != null) {
                String content = new String(fileBytes);
                System.out.println("获取文件字节数组成功，内容: " + content.trim());
                System.out.println("文件大小: " + fileBytes.length + " bytes");
            }

            // 获取文件流
            try (InputStream fileStream = httpClient.getFileStream("https://httpbin.org/uuid")) {
                if (fileStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = fileStream.read(buffer);
                    if (bytesRead > 0) {
                        String streamContent = new String(buffer, 0, bytesRead);
                        System.out.println("从文件流读取的内容: " + streamContent.trim());
                        System.out.println("✅ 文件流获取成功");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("文件流获取演示失败: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateMultipleFileUpload(HttpClient httpClient) {
        System.out.println("4. 多文件上传演示");
        System.out.println("----------------");

        try {
            // 创建多个虚拟文件
            List<MultipartFile> files = Arrays.asList(
                MultipartFile.of("file1", "document1.txt", "这是第一个文档的内容".getBytes()),
                MultipartFile.of("file2", "document2.txt", "这是第二个文档的内容".getBytes()),
                MultipartFile.of("config", "config.json", "{\"setting\":\"value\"}".getBytes(), "application/json")
            );

            // 添加表单字段
            Map<String, String> formFields = new HashMap<>();
            formFields.put("title", "批量文档上传");
            formFields.put("description", "演示多文件上传功能");
            formFields.put("category", "demo");

            HttpResponse<String> response = httpClient.uploadFilesWithForm(
                "https://httpbin.org/post",
                files,
                formFields
            );

            System.out.println("多文件上传响应状态码: " + response.getStatusCode());
            if (response.isSuccessful()) {
                System.out.println("✅ 多文件上传成功");
                System.out.println("上传的文件数量: " + files.size());
                System.out.println("表单字段数量: " + formFields.size());
            } else {
                System.out.println("❌ 多文件上传失败");
            }

        } catch (Exception e) {
            System.err.println("多文件上传演示失败: " + e.getMessage());
        }

        System.out.println();
    }
} 