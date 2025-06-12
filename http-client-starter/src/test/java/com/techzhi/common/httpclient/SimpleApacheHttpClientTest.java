package com.techzhi.common.httpclient;

import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.impl.SimpleApacheHttpClientImpl;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化Apache HttpClient 5实现的测试类
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleApacheHttpClientTest {

    private SimpleApacheHttpClientImpl httpClient;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        HttpClientProperties properties = new HttpClientProperties();
        httpClient = new SimpleApacheHttpClientImpl(properties);
    }

    @AfterEach
    void tearDown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    @Test
    void testBasicGetRequest() {
        // 测试基本的GET请求
        HttpRequest request = HttpRequest.get("https://httpbin.org/get")
                .header("User-Agent", "SimpleApache-HttpClient-Test");
        
        HttpResponse<String> response = httpClient.execute(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("httpbin.org"));
        System.out.println("SimpleApache GET test passed: " + response.getStatusCode());
    }

    @Test
    void testPostRequestWithJson() throws Exception {
        // 测试JSON POST请求
        Map<String, Object> data = new HashMap<>();
        data.put("name", "SimpleApache HttpClient Test");
        data.put("version", "5.x");
        
        HttpRequest request = HttpRequest.post("https://httpbin.org/post")
                .contentType("application/json")
                .body(data);
        
        HttpResponse<String> response = httpClient.execute(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("SimpleApache HttpClient Test"));
        System.out.println("SimpleApache POST test passed: " + response.getStatusCode());
    }

    @Test
    void testAsyncRequest() throws Exception {
        // 测试异步请求
        HttpRequest request = HttpRequest.get("https://httpbin.org/delay/1");
        
        CompletableFuture<HttpResponse<String>> future = httpClient.executeAsync(request);
        HttpResponse<String> response = future.get(10, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("SimpleApache Async test passed: " + response.getStatusCode());
    }

    @Test
    void testDownloadFile() throws IOException, InterruptedException {
        // 测试文件下载
        String url = "https://httpbin.org/json";
        String savePath = tempDir.resolve("test-download.json").toString();
        
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] completed = {false};
        
        FileProgressCallback callback = new FileProgressCallback() {
            @Override
            public void onStart() {
                System.out.println("SimpleApache Download started");
            }
            
            @Override
            public void onProgress(long bytesRead, long totalBytes, double percentage) {
                System.out.printf("SimpleApache Download progress: %d/%d bytes (%.1f%%)%n", 
                    bytesRead, totalBytes, percentage);
            }
            
            @Override
            public void onComplete(long totalBytes) {
                System.out.println("SimpleApache Download completed: " + totalBytes + " bytes");
                completed[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(Exception e) {
                System.err.println("SimpleApache Download error: " + e.getMessage());
                latch.countDown();
            }
        };
        
        boolean result = httpClient.downloadFile(url, savePath, callback);
        latch.await(10, TimeUnit.SECONDS);
        
        assertTrue(result);
        assertTrue(completed[0]);
        
        File downloadedFile = new File(savePath);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.length() > 0);
        
        String content = Files.readString(downloadedFile.toPath());
        assertTrue(content.contains("slideshow"));
        System.out.println("SimpleApache Download test passed");
    }

    @Test
    void testGetFileStream() {
        // 测试获取文件流
        String url = "https://httpbin.org/json";
        
        try (InputStream stream = httpClient.getFileStream(url)) {
            assertNotNull(stream);
            
            byte[] buffer = new byte[1024];
            int bytesRead = stream.read(buffer);
            assertTrue(bytesRead > 0);
            
            String content = new String(buffer, 0, bytesRead);
            assertTrue(content.contains("{"));
            System.out.println("SimpleApache FileStream test passed");
        } catch (Exception e) {
            fail("Failed to get file stream: " + e.getMessage());
        }
    }

    @Test
    void testGetFileBytes() {
        // 测试获取文件字节数组
        String url = "https://httpbin.org/json";
        
        byte[] bytes = httpClient.getFileBytes(url);
        
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        
        String content = new String(bytes);
        assertTrue(content.contains("slideshow"));
        System.out.println("SimpleApache FileBytes test passed: " + bytes.length + " bytes");
    }

    @Test
    void testGetFileBytesWithHeaders() {
        // 测试带请求头的文件字节获取
        String url = "https://httpbin.org/headers";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test-Header", "SimpleApache-Test-Value");
        
        byte[] bytes = httpClient.getFileBytes(url, headers);
        
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        
        String content = new String(bytes);
        assertTrue(content.contains("X-Test-Header"));
        assertTrue(content.contains("SimpleApache-Test-Value"));
        System.out.println("SimpleApache FileBytes with headers test passed");
    }

    @Test
    void testAsyncDownload() throws Exception {
        // 测试异步文件下载
        String url = "https://httpbin.org/json";
        String savePath = tempDir.resolve("test-async-download.json").toString();
        
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] completed = {false};
        
        FileProgressCallback callback = new FileProgressCallback() {
            @Override
            public void onStart() {
                System.out.println("SimpleApache Async download started");
            }
            
            @Override
            public void onProgress(long bytesRead, long totalBytes, double percentage) {
                System.out.printf("SimpleApache Async download progress: %d/%d bytes (%.1f%%)%n", 
                    bytesRead, totalBytes, percentage);
            }
            
            @Override
            public void onComplete(long totalBytes) {
                System.out.println("SimpleApache Async download completed: " + totalBytes + " bytes");
                completed[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(Exception e) {
                System.err.println("SimpleApache Async download error: " + e.getMessage());
                latch.countDown();
            }
        };
        
        CompletableFuture<Boolean> future = httpClient.downloadFileAsync(url, savePath, callback);
        Boolean result = future.get(10, TimeUnit.SECONDS);
        latch.await(10, TimeUnit.SECONDS);
        
        assertTrue(result);
        assertTrue(completed[0]);
        
        File downloadedFile = new File(savePath);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.length() > 0);
        System.out.println("SimpleApache Async download test passed");
    }

    @Test
    void testErrorHandling() {
        // 测试错误处理
        HttpRequest request = HttpRequest.get("https://httpbin.org/status/404");
        
        HttpResponse<String> response = httpClient.execute(request);
        
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        System.out.println("SimpleApache Error handling test passed: " + response.getStatusCode());
    }

    @Test
    void testMultipleRequests() {
        // 测试多个请求以验证连接池
        for (int i = 0; i < 3; i++) {
            HttpRequest request = HttpRequest.get("https://httpbin.org/get?test=" + i);
            HttpResponse<String> response = httpClient.execute(request);
            
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertTrue(response.getBody().contains("test=" + i));
        }
        System.out.println("SimpleApache Multiple requests test passed");
    }
} 