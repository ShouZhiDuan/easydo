# Spring Boot Harbor Starter

一个用于Harbor镜像管理的Spring Boot Starter组件，提供便捷的Harbor镜像操作功能。

## 功能特性

- 🐳 **镜像管理**: 支持查询、下载、上传、删除Harbor镜像
- 📦 **镜像导入导出**: 支持镜像tar包的导入导出功能
- 🔍 **镜像检索**: 支持镜像搜索和存在性检查
- 📊 **统计信息**: 提供镜像统计和健康检查功能
- 🛠 **工具类**: 提供便捷的批量操作工具
- ⚙️ **自动配置**: 基于Spring Boot自动配置，使用简单

## 快速开始

### 1. 添加依赖

在你的Spring Boot项目中添加以下依赖：

```xml
<dependency>
    <groupId>com.techzhi.harbor</groupId>
    <artifactId>spring-boot-starter-harbor</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置Harbor连接信息

在`application.yml`中添加Harbor配置：

```yaml
harbor:
  host: http://192.168.50.103
  username: admin
  password: Harbor12345
  project: flow
  connect-timeout: 30000
  read-timeout: 60000
  write-timeout: 60000
  ssl-enabled: false
```

### 3. 使用Harbor服务

```java
@Service
public class ImageManagementService {
    
    @Autowired
    private HarborImageService harborImageService;
    
    @Autowired
    private DockerImageService dockerImageService;
    
    @Autowired
    private HarborUtil harborUtil;
    
    // 获取镜像列表
    public List<HarborImage> getImages() {
        return harborImageService.listImages();
    }
    
    // 检查镜像是否存在
    public boolean checkImage(String imageName) {
        return harborImageService.imageExists(imageName);
    }
    
    // 下载镜像
    public void downloadImage(String imageName, String tag) {
        dockerImageService.pullImage(imageName, tag);
    }
    
    // 获取镜像统计信息
    public HarborUtil.ImageStatistics getStatistics() {
        return harborUtil.getImageStatistics("flow");
    }
}
```

## 主要API

### HarborImageService

Harbor镜像管理服务，提供对Harbor API的封装：

```java
// 获取镜像列表
List<HarborImage> listImages()
List<HarborImage> listImages(String projectName)

// 获取镜像标签
List<HarborTag> listImageTags(String imageName)
List<HarborTag> listImageTags(String projectName, String imageName)

// 检查镜像存在性
boolean imageExists(String imageName)
boolean imageTagExists(String imageName, String tag)

// 删除镜像
void deleteImage(String imageName)
void deleteImageTag(String imageName, String tag)

// 获取镜像详细信息
HarborImage getImageInfo(String imageName)

// 搜索镜像
List<HarborImage> searchImages(String keyword)
```

### DockerImageService

Docker镜像操作服务，提供对Docker引擎的操作：

```java
// 拉取镜像
void pullImage(String imageName, String tag)

// 推送镜像
void pushImage(String imageName, String tag)

// 保存镜像为tar文件
void saveImageToFile(String imageName, String tag, String filePath)

// 从tar文件加载镜像
void loadImageFromFile(String filePath)

// 加载并推送镜像（返回完整的Harbor镜像地址）
String loadAndPushImage(String filePath, String imageName, String tag)
String loadAndPushImage(String filePath, String projectName, String imageName, String tag)

// 🆕 自动解析镜像信息并推送（从tar文件自动解析镜像名和标签）
String loadAndPushImage(String filePath)
String loadAndPushImage(String filePath, String projectName)

// 删除本地镜像
void removeLocalImage(String imageName, String tag)

// 列出本地镜像
List<Image> listLocalImages()
```

### HarborUtil

Harbor工具类，提供便捷的批量操作：

```java
// 镜像同步
void syncImage(String sourceProject, String targetProject, String imageName, String tag)

// 批量下载项目镜像
void pullAllImagesInProject(String projectName)

// 批量导出项目镜像
void exportAllImagesInProject(String projectName, String exportDir)

