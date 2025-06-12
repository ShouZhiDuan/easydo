package com.techzhi.common.httpclient;

import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.impl.OkHttpClientImpl;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP客户端基本测试
 */
public class HttpClientTest {

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        HttpClientProperties properties = new HttpClientProperties();
        httpClient = new OkHttpClientImpl(properties);
    }

    @Test
    void testGetRequest() {
        HttpResponse<String> response = httpClient.get("https://httpbin.org/get");
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.isSuccessful());
    }

    @Test
    void testPostRequest() {
        String testData = "{\"test\": \"data\"}";
        HttpResponse<String> response = httpClient.post("https://httpbin.org/post", testData);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.isSuccessful());
    }

    @Test
    void testCustomRequest() {
        HttpRequest request = HttpRequest.get("https://httpbin.org/headers")
                .header("User-Agent", "TechZhi-HttpClient-Test")
                .header("Accept", "application/json");

        HttpResponse<String> response = httpClient.execute(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.isSuccessful());
    }

    @Test
    void testErrorResponse() {
        HttpResponse<String> response = httpClient.get("https://httpbin.org/status/404");
        
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertFalse(response.isSuccessful());
        assertTrue(response.isClientError());
    }
} 