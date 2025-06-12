package com.example.excelviewer.service;

import com.example.excelviewer.model.ExcelData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @InjectMocks
    private ExcelService excelService;

    private MockMultipartFile createTestExcelFile(boolean isXlsx) throws IOException {
        Workbook workbook = isXlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        
        // 创建第一个工作表
        Sheet sheet1 = workbook.createSheet("Sheet1");
        
        // 创建表头
        Row headerRow = sheet1.createRow(0);
        headerRow.createCell(0).setCellValue("姓名");
        headerRow.createCell(1).setCellValue("年龄");
        headerRow.createCell(2).setCellValue("薪水");
        headerRow.createCell(3).setCellValue("入职日期");
        headerRow.createCell(4).setCellValue("是否在职");
        
        // 创建数据行
        Row dataRow1 = sheet1.createRow(1);
        dataRow1.createCell(0).setCellValue("张三");
        dataRow1.createCell(1).setCellValue(28);
        dataRow1.createCell(2).setCellValue(8500.50);
        dataRow1.createCell(3).setCellValue(new Date());
        dataRow1.createCell(4).setCellValue(true);
        
        Row dataRow2 = sheet1.createRow(2);
        dataRow2.createCell(0).setCellValue("李四");
        dataRow2.createCell(1).setCellValue(32);
        dataRow2.createCell(2).setCellValue(9200.00);
        dataRow2.createCell(3).setCellValue(new Date());
        dataRow2.createCell(4).setCellValue(false);
        
        // 创建第二个工作表
        Sheet sheet2 = workbook.createSheet("部门信息");
        Row headerRow2 = sheet2.createRow(0);
        headerRow2.createCell(0).setCellValue("部门名称");
        headerRow2.createCell(1).setCellValue("部门代码");
        
        Row dataRow3 = sheet2.createRow(1);
        dataRow3.createCell(0).setCellValue("技术部");
        dataRow3.createCell(1).setCellValue("TECH001");
        
        // 将工作簿转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        String fileName = isXlsx ? "test.xlsx" : "test.xls";
        String contentType = isXlsx ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" 
                                   : "application/vnd.ms-excel";
        
        return new MockMultipartFile("file", fileName, contentType, outputStream.toByteArray());
    }

    @Test
    void testParseExcelFile_Success_Xlsx() throws IOException {
        // 准备测试数据
        MockMultipartFile file = createTestExcelFile(true);
        
        // 执行测试
        ExcelData result = excelService.parseExcelFile(file);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test.xlsx", result.getFileName());
        assertEquals(2, result.getSheets().size());
        
        // 验证第一个工作表
        ExcelData.SheetData sheet1 = result.getSheets().get(0);
        assertEquals("Sheet1", sheet1.getSheetName());
        assertEquals(5, sheet1.getHeaders().size());
        assertEquals(2, sheet1.getRows().size());
        assertEquals(2, sheet1.getTotalRows());
        
        // 验证表头
        assertTrue(sheet1.getHeaders().contains("姓名"));
        assertTrue(sheet1.getHeaders().contains("年龄"));
        assertTrue(sheet1.getHeaders().contains("薪水"));
        
        // 验证数据
        assertEquals("张三", sheet1.getRows().get(0).get("姓名"));
        assertEquals(28L, sheet1.getRows().get(0).get("年龄"));
        assertEquals(8500.5, sheet1.getRows().get(0).get("薪水"));
        assertEquals(true, sheet1.getRows().get(0).get("是否在职"));
        
        // 验证第二个工作表
        ExcelData.SheetData sheet2 = result.getSheets().get(1);
        assertEquals("部门信息", sheet2.getSheetName());
        assertEquals(2, sheet2.getHeaders().size());
        assertEquals(1, sheet2.getRows().size());
        assertEquals("技术部", sheet2.getRows().get(0).get("部门名称"));
    }

    @Test
    void testParseExcelFile_Success_Xls() throws IOException {
        // 准备测试数据
        MockMultipartFile file = createTestExcelFile(false);
        
        // 执行测试
        ExcelData result = excelService.parseExcelFile(file);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test.xls", result.getFileName());
        assertEquals(2, result.getSheets().size());
    }

    @Test
    void testParseExcelFile_NullFile() {
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> excelService.parseExcelFile((MockMultipartFile) null)
        );
        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    void testParseExcelFile_EmptyFile() {
        // 准备测试数据
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> excelService.parseExcelFile(emptyFile)
        );
        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    void testParseExcelFile_InvalidFileFormat() {
        // 准备测试数据
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", 
            "text/plain", "This is not an Excel file".getBytes());
        
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> excelService.parseExcelFile(invalidFile)
        );
        assertEquals("文件格式不正确，仅支持.xlsx和.xls格式", exception.getMessage());
    }

    @Test
    void testParseExcelFile_CorruptedFile() {
        // 准备测试数据 - 损坏的Excel文件
        MockMultipartFile corruptedFile = new MockMultipartFile("file", "corrupted.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "This is corrupted Excel content".getBytes());
        
        // 执行测试并验证异常 - 损坏的文件会抛出NotOfficeXmlFileException，它是IOException的子类
        assertThrows(Exception.class, () -> excelService.parseExcelFile(corruptedFile));
    }

    @Test
    void testParseExcelFileFromPath_NullPath() {
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> excelService.parseExcelFile((String) null)
        );
        assertEquals("文件路径不能为空", exception.getMessage());
    }

    @Test
    void testParseExcelFileFromPath_EmptyPath() {
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> excelService.parseExcelFile("")
        );
        assertEquals("文件路径不能为空", exception.getMessage());
    }

    @Test
    void testParseExcelFileFromPath_FileNotFound() {
        // 执行测试并验证异常
        IOException exception = assertThrows(
            IOException.class, 
            () -> excelService.parseExcelFile("nonexistent.xlsx")
        );
        assertTrue(exception.getMessage().contains("文件不存在"));
    }

    @Test
    void testParseEmptySheet() throws IOException {
        // 创建只有表头的Excel文件
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("EmptySheet");
        
        // 只创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("列1");
        headerRow.createCell(1).setCellValue("列2");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            outputStream.toByteArray());
        
        // 执行测试
        ExcelData result = excelService.parseExcelFile(file);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getSheets().size());
        ExcelData.SheetData sheetData = result.getSheets().get(0);
        assertEquals("EmptySheet", sheetData.getSheetName());
        assertEquals(2, sheetData.getHeaders().size());
        assertEquals(0, sheetData.getRows().size());
        assertEquals(0, sheetData.getTotalRows());
    }

    @Test
    void testParseSheetWithFormulas() throws IOException {
        // 创建包含公式的Excel文件
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("FormulaSheet");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("A");
        headerRow.createCell(1).setCellValue("B");
        headerRow.createCell(2).setCellValue("Sum");
        
        // 创建数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(10);
        dataRow.createCell(1).setCellValue(20);
        // 创建公式单元格
        Cell formulaCell = dataRow.createCell(2);
        formulaCell.setCellFormula("A2+B2");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        MockMultipartFile file = new MockMultipartFile("file", "formula.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            outputStream.toByteArray());
        
        // 执行测试
        ExcelData result = excelService.parseExcelFile(file);
        
        // 验证结果
        assertNotNull(result);
        ExcelData.SheetData sheetData = result.getSheets().get(0);
        assertEquals(1, sheetData.getRows().size());
        
        // 验证公式结果（应该返回计算结果或公式字符串）
        Object sumValue = sheetData.getRows().get(0).get("Sum");
        assertNotNull(sumValue);
    }
} 