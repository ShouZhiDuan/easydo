package com.example.filejson.controller;

import com.example.filejson.model.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileJsonController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/file-json")
    public ResponseEntity<MultiValueMap<String, Object>> getFileAndJson(
            @RequestParam(value = "filePath", required = false, defaultValue = "sample.txt") String filePath) {
        
        try {
            // 创建示例文件（如果不存在）
            createSampleFileIfNotExists();
            
            // 获取文件
            Path path = Paths.get("sample.txt");
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource fileResource = new FileSystemResource(path.toFile());
            
            // 创建自定义JSON对象
            Map<String, Object> customData = new HashMap<>();
            customData.put("userId", 12345);
            customData.put("userName", "张三");
            customData.put("fileSize", Files.size(path));
            customData.put("fileName", path.getFileName().toString());
            
            ResponseData responseData = new ResponseData(
                "文件和数据获取成功", 
                "success", 
                customData
            );
            
            // 将JSON对象转换为字符串
            String jsonString = objectMapper.writeValueAsString(responseData);
            
            // 创建多部分响应
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", fileResource);
            parts.add("json", jsonString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            return new ResponseEntity<>(parts, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            // 错误处理
            ResponseData errorResponse = new ResponseData(
                "文件读取失败: " + e.getMessage(), 
                "error"
            );
            
            try {
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                MultiValueMap<String, Object> errorParts = new LinkedMultiValueMap<>();
                errorParts.add("json", errorJson);
                
                return new ResponseEntity<>(errorParts, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception jsonException) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    @PostMapping("/file-json-alternative")
    public ResponseEntity<String> getFileAndJsonAlternative(
            @RequestParam(value = "filePath", required = false, defaultValue = "sample.txt") String filePath) {
        
        try {
            // 创建示例文件（如果不存在）
            createSampleFileIfNotExists();
            
            // 获取文件内容
            Path path = Paths.get("sample.txt");
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(path);
            String fileContentBase64 = java.util.Base64.getEncoder().encodeToString(fileContent);
            
            // 创建自定义JSON对象
            Map<String, Object> customData = new HashMap<>();
            customData.put("userId", 12345);
            customData.put("userName", "李四");
            customData.put("fileSize", fileContent.length);
            customData.put("fileName", path.getFileName().toString());
            
            // 创建包含文件和数据的完整响应
            Map<String, Object> completeResponse = new HashMap<>();
            completeResponse.put("fileContent", fileContentBase64);
            completeResponse.put("fileInfo", customData);
            completeResponse.put("message", "文件和数据获取成功");
            completeResponse.put("status", "success");
            completeResponse.put("timestamp", System.currentTimeMillis());
            
            String jsonResponse = objectMapper.writeValueAsString(completeResponse);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            // 错误处理
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "文件读取失败: " + e.getMessage());
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            try {
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                return new ResponseEntity<>(errorJson, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception jsonException) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    private void createSampleFileIfNotExists() throws IOException {
        Path sampleFile = Paths.get("sample.txt");
        if (!Files.exists(sampleFile)) {
            String content = "这是一个示例文件内容。\n" +
                           "文件创建时间: " + new java.util.Date() + "\n" +
                           "这个文件用于演示Spring Boot同时返回文件流和JSON对象的功能。";
            Files.write(sampleFile, content.getBytes("UTF-8"));
        }
    }
}