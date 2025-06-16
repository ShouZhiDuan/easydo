package com.techzhi.harbor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Harbor镜像信息
 * 
 * @author techzhi
 */
public class HarborImage {

    /**
     * 镜像名称
     */
    private String name;

    /**
     * 项目ID
     */
    @JsonProperty("project_id")
    private Long projectId;

    /**
     * 仓库ID
     */
    @JsonProperty("repository_id")
    private Long repositoryId;

    /**
     * 标签数量
     */
    @JsonProperty("tags_count")
    private Integer tagsCount;

    /**
     * 拉取次数
     */
    @JsonProperty("pull_count")
    private Long pullCount;

    /**
     * 推送时间
     */
    @JsonProperty("push_time")
    private LocalDateTime pushTime;

    /**
     * 创建时间
     */
    @JsonProperty("creation_time")
    private LocalDateTime creationTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    private LocalDateTime updateTime;

    /**
     * 镜像大小
     */
    private Long size;

    /**
     * 镜像标签列表
     */
    private List<String> tags;

    /**
     * 镜像摘要
     */
    private String digest;

    /**
     * 镜像架构
     */
    private String architecture;

    /**
     * 操作系统
     */
    private String os;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Integer getTagsCount() {
        return tagsCount;
    }

    public void setTagsCount(Integer tagsCount) {
        this.tagsCount = tagsCount;
    }

    public Long getPullCount() {
        return pullCount;
    }

    public void setPullCount(Long pullCount) {
        this.pullCount = pullCount;
    }

    public LocalDateTime getPushTime() {
        return pushTime;
    }

    public void setPushTime(LocalDateTime pushTime) {
        this.pushTime = pushTime;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Override
    public String toString() {
        return "HarborImage{" +
                "name='" + name + '\'' +
                ", projectId=" + projectId +
                ", repositoryId=" + repositoryId +
                ", tagsCount=" + tagsCount +
                ", pullCount=" + pullCount +
                ", pushTime=" + pushTime +
                ", creationTime=" + creationTime +
                ", updateTime=" + updateTime +
                ", size=" + size +
                ", tags=" + tags +
                ", digest='" + digest + '\'' +
                ", architecture='" + architecture + '\'' +
                ", os='" + os + '\'' +
                '}';
    }
} 