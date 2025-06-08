package com.techzhi.common.s3.seaweedfs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SeaweedFS S3 配置属性
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "seaweedfs.s3")
public class SeaweedFsS3Properties {

    /**
     * S3 访问密钥ID
     */
    private String accessKey = "nvx1";

    /**
     * S3 秘密访问密钥
     */
    private String secretKey = "nvx1";

    /**
     * S3 存储桶名称
     */
    private String bucketName = "gongan";

    /**
     * SeaweedFS S3 端点URL
     */
    private String endpoint = "http://192.168.60.70:38333";

    /**
     * AWS 区域
     */
    private String region = "us-east-1";

    /**
     * 是否启用路径样式访问
     */
    private boolean pathStyleAccessEnabled = true;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 10000;

    /**
     * 请求超时时间（毫秒）
     */
    private int requestTimeout = 30000;

    /**
     * 最大连接数
     */
    private int maxConnections = 50;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isPathStyleAccessEnabled() {
        return pathStyleAccessEnabled;
    }

    public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
        this.pathStyleAccessEnabled = pathStyleAccessEnabled;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}