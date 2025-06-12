package com.techzhi.common.httpclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techzhi.common.httpclient.HttpClient;
import com.techzhi.common.httpclient.config.HttpClientProperties;
import com.techzhi.common.httpclient.model.FileProgressCallback;
import com.techzhi.common.httpclient.model.HttpRequest;
import com.techzhi.common.httpclient.model.HttpResponse;
import com.techzhi.common.httpclient.model.MultipartFile;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 基于OkHttp的HTTP客户端实现
 */
public class OkHttpClientImpl implements HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpClientImpl.class);

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final HttpClientProperties properties;

    public OkHttpClientImpl(HttpClientProperties properties) {
        this.properties = properties;
        this.objectMapper = createObjectMapper();
        this.okHttpClient = createOkHttpClient();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS);

        // 配置代理
        if (properties.getProxy().isEnabled()) {
            configureProxy(builder);
        }

        // 配置SSL
        if (!properties.getSsl().isVerifyHostname() || !properties.getSsl().isVerifyCertificateChain()) {
            configureSSL(builder);
        }

        return builder.build();
    }

    private void configureProxy(OkHttpClient.Builder builder) {
        HttpClientProperties.Proxy proxyConfig = properties.getProxy();
        Proxy.Type proxyType = proxyConfig.getType() == HttpClientProperties.Proxy.ProxyType.HTTP 
                ? Proxy.Type.HTTP : Proxy.Type.SOCKS;
        
        Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
        builder.proxy(proxy);

        if (proxyConfig.getUsername() != null && proxyConfig.getPassword() != null) {
            builder.proxyAuthenticator((route, response) -> {
                String credential = Credentials.basic(proxyConfig.getUsername(), proxyConfig.getPassword());
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
        }
    }

    private void configureSSL(OkHttpClient.Builder builder) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            
            if (!properties.getSsl().isVerifyHostname()) {
                builder.hostnameVerifier((hostname, session) -> true);
            }
        } catch (Exception e) {
            logger.warn("Failed to configure SSL settings", e);
        }
    }

    @Override
    public HttpResponse<String> execute(HttpRequest request) {
        return execute(request, String.class);
    }

    @Override
    public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
        Instant startTime = Instant.now();
        
        try {
            Request okRequest = createOkHttpRequest(request);
            Response response = okHttpClient.newCall(okRequest).execute();
            
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
        CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
        Instant startTime = Instant.now();
        
        try {
            Request okRequest = createOkHttpRequest(request);
            
            okHttpClient.newCall(okRequest).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        HttpResponse<T> httpResponse = processResponse(response, responseType, startTime);
                        future.complete(httpResponse);
                    } catch (Exception e) {
                        onFailure(call, new IOException("Failed to process response", e));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("Async HTTP request failed", e);
                    HttpResponse<T> errorResponse = new HttpResponse<T>()
                            .statusCode(0)
                            .exception(e)
                            .responseTime(Duration.between(startTime, Instant.now()));
                    future.complete(errorResponse);
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to execute async HTTP request", e);
            HttpResponse<T> errorResponse = new HttpResponse<T>()
                    .statusCode(0)
                    .exception(e)
                    .responseTime(Duration.between(startTime, Instant.now()));
            future.complete(errorResponse);
        }
        
        return future;
    }

    private Request createOkHttpRequest(HttpRequest request) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(buildUrl(request));

        // 设置请求头
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }

        // 设置请求方法和请求体
        RequestBody requestBody = null;
        
        // 处理文件上传
        if (request.hasMultipartFiles() || request.hasFormFields()) {
            requestBody = createMultipartBody(request);
        } else if (request.getBody() != null) {
            if ("application/x-www-form-urlencoded".equals(request.getContentType()) && request.getBody() instanceof Map) {
                requestBody = createFormBody((Map<String, String>) request.getBody());
            } else {
                String bodyString = serializeBody(request);
                requestBody = createProgressAwareRequestBody(bodyString, request.getContentType(), request.getProgressCallback());
            }
        }

        switch (request.getMethod()) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case PUT:
                builder.put(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case DELETE:
                if (requestBody != null) {
                    builder.delete(requestBody);
                } else {
                    builder.delete();
                }
                break;
            case PATCH:
                builder.patch(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case HEAD:
                builder.head();
                break;
            default:
                builder.method(request.getMethod().name(), requestBody);
                break;
        }

        return builder.build();
    }

    private HttpUrl buildUrl(HttpRequest request) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(request.getUri().toString()).newBuilder();
        
        for (Map.Entry<String, String> param : request.getQueryParams().entrySet()) {
            urlBuilder.addQueryParameter(param.getKey(), param.getValue());
        }
        
        return urlBuilder.build();
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

    private <T> HttpResponse<T> processResponse(Response response, Class<T> responseType, Instant startTime) throws IOException {
        Duration responseTime = Duration.between(startTime, Instant.now());
        
        HttpResponse<T> httpResponse = new HttpResponse<T>()
                .statusCode(response.code())
                .reasonPhrase(response.message())
                .responseTime(responseTime);

        // 处理响应头
        for (String name : response.headers().names()) {
            List<String> values = response.headers().values(name);
            for (String value : values) {
                httpResponse.header(name, value);
            }
        }

        // 处理响应体
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String rawBody = responseBody.string();
            httpResponse.rawBody(rawBody);

            T body = deserializeBody(rawBody, responseType, responseBody.contentType());
            httpResponse.body(body);
        }

        return httpResponse;
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeBody(String rawBody, Class<T> responseType, MediaType contentType) throws IOException {
        if (responseType == String.class) {
            return (T) rawBody;
        }

        String contentTypeString = contentType != null ? contentType.toString() : "";
        if ("application/json".equals(contentTypeString) || contentTypeString.contains("json")) {
            return objectMapper.readValue(rawBody, responseType);
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

    // =============== 文件操作相关方法实现 ===============

    private RequestBody createMultipartBody(HttpRequest request) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 添加表单字段
        for (Map.Entry<String, String> field : request.getFormFields().entrySet()) {
            builder.addFormDataPart(field.getKey(), field.getValue());
        }

        // 添加文件
        for (MultipartFile multipartFile : request.getMultipartFiles()) {
            RequestBody fileBody = createFileRequestBody(multipartFile, request.getProgressCallback());
            builder.addFormDataPart(
                    multipartFile.getFieldName(),
                    multipartFile.getFileName(),
                    fileBody
            );
        }

        return builder.build();
    }

    private RequestBody createFormBody(Map<String, String> formData) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private RequestBody createFileRequestBody(MultipartFile multipartFile, FileProgressCallback progressCallback) throws IOException {
        MediaType mediaType = MediaType.parse(multipartFile.getContentType());
        
        if (multipartFile.getFile() != null) {
            return createProgressAwareFileRequestBody(multipartFile.getFile(), mediaType, progressCallback);
        } else if (multipartFile.getContent() != null) {
            return createProgressAwareRequestBody(multipartFile.getContent(), mediaType, progressCallback);
        } else if (multipartFile.getInputStream() != null) {
            return createProgressAwareStreamRequestBody(multipartFile.getInputStream(), mediaType, progressCallback);
        } else {
            throw new IllegalArgumentException("MultipartFile must have file, content, or inputStream");
        }
    }

    private RequestBody createProgressAwareRequestBody(String content, String contentType, FileProgressCallback progressCallback) {
        MediaType mediaType = MediaType.parse(contentType);
        byte[] bytes = content.getBytes();
        return createProgressAwareRequestBody(bytes, mediaType, progressCallback);
    }

    private RequestBody createProgressAwareRequestBody(byte[] content, MediaType mediaType, FileProgressCallback progressCallback) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return content.length;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                if (progressCallback != null) {
                    progressCallback.onStart();
                }
                
                Source source = Okio.source(new ByteArrayInputStream(content));
                try {
                    long totalBytesRead = 0;
                    long bufferSize = 8192;
                    
                    while (totalBytesRead < content.length) {
                        long bytesRead = sink.writeAll(source);
                        totalBytesRead += bytesRead;
                        
                        if (progressCallback != null) {
                            double percentage = (double) totalBytesRead / content.length * 100;
                            progressCallback.onProgress(totalBytesRead, content.length, percentage);
                        }
                    }
                    
                    if (progressCallback != null) {
                        progressCallback.onComplete(content.length);
                    }
                } catch (IOException e) {
                    if (progressCallback != null) {
                        progressCallback.onError(e);
                    }
                    throw e;
                } finally {
                    source.close();
                }
            }
        };
    }

    private RequestBody createProgressAwareFileRequestBody(java.io.File file, MediaType mediaType, FileProgressCallback progressCallback) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                if (progressCallback != null) {
                    progressCallback.onStart();
                }
                
                Source source = Okio.source(file);
                try {
                    long totalBytesRead = 0;
                    long fileSize = file.length();
                    
                    while (totalBytesRead < fileSize) {
                        long bytesRead = sink.writeAll(source);
                        totalBytesRead += bytesRead;
                        
                        if (progressCallback != null) {
                            double percentage = (double) totalBytesRead / fileSize * 100;
                            progressCallback.onProgress(totalBytesRead, fileSize, percentage);
                        }
                    }
                    
                    if (progressCallback != null) {
                        progressCallback.onComplete(fileSize);
                    }
                } catch (IOException e) {
                    if (progressCallback != null) {
                        progressCallback.onError(e);
                    }
                    throw e;
                } finally {
                    source.close();
                }
            }
        };
    }

    private RequestBody createProgressAwareStreamRequestBody(InputStream inputStream, MediaType mediaType, FileProgressCallback progressCallback) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                if (progressCallback != null) {
                    progressCallback.onStart();
                }
                
                Source source = Okio.source(inputStream);
                try {
                    long totalBytesRead = 0;
                    long bufferSize = 8192;
                    
                    while (true) {
                        long bytesRead = sink.writeAll(source);
                        if (bytesRead == 0) break;
                        
                        totalBytesRead += bytesRead;
                        
                        if (progressCallback != null) {
                            progressCallback.onProgress(totalBytesRead, -1, -1);
                        }
                    }
                    
                    if (progressCallback != null) {
                        progressCallback.onComplete(totalBytesRead);
                    }
                } catch (IOException e) {
                    if (progressCallback != null) {
                        progressCallback.onError(e);
                    }
                    throw e;
                } finally {
                    source.close();
                }
            }
        };
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
            
            Request okRequest = createOkHttpRequest(httpRequest);
            Response response = okHttpClient.newCall(okRequest).execute();
            
            if (!response.isSuccessful()) {
                logger.error("Download failed with status: {}", response.code());
                return false;
            }
            
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                logger.error("Response body is null");
                return false;
            }
            
            Path path = Paths.get(savePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            
            if (progressCallback != null) {
                progressCallback.onStart();
            }
            
            try (InputStream inputStream = responseBody.byteStream();
                 OutputStream outputStream = Files.newOutputStream(path)) {
                
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                long contentLength = responseBody.contentLength();
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
            
            Request okRequest = createOkHttpRequest(httpRequest);
            Response response = okHttpClient.newCall(okRequest).execute();
            
            if (!response.isSuccessful()) {
                logger.error("Failed to get file stream with status: {}", response.code());
                return null;
            }
            
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.byteStream() : null;
            
        } catch (Exception e) {
            logger.error("Failed to get file stream from {}", url, e);
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
            
            Request okRequest = createOkHttpRequest(httpRequest);
            Response response = okHttpClient.newCall(okRequest).execute();
            
            if (!response.isSuccessful()) {
                logger.error("Failed to get file bytes with status: {}", response.code());
                return null;
            }
            
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.bytes() : null;
            
        } catch (Exception e) {
            logger.error("Failed to get file bytes from {}", url, e);
            return null;
        }
    }

    @Override
    public void close() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
        }
    }
} 