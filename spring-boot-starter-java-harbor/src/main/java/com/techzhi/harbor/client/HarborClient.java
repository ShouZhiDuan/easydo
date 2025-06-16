package com.techzhi.harbor.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techzhi.harbor.config.HarborProperties;
import com.techzhi.harbor.exception.HarborException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Harbor HTTP客户端
 * 
 * @author techzhi
 */
public class HarborClient {

    private static final Logger logger = LoggerFactory.getLogger(HarborClient.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HarborProperties properties;
    private final String basicAuth;

    public HarborClient(HarborProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                (properties.getUsername() + ":" + properties.getPassword()).getBytes());
        this.httpClient = createHttpClient(properties);
    }

    private OkHttpClient createHttpClient(HarborProperties properties) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS);

        // 如果不启用SSL验证，则忽略SSL证书
        if (!properties.isSslEnabled()) {
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[]{};
                            }
                        }
                };

                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                logger.warn("Failed to disable SSL verification", e);
            }
        }

        return builder.build();
    }

    /**
     * 执行GET请求
     */
    public <T> T get(String path, TypeReference<T> typeReference) throws HarborException {
        String url = properties.getHost() + path;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", basicAuth)
                .header("Accept", "application/json")
                .get()
                .build();

        return executeRequest(request, typeReference);
    }

    /**
     * 执行POST请求
     */
    public <T> T post(String path, Object body, TypeReference<T> typeReference) throws HarborException {
        String url = properties.getHost() + path;
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                serializeBody(body)
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", basicAuth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        return executeRequest(request, typeReference);
    }

    /**
     * 执行PUT请求
     */
    public <T> T put(String path, Object body, TypeReference<T> typeReference) throws HarborException {
        String url = properties.getHost() + path;
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                serializeBody(body)
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", basicAuth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .put(requestBody)
                .build();

        return executeRequest(request, typeReference);
    }

    /**
     * 执行DELETE请求
     */
    public void delete(String path) throws HarborException {
        String url = properties.getHost() + path;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", basicAuth)
                .delete()
                .build();

        executeRequest(request, null);
    }

    /**
     * 执行请求并处理响应
     */
    private <T> T executeRequest(Request request, TypeReference<T> typeReference) throws HarborException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                logger.error("Harbor API request failed: {} {}, Response: {}", 
                        request.method(), request.url(), responseBody);
                throw new HarborException(response.code(), 
                        "Harbor API request failed: " + response.code() + " " + response.message());
            }

            if (typeReference != null && !responseBody.isEmpty()) {
                return objectMapper.readValue(responseBody, typeReference);
            }

            return null;
        } catch (IOException e) {
            logger.error("Harbor API request IO error: {} {}", request.method(), request.url(), e);
            throw new HarborException("Harbor API request IO error", e);
        }
    }

    /**
     * 序列化请求体
     */
    private String serializeBody(Object body) {
        if (body == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new HarborException("Failed to serialize request body", e);
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
} 