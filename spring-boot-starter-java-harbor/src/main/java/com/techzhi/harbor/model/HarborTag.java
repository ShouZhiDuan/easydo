package com.techzhi.harbor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Harbor镜像标签信息
 * 
 * @author techzhi
 */
public class HarborTag {

    /**
     * 标签名称
     */
    private String name;

    /**
     * 镜像大小
     */
    private Long size;

    /**
     * 镜像架构
     */
    private String architecture;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 摘要
     */
    private String digest;

    /**
     * 推送时间
     */
    @JsonProperty("push_time")
    private LocalDateTime pushTime;

    /**
     * 拉取时间
     */
    @JsonProperty("pull_time")
    private LocalDateTime pullTime;

    /**
     * 是否不可变
     */
    private Boolean immutable;

    /**
     * 是否已签名
     */
    private Boolean signed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
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

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public LocalDateTime getPushTime() {
        return pushTime;
    }

    public void setPushTime(LocalDateTime pushTime) {
        this.pushTime = pushTime;
    }

    public LocalDateTime getPullTime() {
        return pullTime;
    }

    public void setPullTime(LocalDateTime pullTime) {
        this.pullTime = pullTime;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    @Override
    public String toString() {
        return "HarborTag{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", architecture='" + architecture + '\'' +
                ", os='" + os + '\'' +
                ", digest='" + digest + '\'' +
                ", pushTime=" + pushTime +
                ", pullTime=" + pullTime +
                ", immutable=" + immutable +
                ", signed=" + signed +
                '}';
    }
} 