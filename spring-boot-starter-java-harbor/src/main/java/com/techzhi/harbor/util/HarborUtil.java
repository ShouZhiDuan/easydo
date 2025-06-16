package com.techzhi.harbor.util;

import com.github.dockerjava.api.model.Image;
import com.techzhi.harbor.model.HarborImage;
import com.techzhi.harbor.model.HarborTag;
import com.techzhi.harbor.service.DockerImageService;
import com.techzhi.harbor.service.HarborImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Harbor工具类
 * 提供便捷的Harbor镜像管理操作
 * 
 * @author techzhi
 */
public class HarborUtil {

    private static final Logger logger = LoggerFactory.getLogger(HarborUtil.class);

    private final HarborImageService harborImageService;
    private final DockerImageService dockerImageService;

    public HarborUtil(HarborImageService harborImageService, DockerImageService dockerImageService) {
        this.harborImageService = harborImageService;
        this.dockerImageService = dockerImageService;
    }

    /**
     * 完整的镜像同步操作：从源Harbor下载镜像并上传到目标Harbor
     */
    public void syncImage(String sourceProject, String targetProject, String imageName, String tag) {
        logger.info("Starting image sync: {}/{}: {} -> {}", sourceProject, imageName, tag, targetProject);
        
        try {
            // 1. 从源项目下载镜像
            dockerImageService.pullImage(sourceProject, imageName, tag);
            
            // 2. 重新标记镜像到目标项目
            dockerImageService.pushImage(targetProject, imageName, tag);
            
            logger.info("Successfully synced image: {}/{}: {} -> {}", sourceProject, imageName, tag, targetProject);
        } catch (Exception e) {
            logger.error("Failed to sync image: {}/{}: {} -> {}", sourceProject, imageName, tag, targetProject, e);
            throw e;
        }
    }

    /**
     * 批量下载项目中的所有镜像
     */
    public void pullAllImagesInProject(String projectName) {
        logger.info("Starting to pull all images in project: {}", projectName);
        
        try {
            List<HarborImage> images = harborImageService.listImages(projectName);
            
            for (HarborImage image : images) {
                List<HarborTag> tags = harborImageService.listImageTags(projectName, image.getName());
                
                for (HarborTag tag : tags) {
                    try {
                        dockerImageService.pullImage(projectName, image.getName(), tag.getName());
                        logger.info("Successfully pulled image: {}/{}: {}", projectName, image.getName(), tag.getName());
                    } catch (Exception e) {
                        logger.error("Failed to pull image: {}/{}: {}", projectName, image.getName(), tag.getName(), e);
                    }
                }
            }
            
            logger.info("Completed pulling all images in project: {}", projectName);
        } catch (Exception e) {
            logger.error("Failed to pull all images in project: {}", projectName, e);
            throw e;
        }
    }

    /**
     * 批量导出项目中的所有镜像为tar文件
     */
    public void exportAllImagesInProject(String projectName, String exportDir) {
        logger.info("Starting to export all images in project: {} to directory: {}", projectName, exportDir);
        
        try {
            List<HarborImage> images = harborImageService.listImages(projectName);
            
            for (HarborImage image : images) {
                List<HarborTag> tags = harborImageService.listImageTags(projectName, image.getName());
                
                for (HarborTag tag : tags) {
                    try {
                        String fileName = String.format("%s_%s_%s.tar", projectName, image.getName(), tag.getName())
                                .replaceAll("[^a-zA-Z0-9._-]", "_");
                        String filePath = exportDir + "/" + fileName;
                        
                        dockerImageService.saveImageToFile(projectName, image.getName(), tag.getName(), filePath);
                        logger.info("Successfully exported image: {}/{}: {} to {}", 
                                projectName, image.getName(), tag.getName(), filePath);
                    } catch (Exception e) {
                        logger.error("Failed to export image: {}/{}: {}", 
                                projectName, image.getName(), tag.getName(), e);
                    }
                }
            }
            
            logger.info("Completed exporting all images in project: {}", projectName);
        } catch (Exception e) {
            logger.error("Failed to export all images in project: {}", projectName, e);
            throw e;
        }
    }

