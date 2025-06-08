package com.techzhi.common.s3.seaweedfs.util;

import com.amazonaws.services.s3.model.*;
import com.techzhi.common.s3.seaweedfs.service.SeaweedFsS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * SeaweedFS S3 工具类
 * 提供静态方法便于使用
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@Component
public class SeaweedFsS3Util {

    private static SeaweedFsS3Service seaweedFsS3Service;

    @Autowired
    public void setSeaweedFsS3Service(SeaweedFsS3Service seaweedFsS3Service) {
        SeaweedFsS3Util.seaweedFsS3Service = seaweedFsS3Service;
    }

    /**
     * 上传文件
     * 
     * @param key 文件键名
     * @param inputStream 文件输入流
     * @param contentLength 文件大小
     * @param contentType 文件类型
     * @return 上传结果
     */
    public static PutObjectResult upload(String key, InputStream inputStream, long contentLength, String contentType) {
        checkService();
        return seaweedFsS3Service.uploadFile(key, inputStream, contentLength, contentType);
    }

    /**
     * 上传字节数组
     * 
     * @param key 文件键名
     * @param data 字节数据
     * @param contentType 文件类型
     * @return 上传结果
     */
    public static PutObjectResult upload(String key, byte[] data, String contentType) {
        checkService();
        return seaweedFsS3Service.uploadFile(key, data, contentType);
    }

    /**
     * 上传字节数组（自动检测内容类型）
     * 
     * @param key 文件键名
     * @param data 字节数据
     * @return 上传结果
     */
    public static PutObjectResult upload(String key, byte[] data) {
        return upload(key, data, null);
    }

    /**
     * 下载文件
     * 
     * @param key 文件键名
     * @return S3对象
     */
    public static S3Object download(String key) {
        checkService();
        return seaweedFsS3Service.downloadFile(key);
    }

    /**
     * 获取文件输入流
     * 
     * @param key 文件键名
     * @return 文件输入流
     */
    public static InputStream getInputStream(String key) {
        checkService();
        return seaweedFsS3Service.getFileInputStream(key);
    }

    /**
     * 获取文件字节数组
     * 
     * @param key 文件键名
     * @return 文件字节数组
     */
    public static byte[] getBytes(String key) {
        checkService();
        return seaweedFsS3Service.getFileBytes(key);
    }

    /**
     * 删除文件
     * 
     * @param key 文件键名
     */
    public static void delete(String key) {
        checkService();
        seaweedFsS3Service.deleteFile(key);
    }

    /**
     * 检查文件是否存在
     * 
     * @param key 文件键名
     * @return 是否存在
     */
    public static boolean exists(String key) {
        checkService();
        return seaweedFsS3Service.doesFileExist(key);
    }

    /**
     * 获取文件元数据
     * 
     * @param key 文件键名
     * @return 文件元数据
     */
    public static ObjectMetadata getMetadata(String key) {
        checkService();
        return seaweedFsS3Service.getFileMetadata(key);
    }

    /**
     * 列出文件
     * 
     * @param prefix 前缀
     * @return 文件列表
     */
    public static List<S3ObjectSummary> list(String prefix) {
        checkService();
        return seaweedFsS3Service.listFiles(prefix);
    }

    /**
     * 列出所有文件
     * 
     * @return 文件列表
     */
    public static List<S3ObjectSummary> listAll() {
        checkService();
        return seaweedFsS3Service.listAllFiles();
    }

    /**
     * 生成预签名URL
     * 
     * @param key 文件键名
     * @param expiration 过期时间
     * @return 预签名URL
     */
    public static URL generatePresignedUrl(String key, Date expiration) {
        checkService();
        return seaweedFsS3Service.generatePresignedUrl(key, expiration);
    }

    /**
     * 生成预签名URL（默认1小时过期）
     * 
     * @param key 文件键名
     * @return 预签名URL
     */
    public static URL generatePresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1小时后过期
        return generatePresignedUrl(key, expiration);
    }

    /**
     * 复制文件
     * 
     * @param sourceKey 源文件键名
     * @param destinationKey 目标文件键名
     * @return 复制结果
     */
    public static CopyObjectResult copy(String sourceKey, String destinationKey) {
        checkService();
        return seaweedFsS3Service.copyFile(sourceKey, destinationKey);
    }

    /**
     * 获取存储桶名称
     * 
     * @return 存储桶名称
     */
    public static String getBucketName() {
        checkService();
        return seaweedFsS3Service.getBucketName();
    }

    /**
     * 检查服务是否已初始化
     */
    private static void checkService() {
        if (seaweedFsS3Service == null) {
            throw new IllegalStateException("SeaweedFsS3Service is not initialized. Please ensure Spring Boot auto-configuration is enabled.");
        }
    }

    /**
     * 获取服务实例
     * 
     * @return 服务实例
     */
    public static SeaweedFsS3Service getService() {
        checkService();
        return seaweedFsS3Service;
    }
}