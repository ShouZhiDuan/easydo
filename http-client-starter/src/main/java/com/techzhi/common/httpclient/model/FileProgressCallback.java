package com.techzhi.common.httpclient.model;

/**
 * 文件操作进度回调接口
 * 用于监控文件上传和下载的进度
 * 
 * @author TechZhi
 * @version 1.0.0
 */
public interface FileProgressCallback {

    /**
     * 进度更新回调
     * 
     * @param bytesTransferred 已传输的字节数
     * @param totalBytes 总字节数，如果未知则为-1
     * @param percentage 进度百分比（0-100），如果总字节数未知则为-1
     */
    void onProgress(long bytesTransferred, long totalBytes, double percentage);

    /**
     * 传输完成回调
     * 
     * @param totalBytes 总字节数
     */
    default void onComplete(long totalBytes) {
        onProgress(totalBytes, totalBytes, 100.0);
    }

    /**
     * 传输失败回调
     * 
     * @param exception 异常信息
     */
    default void onError(Exception exception) {
        // 默认空实现
    }

    /**
     * 传输开始回调
     */
    default void onStart() {
        // 默认空实现
    }
} 