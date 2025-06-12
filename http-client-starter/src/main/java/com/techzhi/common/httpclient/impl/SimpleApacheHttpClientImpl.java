package com.techzhi.common.httpclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 简化的Apache HttpClient 5实现
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class SimpleApacheHttpClientImpl implements HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleApacheHttpClientImpl.class);

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SimpleApacheHttpClientImpl(HttpClientProperties properties) {
        this.objectMapper = createObjectMapper();
        this.httpClient = createHttpClient(properties);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private CloseableHttpClient createHttpClient(HttpClientProperties properties) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(properties.getPool().getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(properties.getPool().getDefaultMaxPerRoute());

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

    @Override
    public HttpResponse<String> execute(HttpRequest request) {
        return execute(request, String.class);
    }

    @Override
    public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
        Instant startTime = Instant.now();
        
        try {
            ClassicHttpRequest httpRequest = createApacheHttpRequest(request);
            ClassicHttpResponse response = httpClient.execute(httpRequest);
            
            return processResponse(response, responseType, startTime);
            
        } catch (Exception e) {
            logger.error("HTTP request failed", e);
            return new HttpResponse<T>()
                    .statusCode(0)
                    .exception(e)
                    .responseTime(Duration.between(startTime, Instant.now()));
        }
    }

    @Override
    public CompletableFuture<HttpResponse<String>> executeAsync(HttpRequest request) {
        return executeAsync(request, String.class);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpRequest request, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> execute(request, responseType));
    }

    private ClassicHttpRequest createApacheHttpRequest(HttpRequest request) throws IOException {
        String url = buildUrl(request);
        ClassicHttpRequest httpRequest;

        // 创建请求
        switch (request.getMethod()) {
            case GET:
                httpRequest = new HttpGet(url);
                break;
            case POST:
                httpRequest = new HttpPost(url);
                break;
            case PUT:
                httpRequest = new HttpPut(url);
                break;
            case DELETE:
                httpRequest = new HttpDelete(url);
                break;
            case PATCH:
                httpRequest = new HttpPatch(url);
                break;
            case HEAD:
                httpRequest = new HttpHead(url);
                break;
            case OPTIONS:
                httpRequest = new HttpOptions(url);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported HTTP method: " + request.getMethod());
        }

        // 设置请求头
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            httpRequest.addHeader(header.getKey(), header.getValue());
        }

        // 设置请求体（仅支持简单的body，不支持multipart）
        if (httpRequest instanceof HttpPost || httpRequest instanceof HttpPut || httpRequest instanceof HttpPatch) {
            HttpUriRequestBase entityRequest = (HttpUriRequestBase) httpRequest;
            
            if (request.getBody() != null) {
                String bodyString = serializeBody(request);
                StringEntity entity = new StringEntity(bodyString, ContentType.parse(request.getContentType()));
                entityRequest.setEntity(entity);
            }
        }

        return httpRequest;
    }

    private String buildUrl(HttpRequest request) {
        StringBuilder urlBuilder = new StringBuilder(request.getUri().toString());
        
        if (!request.getQueryParams().isEmpty()) {
            urlBuilder.append(request.getUri().getQuery() == null ? "?" : "&");
            boolean first = request.getUri().getQuery() == null;
            for (Map.Entry<String, String> param : request.getQueryParams().entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(param.getKey()).append("=").append(param.getValue());
                first = false;
            }
        }
        
        return urlBuilder.toString();
    }

    private String serializeBody(HttpRequest request) throws IOException {
        if (request.getBody() instanceof String) {
            return (String) request.getBody();
        } else if ("application/json".equals(request.getContentType())) {
            return objectMapper.writeValueAsString(request.getBody());
        } else {
            return request.getBody().toString();
        }
    }

    private <T> HttpResponse<T> processResponse(ClassicHttpResponse response, Class<T> responseType, Instant startTime) throws IOException {
        Duration responseTime = Duration.between(startTime, Instant.now());
        
        HttpResponse<T> httpResponse = new HttpResponse<T>()
                .statusCode(response.getCode())
                .reasonPhrase(response.getReasonPhrase())
                .responseTime(responseTime);

        // 处理响应头
        for (Header header : response.getHeaders()) {
            httpResponse.header(header.getName(), header.getValue());
        }

        // 处理响应体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String rawBody = new String(entity.getContent().readAllBytes());
            httpResponse.rawBody(rawBody);

            T body = deserializeBody(rawBody, responseType);
            httpResponse.body(body);
        }

        return httpResponse;
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeBody(String rawBody, Class<T> responseType) throws IOException {
        if (responseType == String.class) {
            return (T) rawBody;
        }

        try {
            return objectMapper.readValue(rawBody, responseType);
        } catch (Exception e) {
            if (responseType == String.class) {
                return (T) rawBody;
            }
            throw new RuntimeException("Failed to deserialize response body", e);
        }
    }

    @Override
    public boolean downloadFile(String url, String savePath) {
        return downloadFile(url, savePath, null, null);
    }

    @Override
    public boolean downloadFile(String url, String savePath, FileProgressCallback progressCallback) {
        return downloadFile(url, savePath, null, progressCallback);
    }

    @Override
    public boolean downloadFile(String url, String savePath, Map<String, String> headers) {
        return downloadFile(url, savePath, headers, null);
    }

    @Override
    public boolean downloadFile(String url, String savePath, Map<String, String> headers, FileProgressCallback progressCallback) {
        try {
            HttpRequest httpRequest = HttpRequest.get(url);
            if (headers != null) {
                httpRequest.headers(headers);
            }
            
            ClassicHttpRequest apacheRequest = createApacheHttpRequest(httpRequest);
            ClassicHttpResponse response = httpClient.execute(apacheRequest);
            
            if (response.getCode() < 200 || response.getCode() >= 300) {
                logger.error("Download failed with status: {}", response.getCode());
                return false;
            }
            
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                logger.error("Response entity is null");
                return false;
            }
            
            Path path = Paths.get(savePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            
            if (progressCallback != null) {
                progressCallback.onStart();
            }
            
            try (InputStream inputStream = entity.getContent();
                 OutputStream outputStream = Files.newOutputStream(path)) {
                
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                long contentLength = entity.getContentLength();
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (progressCallback != null) {
                        double percentage = contentLength > 0 ? (double) totalBytesRead / contentLength * 100 : -1;
                        progressCallback.onProgress(totalBytesRead, contentLength, percentage);
                    }
                }
                
                if (progressCallback != null) {
                    progressCallback.onComplete(totalBytesRead);
                }
                
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Failed to download file from {} to {}", url, savePath, e);
            if (progressCallback != null) {
                progressCallback.onError(e);
            }
            return false;
        }
    }

    @Override
    public InputStream getFileStream(String url) {
        return getFileStream(url, null);
    }

    @Override
    public InputStream getFileStream(String url, Map<String, String> headers) {
        try {
            HttpRequest httpRequest = HttpRequest.get(url);
            if (headers != null) {
                httpRequest.headers(headers);
            }
            
            ClassicHttpRequest apacheRequest = createApacheHttpRequest(httpRequest);
            ClassicHttpResponse response = httpClient.execute(apacheRequest);
            
            if (response.getCode() < 200 || response.getCode() >= 300) {
                logger.error("Failed to get file stream with status: {}", response.getCode());
                return null;
            }
            
            HttpEntity entity = response.getEntity();
            return entity != null ? entity.getContent() : null;
            
        } catch (Exception e) {
            logger.error("Failed to get file stream from {}", url, e);
            return null;
        }
    }

    @Override
    public byte[] getFileBytes(String url) {
        return getFileBytes(url, null);
    }

    @Override
    public byte[] getFileBytes(String url, Map<String, String> headers) {
        try {
            HttpRequest httpRequest = HttpRequest.get(url);
            if (headers != null) {
                httpRequest.headers(headers);
            }
            
            ClassicHttpRequest apacheRequest = createApacheHttpRequest(httpRequest);
            ClassicHttpResponse response = httpClient.execute(apacheRequest);
            
            if (response.getCode() < 200 || response.getCode() >= 300) {
                logger.error("Failed to get file bytes with status: {}", response.getCode());
                return null;
            }
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    return inputStream.readAllBytes();
                }
            }
            return null;
            
        } catch (Exception e) {
            logger.error("Failed to get file bytes from {}", url, e);
            return null;
        }
    }

    @Override
    public CompletableFuture<Boolean> downloadFileAsync(String url, String savePath) {
        return downloadFileAsync(url, savePath, null);
    }

    @Override
    public CompletableFuture<Boolean> downloadFileAsync(String url, String savePath, FileProgressCallback progressCallback) {
        return CompletableFuture.supplyAsync(() -> downloadFile(url, savePath, null, progressCallback));
    }

    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
} 