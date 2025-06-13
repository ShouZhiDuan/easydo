package com.example.excelviewer.controller;

import com.example.excelviewer.model.ExcelData;
import com.example.excelviewer.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*") // 允许跨域访问
public class ExcelController {
    
    @Autowired
    private ExcelService excelService;
    
    /**
     * 上传并预览Excel文件
     * @param file 上传的Excel文件
     * @return Excel数据
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ExcelData excelData = excelService.parseExcelFile(file);
            response.put("success", true);
            response.put("data", excelData);
            response.put("message", "文件解析成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 预览服务器上的Excel文件
     * @param filePath 文件路径
     * @return Excel数据
     */
    @GetMapping("/preview/{filePath}")
    public ResponseEntity<Map<String, Object>> previewServerFile(@PathVariable String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ExcelData excelData = excelService.parseExcelFile(filePath);
            response.put("success", true);
            response.put("data", excelData);
            response.put("message", "文件解析成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 健康检查接口
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Excel Viewer Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 