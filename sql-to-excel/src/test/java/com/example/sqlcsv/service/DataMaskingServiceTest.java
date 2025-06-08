package com.example.sqlcsv.service;

import com.example.sqlcsv.config.DataMaskingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据脱敏服务测试
 */
@SpringBootTest
class DataMaskingServiceTest {
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    @Test
    void testMaskValue_Phone() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "phone", DataMaskingConfig.MaskingType.PHONE
        );
        
        String originalPhone = "13812345678";
        String maskedPhone = dataMaskingService.maskValue(originalPhone, rule);
        
        assertEquals("138****5678", maskedPhone);
    }
    
    @Test
    void testMaskValue_IdCard() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "id_card", DataMaskingConfig.MaskingType.ID_CARD
        );
        
        String originalIdCard = "110101199001011234";
        String maskedIdCard = dataMaskingService.maskValue(originalIdCard, rule);
        
        assertEquals("110101********1234", maskedIdCard);
    }
    
    @Test
    void testMaskValue_Email() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "email", DataMaskingConfig.MaskingType.EMAIL
        );
        
        String originalEmail = "zhangsan@example.com";
        String maskedEmail = dataMaskingService.maskValue(originalEmail, rule);
        
        assertEquals("zha***@example.com", maskedEmail);
    }
    
    @Test
    void testMaskValue_BankCard() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "bank_card", DataMaskingConfig.MaskingType.BANK_CARD
        );
        
        String originalBankCard = "6222021234567890";
        String maskedBankCard = dataMaskingService.maskValue(originalBankCard, rule);
        
        assertEquals("6222****7890", maskedBankCard);
    }
    
    @Test
    void testMaskValue_Name() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "name", DataMaskingConfig.MaskingType.NAME
        );
        
        String originalName = "张三";
        String maskedName = dataMaskingService.maskValue(originalName, rule);
        
        assertEquals("张*三", maskedName);
        
        // 测试单字名
        String singleName = "李";
        String maskedSingleName = dataMaskingService.maskValue(singleName, rule);
        assertEquals("李*李", maskedSingleName);
    }
    
    @Test
    void testMaskValue_Custom() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "custom_field", "(\\d{2})\\d*(\\d{2})", "$1****$2"
        );
        
        String originalValue = "1234567890";
        String maskedValue = dataMaskingService.maskValue(originalValue, rule);
        
        assertEquals("12****90", maskedValue);
    }
    
    @Test
    void testMaskRowData() {
        // 准备测试数据
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "张三");
        row.put("phone", "13812345678");
        row.put("email", "zhangsan@example.com");
        row.put("id_card", "110101199001011234");
        row.put("salary", 8000.00);
        
        List<String> columnNames = Arrays.asList("id", "name", "phone", "email", "id_card", "salary");
        
        // 准备脱敏规则
        List<DataMaskingConfig.FieldMaskingRule> maskingRules = Arrays.asList(
            new DataMaskingConfig.FieldMaskingRule("name", DataMaskingConfig.MaskingType.NAME),
            new DataMaskingConfig.FieldMaskingRule("phone", DataMaskingConfig.MaskingType.PHONE),
            new DataMaskingConfig.FieldMaskingRule("email", DataMaskingConfig.MaskingType.EMAIL),
            new DataMaskingConfig.FieldMaskingRule("id_card", DataMaskingConfig.MaskingType.ID_CARD)
        );
        
        // 执行脱敏
        Map<String, Object> maskedRow = dataMaskingService.maskRowData(row, columnNames, maskingRules);
        
        // 验证结果
        assertNotNull(maskedRow);
        assertEquals(1, maskedRow.get("id")); // 未脱敏字段保持原值
        assertEquals("张*三", maskedRow.get("name"));
        assertEquals("138****5678", maskedRow.get("phone"));
        assertEquals("zha***@example.com", maskedRow.get("email"));
        assertEquals("110101********1234", maskedRow.get("id_card"));
        assertEquals(8000.00, maskedRow.get("salary")); // 未脱敏字段保持原值
    }
    
    @Test
    void testMaskRowData_EmptyRules() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "张三");
        row.put("phone", "13812345678");
        
        List<String> columnNames = Arrays.asList("name", "phone");
        List<DataMaskingConfig.FieldMaskingRule> emptyRules = new ArrayList<>();
        
        Map<String, Object> result = dataMaskingService.maskRowData(row, columnNames, emptyRules);
        
        // 没有脱敏规则时，应该返回原数据
        assertEquals("张三", result.get("name"));
        assertEquals("13812345678", result.get("phone"));
    }
    
    @Test
    void testMaskRowData_NullValues() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", null);
        row.put("phone", "13812345678");
        row.put("empty_field", "");
        
        List<String> columnNames = Arrays.asList("name", "phone", "empty_field");
        List<DataMaskingConfig.FieldMaskingRule> maskingRules = Arrays.asList(
            new DataMaskingConfig.FieldMaskingRule("name", DataMaskingConfig.MaskingType.NAME),
            new DataMaskingConfig.FieldMaskingRule("phone", DataMaskingConfig.MaskingType.PHONE)
        );
        
        Map<String, Object> result = dataMaskingService.maskRowData(row, columnNames, maskingRules);
        
        // null值应该保持null
        assertNull(result.get("name"));
        assertEquals("138****5678", result.get("phone"));
        assertEquals("", result.get("empty_field"));
    }
    
    @Test
    void testValidateMaskingRule() {
        // 有效规则
        DataMaskingConfig.FieldMaskingRule validRule = new DataMaskingConfig.FieldMaskingRule(
            "phone", DataMaskingConfig.MaskingType.PHONE
        );
        assertTrue(dataMaskingService.validateMaskingRule(validRule));
        
        // 自定义有效规则
        DataMaskingConfig.FieldMaskingRule customValidRule = new DataMaskingConfig.FieldMaskingRule(
            "custom", "(\\d{2})\\d*(\\d{2})", "$1****$2"
        );
        assertTrue(dataMaskingService.validateMaskingRule(customValidRule));
        
        // 无效规则 - 空字段名
        DataMaskingConfig.FieldMaskingRule invalidRule1 = new DataMaskingConfig.FieldMaskingRule(
            "", DataMaskingConfig.MaskingType.PHONE
        );
        assertFalse(dataMaskingService.validateMaskingRule(invalidRule1));
        
        // 无效规则 - null
        assertFalse(dataMaskingService.validateMaskingRule(null));
        
        // 无效规则 - 错误的正则表达式
        DataMaskingConfig.FieldMaskingRule invalidRule2 = new DataMaskingConfig.FieldMaskingRule(
            "custom", "[invalid regex", "$1****$2"
        );
        assertFalse(dataMaskingService.validateMaskingRule(invalidRule2));
    }
    
    @Test
    void testMaskValue_DisabledRule() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "phone", DataMaskingConfig.MaskingType.PHONE
        );
        rule.setEnabled(false);
        
        String originalPhone = "13812345678";
        String result = dataMaskingService.maskValue(originalPhone, rule);
        
        // 禁用的规则应该返回原值
        assertEquals(originalPhone, result);
    }
    
    @Test
    void testMaskValue_NonStringValue() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 123); // 非字符串值
        row.put("amount", 99.99); // 非字符串值
        row.put("name", "张三"); // 字符串值
        
        List<String> columnNames = Arrays.asList("id", "amount", "name");
        List<DataMaskingConfig.FieldMaskingRule> maskingRules = Arrays.asList(
            new DataMaskingConfig.FieldMaskingRule("id", DataMaskingConfig.MaskingType.PHONE),
            new DataMaskingConfig.FieldMaskingRule("amount", DataMaskingConfig.MaskingType.PHONE),
            new DataMaskingConfig.FieldMaskingRule("name", DataMaskingConfig.MaskingType.NAME)
        );
        
        Map<String, Object> result = dataMaskingService.maskRowData(row, columnNames, maskingRules);
        
        // 非字符串值应该保持原值
        assertEquals(123, result.get("id"));
        assertEquals(99.99, result.get("amount"));
        // 字符串值应该被脱敏
        assertEquals("张*三", result.get("name"));
    }
    
    @Test
    void testPatternCache() {
        DataMaskingConfig.FieldMaskingRule rule = new DataMaskingConfig.FieldMaskingRule(
            "phone", DataMaskingConfig.MaskingType.PHONE
        );
        
        // 多次调用相同规则，测试缓存
        String phone1 = "13812345678";
        String phone2 = "13987654321";
        
        String result1 = dataMaskingService.maskValue(phone1, rule);
        String result2 = dataMaskingService.maskValue(phone2, rule);
        
        assertEquals("138****5678", result1);
        assertEquals("139****4321", result2);
        
        // 验证缓存大小
        assertTrue(dataMaskingService.getPatternCacheSize() > 0);
        
        // 清空缓存
        dataMaskingService.clearPatternCache();
        assertEquals(0, dataMaskingService.getPatternCacheSize());
    }
}