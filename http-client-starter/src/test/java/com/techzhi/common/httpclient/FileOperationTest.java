package com.techzhi.common.httpclient;

import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.impl.OkHttpClientImpl;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件操作测试
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class FileOperationTest {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationTest.class);

    private HttpClient httpClient;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        HttpClientProperties properties = new HttpClientProperties();
        httpClient = new OkHttpClientImpl(properties);
        
        // 创建临时目录
        tempDir = Files.createTempDirectory("http-client-test");
        logger.info("Test temp directory: {}", tempDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
        
        // 清理临时目录
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((path1, path2) -> path2.compareTo(path1)) // 先删除文件再删除目录
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete temp file: {}", path, e);
                    }
                });
        }
    }

    @Test
    public void testSingleFileUpload() throws IOException {
        // 创建测试文件
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "Hello, World!".getBytes());

        // 测试文件上传
        HttpResponse<String> response = httpClient.uploadFile(
            "https://httpbin.org/post", 
            "file", 
            testFile.toFile()
        );
        
        logger.info("Upload response status: {}", response.getStatusCode());
        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
    }

    @Test
    public void testFileUploadWithProgress() throws IOException, InterruptedException {
        // 创建测试文件
        Path testFile = tempDir.resolve("progress_test.txt");
        String content = "This is a test file for progress tracking.";
        Files.write(testFile, content.getBytes());

        CountDownLatch latch = new CountDownLatch(1);
        FileProgressCallback progressCallback = new FileProgressCallback() {
            @Override
            public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                logger.info("Upload progress: {}/{} bytes ({}%)", bytesTransferred, totalBytes, percentage);
            }

            @Override
            public void onComplete(long totalBytes) {
                logger.info("Upload completed: {} bytes", totalBytes);
                latch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                logger.error("Upload failed", exception);
                latch.countDown();
            }
        };

        HttpResponse<String> response = httpClient.uploadFile(
            "https://httpbin.org/post", 
            "file", 
            testFile.toFile(), 
            progressCallback
        );
        
        // 等待进度回调完成
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
    }

    @Test
    public void testMultipleFileUpload() throws IOException {
        // 创建多个测试文件
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.write(file1, "Content of file 1".getBytes());
        Files.write(file2, "Content of file 2".getBytes());

        List<MultipartFile> files = Arrays.asList(
            MultipartFile.of("file1", file1.toFile()),
            MultipartFile.of("file2", file2.toFile()),
            MultipartFile.of("data", "inline.txt", "Inline content".getBytes())
        );

        HttpResponse<String> response = httpClient.uploadFiles("https://httpbin.org/post", files);
        
        logger.info("Multiple files upload response status: {}", response.getStatusCode());
        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
    }

    @Test
    public void testFileUploadWithForm() throws IOException {
        // 创建测试文件
        Path testFile = tempDir.resolve("form_test.txt");
        Files.write(testFile, "File with form data".getBytes());

        List<MultipartFile> files = Arrays.asList(
            MultipartFile.of("document", testFile.toFile())
        );

        Map<String, String> formFields = new HashMap<>();
        formFields.put("title", "Test Document");
        formFields.put("description", "A test document for form upload");
        formFields.put("category", "test");

        HttpResponse<String> response = httpClient.uploadFilesWithForm(
            "https://httpbin.org/post", 
            files, 
            formFields
        );
        
        logger.info("File with form upload response status: {}", response.getStatusCode());
        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
    }

    @Test
    public void testFileDownload() {
        Path downloadPath = tempDir.resolve("downloaded.json");
        
        boolean success = httpClient.downloadFile(
            "https://httpbin.org/json", 
            downloadPath.toString()
        );
        
        logger.info("File download result: {}", success);
        assertTrue(success);
        assertTrue(Files.exists(downloadPath));
        
        try {
            assertTrue(Files.size(downloadPath) > 0);
        } catch (IOException e) {
            fail("Failed to check downloaded file size", e);
        }
    }

    @Test
    public void testFileDownloadWithProgress() throws InterruptedException {
        Path downloadPath = tempDir.resolve("progress_download.json");
        CountDownLatch latch = new CountDownLatch(1);
        
        FileProgressCallback progressCallback = new FileProgressCallback() {
            @Override
            public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
                logger.info("Download progress: {}/{} bytes", bytesTransferred, totalBytes);
            }

            @Override
            public void onComplete(long totalBytes) {
                logger.info("Download completed: {} bytes", totalBytes);
                latch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                logger.error("Download failed", exception);
                latch.countDown();
            }
        };
        
        boolean success = httpClient.downloadFile(
            "https://httpbin.org/json", 
            downloadPath.toString(), 
            progressCallback
        );
        
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertTrue(success);
        assertTrue(Files.exists(downloadPath));
    }

    @Test
    public void testFileDownloadWithHeaders() {
        Path downloadPath = tempDir.resolve("headers_download.json");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "TechZhi-HttpClient-Test/1.0");
        headers.put("Accept", "application/json");
        
        boolean success = httpClient.downloadFile(
            "https://httpbin.org/json", 
            downloadPath.toString(), 
            headers
        );
        
        logger.info("File download with headers result: {}", success);
        assertTrue(success);
        assertTrue(Files.exists(downloadPath));
    }

    @Test
    public void testGetFileStream() {
        try (InputStream inputStream = httpClient.getFileStream("https://httpbin.org/uuid")) {
            assertNotNull(inputStream);
            
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            assertTrue(bytesRead > 0);
            
            String content = new String(buffer, 0, bytesRead);
            logger.info("File stream content: {}", content);
            assertNotNull(content);
            assertTrue(content.length() > 0);
            
        } catch (IOException e) {
            fail("Failed to read file stream", e);
        }
    }

    @Test
    public void testGetFileBytes() {
        byte[] fileBytes = httpClient.getFileBytes("https://httpbin.org/uuid");
        
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 0);
        
        String content = new String(fileBytes);
        logger.info("File bytes content: {}", content);
        assertNotNull(content);
        assertTrue(content.length() > 0);
    }

    @Test
    public void testGetFileBytesWithHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        
        byte[] fileBytes = httpClient.getFileBytes("https://httpbin.org/json", headers);
        
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 0);
        
        String content = new String(fileBytes);
        logger.info("File bytes with headers content length: {}", content.length());
        assertTrue(content.contains("\""));  // JSON content should contain quotes
    }

    @Test
    public void testMultipartFileCreation() {
        // 测试从File创建
        File testFile = new File("test.txt");
        MultipartFile multipartFile1 = MultipartFile.of("file", testFile);
        assertEquals("file", multipartFile1.getFieldName());
        assertEquals("test.txt", multipartFile1.getFileName());
        assertNotNull(multipartFile1.getContentType());

        // 测试从字节数组创建
        byte[] content = "Test content".getBytes();
        MultipartFile multipartFile2 = MultipartFile.of("data", "test.txt", content);
        assertEquals("data", multipartFile2.getFieldName());
        assertEquals("test.txt", multipartFile2.getFileName());
        assertEquals(content.length, multipartFile2.getContentLength());

        // 测试自定义Content-Type
        MultipartFile multipartFile3 = MultipartFile.of("json", "data.json", content, "application/json");
        assertEquals("application/json", multipartFile3.getContentType());
    }
} 