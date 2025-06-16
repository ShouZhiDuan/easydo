package examples;

import com.techzhi.harbor.service.DockerImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Harbor镜像上传示例
 * 
 * @author techzhi
 */
@Component
public class ImageUploadExample {

    @Autowired
    private DockerImageService dockerImageService;

    /**
     * 从tar文件加载并推送镜像到Harbor的示例
     */
    public void uploadImageExample() {
        try {
            // 使用默认项目推送镜像
            String imageUrl1 = dockerImageService.loadAndPushImage(
                "/path/to/image.tar", 
                "my-app", 
                "v1.0.0"
            );
            System.out.println("镜像推送成功，Harbor地址: " + imageUrl1);
            // 输出示例: 192.168.50.103/flow/my-app:v1.0.0

            // 推送到指定项目
            String imageUrl2 = dockerImageService.loadAndPushImage(
                "/path/to/image.tar", 
                "production",  // 指定项目
                "my-app", 
                "v1.0.0"
            );
            System.out.println("镜像推送成功，Harbor地址: " + imageUrl2);
            // 输出示例: 192.168.50.103/production/my-app:v1.0.0

            // 可以将返回的地址用于后续操作
            deployToKubernetes(imageUrl2);
            
        } catch (Exception e) {
            System.err.println("镜像推送失败: " + e.getMessage());
        }
    }

    /**
     * 使用镜像地址部署到Kubernetes的示例
     */
    private void deployToKubernetes(String imageUrl) {
        // 这里可以使用完整的镜像地址进行部署
        System.out.println("正在使用镜像地址部署到Kubernetes: " + imageUrl);
        
        // 示例：生成Kubernetes部署配置
        String kubernetesYaml = generateDeploymentYaml(imageUrl);
        System.out.println("生成的Kubernetes配置:\n" + kubernetesYaml);
    }

    /**
     * 生成Kubernetes部署配置示例
     */
    private String generateDeploymentYaml(String imageUrl) {
        return String.format(
            "apiVersion: apps/v1\n" +
            "kind: Deployment\n" +
            "metadata:\n" +
            "  name: my-app\n" +
            "spec:\n" +
            "  replicas: 3\n" +
            "  selector:\n" +
            "    matchLabels:\n" +
            "      app: my-app\n" +
            "  template:\n" +
            "    metadata:\n" +
            "      labels:\n" +
            "        app: my-app\n" +
            "    spec:\n" +
            "      containers:\n" +
            "      - name: my-app\n" +
            "        image: %s\n" +
            "        ports:\n" +
            "        - containerPort: 8080\n", 
            imageUrl);
    }

    /**
     * 批量处理多个镜像的示例
     */
    public void batchUploadExample() {
        String[] imagePaths = {
            "/path/to/app1.tar",
            "/path/to/app2.tar",
            "/path/to/app3.tar"
        };

        String[] imageNames = {"app1", "app2", "app3"};
        String tag = "latest";

        for (int i = 0; i < imagePaths.length; i++) {
            try {
                String imageUrl = dockerImageService.loadAndPushImage(
                    imagePaths[i], 
                    imageNames[i], 
                    tag
                );
                System.out.println(String.format("✅ %s 推送成功: %s", 
                    imageNames[i], imageUrl));
                
                // 可以将成功的镜像地址存储到数据库或配置文件中
                saveImageUrlToDatabase(imageNames[i], imageUrl);
                
            } catch (Exception e) {
                System.err.println(String.format("❌ %s 推送失败: %s", 
                    imageNames[i], e.getMessage()));
            }
        }
    }

    /**
     * 保存镜像地址到数据库的示例
     */
    private void saveImageUrlToDatabase(String imageName, String imageUrl) {
        // 这里是保存到数据库的示例逻辑
        System.out.println(String.format("保存镜像信息到数据库: %s -> %s", 
            imageName, imageUrl));
    }
} 