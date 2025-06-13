package com.example.serviceb.service;

import com.example.serviceb.config.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Service
public class FtpClientService {
    
    private static final Logger log = LoggerFactory.getLogger(FtpClientService.class);
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int DATA_TIMEOUT = 60000;
    private static final int BUFFER_SIZE = 1024 * 1024;
    
    @Autowired
    private FtpConfig ftpConfig;
    
    /**
     * 创建安全的FTPS连接 (经过验证的方法)
     */
    private FTPSClient createSecureConnection(String server, int port, String username, String password) throws IOException {
        FTPSClient ftpsClient;
        
        try {
            // 创建信任所有证书的TrustManager（仅用于测试，生产环境应使用正确的证书验证）
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // 创建FTPS客户端，使用显式SSL模式
            ftpsClient = new FTPSClient("TLS", false);
            ftpsClient.setTrustManager(trustAllCerts[0]);
            
        } catch (Exception e) {
            throw new IOException("创建SSL上下文失败: " + e.getMessage(), e);
        }
        
        try {
            // 设置连接超时
            ftpsClient.setConnectTimeout(CONNECT_TIMEOUT);
            ftpsClient.setDataTimeout(DATA_TIMEOUT);
            
            log.info("正在连接到FTPS服务器: {}:{}", server, port);
            
            // 连接到FTP服务器
            ftpsClient.connect(server, port);
            
            // 检查连接状态
            int reply = ftpsClient.getReplyCode();
            log.info("FTPS服务器响应码: {}, 响应信息: {}", reply, ftpsClient.getReplyString());
            
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpsClient.disconnect();
                throw new IOException("FTPS服务器连接失败，响应码: " + reply + ", 响应信息: " + ftpsClient.getReplyString());
            }
            
            // 执行AUTH TLS命令建立安全连接
            ftpsClient.execAUTH("TLS");
            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");
            
            log.info("正在尝试安全登录，用户名: {}", username);
            
            // 登录
            boolean loginSuccess = ftpsClient.login(username, password);
            int loginReply = ftpsClient.getReplyCode();
            log.info("登录响应码: {}, 响应信息: {}", loginReply, ftpsClient.getReplyString());
            
            if (!loginSuccess) {
                String errorMsg = String.format("FTPS登录失败，用户名: %s, 响应码: %d, 响应信息: %s",
                    username, loginReply, ftpsClient.getReplyString());
                throw new IOException(errorMsg);
            }
            
            // 配置FTPS客户端
            ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            try {
                ftpsClient.setControlEncoding("UTF-8");
                log.debug("设置控制连接编码为UTF-8");
            } catch (Exception e) {
                log.warn("设置UTF-8编码失败，使用默认编码: {}", e.getMessage());
            }
            
            // 进入被动模式
            ftpsClient.enterLocalPassiveMode();
            log.debug("已进入被动模式");
            
            // 设置缓冲区大小
            ftpsClient.setBufferSize(BUFFER_SIZE);
            
            // 保持连接活跃
            ftpsClient.setKeepAlive(true);
            
            log.info("成功连接并登录到FTPS服务器: {}:{}", server, port);
            return ftpsClient;
            
        } catch (IOException e) {
            if (ftpsClient.isConnected()) {
                try {
                    ftpsClient.disconnect();
                } catch (IOException ex) {
                    log.error("关闭FTPS连接时发生错误", ex);
                }
            }
            throw e;
        }
    }
    
    /**
     * 连接到FTPS服务器 (兼容性方法)
     */
    private FTPSClient connectToFtp(String host, int port, String username, String password) throws IOException {
        return createSecureConnection(host, port, username, password);
    }
    
    /**
     * 断开FTPS连接
     */
    private void disconnectFtp(FTPSClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            log.error("Error disconnecting from FTP server", e);
        }
    }
    
    /**
     * 从本地FTP服务器下载请求文件
     */
    public String downloadRequest(String fileName) throws IOException {
        FTPSClient ftpClient = connectToFtp(
            ftpConfig.getLocalHost(),
            ftpConfig.getLocalPort(),
            ftpConfig.getLocalUsername(),
            ftpConfig.getLocalPassword()
        );
        
        try {
            ftpClient.changeWorkingDirectory(ftpConfig.getRequestDirectory());
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean downloaded = ftpClient.retrieveFile(fileName, outputStream);
            
            if (downloaded) {
                String content = outputStream.toString(StandardCharsets.UTF_8.name());
                log.info("Request downloaded successfully: {}", fileName);
                
                // 删除已处理的文件
                ftpClient.deleteFile(fileName);
                return content;
            } else {
                throw new IOException("Failed to download request file: " + fileName);
            }
        } finally {
            disconnectFtp(ftpClient);
        }
    }
    
    /**
     * 上传响应文件到远程FTP服务器
     */
    public void uploadResponse(String requestId, String content) throws IOException {
        String fileName = "response_" + requestId + ".json";
        
        FTPSClient ftpClient = connectToFtp(
            ftpConfig.getRemoteHost(),
            ftpConfig.getRemotePort(),
            ftpConfig.getRemoteUsername(),
            ftpConfig.getRemotePassword()
        );
        
        try {
            // 确保目录存在
            ftpClient.makeDirectory(ftpConfig.getResponseDirectory());
            ftpClient.changeWorkingDirectory(ftpConfig.getResponseDirectory());
            
            // 上传文件
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            boolean uploaded = ftpClient.storeFile(fileName, inputStream);
            
            if (uploaded) {
                log.info("Response uploaded successfully: {}", fileName);
            } else {
                throw new IOException("Failed to upload response file: " + fileName);
            }
        } finally {
            disconnectFtp(ftpClient);
        }
    }
    
    /**
     * 列出本地FTP服务器请求目录中的文件
     */
    public FTPFile[] listRequestFiles() throws IOException {
        FTPSClient ftpClient = connectToFtp(
            ftpConfig.getLocalHost(),
            ftpConfig.getLocalPort(),
            ftpConfig.getLocalUsername(),
            ftpConfig.getLocalPassword()
        );
        
        try {
            ftpClient.changeWorkingDirectory(ftpConfig.getRequestDirectory());
            return ftpClient.listFiles("request_*.json");
        } finally {
            disconnectFtp(ftpClient);
        }
    }
}