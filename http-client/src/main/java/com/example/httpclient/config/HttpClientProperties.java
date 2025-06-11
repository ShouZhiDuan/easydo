package com.example.httpclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "custom.http.client")
public class HttpClientProperties {

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout = 10000; // 10 seconds

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 30000; // 30 seconds

    /**
     * Write timeout in milliseconds.
     */
    private int writeTimeout = 30000; // 30 seconds

    /**
     * Whether to follow redirects.
     */
    private boolean followRedirects = true;

    /**
     * Whether to retry on connection failure.
     */
    private boolean retryOnConnectionFailure = true;

    /**
     * Maximum number of idle connections.
     */
    private int maxIdleConnections = 5;

    /**
     * Keep alive duration for idle connections in milliseconds.
     */
    private long keepAliveDuration = 300000; // 5 minutes

    /**
     * Whether to bypass SSL certificate validation (USE WITH CAUTION).
     */
    private boolean bypassSslValidation = false;

    // You can add more properties as needed, e.g., proxy settings
    // private String proxyHost;
    // private int proxyPort;
    // private String proxyUsername;
    // private String proxyPassword;
}