// 清理本地镜像缓存
void cleanupLocalImages(String projectName)

// 检查镜像健康状态
boolean checkImageHealth(String projectName, String imageName, String tag)

// 获取镜像统计信息
ImageStatistics getImageStatistics(String projectName)

// 🆕 自动解析并推送tar文件
String autoLoadAndPushImage(String filePath)
String autoLoadAndPushImage(String filePath, String projectName)

// 🆕 批量自动解析并推送多个tar文件
List<String> batchAutoLoadAndPushImages(List<String> filePaths)
List<String> batchAutoLoadAndPushImages(List<String> filePaths, String projectName)
```

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `harbor.host` | `http://192.168.50.103` | Harbor服务器地址 |
| `harbor.username` | `admin` | Harbor用户名 |
| `harbor.password` | `Harbor12345` | Harbor密码 |
| `harbor.project` | `flow` | 默认项目空间 |
| `harbor.connect-timeout` | `30000` | 连接超时时间（毫秒） |
| `harbor.read-timeout` | `60000` | 读取超时时间（毫秒） |
| `harbor.write-timeout` | `60000` | 写入超时时间（毫秒） |
| `harbor.ssl-enabled` | `false` | 是否启用SSL验证 |

## 使用示例

### 1. 镜像查询和管理

```java
@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    @Autowired
    private HarborImageService harborImageService;
    
    @GetMapping("/list")
    public List<HarborImage> listImages() {
        return harborImageService.listImages();
    }
    
    @GetMapping("/{imageName}/exists")
    public boolean checkImageExists(@PathVariable String imageName) {
        return harborImageService.imageExists(imageName);
    }
    
    @DeleteMapping("/{imageName}")
    public void deleteImage(@PathVariable String imageName) {
        harborImageService.deleteImage(imageName);
    }
}
```

### 2. 镜像导入导出

```java
@Service
public class ImageBackupService {
    
    @Autowired
    private HarborUtil harborUtil;
    
    // 导出项目所有镜像
    public void backupProject(String projectName, String backupDir) {
        harborUtil.exportAllImagesInProject(projectName, backupDir);
    }
    
    // 获取项目统计信息
    public HarborUtil.ImageStatistics getProjectStats(String projectName) {
        return harborUtil.getImageStatistics(projectName);
    }
}
```

### 3. 镜像同步

```java
@Service
public class ImageSyncService {
    
    @Autowired
    private HarborUtil harborUtil;
    
    // 同步镜像到另一个项目
    public void syncImage(String imageName, String tag) {
        harborUtil.syncImage("source-project", "target-project", imageName, tag);
    }
}
```

### 4. 镜像上传并获取完整地址

```java
@Service
public class ImageUploadService {
    
    @Autowired
    private DockerImageService dockerImageService;
    
    @Autowired
    private HarborUtil harborUtil;
    
    // 上传镜像并获取Harbor地址用于后续部署
    public String uploadAndDeploy(String tarFilePath, String imageName, String tag) {
        // 上传镜像到Harbor，返回完整的镜像地址
        String harborImageUrl = dockerImageService.loadAndPushImage(tarFilePath, imageName, tag);
        
        // 使用返回的地址进行Kubernetes部署
        deployToKubernetes(harborImageUrl);
        
        return harborImageUrl;
    }
    
    // 🆕 自动解析并上传镜像（推荐使用）
    public String autoUploadAndDeploy(String tarFilePath) {
        // 自动从tar文件解析镜像名和标签，无需手动指定
        String harborImageUrl = dockerImageService.loadAndPushImage(tarFilePath);
        
        // 使用返回的地址进行Kubernetes部署
        deployToKubernetes(harborImageUrl);
        
        return harborImageUrl;
    }
    
    // 批量上传多个镜像
    public List<String> batchUpload(Map<String, String> imageFiles) {
        List<String> uploadedUrls = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : imageFiles.entrySet()) {
            String imageName = entry.getKey();
            String filePath = entry.getValue();
            
            try {
                String imageUrl = dockerImageService.loadAndPushImage(filePath, imageName, "latest");
                uploadedUrls.add(imageUrl);
                logger.info("✅ 镜像上传成功: {} -> {}", imageName, imageUrl);
            } catch (Exception e) {
                logger.error("❌ 镜像上传失败: {}", imageName, e);
            }
        }
        
        return uploadedUrls;
    }
    
    // 🆕 批量自动解析上传（推荐使用）
    public List<String> batchAutoUpload(List<String> tarFilePaths) {
        // 使用HarborUtil的批量自动解析功能
        return harborUtil.batchAutoLoadAndPushImages(tarFilePaths);
    }
    
    private void deployToKubernetes(String imageUrl) {
        // 使用完整的Harbor镜像地址进行部署
        logger.info("正在部署镜像: {}", imageUrl);
    }
}
```

