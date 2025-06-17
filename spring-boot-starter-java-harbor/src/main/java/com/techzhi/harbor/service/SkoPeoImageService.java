package com.techzhi.harbor.service;


import com.techzhi.harbor.config.HarborProperties;
import com.techzhi.harbor.util.SkopeoUtil;
import org.springframework.stereotype.Service;

/**
 * @author shouzhi
 */
@Service
public class SkoPeoImageService {

     private final HarborProperties properties;

     public SkoPeoImageService(HarborProperties properties) {
         this.properties = properties;
     }


     /**
      * 推送镜像到harbor
      * @param tarFilePath 本地tar文件路径
      * @param imageName 镜像名称
      * @param imageTag 镜像标签
      * @return 执行结果
      */
     public String pushTarToHarbor(String tarFilePath, String imageName, String imageTag) {
         return SkopeoUtil.pushTarToHarbor(
                 properties.getHost(),
                 properties.getUsername(),
                 properties.getPassword(),
                 tarFilePath,
                 properties.getProject(),
                 imageName,
                 imageTag
         );
     }

}