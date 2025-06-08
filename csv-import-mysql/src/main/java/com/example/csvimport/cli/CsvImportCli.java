package com.example.csvimport.cli;

import com.example.csvimport.config.DatabaseConfig;
import com.example.csvimport.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvImportCli implements CommandLineRunner {
    
    private final CsvImportService csvImportService;
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "--interactive".equals(args[0])) {
            runInteractiveMode();
        } else if (args.length >= 4) {
            runCommandLineMode(args);
        } else {
            printUsage();
        }
    }
    
    /**
     * 交互式模式
     */
    private void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== CSV导入工具 - 交互式模式 ===");
            
            // 获取CSV文件路径
            System.out.print("请输入CSV文件路径: ");
            String csvFilePath = scanner.nextLine().trim();
            
            // 获取数据库连接信息
            System.out.print("请输入数据库URL (例: jdbc:mysql://localhost:3306/testdb): ");
            String dbUrl = scanner.nextLine().trim();
            
            System.out.print("请输入数据库用户名: ");
            String dbUsername = scanner.nextLine().trim();
            
            System.out.print("请输入数据库密码: ");
            String dbPassword = scanner.nextLine().trim();
            
            // 获取可选参数
            System.out.print("请输入需要创建索引的列名 (多个列用逗号分隔，直接回车跳过): ");
            String indexColumnsInput = scanner.nextLine().trim();
            List<String> indexColumns = null;
            if (!indexColumnsInput.isEmpty()) {
                indexColumns = Arrays.asList(indexColumnsInput.split(","));
                indexColumns.replaceAll(String::trim);
            }
            
            System.out.print("请输入批次大小 (默认1000): ");
            String batchSizeInput = scanner.nextLine().trim();
            int batchSize = batchSizeInput.isEmpty() ? 1000 : Integer.parseInt(batchSizeInput);
            
            System.out.print("请输入类型推断样本大小 (默认1000): ");
            String sampleSizeInput = scanner.nextLine().trim();
            int sampleSize = sampleSizeInput.isEmpty() ? 1000 : Integer.parseInt(sampleSizeInput);
            
            // 创建数据库配置
            DatabaseConfig databaseConfig = new DatabaseConfig(dbUrl, dbUsername, dbPassword);
            
            // 执行导入
            System.out.println("\n开始导入...");
            csvImportService.importCsv(csvFilePath, databaseConfig, indexColumns, batchSize, sampleSize);
            
            System.out.println("导入完成!");
            
        } catch (Exception e) {
            log.error("交互式模式执行失败", e);
            System.err.println("错误: " + e.getMessage());
        } finally {
            csvImportService.closeConnection();
            scanner.close();
        }
    }
    
    /**
     * 命令行模式
     */
    private void runCommandLineMode(String[] args) {
        try {
            String csvFilePath = args[0];
            String dbUrl = args[1];
            String dbUsername = args[2];
            String dbPassword = args[3];
            
            List<String> indexColumns = null;
            if (args.length > 4 && !args[4].isEmpty()) {
                indexColumns = Arrays.asList(args[4].split(","));
                indexColumns.replaceAll(String::trim);
            }
            
            int batchSize = args.length > 5 ? Integer.parseInt(args[5]) : 1000;
            int sampleSize = args.length > 6 ? Integer.parseInt(args[6]) : 1000;
            
            DatabaseConfig databaseConfig = new DatabaseConfig(dbUrl, dbUsername, dbPassword);
            
            log.info("开始命令行模式导入: {}", csvFilePath);
            csvImportService.importCsv(csvFilePath, databaseConfig, indexColumns, batchSize, sampleSize);
            
            log.info("命令行模式导入完成");
            
        } catch (Exception e) {
            log.error("命令行模式执行失败", e);
            System.err.println("错误: " + e.getMessage());
            System.exit(1);
        } finally {
            csvImportService.closeConnection();
        }
    }
    
    /**
     * 打印使用说明
     */
    private void printUsage() {
        System.out.println("CSV导入工具使用说明:");
        System.out.println();
        System.out.println("交互式模式:");
        System.out.println("  java -jar csv-import.jar --interactive");
        System.out.println();
        System.out.println("命令行模式:");
        System.out.println("  java -jar csv-import.jar <csv文件路径> <数据库URL> <用户名> <密码> [索引列] [批次大小] [样本大小]");
        System.out.println();
        System.out.println("参数说明:");
        System.out.println("  csv文件路径    - CSV文件的完整路径");
        System.out.println("  数据库URL      - MySQL数据库连接URL");
        System.out.println("  用户名         - 数据库用户名");
        System.out.println("  密码           - 数据库密码");
        System.out.println("  索引列         - 需要创建索引的列名，多个用逗号分隔 (可选)");
        System.out.println("  批次大小       - 批量插入的记录数，默认1000 (可选)");
        System.out.println("  样本大小       - 类型推断的样本记录数，默认1000 (可选)");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar csv-import.jar /path/to/data.csv jdbc:mysql://localhost:3306/testdb root password id,name 2000 500");
    }
}