### 5. 🆕 自动解析镜像信息功能

新增的自动解析功能可以从Docker tar文件中自动提取镜像名称和标签，无需手动指定：

```java
@Service
public class AutoImageService {
    
    @Autowired
    private DockerImageService dockerImageService;
    
    @Autowired
    private HarborUtil harborUtil;
    
    // 方式1: 直接使用DockerImageService
    public String processImageFile(String tarFilePath) {
        // 自动解析文件: /Users/shouzhi/temp/shanxi/image_tar/cust-cont-x86_20250616181022.tar
        // 会自动解析出: imageName = "cust-cont-x86", tag = "20250616181022"
        return dockerImageService.loadAndPushImage(tarFilePath);
    }
    
    // 方式2: 使用HarborUtil（推荐）
    public String processImageFileWithUtil(String tarFilePath) {
        return harborUtil.autoLoadAndPushImage(tarFilePath);
    }
    
    // 批量处理多个tar文件
    public List<String> processBatchFiles(String directory) {
        List<String> tarFiles = findTarFiles(directory);
        return harborUtil.batchAutoLoadAndPushImages(tarFiles);
    }
    
    // 支持的文件名格式示例:
    public void demonstrateFileNameFormats() {
        // 格式1: imageName_tag.tar
        String url1 = dockerImageService.loadAndPushImage("/path/to/nginx_1.21.tar");
        // 解析结果: nginx:1.21
        
        // 格式2: imageName-tag.tar  
        String url2 = dockerImageService.loadAndPushImage("/path/to/redis-6.2.tar");
        // 解析结果: redis:6.2
        
        // 格式3: 复杂文件名
        String url3 = dockerImageService.loadAndPushImage("/path/to/cust-cont-x86_20250616181022.tar");
        // 解析结果: cust-cont-x86:20250616181022
        
        // 格式4: 如果无法解析标签，使用latest
        String url4 = dockerImageService.loadAndPushImage("/path/to/myapp.tar");
        // 解析结果: myapp:latest
    }
    
    private List<String> findTarFiles(String directory) {
        // 查找目录下所有.tar文件的实现
        return java.nio.file.Files.walk(java.nio.file.Paths.get(directory))
                .filter(path -> path.toString().endsWith(".tar"))
                .map(java.nio.file.Path::toString)
                .collect(java.util.stream.Collectors.toList());
    }
}
```

## 注意事项

1. **Docker环境**: 使用Docker相关功能需要确保运行环境中已安装Docker，并且Docker daemon正在运行
2. **权限配置**: 确保配置的Harbor用户具有相应项目的读写权限
3. **网络连通性**: 确保应用服务器能够访问Harbor服务器和Docker registry
4. **SSL证书**: 如果Harbor启用了HTTPS但证书不受信任，可以设置`ssl-enabled: false`来跳过SSL验证

## 构建和安装

### 本地构建

```bash
mvn clean compile
mvn clean package
```

### 安装到本地仓库

```bash
mvn clean install
```

### 发布到Maven仓库

```bash
mvn clean deploy
```

## 许可证

本项目基于 MIT 许可证开源。

## 贡献

欢迎提交Issue和Pull Request来改进项目。

## 联系方式

如有问题或建议，请联系项目维护者。 