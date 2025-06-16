package com.techzhi.harbor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.techzhi.harbor.client.HarborClient;
import com.techzhi.harbor.config.HarborProperties;
import com.techzhi.harbor.exception.HarborException;
import com.techzhi.harbor.model.HarborImage;
import com.techzhi.harbor.model.HarborTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Harbor镜像服务
 * 
 * @author techzhi
 */
@Service
public class HarborImageService {

    private static final Logger logger = LoggerFactory.getLogger(HarborImageService.class);

    private final HarborClient harborClient;
    private final HarborProperties properties;

    public HarborImageService(HarborClient harborClient, HarborProperties properties) {
        this.harborClient = harborClient;
        this.properties = properties;
    }

    /**
     * 获取项目下的所有镜像列表
     */
    public List<HarborImage> listImages() {
        return listImages(properties.getProject());
    }

    /**
     * 获取指定项目下的所有镜像列表
     */
    public List<HarborImage> listImages(String projectName) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8));
            return harborClient.get(path, new TypeReference<List<HarborImage>>() {});
        } catch (Exception e) {
            logger.error("Failed to list images for project: {}", projectName, e);
            throw new HarborException("Failed to list images", e);
        }
    }

    /**
     * 获取镜像的所有标签
     */
    public List<HarborTag> listImageTags(String imageName) {
        return listImageTags(properties.getProject(), imageName);
    }

    /**
     * 获取指定项目和镜像的所有标签
     */
    public List<HarborTag> listImageTags(String projectName, String imageName) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s/artifacts", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8));
            return harborClient.get(path, new TypeReference<List<HarborTag>>() {});
        } catch (Exception e) {
            logger.error("Failed to list tags for image: {}/{}", projectName, imageName, e);
            throw new HarborException("Failed to list image tags", e);
        }
    }

    /**
     * 检查镜像是否存在
     */
    public boolean imageExists(String imageName) {
        return imageExists(properties.getProject(), imageName);
    }

    /**
     * 检查指定项目中的镜像是否存在
     */
    public boolean imageExists(String projectName, String imageName) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8));
            harborClient.get(path, new TypeReference<Map<String, Object>>() {});
            return true;
        } catch (HarborException e) {
            if (e.getCode() == 404) {
                return false;
            }
            throw e;
        } catch (Exception e) {
            logger.error("Failed to check image existence: {}/{}", projectName, imageName, e);
            throw new HarborException("Failed to check image existence", e);
        }
    }

    /**
     * 检查镜像标签是否存在
     */
    public boolean imageTagExists(String imageName, String tag) {
        return imageTagExists(properties.getProject(), imageName, tag);
    }

    /**
     * 检查指定项目中的镜像标签是否存在
     */
    public boolean imageTagExists(String projectName, String imageName, String tag) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s/artifacts/%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8),
                    URLEncoder.encode(tag, StandardCharsets.UTF_8));
            harborClient.get(path, new TypeReference<Map<String, Object>>() {});
            return true;
        } catch (HarborException e) {
            if (e.getCode() == 404) {
                return false;
            }
            throw e;
        } catch (Exception e) {
            logger.error("Failed to check image tag existence: {}/{}: {}", projectName, imageName, tag, e);
            throw new HarborException("Failed to check image tag existence", e);
        }
    }

    /**
     * 删除镜像
     */
    public void deleteImage(String imageName) {
        deleteImage(properties.getProject(), imageName);
    }

    /**
     * 删除指定项目中的镜像
     */
    public void deleteImage(String projectName, String imageName) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8));
            harborClient.delete(path);
            logger.info("Successfully deleted image: {}/{}", projectName, imageName);
        } catch (Exception e) {
            logger.error("Failed to delete image: {}/{}", projectName, imageName, e);
            throw new HarborException("Failed to delete image", e);
        }
    }

    /**
     * 删除镜像标签
     */
    public void deleteImageTag(String imageName, String tag) {
        deleteImageTag(properties.getProject(), imageName, tag);
    }

    /**
     * 删除指定项目中的镜像标签
     */
    public void deleteImageTag(String projectName, String imageName, String tag) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s/artifacts/%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8),
                    URLEncoder.encode(tag, StandardCharsets.UTF_8));
            harborClient.delete(path);
            logger.info("Successfully deleted image tag: {}/{}: {}", projectName, imageName, tag);
        } catch (Exception e) {
            logger.error("Failed to delete image tag: {}/{}: {}", projectName, imageName, tag, e);
            throw new HarborException("Failed to delete image tag", e);
        }
    }

    /**
     * 获取镜像详细信息
     */
    public HarborImage getImageInfo(String imageName) {
        return getImageInfo(properties.getProject(), imageName);
    }

    /**
     * 获取指定项目中镜像的详细信息
     */
    public HarborImage getImageInfo(String projectName, String imageName) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories/%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(imageName, StandardCharsets.UTF_8));
            return harborClient.get(path, new TypeReference<HarborImage>() {});
        } catch (Exception e) {
            logger.error("Failed to get image info: {}/{}", projectName, imageName, e);
            throw new HarborException("Failed to get image info", e);
        }
    }

    /**
     * 搜索镜像
     */
    public List<HarborImage> searchImages(String keyword) {
        return searchImages(properties.getProject(), keyword);
    }

    /**
     * 在指定项目中搜索镜像
     */
    public List<HarborImage> searchImages(String projectName, String keyword) {
        try {
            String path = String.format("/api/v2.0/projects/%s/repositories?q=%s", 
                    URLEncoder.encode(projectName, StandardCharsets.UTF_8),
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8));
            return harborClient.get(path, new TypeReference<List<HarborImage>>() {});
        } catch (Exception e) {
            logger.error("Failed to search images with keyword: {} in project: {}", keyword, projectName, e);
            throw new HarborException("Failed to search images", e);
        }
    }
} 