package com.techzhi.test.s3.seaweedfs.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.techzhi.common.s3.seaweedfs.service.SeaweedFsS3Service;
import com.techzhi.common.s3.seaweedfs.util.SeaweedFsS3Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件操作控制器
 * 提供文件上传、下载、删除等REST API
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@Tag(name = "文件操作", description = "SeaweedFS S3 文件操作接口")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private SeaweedFsS3Service seaweedFsS3Service;

    /**
     * 上传文件
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件路径前缀（可选）") @RequestParam(value = "prefix", required = false, defaultValue = "uploads") String prefix) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            String originalFilename = file.getOriginalFilename();
            String key = prefix + "/" + System.currentTimeMillis() + "_" + originalFilename;
            
            logger.info("Uploading file: {} with key: {}", originalFilename, key);
            
            // 使用工具类上传
            SeaweedFsS3Util.upload(key, file.getBytes(), file.getContentType());
            
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("key", key);
            result.put("originalFilename", originalFilename);
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());
            
            logger.info("File uploaded successfully: {}", key);
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 下载文件
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download/{key:.+}")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "文件键名") @PathVariable String key) {
        
        try {
            logger.info("Downloading file with key: {}", key);
            
            if (!SeaweedFsS3Util.exists(key)) {
                logger.warn("File not found: {}", key);
                return ResponseEntity.notFound().build();
            }
            
            byte[] data = SeaweedFsS3Util.getBytes(key);
            ObjectMetadata metadata = SeaweedFsS3Util.getMetadata(key);
            
            String filename = key.substring(key.lastIndexOf('/') + 1);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);
            
            logger.info("File downloaded successfully: {}", key);
            return ResponseEntity.ok().headers(headers).body(data);
            
        } catch (Exception e) {
            logger.error("Failed to download file: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/delete/{key:.+}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "文件键名") @PathVariable String key) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("Deleting file with key: {}", key);
            
            if (!SeaweedFsS3Util.exists(key)) {
                result.put("success", false);
                result.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            
            SeaweedFsS3Util.delete(key);
            
            result.put("success", true);
            result.put("message", "文件删除成功");
            result.put("key", key);
            
            logger.info("File deleted successfully: {}", key);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", key, e);
            result.put("success", false);
            result.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 检查文件是否存在
     */
    @Operation(summary = "检查文件是否存在")
    @GetMapping("/exists/{key:.+}")
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @Parameter(description = "文件键名") @PathVariable String key) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = SeaweedFsS3Util.exists(key);
            
            result.put("key", key);
            result.put("exists", exists);
            
            if (exists) {
                ObjectMetadata metadata = SeaweedFsS3Util.getMetadata(key);
                result.put("size", metadata.getContentLength());
                result.put("contentType", metadata.getContentType());
                result.put("lastModified", metadata.getLastModified());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to check file existence: {}", key, e);
            result.put("key", key);
            result.put("exists", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 列出文件
     */
    @Operation(summary = "列出文件")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listFiles(
            @Parameter(description = "文件前缀（可选）") @RequestParam(value = "prefix", required = false) String prefix,
            @Parameter(description = "最大数量") @RequestParam(value = "maxKeys", required = false, defaultValue = "100") int maxKeys) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<S3ObjectSummary> files = SeaweedFsS3Util.list(prefix);
            
            // 限制返回数量
            if (files.size() > maxKeys) {
                files = files.stream().limit(maxKeys).collect(Collectors.toList());
            }
            
            List<Map<String, Object>> fileList = files.stream().map(file -> {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("key", file.getKey());
                fileInfo.put("size", file.getSize());
                fileInfo.put("lastModified", file.getLastModified());
                fileInfo.put("etag", file.getETag());
                return fileInfo;
            }).collect(Collectors.toList());
            
            result.put("files", fileList);
            result.put("count", fileList.size());
            result.put("prefix", prefix);
            result.put("bucketName", SeaweedFsS3Util.getBucketName());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to list files", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 生成预签名URL
     */
    @Operation(summary = "生成预签名URL")
    @GetMapping("/presigned-url/{key:.+}")
    public ResponseEntity<Map<String, Object>> generatePresignedUrl(
            @Parameter(description = "文件键名") @PathVariable String key,
            @Parameter(description = "过期时间（小时）") @RequestParam(value = "expireHours", required = false, defaultValue = "1") int expireHours) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!SeaweedFsS3Util.exists(key)) {
                result.put("success", false);
                result.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + expireHours * 3600000L);
            URL presignedUrl = SeaweedFsS3Util.generatePresignedUrl(key, expiration);
            
            result.put("success", true);
            result.put("key", key);
            result.put("presignedUrl", presignedUrl.toString());
            result.put("expiration", expiration);
            result.put("expireHours", expireHours);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to generate presigned URL for: {}", key, e);
            result.put("success", false);
            result.put("message", "生成预签名URL失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 复制文件
     */
    @Operation(summary = "复制文件")
    @PostMapping("/copy")
    public ResponseEntity<Map<String, Object>> copyFile(
            @Parameter(description = "源文件键名") @RequestParam("sourceKey") String sourceKey,
            @Parameter(description = "目标文件键名") @RequestParam("destinationKey") String destinationKey) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!SeaweedFsS3Util.exists(sourceKey)) {
                result.put("success", false);
                result.put("message", "源文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            
            if (SeaweedFsS3Util.exists(destinationKey)) {
                result.put("success", false);
                result.put("message", "目标文件已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
            SeaweedFsS3Util.copy(sourceKey, destinationKey);
            
            result.put("success", true);
            result.put("message", "文件复制成功");
            result.put("sourceKey", sourceKey);
            result.put("destinationKey", destinationKey);
            
            logger.info("File copied successfully from {} to {}", sourceKey, destinationKey);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to copy file from {} to {}", sourceKey, destinationKey, e);
            result.put("success", false);
            result.put("message", "文件复制失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取系统信息
     */
    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("bucketName", SeaweedFsS3Util.getBucketName());
            result.put("totalFiles", SeaweedFsS3Util.listAll().size());
            result.put("timestamp", System.currentTimeMillis());
            result.put("status", "running");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to get system info", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}