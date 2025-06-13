package com.example.excelviewer.controller;

import com.example.excelviewer.model.ExcelData;
import com.example.excelviewer.service.ExcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExcelController.class)
class ExcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelService excelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPreviewExcel_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "test content".getBytes()
        );
        
        // 创建测试用的ExcelData
        ExcelData.SheetData sheetData = new ExcelData.SheetData();
        sheetData.setSheetName("Sheet1");
        sheetData.setHeaders(Arrays.asList("列1", "列2"));
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("列1", "值1");
        row1.put("列2", "值2");
        sheetData.setRows(Arrays.asList(row1));
        
        ExcelData excelData = new ExcelData("test.xlsx", Arrays.asList(sheetData));
        
        // 模拟服务层调用
        when(excelService.parseExcelFile(any(MockMultipartFile.class))).thenReturn(excelData);
        
        // 执行测试
        mockMvc.perform(multipart("/api/excel/preview")
                .file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件解析成功"))
                .andExpect(jsonPath("$.data.fileName").value("test.xlsx"))
                .andExpect(jsonPath("$.data.sheets").isArray())
                .andExpect(jsonPath("$.data.sheets[0].sheetName").value("Sheet1"))
                .andExpect(jsonPath("$.data.sheets[0].headers").isArray())
                .andExpect(jsonPath("$.data.sheets[0].rows").isArray())
                .andExpect(jsonPath("$.data.sheets[0].totalRows").value(1));
    }

    @Test
    void testPreviewExcel_IllegalArgumentException() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "not excel content".getBytes()
        );
        
        // 模拟服务层抛出异常
        when(excelService.parseExcelFile(any(MockMultipartFile.class)))
            .thenThrow(new IllegalArgumentException("文件格式不正确，仅支持.xlsx和.xls格式"));
        
        // 执行测试
        mockMvc.perform(multipart("/api/excel/preview")
                .file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件格式不正确，仅支持.xlsx和.xls格式"));
    }

    @Test
    void testPreviewExcel_IOException() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "corrupted.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "corrupted content".getBytes()
        );
        
        // 模拟服务层抛出IOException
        when(excelService.parseExcelFile(any(MockMultipartFile.class)))
            .thenThrow(new IOException("文件读取失败"));
        
        // 执行测试
        mockMvc.perform(multipart("/api/excel/preview")
                .file(file))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件读取失败: 文件读取失败"));
    }

    @Test
    void testPreviewExcel_GeneralException() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "test content".getBytes()
        );
        
        // 模拟服务层抛出一般异常
        when(excelService.parseExcelFile(any(MockMultipartFile.class)))
            .thenThrow(new RuntimeException("系统内部错误"));
        
        // 执行测试
        mockMvc.perform(multipart("/api/excel/preview")
                .file(file))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统错误: 系统内部错误"));
    }

    @Test
    void testPreviewServerFile_Success() throws Exception {
        // 创建测试用的ExcelData
        ExcelData.SheetData sheetData = new ExcelData.SheetData();
        sheetData.setSheetName("Sheet1");
        sheetData.setHeaders(Arrays.asList("列1", "列2"));
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("列1", "值1");
        row1.put("列2", "值2");
        sheetData.setRows(Arrays.asList(row1));
        
        ExcelData excelData = new ExcelData("test.xlsx", Arrays.asList(sheetData));
        
        // 模拟服务层调用
        when(excelService.parseExcelFile("test.xlsx")).thenReturn(excelData);
        
        // 执行测试
        mockMvc.perform(get("/api/excel/preview/test.xlsx"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件解析成功"))
                .andExpect(jsonPath("$.data.fileName").value("test.xlsx"));
    }

    @Test
    void testPreviewServerFile_FileNotFound() throws Exception {
        // 模拟服务层抛出IOException（文件不存在）
        when(excelService.parseExcelFile("nonexistent.xlsx"))
            .thenThrow(new IOException("文件不存在: nonexistent.xlsx"));
        
        // 执行测试
        mockMvc.perform(get("/api/excel/preview/nonexistent.xlsx"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件读取失败: 文件不存在: nonexistent.xlsx"));
    }

    @Test
    void testPreviewServerFile_IllegalArgumentException() throws Exception {
        // 模拟服务层抛出IllegalArgumentException
        when(excelService.parseExcelFile("invalid.txt"))
            .thenThrow(new IllegalArgumentException("文件格式不正确"));
        
        // 执行测试
        mockMvc.perform(get("/api/excel/preview/invalid.txt"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件格式不正确"));
    }

    @Test
    void testHealth() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/excel/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Excel Viewer Service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testPreviewExcel_NoFileParameter() throws Exception {
        // 执行测试 - 不提供file参数
        mockMvc.perform(post("/api/excel/preview")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCorsHeaders() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "test content".getBytes()
        );
        
        ExcelData excelData = new ExcelData("test.xlsx", Arrays.asList());
        when(excelService.parseExcelFile(any(MockMultipartFile.class))).thenReturn(excelData);
        
        // 执行测试并验证CORS头
        mockMvc.perform(multipart("/api/excel/preview")
                .file(file)
                .header("Origin", "http://localhost:3000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
} 