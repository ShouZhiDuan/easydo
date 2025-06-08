package com.techzhi.common.s3.seaweedfs.service;

import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.techzhi.common.s3.seaweedfs.config.SeaweedFsS3Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SeaweedFS S3 服务测试类
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SeaweedFsS3ServiceTest {

    @Autowired
    private SeaweedFsS3Service seaweedFsS3Service;

    @Autowired
    private SeaweedFsS3Properties properties;

    private static final String TEST_KEY = "test/sample.txt";
    private static final String TEST_CONTENT = "Hello, SeaweedFS S3!";
    private static final String TEST_CONTENT_TYPE = "text/plain";

    @Before
    public void setUp() {
        // 清理测试数据
        if (seaweedFsS3Service.doesFileExist(TEST_KEY)) {
            seaweedFsS3Service.deleteFile(TEST_KEY);
        }
    }

    @Test
    public void testUploadAndDownloadFile() throws IOException {
        // 测试上传
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        PutObjectResult uploadResult = seaweedFsS3Service.uploadFile(
                TEST_KEY, 
                new ByteArrayInputStream(testData), 
                testData.length, 
                TEST_CONTENT_TYPE
        );
        
        assertNotNull("Upload result should not be null", uploadResult);
        assertNotNull("ETag should not be null", uploadResult.getETag());

        // 测试文件是否存在
        assertTrue("File should exist after upload", seaweedFsS3Service.doesFileExist(TEST_KEY));

        // 测试下载
        S3Object s3Object = seaweedFsS3Service.downloadFile(TEST_KEY);
        assertNotNull("Downloaded object should not be null", s3Object);
        assertEquals("Object key should match", TEST_KEY, s3Object.getKey());

        // 验证内容
        byte[] downloadedData = seaweedFsS3Service.getFileBytes(TEST_KEY);
        String downloadedContent = new String(downloadedData, StandardCharsets.UTF_8);
        assertEquals("Downloaded content should match uploaded content", TEST_CONTENT, downloadedContent);
    }

    @Test
    public void testUploadByteArray() {
        // 测试字节数组上传
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        PutObjectResult uploadResult = seaweedFsS3Service.uploadFile(TEST_KEY, testData, TEST_CONTENT_TYPE);
        
        assertNotNull("Upload result should not be null", uploadResult);
        assertTrue("File should exist after upload", seaweedFsS3Service.doesFileExist(TEST_KEY));

        // 验证内容
        byte[] downloadedData = seaweedFsS3Service.getFileBytes(TEST_KEY);
        assertArrayEquals("Downloaded data should match uploaded data", testData, downloadedData);
    }

    @Test
    public void testFileMetadata() {
        // 上传文件
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        seaweedFsS3Service.uploadFile(TEST_KEY, testData, TEST_CONTENT_TYPE);

        // 获取元数据
        ObjectMetadata metadata = seaweedFsS3Service.getFileMetadata(TEST_KEY);
        assertNotNull("Metadata should not be null", metadata);
        assertEquals("Content length should match", testData.length, metadata.getContentLength());
        assertEquals("Content type should match", TEST_CONTENT_TYPE, metadata.getContentType());
    }

    @Test
    public void testListFiles() {
        // 上传测试文件
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        seaweedFsS3Service.uploadFile(TEST_KEY, testData, TEST_CONTENT_TYPE);

        // 列出文件
        List<S3ObjectSummary> files = seaweedFsS3Service.listFiles("test/");
        assertNotNull("File list should not be null", files);
        assertTrue("Should contain at least one file", files.size() > 0);
        
        boolean found = files.stream().anyMatch(file -> TEST_KEY.equals(file.getKey()));
        assertTrue("Should find the uploaded file", found);
    }

    @Test
    public void testCopyFile() {
        String sourceKey = TEST_KEY;
        String destinationKey = "test/copy-sample.txt";
        
        try {
            // 上传源文件
            byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
            seaweedFsS3Service.uploadFile(sourceKey, testData, TEST_CONTENT_TYPE);

            // 复制文件
            CopyObjectResult copyResult = seaweedFsS3Service.copyFile(sourceKey, destinationKey);
            assertNotNull("Copy result should not be null", copyResult);

            // 验证复制的文件存在
            assertTrue("Copied file should exist", seaweedFsS3Service.doesFileExist(destinationKey));

            // 验证内容一致
            byte[] originalData = seaweedFsS3Service.getFileBytes(sourceKey);
            byte[] copiedData = seaweedFsS3Service.getFileBytes(destinationKey);
            assertArrayEquals("Copied file content should match original", originalData, copiedData);
            
        } finally {
            // 清理复制的文件
            if (seaweedFsS3Service.doesFileExist(destinationKey)) {
                seaweedFsS3Service.deleteFile(destinationKey);
            }
        }
    }

    @Test
    public void testDeleteFile() {
        // 上传文件
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        seaweedFsS3Service.uploadFile(TEST_KEY, testData, TEST_CONTENT_TYPE);
        
        // 确认文件存在
        assertTrue("File should exist before deletion", seaweedFsS3Service.doesFileExist(TEST_KEY));

        // 删除文件
        seaweedFsS3Service.deleteFile(TEST_KEY);
        
        // 确认文件已删除
        assertFalse("File should not exist after deletion", seaweedFsS3Service.doesFileExist(TEST_KEY));
    }

    @Test
    public void testGeneratePresignedUrl() {
        // 上传文件
        byte[] testData = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        seaweedFsS3Service.uploadFile(TEST_KEY, testData, TEST_CONTENT_TYPE);

        // 生成预签名URL
        java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 3600000); // 1小时后过期
        java.net.URL presignedUrl = seaweedFsS3Service.generatePresignedUrl(TEST_KEY, expiration);
        
        assertNotNull("Presigned URL should not be null", presignedUrl);
        assertTrue("URL should contain the endpoint", presignedUrl.toString().contains(properties.getEndpoint()));
        assertTrue("URL should contain the key", presignedUrl.toString().contains(TEST_KEY));
    }
}