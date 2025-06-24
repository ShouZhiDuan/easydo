package com.example.provider.config;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;


@Slf4j
public class SkopeoUtil {

    // Docker仓库地址格式校验的正则表达式 - 只支持私有仓库和IP地址仓库格式
    private static final String DOCKER_REGISTRY_PATTERN = 
        "^" +
        // Registry部分 (必需): 域名或IP地址，可包含端口
        "(?:" +
            // 域名格式 - 必须包含至少一个点
            "(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)*)" +
            "|" +
            // IP地址格式
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
        ")" +
        // 端口号 (可选)
        "(?::[1-9][0-9]{0,4})?" +
        "/" +
        // 命名空间/项目名 (必需)
        "[a-z0-9]+(?:[._-][a-z0-9]+)*" +
        "/" +
        // 镜像名 (必需)
        "[a-z0-9]+(?:[._-][a-z0-9]+)*" +
        // 标签部分 (可选)
        "(?::[a-zA-Z0-9_][a-zA-Z0-9._-]{0,127})?$";

    private static final Pattern DOCKER_REGISTRY_REGEX = Pattern.compile(DOCKER_REGISTRY_PATTERN);

    /**
     * 校验Docker仓库地址格式是否标准
     * 仅支持私有仓库和IP地址仓库格式：
     * - registry.example.com/namespace/repository:tag (私有仓库)
     * - registry.example.com:5000/project/image:v1.0 (带端口的私有仓库)
     * - 192.168.1.100:5000/namespace/repository:tag (IP地址仓库)
     * - 192.168.50.103/flow/cust-cont:20250617191632-x86 (IP地址仓库)
     * 
     * 注意：不支持Docker Hub简单格式（如ubuntu:20.04）
     * 
     * @param repositoryUrl Docker仓库地址
     * @return true 如果格式标准，false 如果格式不标准
     */
    public static boolean isValidDockerRepositoryUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            return false;
        }
        
        String trimmedUrl = repositoryUrl.trim();
        
        // 移除可能的协议前缀 (docker://)
        if (trimmedUrl.startsWith("docker://")) {
            trimmedUrl = trimmedUrl.substring(9);
        }
        
        return DOCKER_REGISTRY_REGEX.matcher(trimmedUrl).matches();
    }

    /**
     * 校验Docker仓库地址格式并返回详细信息
     * 
     * @param repositoryUrl Docker仓库地址
     * @return 包含校验结果和详细信息的字符串
     */
    public static boolean validateDockerRepositoryUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            log.error("Docker仓库地址不能为空");
            return false;
        }
        
        String trimmedUrl = repositoryUrl.trim();
        //String originalUrl = trimmedUrl;
        
        // 移除可能的协议前缀
        if (trimmedUrl.startsWith("docker://")) {
            trimmedUrl = trimmedUrl.substring(9);
        }
        
        if (DOCKER_REGISTRY_REGEX.matcher(trimmedUrl).matches()) {
            //return "Docker仓库地址格式正确: " + originalUrl;
            return true;
        } else {
            return false;
            // return "Docker仓库地址格式不正确: " + originalUrl + 
            //        "\n支持的格式示例:" +
            //        "\n- ubuntu:20.04" +
            //        "\n- library/ubuntu:latest" +
            //        "\n- username/repository:tag" +
            //        "\n- registry.example.com/namespace/repository:tag" +
            //        "\n- registry.example.com:5000/project/image:v1.0" +
            //        "\n- 192.168.1.100:5000/namespace/repository:tag";
        }
    }

       /**
     * 通用的skopeo命令执行方法
     * @param skopeoCommand skopeo命令数组
     * @return 返回命令的执行结果
     */
    private static String executeSkopeoCommand(String[] skopeoCommand) {
      ProcessBuilder processBuilder = new ProcessBuilder(skopeoCommand);
      processBuilder.redirectErrorStream(true); // 将错误输出流重定向到标准输出流

      StringBuilder output = new StringBuilder();
      try {
          Process process = processBuilder.start(); // 启动进程
          BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); // 获取输出流
          String line;
          while ((line = reader.readLine()) != null) { // 读取输出
              output.append(line).append("\n");
          }
          int exitCode = process.waitFor(); // 等待进程结束
          if (exitCode == 0) {
              System.out.println("命令执行成功 exitCode = " + exitCode);
          } else {
              output.append("镜像操作失败，退出码：").append(exitCode).append("\n");
          }
      } catch (IOException e) {
          output.append("启动skopeo进程失败！\n");
          e.printStackTrace();
      } catch (InterruptedException e) {
          output.append("skopeo进程被中断！\n");
          e.printStackTrace();
      }
      return output.toString();
  }


    /**
     * 判断镜像是否存在
     * skopeo inspect --creds flow:Nvx_1024 --tls-verify=false docker://192.168.50.103/flow/cust-cont:20250617191632-x86
     */
  public static String  checkImageExist(String harborUrl, String harborUsername, String harborPassword) {
//      String[] command = {
//              "skopeo",
//              "inspect",
//              "--src-tls-verify=false",
//              "--dest-tls-verify=false",
//              "--dest-creds=" + harborUsername + ":" + harborPassword,
//              "docker://" + harborUrl
//      };
      String[] command = {
              "skopeo",
              "inspect",
              "--tls-verify=false",
              "--creds",
              harborUsername + ":" + harborPassword,
              "docker://" + harborUrl
      };

      String rst = executeSkopeoCommand(command);

      log.info("命令执行判断镜像是否存在结果：" + rst);

      return rst;
  }




  /**
   * 将本地tar包中的镜像推送到Harbor仓库
   * @param harborUrl Harbor仓库地址
   * @param harborUsername Harbor用户名
   * @param harborPassword Harbor密码
   * @param tarFilePath 本地tar包路径
   * @param harborProject Harbor项目名称
   * @param imageName 镜像名称（不包含标签）
   * @param imageTag 镜像标签
   * @return 返回命令的执行结果
   */
  public static String pushTarToHarbor(String harborUrl, String harborUsername, String harborPassword,
                                       String tarFilePath, String harborProject,
                                       String imageName, String imageTag) {
      String harborImage = harborUrl + "/" + harborProject + "/" + imageName + ":" + imageTag;
      log.info("正在将镜像推送到Harbor仓库：" + harborImage);
      String[] command = {
          "skopeo", "copy", "--policy", "/home/app/skopeo/default-policy.json",
          "--src-tls-verify=false", "--dest-tls-verify=false",
          "--dest-creds=" + harborUsername + ":" + harborPassword,
          "docker-archive:" + tarFilePath,
          "docker://" + harborImage
      };

      log.info("执行命令：" + String.join(" ", command));
      String result = executeSkopeoCommand(command);
      log.info("命令执行结果：" + result);
      return result;
  }

  /**
   * 将本地tar包中的镜像推送到Harbor仓库（简化版，使用默认项目和标签）
   * @param harborUrl Harbor仓库地址
   * @param harborUsername Harbor用户名
   * @param harborPassword Harbor密码
   * @param tarFilePath 本地tar包路径
   * @return 返回命令的执行结果
   */
  public static String pushTarToHarbor(String harborUrl, String harborUsername, String harborPassword,
                                       String tarFilePath) {
      String harborProject = "default"; // 默认项目
      String imageName = "default-image"; // 默认镜像名
      String imageTag = "latest"; // 默认标签
      return pushTarToHarbor(harborUrl, harborUsername, harborPassword, tarFilePath,
              harborProject, imageName, imageTag);
  }

  /**
   * 将本地tar包中的镜像推送到Harbor仓库（仅指定基本参数）
   * @param harborUrl Harbor仓库地址
   * @param harborUsername Harbor用户名
   * @param harborPassword Harbor密码
   * @param tarFilePath 本地tar包路径
   * @param harborProject Harbor项目名称
   * @return 返回命令的执行结果
   */
  public static String pushTarToHarbor(String harborUrl, String harborUsername, String harborPassword,
                                       String tarFilePath, String harborProject) {
      String imageName = "default-image"; // 默认镜像名
      String imageTag = "latest"; // 默认标签
      return pushTarToHarbor(harborUrl, harborUsername, harborPassword, tarFilePath,
              harborProject, imageName, imageTag);
  }

  /**
   * 测试方法
   * @param args 命令行参数
   */
  public static void main(String[] args) {
      boolean validDockerRepositoryUrl =
              com.example.provider.config.SkopeoUtil.isValidDockerRepositoryUrl("19280:8080/flow/cust-cont:20250618174715-x86");
      System.out.println(validDockerRepositoryUrl);

  }

}
