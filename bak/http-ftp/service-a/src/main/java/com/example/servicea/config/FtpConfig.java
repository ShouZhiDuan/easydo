package com.example.servicea.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {
    
    private String localHost = "192.168.60.69";
    private int localPort = 10021;
    private String localUsername = "ftpuser0";
    private String localPassword = "cft6yhnbv@ga0351";
    
    private String remoteHost = "192.168.60.70";
    private int remotePort = 10021;
    private String remoteUsername = "ftpuser0";
    private String remotePassword = "cft6yhnbv@ga0351";
    
    private String workingDirectory = "/data/exchange";
    private String requestDirectory = "/data/exchange/requests";
    private String responseDirectory = "/data/exchange/responses";
    
    // Getters and Setters
    public String getLocalHost() {
        return localHost;
    }
    
    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    public String getLocalUsername() {
        return localUsername;
    }
    
    public void setLocalUsername(String localUsername) {
        this.localUsername = localUsername;
    }
    
    public String getLocalPassword() {
        return localPassword;
    }
    
    public void setLocalPassword(String localPassword) {
        this.localPassword = localPassword;
    }
    
    public String getRemoteHost() {
        return remoteHost;
    }
    
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    
    public int getRemotePort() {
        return remotePort;
    }
    
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    
    public String getRemoteUsername() {
        return remoteUsername;
    }
    
    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }
    
    public String getRemotePassword() {
        return remotePassword;
    }
    
    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }
    
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    public String getRequestDirectory() {
        return requestDirectory;
    }
    
    public void setRequestDirectory(String requestDirectory) {
        this.requestDirectory = requestDirectory;
    }
    
    public String getResponseDirectory() {
        return responseDirectory;
    }
    
    public void setResponseDirectory(String responseDirectory) {
        this.responseDirectory = responseDirectory;
    }
}