    /**
     * 清理本地镜像缓存
     */
    public void cleanupLocalImages(String projectName) {
        logger.info("Starting to cleanup local images for project: {}", projectName);
        
        try {
            List<Image> localImages = dockerImageService.listLocalImages();
            int cleanedCount = 0;
            
            for (Image image : localImages) {
                if (image.getRepoTags() != null) {
                    for (String repoTag : image.getRepoTags()) {
                        if (repoTag.contains("/" + projectName + "/")) {
                            try {
                                String[] parts = repoTag.split(":");
                                if (parts.length == 2) {
                                    String[] nameParts = parts[0].split("/");
                                    if (nameParts.length >= 2) {
                                        String imageName = nameParts[nameParts.length - 1];
                                        String tag = parts[1];
                                        dockerImageService.removeLocalImage(projectName, imageName, tag);
                                        cleanedCount++;
                                        logger.info("Cleaned up local image: {}", repoTag);
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("Failed to cleanup local image: {}", repoTag, e);
                            }
                        }
                    }
                }
            }
            
            logger.info("Completed cleanup of {} local images for project: {}", cleanedCount, projectName);
        } catch (Exception e) {
            logger.error("Failed to cleanup local images for project: {}", projectName, e);
            throw e;
        }
    }

    /**
     * 检查镜像健康状态
     */
    public boolean checkImageHealth(String projectName, String imageName, String tag) {
        try {
            // 检查Harbor中是否存在
            boolean existsInHarbor = harborImageService.imageTagExists(projectName, imageName, tag);
            
            if (!existsInHarbor) {
                logger.warn("Image does not exist in Harbor: {}/{}: {}", projectName, imageName, tag);
                return false;
            }
            
            // 尝试获取镜像信息
            HarborImage imageInfo = harborImageService.getImageInfo(projectName, imageName);
            if (imageInfo == null) {
                logger.warn("Failed to get image info: {}/{}", projectName, imageName);
                return false;
            }
            
            logger.info("Image health check passed: {}/{}: {}", projectName, imageName, tag);
            return true;
        } catch (Exception e) {
            logger.error("Image health check failed: {}/{}: {}", projectName, imageName, tag, e);
            return false;
        }
    }

    /**
     * 自动解析并推送tar文件到Harbor
     * 该方法会自动从tar文件中解析镜像名称和标签
     */
    public String autoLoadAndPushImage(String filePath) {
        logger.info("Starting auto-parse load and push for tar file: {}", filePath);
        
        try {
            String result = dockerImageService.loadAndPushImage(filePath);
            logger.info("Successfully auto-parsed and pushed image: {} -> {}", filePath, result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to auto-parse and push image: {}", filePath, e);
            throw e;
        }
    }

    /**
     * 自动解析并推送tar文件到指定项目
     */
    public String autoLoadAndPushImage(String filePath, String projectName) {
        logger.info("Starting auto-parse load and push for tar file: {} to project: {}", filePath, projectName);
        
        try {
            String result = dockerImageService.loadAndPushImage(filePath, projectName);
            logger.info("Successfully auto-parsed and pushed image: {} -> {}", filePath, result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to auto-parse and push image to project {}: {}", projectName, filePath, e);
            throw e;
        }
    }

    /**
     * 批量自动解析并推送多个tar文件
     */
    public List<String> batchAutoLoadAndPushImages(List<String> filePaths) {
        return batchAutoLoadAndPushImages(filePaths, null);
    }

    /**
     * 批量自动解析并推送多个tar文件到指定项目
     */
    public List<String> batchAutoLoadAndPushImages(List<String> filePaths, String projectName) {
        logger.info("Starting batch auto-parse load and push for {} tar files", filePaths.size());
        
        List<String> results = new java.util.ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (String filePath : filePaths) {
            try {
                String result;
                if (projectName != null) {
                    result = dockerImageService.loadAndPushImage(filePath, projectName);
                } else {
                    result = dockerImageService.loadAndPushImage(filePath);
                }
                results.add(result);
                successCount++;
                logger.info("Successfully processed file {}/{}: {} -> {}", 
                        successCount + failureCount, filePaths.size(), filePath, result);
            } catch (Exception e) {
                failureCount++;
                logger.error("Failed to process file {}/{}: {}", 
                        successCount + failureCount, filePaths.size(), filePath, e);
                results.add("FAILED: " + filePath + " - " + e.getMessage());
            }
        }
        
        logger.info("Batch auto-parse operation completed: {} success, {} failures", successCount, failureCount);
        return results;
    }

    /**
     * 获取镜像统计信息
     */
    public ImageStatistics getImageStatistics(String projectName) {
        try {
            List<HarborImage> images = harborImageService.listImages(projectName);
            
            long totalImages = images.size();
            long totalSize = 0;
            long totalPullCount = 0;
            int totalTags = 0;
            
            for (HarborImage image : images) {
                if (image.getSize() != null) {
                    totalSize += image.getSize();
                }
                if (image.getPullCount() != null) {
                    totalPullCount += image.getPullCount();
                }
                if (image.getTagsCount() != null) {
                    totalTags += image.getTagsCount();
                }
            }
            
            return new ImageStatistics(totalImages, totalTags, totalSize, totalPullCount);
        } catch (Exception e) {
            logger.error("Failed to get image statistics for project: {}", projectName, e);
            throw e;
        }
    }

    /**
     * 镜像统计信息内部类
     */
    public static class ImageStatistics {
        private final long totalImages;
        private final int totalTags;
        private final long totalSize;
        private final long totalPullCount;

        public ImageStatistics(long totalImages, int totalTags, long totalSize, long totalPullCount) {
            this.totalImages = totalImages;
            this.totalTags = totalTags;
            this.totalSize = totalSize;
            this.totalPullCount = totalPullCount;
        }

        public long getTotalImages() {
            return totalImages;
        }

        public int getTotalTags() {
            return totalTags;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getTotalPullCount() {
            return totalPullCount;
        }

        public String getFormattedSize() {
            if (totalSize < 1024) {
                return totalSize + " B";
            } else if (totalSize < 1024 * 1024) {
                return String.format("%.2f KB", totalSize / 1024.0);
            } else if (totalSize < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", totalSize / (1024.0 * 1024));
            } else {
                return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
            }
        }

        @Override
        public String toString() {
            return String.format("ImageStatistics{totalImages=%d, totalTags=%d, totalSize=%s, totalPullCount=%d}",
                    totalImages, totalTags, getFormattedSize(), totalPullCount);
        }
    }
} 