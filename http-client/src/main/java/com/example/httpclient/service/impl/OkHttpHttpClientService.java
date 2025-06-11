package com.example.httpclient.service.impl;

import com.example.httpclient.config.HttpClientProperties;
import com.example.httpclient.service.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OkHttpHttpClientService implements HttpClientService {

    private final OkHttpClient client;
    private final HttpClientProperties properties;
    private final ObjectMapper objectMapper;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded");

    public OkHttpHttpClientService(HttpClientProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(properties.isFollowRedirects())
                .retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
                .connectionPool(new ConnectionPool(properties.getMaxIdleConnections(),
                        properties.getKeepAliveDuration(), TimeUnit.MILLISECONDS));

        if (properties.isBypassSslValidation()) {
            configureToBypassSslValidation(builder);
        }
        this.client = builder.build();
    }

    private void configureToBypassSslValidation(OkHttpClient.Builder builder) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            log.warn("Bypassing SSL validation. This should only be used in development/testing environments.");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to configure SSL bypass", e);
            throw new RuntimeException("Failed to configure SSL bypass", e);
        }
    }

    @Override
    public String get(String url, Map<String, String> headers, Map<String, String> queryParams) throws IOException {
        try (Response response = execute(url, "GET", headers, queryParams, null)) {
            // The execute method now throws an IOException if !response.isSuccessful(),
            // so we can directly proceed to read the body if no exception was thrown.
            return Objects.requireNonNull(response.body()).string();
        }
    }

    @Override
    public String postJson(String url, Map<String, String> headers, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        try (Response response = execute(url, "POST", headers, null, body)) {
            // The execute method now throws an IOException if !response.isSuccessful()
            return Objects.requireNonNull(response.body()).string();
        }
    }

    @Override
    public String postForm(String url, Map<String, String> headers, Map<String, String> formData) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formData.forEach(formBuilder::add);
        RequestBody body = formBuilder.build();
        try (Response response = execute(url, "POST", headers, null, body)) {
            // The execute method now throws an IOException if !response.isSuccessful()
            return Objects.requireNonNull(response.body()).string();
        }
    }

    @Override
    public String putJson(String url, Map<String, String> headers, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        try (Response response = execute(url, "PUT", headers, null, body)) {
            // The execute method now throws an IOException if !response.isSuccessful()
            return Objects.requireNonNull(response.body()).string();
        }
    }

    @Override
    public String delete(String url, Map<String, String> headers) throws IOException {
        try (Response response = execute(url, "DELETE", headers, null, null)) {
            // The execute method now throws an IOException if !response.isSuccessful()
            return Objects.requireNonNull(response.body()).string();
        }
    }

    @Override
    public String uploadFile(String url, Map<String, String> headers, String fileParamName, File file, Map<String, String> otherParams) throws IOException {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileParamName, file.getName(),
                        RequestBody.create(file, MediaType.parse(determineMediaType(file.getName()))));

        if (otherParams != null) {
            otherParams.forEach(requestBodyBuilder::addFormDataPart);
        }

        RequestBody requestBody = requestBodyBuilder.build();
        Response response = execute(url, "POST", headers, null, requestBody);
        try (ResponseBody body = response.body()) {
            return Objects.requireNonNull(body).string();
        }
    }

    String determineMediaType(String fileName) {
        // Basic media type determination, can be expanded
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".xml")) return "application/xml";
        return "application/octet-stream"; // Default
    }

    @Override
    public InputStream downloadFile(String url, Map<String, String> headers) throws IOException {
        Response response = execute(url, "GET", headers, null, null);
        if (!response.isSuccessful()) {
            String responseBodyString = "";
            try (ResponseBody body = response.body()) { // body should be readable now
                if (body != null) {
                    responseBodyString = body.string(); // Read the body for the exception message
                }
            } catch (Exception e) {
                // Log if reading body for error message fails, but proceed with response.message()
                log.warn("Failed to read error response body for URL: {}. Error: {}", url, e.getMessage());
                // Fallback to response.message() if body.string() fails or body is null
                if (responseBodyString.isEmpty()) {
                     responseBodyString = response.message(); // Use response message as fallback
                }
            }
            // Ensure responseBodyString is not null for concatenation
            if (responseBodyString == null) responseBodyString = "";

            throw new IOException("Failed to download file: " + response.code() + " " + responseBodyString.trim());
        }
        // If successful, return the byte stream. Caller must close.
        return Objects.requireNonNull(response.body()).byteStream();
    }

    @Override
    public Response execute(String url, String method, Map<String, String> headers, Map<String, String> queryParams, RequestBody requestBody) throws IOException {
        HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();

        if (queryParams != null) {
            queryParams.forEach(httpUrlBuilder::addQueryParameter);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(httpUrlBuilder.build())
                .method(method.toUpperCase(), requestBody);

        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        log.debug("Executing {} request to URL: {} with headers: {} and queryParams: {}", method, url, headers, queryParams);
        Request finalRequest = requestBuilder.build(); // Build the request
        Response response = client.newCall(finalRequest).execute(); // Use the built request
        if (!response.isSuccessful()) {
            String errorBody = "";
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    errorBody = body.string(); // Consume the body for the error message
                }
            } catch (Exception e) {
                log.warn("Failed to read error response body for URL: {}. Error: {}", url, e.getMessage());
                // Fallback to response.message() if body.string() fails or body is null
                if (errorBody.isEmpty()) {
                    errorBody = response.message();
                }
            }
            // Ensure errorBody is not null for concatenation
            if (errorBody == null) errorBody = "";

            log.error("HTTP request failed with code: {}. URL: {}. Method: {}. Response: {}",
                    response.code(), url, method, errorBody.trim());
            throw new IOException("HTTP request failed with code " + response.code() + ": " + errorBody.trim());
        }
        return response; // Return the response, caller is responsible for body
    }

    // Helper to convert objects to JSON string for request bodies
    public String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}