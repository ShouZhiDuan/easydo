package com.techzhi.harbor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Harbor配置属性
 * 
 * @author techzhi
 */
@ConfigurationProperties(prefix = "harbor")
public class HarborProperties {

    /**
     * Harbor服务器地址
     */
    private String host = "http://192.168.50.103:80";

    /**
     * Harbor用户名
     */
    private String username = "admin";

    /**
     * Harbor密码
     */
    private String password = "Harbor12345";

    /**
     * 默认项目空间
     */
    private String project = "flow";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 30000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 60000;

    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeout = 60000;

    /**
     * 是否启用SSL验证
     */
    private boolean sslEnabled = false;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
} 