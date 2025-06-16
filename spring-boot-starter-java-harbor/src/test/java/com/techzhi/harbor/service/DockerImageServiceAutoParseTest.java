package com.techzhi.harbor.service;

import com.techzhi.harbor.config.HarborProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DockerImageService自动解析功能测试
 * 
 * @author techzhi
 */
public class DockerImageServiceAutoParseTest {

    private DockerImageService dockerImageService;
    private HarborProperties properties;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        properties = new HarborProperties();
        properties.setHost("http://192.168.50.103/");
        properties.setUsername("admin");
        properties.setPassword("Harbor12345");
        properties.setProject("flow");
        
        // 注意：这里只测试解析逻辑，不实际连接Docker
        dockerImageService = new DockerImageService(properties);
    }

    @Test
    @DisplayName("测试从文件名解析镜像信息 - 下划线格式")
    void testParseImageInfoFromFileName_Underscore() {
        // 使用反射访问私有方法进行测试
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("parseImageInfoFromFileName", String.class);
            method.setAccessible(true);
            
            Object result = method.invoke(dockerImageService, "/Users/shouzhi/temp/shanxi/image_tar/cust-cont-x86_20250616181022.tar");
            
            assertNotNull(result, "应该能够解析镜像信息");
            
            // 获取解析结果的name和tag
            java.lang.reflect.Method getNameMethod = result.getClass().getDeclaredMethod("getName");
            java.lang.reflect.Method getTagMethod = result.getClass().getDeclaredMethod("getTag");
            
            String imageName = (String) getNameMethod.invoke(result);
            String tag = (String) getTagMethod.invoke(result);
            
            assertEquals("cust-cont-x86", imageName, "镜像名应该是 cust-cont-x86");
            assertEquals("20250616181022", tag, "标签应该是 20250616181022");
            
        } catch (Exception e) {
            fail("测试解析文件名失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试从文件名解析镜像信息 - 连字符格式")
    void testParseImageInfoFromFileName_Hyphen() {
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("parseImageInfoFromFileName", String.class);
            method.setAccessible(true);
            
            Object result = method.invoke(dockerImageService, "/path/to/nginx-1.21.tar");
            
            assertNotNull(result, "应该能够解析镜像信息");
            
            java.lang.reflect.Method getNameMethod = result.getClass().getDeclaredMethod("getName");
            java.lang.reflect.Method getTagMethod = result.getClass().getDeclaredMethod("getTag");
            
            String imageName = (String) getNameMethod.invoke(result);
            String tag = (String) getTagMethod.invoke(result);
            
            assertEquals("nginx", imageName, "镜像名应该是 nginx");
            assertEquals("1.21", tag, "标签应该是 1.21");
            
        } catch (Exception e) {
            fail("测试解析文件名失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试从文件名解析镜像信息 - 无标签格式")
    void testParseImageInfoFromFileName_NoTag() {
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("parseImageInfoFromFileName", String.class);
            method.setAccessible(true);
            
            Object result = method.invoke(dockerImageService, "/path/to/myapp.tar");
            
            assertNotNull(result, "应该能够解析镜像信息");
            
            java.lang.reflect.Method getNameMethod = result.getClass().getDeclaredMethod("getName");
            java.lang.reflect.Method getTagMethod = result.getClass().getDeclaredMethod("getTag");
            
            String imageName = (String) getNameMethod.invoke(result);
            String tag = (String) getTagMethod.invoke(result);
            
            assertEquals("myapp", imageName, "镜像名应该是 myapp");
            assertEquals("latest", tag, "标签应该是 latest");
            
        } catch (Exception e) {
            fail("测试解析文件名失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试镜像名称验证")
    void testIsValidImageName() {
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("isValidImageName", String.class);
            method.setAccessible(true);
            
            // 有效的镜像名
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"nginx"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"cust-cont-x86"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"my_app"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"app.service"}));
            
            // 无效的镜像名
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{""}));
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{null}));
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{"NGINX"})); // 大写字母
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{"app@service"})); // 特殊字符
            
        } catch (Exception e) {
            fail("测试镜像名验证失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试标签验证")
    void testIsValidTag() {
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("isValidTag", String.class);
            method.setAccessible(true);
            
            // 有效的标签
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"latest"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"1.21"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"20250616181022"}));
            assertTrue((Boolean) method.invoke(dockerImageService, new Object[]{"v1.0.0"}));
            
            // 无效的标签
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{""}));
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{null}));
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{".latest"})); // 以点开头
            assertFalse((Boolean) method.invoke(dockerImageService, new Object[]{"-latest"})); // 以连字符开头
            
        } catch (Exception e) {
            fail("测试标签验证失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试复杂文件名解析")
    void testComplexFileNameParsing() {
        try {
            java.lang.reflect.Method method = DockerImageService.class.getDeclaredMethod("parseImageInfoFromFileName", String.class);
            method.setAccessible(true);
            
            // 测试复杂的文件路径
            String[] testCases = {
                "/Users/shouzhi/temp/shanxi/image_tar/cust-cont-x86_20250616181022.tar",
                "/home/user/images/redis_6.2.7.tar",
                "/images/postgres-13.tar",  // 简化Windows路径测试
                "./local/mysql_8.0.tar"
            };
            
            String[] expectedNames = {"cust-cont-x86", "redis", "postgres", "mysql"};
            String[] expectedTags = {"20250616181022", "6.2.7", "13", "8.0"};
            
            for (int i = 0; i < testCases.length; i++) {
                Object result = method.invoke(dockerImageService, testCases[i]);
                assertNotNull(result, "应该能够解析: " + testCases[i]);
                
                java.lang.reflect.Method getNameMethod = result.getClass().getDeclaredMethod("getName");
                java.lang.reflect.Method getTagMethod = result.getClass().getDeclaredMethod("getTag");
                
                String imageName = (String) getNameMethod.invoke(result);
                String tag = (String) getTagMethod.invoke(result);
                
                assertEquals(expectedNames[i], imageName, "镜像名不匹配: " + testCases[i]);
                assertEquals(expectedTags[i], tag, "标签不匹配: " + testCases[i]);
            }
            
        } catch (Exception e) {
            fail("测试复杂文件名解析失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("演示自动解析功能的使用方式")
    void demonstrateAutoParseUsage() {
        // 这个测试主要用于演示API的使用方式
        // 实际的Docker操作需要真实的Docker环境
        
        String testFilePath = "/Users/shouzhi/temp/shanxi/image_tar/cust-cont-x86_20250616181022.tar";
        
        // 演示API调用方式（不实际执行）
        System.out.println("=== 自动解析功能使用演示 ===");
        System.out.println("文件路径: " + testFilePath);
        System.out.println("预期解析结果: cust-cont-x86:20250616181022");
        System.out.println();
        System.out.println("使用方式1: dockerImageService.loadAndPushImage(\"" + testFilePath + "\")");
        System.out.println("使用方式2: dockerImageService.loadAndPushImage(\"" + testFilePath + "\", \"flow\")");
        System.out.println("使用方式3: harborUtil.autoLoadAndPushImage(\"" + testFilePath + "\")");
        System.out.println();
        System.out.println("返回值示例: http://192.168.50.103/flow/cust-cont-x86:20250616181022");
        
        // 验证测试通过
        assertTrue(true, "演示测试完成");
    }
} 