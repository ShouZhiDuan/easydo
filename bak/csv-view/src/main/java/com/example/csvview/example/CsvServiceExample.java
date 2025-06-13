package com.example.csvview.example;

import com.example.csvview.model.CsvData;
import com.example.csvview.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * CsvService重载方法使用示例
 * 演示如何直接使用File和InputStream进行CSV预览和Excel下载
 */
@Component
public class CsvServiceExample {

    @Autowired
    private CsvService csvService;

    /**
     * 示例1：使用File直接预览CSV数据
     */
    public void examplePreviewFromFile() {
        try {
            // 创建File对象
            File csvFile = new File("/path/to/your/data.csv");
            
            // 方法1：预览全部数据
            CsvData fullData = csvService.previewCsvFromFile(csvFile, "data.csv");
            System.out.println("文件名: " + fullData.getFileName());
            System.out.println("总行数: " + fullData.getTotalRows());
            System.out.println("表头: " + fullData.getHeaders());
            
            // 方法2：分页预览数据（第1页，每页10条）
            CsvData pagedData = csvService.previewCsvFromFile(csvFile, "data.csv", 0, 10);
            System.out.println("分页数据行数: " + pagedData.getRows().size());
            
        } catch (IOException e) {
            System.err.println("文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 示例2：使用InputStream直接预览CSV数据
     */
    public void examplePreviewFromInputStream() {
        try {
            // 创建CSV数据字符串
            String csvContent = "姓名,年龄,城市\n张三,25,北京\n李四,30,上海\n王五,28,广州";
            
            // 转换为InputStream
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            
            // 方法1：预览全部数据
            CsvData fullData = csvService.previewCsvFromInputStream(inputStream, "sample.csv");
            System.out.println("文件名: " + fullData.getFileName());
            System.out.println("总行数: " + fullData.getTotalRows());
            System.out.println("表头: " + fullData.getHeaders());
            
            // 重新创建InputStream（因为上面已经读取完毕）
            inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            
            // 方法2：分页预览数据（第1页，每页2条）
            CsvData pagedData = csvService.previewCsvFromInputStream(inputStream, "sample.csv", 0, 2);
            System.out.println("分页数据行数: " + pagedData.getRows().size());
            
        } catch (IOException e) {
            System.err.println("数据读取失败: " + e.getMessage());
        }
    }

    /**
     * 示例3：使用File直接转换为Excel
     */
    public void exampleConvertFileToExcel() {
        try {
            // 创建File对象
            File csvFile = new File("/path/to/your/data.csv");
            
            // 直接转换为Excel字节数组
            byte[] excelBytes = csvService.convertFileToExcel(csvFile, "data.csv");
            
            // 保存Excel文件
            try (FileOutputStream fos = new FileOutputStream("output.xlsx")) {
                fos.write(excelBytes);
                System.out.println("Excel文件已保存: output.xlsx");
            }
            
        } catch (IOException e) {
            System.err.println("Excel转换失败: " + e.getMessage());
        }
    }

    /**
     * 示例4：使用InputStream直接转换为Excel
     */
    public void exampleConvertInputStreamToExcel() {
        try {
            // 创建CSV数据字符串
            String csvContent = "姓名,年龄,城市,职业\n张三,25,北京,工程师\n李四,30,上海,设计师\n王五,28,广州,产品经理";
            
            // 转换为InputStream
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            
            // 直接转换为Excel字节数组
            byte[] excelBytes = csvService.convertInputStreamToExcel(inputStream, "sample.csv");
            
            // 保存Excel文件
            try (FileOutputStream fos = new FileOutputStream("sample_output.xlsx")) {
                fos.write(excelBytes);
                System.out.println("Excel文件已保存: sample_output.xlsx");
            }
            
        } catch (IOException e) {
            System.err.println("Excel转换失败: " + e.getMessage());
        }
    }

    /**
     * 示例5：从文件路径字符串创建File对象进行处理
     */
    public void exampleFromFilePath(String filePath) {
        try {
            File csvFile = new File(filePath);
            
            // 检查文件是否存在
            if (!csvFile.exists()) {
                System.err.println("文件不存在: " + filePath);
                return;
            }
            
            // 检查是否为CSV文件
            if (!filePath.toLowerCase().endsWith(".csv")) {
                System.err.println("不是CSV文件: " + filePath);
                return;
            }
            
            // 预览数据
            CsvData csvData = csvService.previewCsvFromFile(csvFile, csvFile.getName());
            System.out.println("成功读取文件: " + csvData.getFileName());
            System.out.println("数据行数: " + csvData.getTotalRows());
            
            // 转换为Excel
            byte[] excelBytes = csvService.convertFileToExcel(csvFile, csvFile.getName());
            System.out.println("Excel数据大小: " + excelBytes.length + " bytes");
            
        } catch (IOException e) {
            System.err.println("处理文件失败: " + e.getMessage());
        }
    }

    /**
     * 示例6：处理网络流或其他输入源
     */
    public void exampleFromNetworkStream() {
        try {
            // 模拟从网络或其他源获取的InputStream
            // 实际使用中可能是 URL.openStream() 或其他数据源
            String networkData = "产品名称,价格,库存\niPhone,6999,100\nAndroid,3999,200\nLaptop,8999,50";
            InputStream networkStream = new ByteArrayInputStream(networkData.getBytes(StandardCharsets.UTF_8));
            
            // 直接处理网络流数据
            CsvData csvData = csvService.previewCsvFromInputStream(networkStream, "network_data.csv");
            System.out.println("网络数据处理成功");
            System.out.println("表头: " + csvData.getHeaders());
            System.out.println("数据行数: " + csvData.getTotalRows());
            
            // 重新创建流进行Excel转换
            networkStream = new ByteArrayInputStream(networkData.getBytes(StandardCharsets.UTF_8));
            byte[] excelBytes = csvService.convertInputStreamToExcel(networkStream, "network_data.csv");
            System.out.println("Excel转换完成，大小: " + excelBytes.length + " bytes");
            
        } catch (IOException e) {
            System.err.println("网络数据处理失败: " + e.getMessage());
        }
    }
}