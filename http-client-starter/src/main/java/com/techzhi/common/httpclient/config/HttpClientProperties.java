package com.techzhi.common.httpclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;

/**
 * HTTP客户端配置属性
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "techzhi.http-client")
public class HttpClientProperties {

    /**
     * 是否启用HTTP客户端
     */
    private boolean enabled = true;

    /**
     * HTTP客户端类型 (APACHE_HTTP_CLIENT, OK_HTTP)
     */
    private ClientType clientType = ClientType.APACHE_HTTP_CLIENT;

    /**
     * 连接超时时间（毫秒）
     */
    @Min(value = 1, message = "连接超时时间必须大于0")
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 读取超时时间（毫秒）
     */
    @Min(value = 1, message = "读取超时时间必须大于0")
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * 写入超时时间（毫秒）
     */
    @Min(value = 1, message = "写入超时时间必须大于0")
    private Duration writeTimeout = Duration.ofSeconds(30);

    /**
     * 连接池配置
     */
    @NotNull
    private Pool pool = new Pool();

    /**
     * 代理配置
     */
    private Proxy proxy = new Proxy();

    /**
     * SSL配置
     */
    @NotNull
    private Ssl ssl = new Ssl();

    /**
     * 重试配置
     */
    @NotNull
    private Retry retry = new Retry();

    /**
     * 日志配置
     */
    @NotNull
    private Logging logging = new Logging();

    public enum ClientType {
        APACHE_HTTP_CLIENT,
        OK_HTTP
    }

    /**
     * 连接池配置
     */
    public static class Pool {
        /**
         * 最大连接数
         */
        @Min(value = 1, message = "最大连接数必须大于0")
        private int maxTotal = 200;

        /**
         * 每个路由的最大连接数
         */
        @Min(value = 1, message = "每个路由的最大连接数必须大于0")
        private int defaultMaxPerRoute = 50;

        /**
         * 连接存活时间
         */
        private Duration timeToLive = Duration.ofMinutes(5);

        /**
         * 连接空闲时间
         */
        private Duration maxIdleTime = Duration.ofMinutes(1);

        /**
         * 验证连接后重用
         */
        private boolean validateAfterInactivity = true;

        // Getters and Setters
        public int getMaxTotal() { return maxTotal; }
        public void setMaxTotal(int maxTotal) { this.maxTotal = maxTotal; }
        public int getDefaultMaxPerRoute() { return defaultMaxPerRoute; }
        public void setDefaultMaxPerRoute(int defaultMaxPerRoute) { this.defaultMaxPerRoute = defaultMaxPerRoute; }
        public Duration getTimeToLive() { return timeToLive; }
        public void setTimeToLive(Duration timeToLive) { this.timeToLive = timeToLive; }
        public Duration getMaxIdleTime() { return maxIdleTime; }
        public void setMaxIdleTime(Duration maxIdleTime) { this.maxIdleTime = maxIdleTime; }
        public boolean isValidateAfterInactivity() { return validateAfterInactivity; }
        public void setValidateAfterInactivity(boolean validateAfterInactivity) { this.validateAfterInactivity = validateAfterInactivity; }
    }

    /**
     * 代理配置
     */
    public static class Proxy {
        /**
         * 是否启用代理
         */
        private boolean enabled = false;

        /**
         * 代理类型 (HTTP, SOCKS)
         */
        private ProxyType type = ProxyType.HTTP;

        /**
         * 代理主机
         */
        private String host;

        /**
         * 代理端口
         */
        private int port = 8080;

        /**
         * 代理用户名
         */
        private String username;

        /**
         * 代理密码
         */
        private String password;

        /**
         * 非代理主机列表（逗号分隔）
         */
        private String nonProxyHosts;

        public enum ProxyType {
            HTTP, SOCKS
        }

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public ProxyType getType() { return type; }
        public void setType(ProxyType type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNonProxyHosts() { return nonProxyHosts; }
        public void setNonProxyHosts(String nonProxyHosts) { this.nonProxyHosts = nonProxyHosts; }
    }

    /**
     * SSL配置
     */
    public static class Ssl {
        /**
         * 是否启用SSL证书验证
         */
        private boolean verifyHostname = true;

        /**
         * 是否验证证书链
         */
        private boolean verifyCertificateChain = true;

        /**
         * 信任存储路径
         */
        private String trustStorePath;

        /**
         * 信任存储密码
         */
        private String trustStorePassword;

        /**
         * 信任存储类型
         */
        private String trustStoreType = "JKS";

        /**
         * 密钥存储路径
         */
        private String keyStorePath;

        /**
         * 密钥存储密码
         */
        private String keyStorePassword;

        /**
         * 密钥存储类型
         */
        private String keyStoreType = "PKCS12";

        // Getters and Setters
        public boolean isVerifyHostname() { return verifyHostname; }
        public void setVerifyHostname(boolean verifyHostname) { this.verifyHostname = verifyHostname; }
        public boolean isVerifyCertificateChain() { return verifyCertificateChain; }
        public void setVerifyCertificateChain(boolean verifyCertificateChain) { this.verifyCertificateChain = verifyCertificateChain; }
        public String getTrustStorePath() { return trustStorePath; }
        public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }
        public String getTrustStorePassword() { return trustStorePassword; }
        public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
        public String getTrustStoreType() { return trustStoreType; }
        public void setTrustStoreType(String trustStoreType) { this.trustStoreType = trustStoreType; }
        public String getKeyStorePath() { return keyStorePath; }
        public void setKeyStorePath(String keyStorePath) { this.keyStorePath = keyStorePath; }
        public String getKeyStorePassword() { return keyStorePassword; }
        public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }
        public String getKeyStoreType() { return keyStoreType; }
        public void setKeyStoreType(String keyStoreType) { this.keyStoreType = keyStoreType; }
    }

    /**
     * 重试配置
     */
    public static class Retry {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        @Min(value = 0, message = "最大重试次数不能小于0")
        private int maxAttempts = 3;

        /**
         * 重试间隔
         */
        private Duration retryInterval = Duration.ofSeconds(1);

        /**
         * 重试间隔增长因子
         */
        private double backoffMultiplier = 2.0;

        /**
         * 最大重试间隔
         */
        private Duration maxRetryInterval = Duration.ofSeconds(10);

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public Duration getRetryInterval() { return retryInterval; }
        public void setRetryInterval(Duration retryInterval) { this.retryInterval = retryInterval; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
        public Duration getMaxRetryInterval() { return maxRetryInterval; }
        public void setMaxRetryInterval(Duration maxRetryInterval) { this.maxRetryInterval = maxRetryInterval; }
    }

    /**
     * 日志配置
     */
    public static class Logging {
        /**
         * 是否启用请求日志
         */
        private boolean logRequests = false;

        /**
         * 是否启用响应日志
         */
        private boolean logResponses = false;

        /**
         * 是否记录请求体
         */
        private boolean logRequestBody = false;

        /**
         * 是否记录响应体
         */
        private boolean logResponseBody = false;

        /**
         * 最大日志体大小（字节）
         */
        private int maxLogBodySize = 1024;

        // Getters and Setters
        public boolean isLogRequests() { return logRequests; }
        public void setLogRequests(boolean logRequests) { this.logRequests = logRequests; }
        public boolean isLogResponses() { return logResponses; }
        public void setLogResponses(boolean logResponses) { this.logResponses = logResponses; }
        public boolean isLogRequestBody() { return logRequestBody; }
        public void setLogRequestBody(boolean logRequestBody) { this.logRequestBody = logRequestBody; }
        public boolean isLogResponseBody() { return logResponseBody; }
        public void setLogResponseBody(boolean logResponseBody) { this.logResponseBody = logResponseBody; }
        public int getMaxLogBodySize() { return maxLogBodySize; }
        public void setMaxLogBodySize(int maxLogBodySize) { this.maxLogBodySize = maxLogBodySize; }
    }

    // Main class getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public ClientType getClientType() { return clientType; }
    public void setClientType(ClientType clientType) { this.clientType = clientType; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public Duration getWriteTimeout() { return writeTimeout; }
    public void setWriteTimeout(Duration writeTimeout) { this.writeTimeout = writeTimeout; }
    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }
    public Ssl getSsl() { return ssl; }
    public void setSsl(Ssl ssl) { this.ssl = ssl; }
    public Retry getRetry() { return retry; }
    public void setRetry(Retry retry) { this.retry = retry; }
    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }
} 