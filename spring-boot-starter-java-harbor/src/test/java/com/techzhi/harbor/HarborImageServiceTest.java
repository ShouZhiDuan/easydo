package com.techzhi.harbor;


import java.util.List;

import com.techzhi.harbor.client.HarborClient;
import com.techzhi.harbor.config.HarborProperties;
import com.techzhi.harbor.model.HarborImage;
import com.techzhi.harbor.service.DockerImageService;
import com.techzhi.harbor.service.HarborImageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


/**
 * Harbor镜像服务测试类
 * 
 * @author techzhi
 */
@SpringBootTest(classes = HarborImageServiceTest.class)
@TestPropertySource(properties = {
        "harbor.host=http://192.168.50.103",
        "harbor.username=admin",
        "harbor.password=Harbor12345",
        "harbor.project=flow"
})
public class HarborImageServiceTest {

    /**
     * 测试获取镜像列表
     * 注意：这个测试需要实际的Harbor环境，仅作为示例
     */
    //@Test
    public void testListImages() {
        //在实际环境中可以启用此测试
        //HarborProperties properties = new HarborProperties();
        //HarborClient client = new HarborClient(properties);
        //HarborImageService service = new HarborImageService(client, properties);
        //List<HarborImage> images = service.listImages();
        //System.out.println("Found " + images.size() + " images");
    }

    /**
     * 测试镜像是否存在
     */
    // @Test
    public void testImageExists() {
        // 在实际环境中可以启用此测试
        // HarborProperties properties = new HarborProperties();
        // HarborClient client = new HarborClient(properties);
        // HarborImageService service = new HarborImageService(client, properties);
        // 
        // boolean exists = service.imageExists("test-image");
        // System.out.println("Image exists: " + exists);
    }



    


} 