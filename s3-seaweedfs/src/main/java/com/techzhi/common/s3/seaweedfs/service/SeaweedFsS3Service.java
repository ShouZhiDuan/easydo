package com.techzhi.common.s3.seaweedfs.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.techzhi.common.s3.seaweedfs.config.SeaweedFsS3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SeaweedFS S3 服务类
 * 提供文件上传、下载、删除等操作
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public class SeaweedFsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(SeaweedFsS3Service.class);

    private final AmazonS3 amazonS3Client;
    private final SeaweedFsS3Properties properties;

    public SeaweedFsS3Service(AmazonS3 amazonS3Client, SeaweedFsS3Properties properties) {
        this.amazonS3Client = amazonS3Client;
        this.properties = properties;
        initializeBucket();
    }

    /**
     * 初始化存储桶
     */
    private void initializeBucket() {
        try {
            if (!amazonS3Client.doesBucketExistV2(properties.getBucketName())) {
                logger.info("Creating bucket: {}", properties.getBucketName());
                amazonS3Client.createBucket(properties.getBucketName());
                logger.info("Bucket created successfully: {}", properties.getBucketName());
            } else {
                logger.info("Bucket already exists: {}", properties.getBucketName());
            }
        } catch (AmazonServiceException e) {
            logger.error("Failed to initialize bucket: {}", properties.getBucketName(), e);
            throw new RuntimeException("Failed to initialize bucket", e);
        }
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
    public PutObjectResult uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            logger.info("Uploading file with key: {}, size: {} bytes", key, contentLength);
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            if (StringUtils.hasText(contentType)) {
                metadata.setContentType(contentType);
            }
            
            PutObjectRequest request = new PutObjectRequest(
                    properties.getBucketName(), 
                    key, 
                    inputStream, 
                    metadata
            );
            
            PutObjectResult result = amazonS3Client.putObject(request);
            logger.info("File uploaded successfully with key: {}, ETag: {}", key, result.getETag());
            return result;
        } catch (AmazonServiceException e) {
            logger.error("Failed to upload file with key: {}", key, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * 上传字节数组
     * 
     * @param key 文件键名
     * @param data 字节数据
     * @param contentType 文件类型
     * @return 上传结果
     */
    public PutObjectResult uploadFile(String key, byte[] data, String contentType) {
        return uploadFile(key, new ByteArrayInputStream(data), data.length, contentType);
    }

    /**
     * 下载文件
     * 
     * @param key 文件键名
     * @return S3对象
     */
    public S3Object downloadFile(String key) {
        try {
            logger.info("Downloading file with key: {}", key);
            S3Object s3Object = amazonS3Client.getObject(properties.getBucketName(), key);
            logger.info("File downloaded successfully with key: {}", key);
            return s3Object;
        } catch (AmazonServiceException e) {
            logger.error("Failed to download file with key: {}", key, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    /**
     * 获取文件输入流
     * 
     * @param key 文件键名
     * @return 文件输入流
     */
    public InputStream getFileInputStream(String key) {
        S3Object s3Object = downloadFile(key);
        return s3Object.getObjectContent();
    }

    /**
     * 获取文件字节数组
     * 
     * @param key 文件键名
     * @return 文件字节数组
     */
    public byte[] getFileBytes(String key) {
        try (S3Object s3Object = downloadFile(key);
             InputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            logger.error("Failed to read file bytes for key: {}", key, e);
            throw new RuntimeException("Failed to read file bytes", e);
        }
    }

    /**
     * 删除文件
     * 
     * @param key 文件键名
     */
    public void deleteFile(String key) {
        try {
            logger.info("Deleting file with key: {}", key);
            amazonS3Client.deleteObject(properties.getBucketName(), key);
            logger.info("File deleted successfully with key: {}", key);
        } catch (AmazonServiceException e) {
            logger.error("Failed to delete file with key: {}", key, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param key 文件键名
     * @return 是否存在
     */
    public boolean doesFileExist(String key) {
        try {
            return amazonS3Client.doesObjectExist(properties.getBucketName(), key);
        } catch (AmazonServiceException e) {
            logger.error("Failed to check file existence for key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取文件元数据
     * 
     * @param key 文件键名
     * @return 文件元数据
     */
    public ObjectMetadata getFileMetadata(String key) {
        try {
            return amazonS3Client.getObjectMetadata(properties.getBucketName(), key);
        } catch (AmazonServiceException e) {
            logger.error("Failed to get file metadata for key: {}", key, e);
            throw new RuntimeException("Failed to get file metadata", e);
        }
    }

    /**
     * 列出文件
     * 
     * @param prefix 前缀
     * @return 文件列表
     */
    public List<S3ObjectSummary> listFiles(String prefix) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(properties.getBucketName())
                    .withPrefix(prefix);
            
            ListObjectsV2Result result = amazonS3Client.listObjectsV2(request);
            return result.getObjectSummaries();
        } catch (AmazonServiceException e) {
            logger.error("Failed to list files with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to list files", e);
        }
    }

    /**
     * 列出所有文件
     * 
     * @return 文件列表
     */
    public List<S3ObjectSummary> listAllFiles() {
        return listFiles(null);
    }

    /**
     * 生成预签名URL
     * 
     * @param key 文件键名
     * @param expiration 过期时间
     * @return 预签名URL
     */
    public URL generatePresignedUrl(String key, Date expiration) {
        try {
            return amazonS3Client.generatePresignedUrl(properties.getBucketName(), key, expiration);
        } catch (AmazonServiceException e) {
            logger.error("Failed to generate presigned URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * 复制文件
     * 
     * @param sourceKey 源文件键名
     * @param destinationKey 目标文件键名
     * @return 复制结果
     */
    public CopyObjectResult copyFile(String sourceKey, String destinationKey) {
        try {
            logger.info("Copying file from {} to {}", sourceKey, destinationKey);
            CopyObjectRequest request = new CopyObjectRequest(
                    properties.getBucketName(), sourceKey,
                    properties.getBucketName(), destinationKey
            );
            CopyObjectResult result = amazonS3Client.copyObject(request);
            logger.info("File copied successfully from {} to {}", sourceKey, destinationKey);
            return result;
        } catch (AmazonServiceException e) {
            logger.error("Failed to copy file from {} to {}", sourceKey, destinationKey, e);
            throw new RuntimeException("Failed to copy file", e);
        }
    }

    /**
     * 获取存储桶名称
     * 
     * @return 存储桶名称
     */
    public String getBucketName() {
        return properties.getBucketName();
    }

    /**
     * 获取S3客户端
     * 
     * @return S3客户端
     */
    public AmazonS3 getAmazonS3Client() {
        return amazonS3Client;
    }
}