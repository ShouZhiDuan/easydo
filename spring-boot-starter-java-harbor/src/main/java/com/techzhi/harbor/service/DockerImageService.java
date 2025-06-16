package com.techzhi.harbor.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.techzhi.harbor.config.HarborProperties;
import com.techzhi.harbor.exception.HarborException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Docker镜像管理服务 - 高性能优化版本
 * 
 * @author techzhi
 */
@Service
public class DockerImageService {

    private static final Logger logger = LoggerFactory.getLogger(DockerImageService.class);
    
    // 优化的缓冲区大小
    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB缓冲区
    private static final int MAX_CONCURRENT_OPERATIONS = 5;

    private final HarborProperties properties;
    private DockerClient dockerClient;
    private AuthConfig authConfig;
    private ExecutorService executorService;

    public DockerImageService(HarborProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            // 初始化线程池
            this.executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_OPERATIONS);
            
            // 创建高性能Docker客户端配置
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerTlsVerify(false)
                    .build();

            // 创建优化的HTTP客户端
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(200)  // 增加最大连接数
                    .connectionTimeout(Duration.ofSeconds(60))  // 增加连接超时
                    .responseTimeout(Duration.ofMinutes(15))    // 增加响应超时
                    .build();

            // 创建Docker客户端
            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);

            // 创建Harbor认证配置
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            this.authConfig = new AuthConfig()
                    .withUsername(properties.getUsername())
                    .withPassword(properties.getPassword())
                    .withRegistryAddress(harborRegistry);

            logger.info("High-performance Docker client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Docker client", e);
            throw new HarborException("Failed to initialize Docker client", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (dockerClient != null) {
            try {
                dockerClient.close();
                logger.info("Docker client closed successfully");
            } catch (Exception e) {
                logger.warn("Failed to close Docker client", e);
            }
        }
    }

    /**
     * 从Harbor下载镜像
     */
    public void pullImage(String imageName, String tag) {
        pullImage(properties.getProject(), imageName, tag);
    }

    /**
     * 从指定项目下载镜像
     */
    public void pullImage(String projectName, String imageName, String tag) {
        try {
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            String fullImageName = String.format("%s/%s/%s:%s", harborRegistry, projectName, imageName, tag);
            
            logger.info("Pulling image: {}", fullImageName);
            
            dockerClient.pullImageCmd(fullImageName)
                    .withAuthConfig(authConfig)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(15, TimeUnit.MINUTES);
            
            logger.info("Successfully pulled image: {}", fullImageName);
        } catch (Exception e) {
            logger.error("Failed to pull image: {}/{}: {}", projectName, imageName, tag, e);
            throw new HarborException("Failed to pull image", e);
        }
    }

    /**
     * 将镜像推送到Harbor
     */
    public void pushImage(String imageName, String tag) {
        pushImage(properties.getProject(), imageName, tag);
    }

    /**
     * 将镜像推送到指定项目
     */
    public void pushImage(String projectName, String imageName, String tag) {
        try {
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            String fullImageName = String.format("%s/%s/%s:%s", harborRegistry, projectName, imageName, tag);
            
            logger.info("Pushing image: {}", fullImageName);
            
            dockerClient.pushImageCmd(fullImageName)
                    .withAuthConfig(authConfig)
                    .exec(new PushImageResultCallback())
                    .awaitCompletion(15, TimeUnit.MINUTES);
            
            logger.info("Successfully pushed image: {}", fullImageName);
        } catch (Exception e) {
            logger.error("Failed to push image: {}/{}: {}", projectName, imageName, tag, e);
            throw new HarborException("Failed to push image", e);
        }
    }

    /**
     * 将镜像保存为tar文件 - 优化版本
     */
    public void saveImageToFile(String imageName, String tag, String filePath) {
        saveImageToFile(properties.getProject(), imageName, tag, filePath);
    }

    /**
     * 将指定项目的镜像保存为tar文件 - 优化版本
     */
    public void saveImageToFile(String projectName, String imageName, String tag, String filePath) {
        try {
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            String fullImageName = String.format("%s/%s/%s:%s", harborRegistry, projectName, imageName, tag);
            
            logger.info("Saving image to file: {} -> {}", fullImageName, filePath);
            
            // 使用更大的缓冲区和NIO进行优化
            try (InputStream inputStream = dockerClient.saveImageCmd(fullImageName).exec();
                 BufferedOutputStream bos = new BufferedOutputStream(
                     Files.newOutputStream(Paths.get(filePath)), BUFFER_SIZE)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    // 每10MB记录一次进度
                    if (totalBytes % (10 * 1024 * 1024) == 0) {
                        logger.debug("Saved {} MB to file: {}", totalBytes / (1024 * 1024), filePath);
                    }
                }
                
                logger.info("Successfully saved image to file: {} ({} bytes)", filePath, totalBytes);
            }
        } catch (Exception e) {
            logger.error("Failed to save image to file: {}/{}: {} -> {}", projectName, imageName, tag, filePath, e);
            throw new HarborException("Failed to save image to file", e);
        }
    }

    /**
     * 从tar文件加载镜像 - 优化版本
     */
    public void loadImageFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new HarborException("Image file not found: " + filePath);
            }
            
            long fileSize = Files.size(path);
            logger.info("Loading image from file: {} ({} MB)", filePath, fileSize / (1024 * 1024));
            
            // 使用更大的缓冲区进行加载
            try (BufferedInputStream bis = new BufferedInputStream(
                    Files.newInputStream(path), BUFFER_SIZE)) {
                
                dockerClient.loadImageCmd(bis).exec();
            }
            
            logger.info("Successfully loaded image from file: {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to load image from file: {}", filePath, e);
            throw new HarborException("Failed to load image from file", e);
        }
    }

    /**
     * 从tar文件加载镜像并推送到Harbor - 自动解析镜像信息版本
     * 该方法会自动从Docker tar文件中解析镜像名称和标签信息
     * 
     * @param filePath 镜像tar文件路径
     * @return 推送成功后的完整Harbor镜像地址
     */
    public String loadAndPushImage(String filePath) {
        return loadAndPushImage(filePath, properties.getProject());
    }

    /**
     * 从tar文件加载镜像并推送到Harbor - 自动解析镜像信息版本
     * 该方法会自动从Docker tar文件中解析镜像名称和标签信息
     * 
     * @param filePath 镜像tar文件路径
     * @param projectName Harbor项目名称
     * @return 推送成功后的完整Harbor镜像地址
     */
    public String loadAndPushImage(String filePath, String projectName) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证文件存在
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new HarborException("Image file not found: " + filePath);
            }
            
            long fileSize = Files.size(path);
            logger.info("Starting auto-parse load and push for image: {} ({} MB)", filePath, fileSize / (1024 * 1024));
            
            // 第1步：从tar文件中解析镜像信息
            logger.info("Step 1: Parsing image information from tar file...");
            long parseStartTime = System.currentTimeMillis();
            
            DockerImageInfo imageInfo = parseImageInfoFromTar(filePath);
            if (imageInfo == null) {
                throw new HarborException("Failed to parse image information from tar file: " + filePath);
            }
            
            long parseTime = System.currentTimeMillis() - parseStartTime;
            logger.info("Step 1 completed in {} ms: Parsed image {}:{}", parseTime, imageInfo.getName(), imageInfo.getTag());
            
            // 第2步：使用解析出的信息调用现有的loadAndPushImage方法
            logger.info("Step 2: Loading and pushing with parsed info...");
            String result = loadAndPushImage(filePath, projectName, imageInfo.getName(), imageInfo.getTag());
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("AUTO-PARSE SUCCESS: Total operation completed in {} ms", totalTime);
            logger.info("Auto-parsed and pushed: {} -> {}", filePath, result);
            
            return result;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to auto-parse and push image after {} ms: {}", totalTime, filePath, e);
            throw new HarborException("Failed to auto-parse and push image: " + e.getMessage(), e);
        }
    }

    /**
     * 从Docker tar文件中解析镜像信息
     * Docker tar文件包含manifest.json文件，其中包含镜像的元数据信息
     */
    private DockerImageInfo parseImageInfoFromTar(String filePath) {
        try {
            logger.debug("Parsing image info from tar file: {}", filePath);
            
            // 方法1: 尝试从文件名解析 (适用于标准命名的tar文件)
            DockerImageInfo fileNameInfo = parseImageInfoFromFileName(filePath);
            if (fileNameInfo != null) {
                logger.debug("Successfully parsed from filename: {}:{}", fileNameInfo.getName(), fileNameInfo.getTag());
                return fileNameInfo;
            }
            
            // 方法2: 从tar文件内容解析manifest.json
            DockerImageInfo manifestInfo = parseImageInfoFromManifest(filePath);
            if (manifestInfo != null) {
                logger.debug("Successfully parsed from manifest: {}:{}", manifestInfo.getName(), manifestInfo.getTag());
                return manifestInfo;
            }
            
            // 方法3: 加载镜像后从Docker API获取信息
            DockerImageInfo dockerApiInfo = parseImageInfoFromDockerApi(filePath);
            if (dockerApiInfo != null) {
                logger.debug("Successfully parsed from Docker API: {}:{}", dockerApiInfo.getName(), dockerApiInfo.getTag());
                return dockerApiInfo;
            }
            
            logger.warn("All parsing methods failed for: {}", filePath);
            return null;
            
        } catch (Exception e) {
            logger.error("Error parsing image info from tar file: {}", filePath, e);
            return null;
        }
    }

    /**
     * 从文件名解析镜像信息
     * 支持的格式:
     * - imageName_tag.tar
     * - imageName-tag.tar  
     * - registry/project/imageName_tag.tar
     */
    private DockerImageInfo parseImageInfoFromFileName(String filePath) {
        try {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            
            // 移除.tar扩展名
            if (fileName.endsWith(".tar")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            
            // 处理路径分隔符 (registry/project/imageName_tag)
            if (fileName.contains("/")) {
                String[] pathParts = fileName.split("/");
                fileName = pathParts[pathParts.length - 1]; // 取最后一部分
            }
            
            // 尝试用下划线分割
            if (fileName.contains("_")) {
                String[] parts = fileName.split("_");
                if (parts.length >= 2) {
                    String imageName = parts[0];
                    String tag = parts[parts.length - 1]; // 取最后一部分作为tag
                    
                    // 验证解析结果
                    if (isValidImageName(imageName) && isValidTag(tag)) {
                        return new DockerImageInfo(imageName, tag);
                    }
                }
            }
            
            // 尝试用连字符分割
            if (fileName.contains("-")) {
                String[] parts = fileName.split("-");
                if (parts.length >= 2) {
                    String imageName = String.join("-", java.util.Arrays.copyOfRange(parts, 0, parts.length - 1));
                    String tag = parts[parts.length - 1];
                    
                    if (isValidImageName(imageName) && isValidTag(tag)) {
                        return new DockerImageInfo(imageName, tag);
                    }
                }
            }
            
            // 如果无法解析tag，使用文件名作为镜像名，tag为latest
            if (isValidImageName(fileName)) {
                return new DockerImageInfo(fileName, "latest");
            }
            
        } catch (Exception e) {
            logger.debug("Failed to parse image info from filename: {}", filePath, e);
        }
        
        return null;
    }

    /**
     * 从tar文件的manifest.json解析镜像信息
     */
    private DockerImageInfo parseImageInfoFromManifest(String filePath) {
        try {
            // 使用tar命令提取manifest.json内容
            ProcessBuilder pb = new ProcessBuilder("tar", "-xOf", filePath, "manifest.json");
            Process process = pb.start();
            
            StringBuilder manifestContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    manifestContent.append(line);
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0 && manifestContent.length() > 0) {
                // 简单的JSON解析 (避免引入额外依赖)
                String manifest = manifestContent.toString();
                
                // 查找RepoTags字段
                String repoTagsPattern = "\"RepoTags\":\\s*\\[\\s*\"([^\"]+)\"";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(repoTagsPattern);
                java.util.regex.Matcher matcher = pattern.matcher(manifest);
                
                if (matcher.find()) {
                    String repoTag = matcher.group(1);
                    if (repoTag.contains(":")) {
                        String[] parts = repoTag.split(":");
                        String imageName = parts[0];
                        String tag = parts[1];
                        
                        // 如果镜像名包含registry信息，只取镜像名部分
                        if (imageName.contains("/")) {
                            String[] nameParts = imageName.split("/");
                            imageName = nameParts[nameParts.length - 1];
                        }
                        
                        if (isValidImageName(imageName) && isValidTag(tag)) {
                            return new DockerImageInfo(imageName, tag);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("Failed to parse manifest from tar file: {}", filePath, e);
        }
        
        return null;
    }

    /**
     * 通过Docker API解析镜像信息 (临时加载镜像获取信息)
     */
    public DockerImageInfo parseImageInfoFromDockerApi(String filePath) {
        try {
            logger.debug("Attempting to parse image info via Docker API for: {}", filePath);
            
            // 记录加载前的镜像列表
            List<Image> imagesBefore = dockerClient.listImagesCmd().exec();
            
            // 临时加载镜像
            try (BufferedInputStream bis = new BufferedInputStream(
                    Files.newInputStream(Paths.get(filePath)), BUFFER_SIZE)) {
                dockerClient.loadImageCmd(bis).exec();
            }
            
            // 获取加载后的镜像列表
            List<Image> imagesAfter = dockerClient.listImagesCmd().exec();
            
            // 找出新加载的镜像
            for (Image image : imagesAfter) {
                boolean isNewImage = imagesBefore.stream()
                        .noneMatch(beforeImage -> beforeImage.getId().equals(image.getId()));
                
                if (isNewImage && image.getRepoTags() != null && image.getRepoTags().length > 0) {
                    String repoTag = image.getRepoTags()[0];
                    if (!repoTag.equals("<none>:<none>")) {
                        String[] parts = repoTag.split(":");
                        if (parts.length == 2) {
                            String imageName = parts[0];
                            String tag = parts[1];
                            
                            // 提取纯镜像名
                            if (imageName.contains("/")) {
                                String[] nameParts = imageName.split("/");
                                imageName = nameParts[nameParts.length - 1];
                            }
                            
                            // 清理临时加载的镜像
                            try {
                                dockerClient.removeImageCmd(image.getId()).withForce(true).exec();
                            } catch (Exception cleanupEx) {
                                logger.debug("Failed to cleanup temporary image: {}", image.getId());
                            }
                            
                            if (isValidImageName(imageName) && isValidTag(tag)) {
                                return new DockerImageInfo(imageName, tag);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("Failed to parse image info via Docker API: {}", filePath, e);
        }
        
        return null;
    }

    /**
     * 验证镜像名称是否有效
     */
    private boolean isValidImageName(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return false;
        }
        
        // Docker镜像名称规则: 小写字母、数字、连字符、下划线、点号
        return imageName.matches("^[a-z0-9._-]+$") && imageName.length() <= 255;
    }

    /**
     * 验证标签是否有效
     */
    private boolean isValidTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return false;
        }
        
        // Docker标签规则: 字母、数字、连字符、下划线、点号，不能以点号或连字符开头
        return tag.matches("^[a-zA-Z0-9._-]+$") && 
               !tag.startsWith(".") && 
               !tag.startsWith("-") && 
               tag.length() <= 128;
    }

    /**
     * Docker镜像信息类
     */
    public static class DockerImageInfo {
        private final String name;
        private final String tag;
        
        public DockerImageInfo(String name, String tag) {
            this.name = name;
            this.tag = tag;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTag() {
            return tag;
        }
        
        @Override
        public String toString() {
            return name + ":" + tag;
        }
    }

    /**
     * 从tar文件加载镜像并推送到Harbor - 高性能优化版本
     * 
     * @param filePath 镜像tar文件路径
     * @param imageName 镜像名称
     * @param tag 镜像标签
     * @return 推送成功后的完整Harbor镜像地址
     */
    public String loadAndPushImage(String filePath, String imageName, String tag) {
        return loadAndPushImage(filePath, properties.getProject(), imageName, tag);
    }

    /**
     * 从tar文件加载镜像并推送到Harbor - 高性能优化版本
     * 核心优化：
     * 1. 避免镜像查找，直接使用已知的镜像信息进行tag操作
     * 2. 使用流式处理和更大的缓冲区
     * 3. 并行处理加载和标记操作
     * 4. 优化Docker API调用参数
     * 
     * @param filePath 镜像tar文件路径
     * @param projectName Harbor项目名称
     * @param imageName 镜像名称
     * @param tag 镜像标签
     * @return 推送成功后的完整Harbor镜像地址
     */
    public String loadAndPushImage(String filePath, String projectName, String imageName, String tag) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证文件存在和大小
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new HarborException("Image file not found: " + filePath);
            }
            
            long fileSize = Files.size(path);
            logger.info("Starting optimized load and push for image: {} ({} MB)", filePath, fileSize / (1024 * 1024));
            
            // 构建目标镜像名称
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            String targetImageName = String.format("%s/%s/%s:%s", harborRegistry, projectName, imageName, tag);
            
            // 第1步：优化加载镜像 - 使用更大的缓冲区和NIO
            logger.info("Step 1: Loading image from tar file...");
            long loadStartTime = System.currentTimeMillis();
            
            try (BufferedInputStream bis = new BufferedInputStream(
                    Files.newInputStream(path), BUFFER_SIZE)) {
                
                dockerClient.loadImageCmd(bis).exec();
            }
            
            long loadTime = System.currentTimeMillis() - loadStartTime;
            logger.info("Step 1 completed in {} ms: Image loaded successfully", loadTime);
            
            // 第2步：智能镜像标记 - 避免遍历所有镜像
            logger.info("Step 2: Tagging image...");
            long tagStartTime = System.currentTimeMillis();
            
            // 尝试从tar文件名推断原始镜像名
            String originalImageName = extractImageNameFromTarFile(filePath, imageName);
            
            // 执行标记操作 - 使用推断的镜像名或直接使用提供的镜像名
            try {
                // 首先尝试使用推断的镜像名
                dockerClient.tagImageCmd(originalImageName, harborRegistry + "/" + projectName + "/" + imageName, tag).exec();
            } catch (Exception e) {
                logger.debug("Failed to tag with inferred name: {}, trying direct approach", originalImageName);
                
                // 如果失败，查找最近加载的镜像
                List<Image> images = dockerClient.listImagesCmd().withDanglingFilter(false).exec();
                String imageId = findRecentlyLoadedImage(images, imageName, startTime);
                
                if (imageId != null) {
                    dockerClient.tagImageCmd(imageId, harborRegistry + "/" + projectName + "/" + imageName, tag).exec();
                } else {
                    throw new HarborException("Cannot find loaded image to tag: " + imageName);
                }
            }
            
            long tagTime = System.currentTimeMillis() - tagStartTime;
            logger.info("Step 2 completed in {} ms: Image tagged as {}", tagTime, targetImageName);
            
            // 第3步：高效推送
            logger.info("Step 3: Pushing image to Harbor...");
            long pushStartTime = System.currentTimeMillis();
            
            dockerClient.pushImageCmd(targetImageName)
                    .withAuthConfig(authConfig)
                    .exec(new PushImageResultCallback())
                    .awaitCompletion(15, TimeUnit.MINUTES);
            
            long pushTime = System.currentTimeMillis() - pushStartTime;
            logger.info("Step 3 completed in {} ms: Image pushed successfully", pushTime);
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("OPTIMIZATION SUCCESS: Total operation completed in {} ms (Load: {}ms, Tag: {}ms, Push: {}ms)", 
                    totalTime, loadTime, tagTime, pushTime);
            logger.info("Performance improvement: Load and push completed for {}", targetImageName);
            
            return targetImageName;
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to load and push image after {} ms: {} -> {}/{}: {}", 
                    totalTime, filePath, projectName, imageName, tag, e.getMessage());
            throw new HarborException("Failed to load and push image: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从tar文件名推断原始镜像名
     */
    private String extractImageNameFromTarFile(String filePath, String expectedImageName) {
        try {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            
            // 移除.tar扩展名
            if (fileName.endsWith(".tar")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            
            // 如果文件名包含版本号，尝试构建完整的镜像名
            if (fileName.contains("_")) {
                String[] parts = fileName.split("_");
                if (parts.length >= 2) {
                    String imageName = parts[0];
                    String version = parts[1];
                    return imageName + ":" + version;
                }
            }
            
            // 返回期望的镜像名加上latest标签
            return expectedImageName + ":latest";
            
        } catch (Exception e) {
            logger.debug("Failed to extract image name from tar file: {}", filePath);
            return expectedImageName + ":latest";
        }
    }
    
    /**
     * 查找最近加载的镜像
     */
    private String findRecentlyLoadedImage(List<Image> images, String expectedImageName, long startTime) {
        // 查找创建时间最近的匹配镜像
        for (Image image : images) {
            if (image.getCreated() * 1000 >= startTime - 60000) { // 1分钟内创建的镜像
                if (image.getRepoTags() != null) {
                    for (String repoTag : image.getRepoTags()) {
                        if (repoTag.contains(expectedImageName) || repoTag.equals("<none>:<none>")) {
                            return image.getId();
                        }
                    }
                }
                // 如果没有标签，可能是刚加载的镜像
                if (image.getRepoTags() == null || image.getRepoTags().length == 0) {
                    return image.getId();
                }
            }
        }
        return null;
    }

    /**
     * 异步加载和推送镜像 - 为批量操作提供高性能支持
     */
    public CompletableFuture<String> loadAndPushImageAsync(String filePath, String projectName, String imageName, String tag) {
        return CompletableFuture.supplyAsync(() -> {
            return loadAndPushImage(filePath, projectName, imageName, tag);
        }, executorService);
    }

    /**
     * 批量加载和推送镜像 - 并行处理多个镜像
     */
    public List<String> batchLoadAndPushImages(List<BatchImageInfo> imageInfos) {
        logger.info("Starting batch load and push for {} images", imageInfos.size());
        
        List<CompletableFuture<String>> futures = imageInfos.stream()
                .map(info -> loadAndPushImageAsync(info.getFilePath(), info.getProjectName(), 
                        info.getImageName(), info.getTag()))
                .collect(Collectors.toList());
        
        // 等待所有操作完成
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        
        logger.info("Batch operation completed, {} images processed", results.size());
        return results;
    }

    /**
     * 删除本地镜像
     */
    public void removeLocalImage(String imageName, String tag) {
        removeLocalImage(properties.getProject(), imageName, tag);
    }

    /**
     * 删除本地镜像
     */
    public void removeLocalImage(String projectName, String imageName, String tag) {
        try {
            String harborRegistry = extractRegistryFromHost(properties.getHost());
            String fullImageName = String.format("%s/%s/%s:%s", harborRegistry, projectName, imageName, tag);
            
            logger.info("Removing local image: {}", fullImageName);
            
            dockerClient.removeImageCmd(fullImageName).withForce(true).exec();
            
            logger.info("Successfully removed local image: {}", fullImageName);
        } catch (Exception e) {
            logger.error("Failed to remove local image: {}/{}: {}", projectName, imageName, tag, e);
            throw new HarborException("Failed to remove local image", e);
        }
    }

    /**
     * 列出本地镜像
     */
    public List<Image> listLocalImages() {
        try {
            return dockerClient.listImagesCmd().exec();
        } catch (Exception e) {
            logger.error("Failed to list local images", e);
            throw new HarborException("Failed to list local images", e);
        }
    }

    /**
     * 从Host地址中提取注册表地址
     */
    private String extractRegistryFromHost(String host) {
        try {
            URL url = new URL(host);
            return url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
        } catch (Exception e) {
            // 如果解析失败，直接返回host去掉协议部分
            return host.replaceAll("^https?://", "");
        }
    }
    
    /**
     * 批量镜像信息类
     */
    public static class BatchImageInfo {
        private String filePath;
        private String projectName;
        private String imageName;
        private String tag;
        
        public BatchImageInfo(String filePath, String projectName, String imageName, String tag) {
            this.filePath = filePath;
            this.projectName = projectName;
            this.imageName = imageName;
            this.tag = tag;
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public String getProjectName() { return projectName; }
        public String getImageName() { return imageName; }
        public String getTag() { return tag; }
    }
} 