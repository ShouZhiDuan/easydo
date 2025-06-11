package com.example.httpclient.service.impl;

import com.example.httpclient.config.HttpClientProperties;
import com.example.httpclient.service.HttpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OkHttpHttpClientServiceTest {

    private MockWebServer mockWebServer;
    private HttpClientService httpClientService;
    private HttpClientProperties properties;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        properties = new HttpClientProperties();
        // Increase timeouts for test stability
        properties.setConnectTimeout(5000); // 5 seconds
        properties.setReadTimeout(5000);    // 5 seconds
        properties.setWriteTimeout(5000);   // 5 seconds

        objectMapper = new ObjectMapper();
        httpClientService = new OkHttpHttpClientService(properties, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void get_shouldReturnResponseBody() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("Hello, world!").setResponseCode(200));

        String url = mockWebServer.url("/test-get").toString();
        String response = httpClientService.get(url, null, null);

        assertEquals("Hello, world!", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test-get", recordedRequest.getPath());
    }

    @Test
    void get_withHeadersAndQueryParams() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("Params and Headers OK").setResponseCode(200));

        String url = mockWebServer.url("/test-get-advanced").toString();
        Map<String, String> headers = Collections.singletonMap("X-Test-Header", "TestValue");
        Map<String, String> queryParams = Collections.singletonMap("param1", "value1");

        String response = httpClientService.get(url, headers, queryParams);

        assertEquals("Params and Headers OK", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test-get-advanced?param1=value1", recordedRequest.getPath());
        assertEquals("TestValue", recordedRequest.getHeader("X-Test-Header"));
    }

    @Test
    void postJson_shouldReturnResponseBody() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("POST Success").setResponseCode(200));

        String url = mockWebServer.url("/test-post-json").toString();
        String jsonBody = "{\"key\":\"value\"}";
        String response = httpClientService.postJson(url, null, jsonBody);

        assertEquals("POST Success", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
        assertEquals(jsonBody, recordedRequest.getBody().readUtf8());
    }

    @Test
    void postForm_shouldReturnResponseBody() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("Form POST Success").setResponseCode(200));

        String url = mockWebServer.url("/test-post-form").toString();
        Map<String, String> formData = new HashMap<>();
        formData.put("field1", "data1");
        formData.put("field2", "data2");

        String response = httpClientService.postForm(url, null, formData);

        assertEquals("Form POST Success", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/x-www-form-urlencoded", recordedRequest.getHeader("Content-Type"));
        assertEquals("field1=data1&field2=data2", recordedRequest.getBody().readUtf8());
    }

    @Test
    void putJson_shouldReturnResponseBody() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("PUT Success").setResponseCode(200));

        String url = mockWebServer.url("/test-put-json").toString();
        String jsonBody = "{\"updateKey\":\"updateValue\"}";
        String response = httpClientService.putJson(url, null, jsonBody);

        assertEquals("PUT Success", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals(jsonBody, recordedRequest.getBody().readUtf8());
    }

    @Test
    void delete_shouldReturnResponseBody() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("DELETE Success").setResponseCode(200));

        String url = mockWebServer.url("/test-delete").toString();
        String response = httpClientService.delete(url, null);

        assertEquals("DELETE Success", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void uploadFile_shouldUploadFileAndReturnResponse() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody("File Uploaded").setResponseCode(200));

        File tempFile = Files.createFile(tempDir.resolve("testupload.txt")).toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Test content".getBytes(StandardCharsets.UTF_8));
        }

        String url = mockWebServer.url("/upload").toString();
        Map<String, String> otherParams = Collections.singletonMap("description", "A test file");

        String response = httpClientService.uploadFile(url, null, "file", tempFile, otherParams);

        assertEquals("File Uploaded", response);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertTrue(recordedRequest.getHeader("Content-Type").startsWith("multipart/form-data"));
        // Further assertions can be made on the multipart body parts if needed
        String body = recordedRequest.getBody().readUtf8();
        assertTrue(body.contains("Content-Disposition: form-data; name=\"file\"; filename=\"testupload.txt\""));
        assertTrue(body.contains("Test content"));
        assertTrue(body.contains("Content-Disposition: form-data; name=\"description\""));
        assertTrue(body.contains("A test file"));
    }

    @Test
    void downloadFile_shouldReturnInputStream() throws IOException, InterruptedException {
        String fileContent = "This is the content of the downloadable file.";
        mockWebServer.enqueue(new MockResponse()
                .setBody(fileContent)
                .setResponseCode(200)
                .addHeader("Content-Type", "text/plain"));

        String url = mockWebServer.url("/download").toString();
        try (InputStream inputStream = httpClientService.downloadFile(url, null)) {
            String downloadedContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            assertEquals(fileContent, downloadedContent);
        }

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void downloadFile_whenHttpError_shouldThrowIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404).setBody("Not Found"));
        String url = mockWebServer.url("/download-fail").toString();

        IOException exception = assertThrows(IOException.class, () -> {
            try (InputStream ignored = httpClientService.downloadFile(url, null)){
                // should not reach here
            }
        });
        System.out.println("DEBUG TEST - downloadFile_whenHttpError_shouldThrowIOException - Exception message: '" + exception.getMessage() + "'");
        assertTrue(exception.getMessage().contains("HTTP request failed with code 404"));
        assertTrue(exception.getMessage().contains(": Not Found")); // Adjusted to match the actual message format
    }

    @Test
    void execute_whenHttpError_shouldThrowIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));
        String url = mockWebServer.url("/error").toString();

        IOException exception = assertThrows(IOException.class, () -> {
            httpClientService.get(url, null, null);
        });
        assertTrue(exception.getMessage().contains("HTTP request failed with code 500"));
        assertTrue(exception.getMessage().contains("Server Error"));
    }

    @Test
    void bypassSslValidation_shouldWorkIfConfigured() throws IOException {
        // This test is more conceptual as MockWebServer doesn't inherently test SSL bypass
        // in the same way a real HTTPS server would. We're testing the configuration path.
        HttpClientProperties sslProps = new HttpClientProperties();
        sslProps.setBypassSslValidation(true);
        HttpClientService sslBypassService = new OkHttpHttpClientService(sslProps, objectMapper);

        // To truly test this, you'd need a self-signed cert server or similar setup.
        // For now, we just ensure no exceptions during client creation with bypass enabled.
        assertNotNull(sslBypassService);

        // Example of a call (would fail if SSL issues weren't bypassed and it was a real HTTPS endpoint)
        mockWebServer.enqueue(new MockResponse().setBody("SSL Bypassed").setResponseCode(200));
        String url = mockWebServer.url("/ssl-test").toString(); // MockWebServer is HTTP, not HTTPS
        String response = sslBypassService.get(url, null, null);
        assertEquals("SSL Bypassed", response);
    }

     @Test
    void determineMediaType_shouldReturnCorrectTypes() {
        OkHttpHttpClientService service = (OkHttpHttpClientService) httpClientService; // Cast to access method
        assertEquals("image/png", service.determineMediaType("image.png"));
        assertEquals("image/jpeg", service.determineMediaType("image.jpg"));
        assertEquals("image/jpeg", service.determineMediaType("image.jpeg"));
        assertEquals("image/gif", service.determineMediaType("image.gif"));
        assertEquals("application/pdf", service.determineMediaType("document.pdf"));
        assertEquals("text/plain", service.determineMediaType("notes.txt"));
        assertEquals("application/json", service.determineMediaType("data.json"));
        assertEquals("application/xml", service.determineMediaType("config.xml"));
        assertEquals("application/octet-stream", service.determineMediaType("archive.zip"));
        assertEquals("application/octet-stream", service.determineMediaType("unknown.xyz"));
    }
}