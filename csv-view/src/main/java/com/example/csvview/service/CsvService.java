package com.example.csvview.service;

import com.example.csvview.model.CsvData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CsvService {

    // 内存存储，实际项目中可以使用Redis或数据库
    private final Map<String, CsvData> fileStorage = new ConcurrentHashMap<>();

    /**
     * 保存上传的MultipartFile并解析
     * @param file 上传的CSV文件(MultipartFile)
     * @return 文件ID
     * @throws IOException 文件读取异常
     */
    public String saveUploadedFile(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        CsvData csvData = parseCsvFile(file);
        fileStorage.put(fileId, csvData);
        return fileId;
    }
    
    /**
     * 保存File对象并解析
     * @param file 本地CSV文件(File)
     * @param fileName 文件名
     * @return 文件ID
     * @throws IOException 文件读取异常
     */
    public String saveFile(File file, String fileName) throws IOException {
        String fileId = UUID.randomUUID().toString();
        CsvData csvData = parseCsvFile(file, fileName);
        fileStorage.put(fileId, csvData);
        return fileId;
    }
    
    /**
     * 保存InputStream并解析
     * @param inputStream CSV数据输入流
     * @param fileName 文件名
     * @return 文件ID
     * @throws IOException 文件读取异常
     */
    public String saveInputStream(InputStream inputStream, String fileName) throws IOException {
        String fileId = UUID.randomUUID().toString();
        CsvData csvData = parseCsvFile(inputStream, fileName);
        fileStorage.put(fileId, csvData);
        return fileId;
    }

    /**
     * 根据文件ID获取CSV数据
     * @param fileId 文件ID
     * @return CsvData对象，如果不存在返回null
     */
    public CsvData getCsvDataById(String fileId) {
        return fileStorage.get(fileId);
    }

    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileId) {
        return fileStorage.remove(fileId) != null;
    }

    /**
     * 从MultipartFile读取CSV文件并解析为CsvData对象
     * @param file 上传的CSV文件
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    private CsvData parseCsvFile(MultipartFile file) throws IOException {
        return parseCsvFile(file.getInputStream(), file.getOriginalFilename());
    }
    
    /**
     * 从File读取CSV文件并解析为CsvData对象
     * @param file 本地CSV文件
     * @param fileName 文件名
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    private CsvData parseCsvFile(File file, String fileName) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return parseCsvFile(inputStream, fileName);
        }
    }
    
    /**
     * 从Path读取CSV文件并解析为CsvData对象
     * @param path 文件路径
     * @param fileName 文件名
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    private CsvData parseCsvFile(Path path, String fileName) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return parseCsvFile(inputStream, fileName);
        }
    }
    
    /**
     * 从InputStream读取CSV文件并解析为CsvData对象
     * 保持原始列顺序
     * @param inputStream CSV数据输入流
     * @param fileName 文件名
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    private CsvData parseCsvFile(InputStream inputStream, String fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // 获取表头，保持原始顺序
            headers.addAll(csvParser.getHeaderNames());

            // 读取数据行
            for (CSVRecord csvRecord : csvParser) {
                List<String> row = new ArrayList<>();
                // 按照表头顺序读取每列数据
                for (String header : headers) {
                    String value = csvRecord.get(header);
                    row.add(value != null ? value : "");
                }
                rows.add(row);
            }
        }

        return new CsvData(headers, rows, fileName);
    }

    /**
     * 将CSV数据转换为Excel格式的字节数组
     * 保持原始列顺序
     * @param csvData CSV数据对象
     * @return Excel格式的字节数组
     * @throws IOException IO异常
     */
    public byte[] convertToExcel(CsvData csvData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("CSV Data");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 写入表头，保持原始顺序
            Row headerRow = sheet.createRow(0);
            List<String> headers = csvData.getHeaders();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 写入数据行，保持原始列顺序
            List<List<String>> rows = csvData.getRows();
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row dataRow = sheet.createRow(rowIndex + 1);
                List<String> rowData = rows.get(rowIndex);
                
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = dataRow.createCell(colIndex);
                    String cellValue = rowData.get(colIndex);
                    cell.setCellValue(cellValue != null ? cellValue : "");
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 获取CSV数据的分页预览
     */
    public CsvData getPagedData(CsvData csvData, int page, int size) {
        if (csvData == null || csvData.getRows() == null) {
            return csvData;
        }

        List<List<String>> allRows = csvData.getRows();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allRows.size());

        if (startIndex >= allRows.size()) {
            return new CsvData(csvData.getHeaders(), new ArrayList<>(), csvData.getFileName());
        }

        List<List<String>> pagedRows = allRows.subList(startIndex, endIndex);
        CsvData pagedData = new CsvData(csvData.getHeaders(), pagedRows, csvData.getFileName());
        pagedData.setTotalRows(csvData.getTotalRows());
        
        return pagedData;
    }

    // ==================== 重载方法：直接处理File和InputStream ====================

    /**
     * 直接从File预览CSV数据（重载方法）
     * @param file CSV文件
     * @param fileName 文件名
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    public CsvData previewCsvFromFile(File file, String fileName) throws IOException {
        return parseCsvFile(file, fileName);
    }

    /**
     * 直接从File预览CSV数据（分页，重载方法）
     * @param file CSV文件
     * @param fileName 文件名
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页后的CsvData对象
     * @throws IOException 文件读取异常
     */
    public CsvData previewCsvFromFile(File file, String fileName, int page, int size) throws IOException {
        CsvData csvData = parseCsvFile(file, fileName);
        return getPagedData(csvData, page, size);
    }

    /**
     * 直接从InputStream预览CSV数据（重载方法）
     * @param inputStream CSV数据输入流
     * @param fileName 文件名
     * @return 解析后的CsvData对象
     * @throws IOException 文件读取异常
     */
    public CsvData previewCsvFromInputStream(InputStream inputStream, String fileName) throws IOException {
        return parseCsvFile(inputStream, fileName);
    }

    /**
     * 直接从InputStream预览CSV数据（分页，重载方法）
     * @param inputStream CSV数据输入流
     * @param fileName 文件名
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页后的CsvData对象
     * @throws IOException 文件读取异常
     */
    public CsvData previewCsvFromInputStream(InputStream inputStream, String fileName, int page, int size) throws IOException {
        CsvData csvData = parseCsvFile(inputStream, fileName);
        return getPagedData(csvData, page, size);
    }

    /**
     * 直接从File转换为Excel（重载方法）
     * @param file CSV文件
     * @param fileName 文件名
     * @return Excel格式的字节数组
     * @throws IOException 文件读取异常
     */
    public byte[] convertFileToExcel(File file, String fileName) throws IOException {
        CsvData csvData = parseCsvFile(file, fileName);
        return convertToExcel(csvData);
    }

    /**
     * 直接从InputStream转换为Excel（重载方法）
     * @param inputStream CSV数据输入流
     * @param fileName 文件名
     * @return Excel格式的字节数组
     * @throws IOException 文件读取异常
     */
    public byte[] convertInputStreamToExcel(InputStream inputStream, String fileName) throws IOException {
        CsvData csvData = parseCsvFile(inputStream, fileName);
        return convertToExcel(csvData);
    }
}