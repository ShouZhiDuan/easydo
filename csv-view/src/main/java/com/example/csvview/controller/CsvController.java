package com.example.csvview.controller;

import com.example.csvview.model.CsvData;
import com.example.csvview.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/csv")
@CrossOrigin(origins = "*")
public class CsvController {

    @Autowired
    private CsvService csvService;

    /**
     * 显示主页面
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * API测试页面
     */
    @GetMapping("/api-test")
    public String apiTestPage() {
        return "api-test";
    }

    /**
     * 多数据源测试页面
     */
    @GetMapping("/multi-source-test")
    public String multiSourceTestPage() {
        return "multi-source-test";
    }

    /**
     * 上传CSV文件 - REST API (MultipartFile)
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择一个CSV文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                response.put("success", false);
                response.put("message", "请上传CSV格式的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 解析CSV文件并保存
            String fileId = csvService.saveUploadedFile(file);
            CsvData csvData = csvService.getCsvDataById(fileId);
            
            response.put("success", true);
            response.put("message", "CSV文件上传成功");
            response.put("fileId", fileId);
            response.put("fileName", csvData.getFileName());
            response.put("totalRows", csvData.getTotalRows());
            response.put("totalColumns", csvData.getHeaders().size());
            response.put("headers", csvData.getHeaders());
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理文件时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 处理本地文件路径 - REST API (File)
     */
    @PostMapping("/api/upload-file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadLocalFile(@RequestParam("filePath") String filePath,
                                                               @RequestParam(value = "fileName", required = false) String fileName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            java.io.File file = new java.io.File(filePath);
            
            if (!file.exists()) {
                response.put("success", false);
                response.put("message", "文件不存在: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!file.isFile()) {
                response.put("success", false);
                response.put("message", "路径不是文件: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }

            // 如果没有提供文件名，使用文件的名称
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = file.getName();
            }
            
            // 检查文件类型
            if (!fileName.toLowerCase().endsWith(".csv")) {
                response.put("success", false);
                response.put("message", "请提供CSV格式的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 解析CSV文件并保存
            String fileId = csvService.saveFile(file, fileName);
            CsvData csvData = csvService.getCsvDataById(fileId);
            
            response.put("success", true);
            response.put("message", "CSV文件处理成功");
            response.put("fileId", fileId);
            response.put("fileName", csvData.getFileName());
            response.put("totalRows", csvData.getTotalRows());
            response.put("totalColumns", csvData.getHeaders().size());
            response.put("headers", csvData.getHeaders());
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理文件时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 处理InputStream数据 - REST API (InputStream)
     * 通过请求体接收CSV数据
     */
    @PostMapping("/api/upload-stream")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadStream(@RequestParam("fileName") String fileName,
                                                           HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查文件类型
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                response.put("success", false);
                response.put("message", "请提供CSV格式的文件名");
                return ResponseEntity.badRequest().body(response);
            }

            // 从请求体获取InputStream
            try (InputStream inputStream = request.getInputStream()) {
                // 解析CSV数据并保存
                String fileId = csvService.saveInputStream(inputStream, fileName);
                CsvData csvData = csvService.getCsvDataById(fileId);
                
                response.put("success", true);
                response.put("message", "CSV数据处理成功");
                response.put("fileId", fileId);
                response.put("fileName", csvData.getFileName());
                response.put("totalRows", csvData.getTotalRows());
                response.put("totalColumns", csvData.getHeaders().size());
                response.put("headers", csvData.getHeaders());
                
                return ResponseEntity.ok(response);
            }

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "数据读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理数据时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 预览CSV数据 - REST API
     * 支持可选分页参数
     */
    @GetMapping("/api/preview/{fileId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewCsv(
            @PathVariable String fileId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "enablePaging", defaultValue = "true") boolean enablePaging) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CsvData csvData = csvService.getCsvDataById(fileId);
            if (csvData == null) {
                response.put("success", false);
                response.put("message", "文件不存在或已过期");
                return ResponseEntity.notFound().build();
            }

            CsvData resultData;
            if (enablePaging && page != null && size != null) {
                // 分页预览
                resultData = csvService.getPagedData(csvData, page, size);
                response.put("currentPage", page);
                response.put("pageSize", size);
                response.put("totalPages", (int) Math.ceil((double) csvData.getTotalRows() / size));
                response.put("isPaged", true);
            } else {
                // 全量预览
                resultData = csvData;
                response.put("isPaged", false);
            }
            
            response.put("success", true);
            response.put("fileName", resultData.getFileName());
            response.put("headers", resultData.getHeaders());
            response.put("rows", resultData.getRows());
            response.put("totalRows", csvData.getTotalRows());
            response.put("displayedRows", resultData.getRows().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 下载Excel文件 - REST API
     */
    @GetMapping("/api/download/{fileId}")
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String fileId) {
        try {
            CsvData csvData = csvService.getCsvDataById(fileId);
            if (csvData == null) {
                return ResponseEntity.notFound().build();
            }

            // 转换为Excel格式
            byte[] excelData = csvService.convertToExcel(csvData);

            // 生成文件名
            String originalFileName = csvData.getFileName();
            String excelFileName = originalFileName.replaceAll("\\.csv$", ".xlsx");
            if (!excelFileName.endsWith(".xlsx")) {
                excelFileName += ".xlsx";
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                URLEncoder.encode(excelFileName, StandardCharsets.UTF_8.toString()));
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除上传的文件 - REST API
     */
    @DeleteMapping("/api/files/{fileId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = csvService.deleteFile(fileId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "文件已删除");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取文件信息 - REST API
     */
    @GetMapping("/api/files/{fileId}/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            CsvData csvData = csvService.getCsvDataById(fileId);
            if (csvData == null) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("fileName", csvData.getFileName());
            response.put("totalRows", csvData.getTotalRows());
            response.put("totalColumns", csvData.getHeaders().size());
            response.put("headers", csvData.getHeaders());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}