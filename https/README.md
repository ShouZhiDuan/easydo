# HTTPS 双向认证与客户端信任配置实践回顾

本文档详细记录了从零开始构建一个支持 HTTPS 的 Spring Boot 服务，并进一步实现一个能够信任自定义证书的 HTTPS 客户端的全过程。

---

## **第一部分：创建支持 HTTPS 的 Spring Boot 服务 (`https-demo`)**

这个阶段的目标是创建一个简单的 Spring Boot 应用，它能通过 HTTPS 协议提供一个 "Hello World" 端点。

### 1. 项目创建与端点实现

*   **做法**：通过 `curl` 调用 `start.spring.io` 的 API，创建了一个名为 `https-demo` 的基础 Maven 项目，仅包含 `web` 依赖。
    ```bash
    # 在 https 目录下执行
    curl https://start.spring.io/starter.zip \
         -d type=maven-project \
         -d dependencies=web \
         -d baseDir=https-demo \
         -o https-demo.zip && unzip https-demo.zip && rm https-demo.zip
    ```
*   **代码**：在 `com.example.demo.controller` 包下创建了 `HelloController`，并提供一个 `/hello` 的 GET 端点，返回 "Hello, HTTPS!"。

### 2. 生成自签名密钥库 (Keystore)

*   **背景**：要启用 HTTPS，服务器需要一个包含私钥和公钥证书的密钥库（Keystore）。
*   **做法**：使用 Java 的 `keytool` 工具在 `https-demo/src/main/resources/` 目录下生成了一个名为 `keystore.p12` 的密钥库文件。
*   **命令**：
    ```bash
    keytool -genkeypair -alias https -keyalg RSA -keysize 2048 \
            -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
            -validity 365 -storepass password \
            -dname "CN=localhost, OU=dev, O=example, L=Test, ST=Test, C=CN"
    ```

### 3. 配置 HTTPS

*   **做法**：修改 `https-demo/src/main/resources/application.properties` 文件，添加 SSL 相关配置。
*   **配置内容**：
    ```properties
    server.port=8443
    server.ssl.key-store=classpath:keystore.p12
    server.ssl.key-store-password=password
    server.ssl.key-store-type=PKCS12
    server.ssl.key-alias=httpss
    ```

### 4. 导出证书用于客户端验证

*   **背景**：由于我们的证书是自签名的，默认情况下客户端（如 `curl` 或浏览器）不会信任它。为了让客户端能验证服务器身份，我们需要将服务器的公共证书导出。
*   **做法**：使用 `keytool` 从 `keystore.p12` 中导出公共证书，并保存为 `certificate.pem`。
*   **命令**：
    ```bash
    keytool -exportcert -alias https -file certificate.pem -keystore src/main/resources/keystore.p12 -storepass password
    ```
*   **验证**：使用 `curl --cacert certificate.pem https://localhost:8443/hello` 命令，成功验证了端点，证明服务端 HTTPS 配置正确。

---

## **第二部分：实现信任自签名证书的 HTTPS 客户端**

这个阶段的目标是让 `https-demo` 作为客户端，去调用另一个同样使用自签名证书的 HTTPS 服务 (`https-server`)，并且要求不能禁用证书校验。

### 1. 新需求与服务端 (`https-server`) 搭建

*   **做法**：以完全相同的方式创建了第二个 Spring Boot 项目 `https-server`，并为其配置了独立的自签名证书，使其在 `8444` 端口上运行，并提供一个 `/external-hello` 端点。

### 2. 创建客户端信任库 (Truststore)

*   **背景**：客户端（`https-demo`）要信任服务端（`https-server`），就需要一个信任库（Truststore），其中包含服务端的公共证书。
*   **做法**：将 `https-server` 的公共证书 (`certificate.pem`) 导入到 `https-demo` 项目的一个新密钥库 `truststore.p12` 中。
*   **命令**：
    ```bash
    keytool -importcert -alias https-server \
            -file ../https-server/src/main/resources/certificate.pem \
            -keystore src/main/resources/truststore.p12 \
            -storetype PKCS12 -storepass password -noprompt
    ```

### 3. 配置客户端：漫长的调试与最终解决方案

这是整个过程中最曲折的部分，我们遇到了多个环环相扣的问题。

*   **遇到的核心问题 1：`javax.net.ssl.SSLHandshakeException: PKIX path building failed`**
    *   **问题描述**：这是最根本的错误。它明确指出，`https-demo` 在调用 `https-server` 时，无法在其信任链中找到 `https-server` 的证书。这证明我们配置的 `truststore.p12` 没有被 `RestTemplate` 正确加载。
    *   **失败的尝试**：我尝试了多种 Spring Boot 自动配置的方式，如在 `application.properties` 中设置 `server.ssl.trust-store` 或 `spring.ssl.bundle` 属性，但均未生效，因为这些配置项的适用范围与我们的场景不完全匹配，或是在复杂环境下容易出错。

*   **遇到的核心问题 2：`ClassNotFoundException` 与 Maven 依赖冲突**
    *   **问题描述**：在反复配置 `RestTemplate` 的过程中，频繁出现 `NoClassDefFoundError` 或 `ClassNotFoundException`，通常指向 `org.apache.hc.client5` 包下的类。这表明项目的 `pom.xml` 依赖管理存在严重问题。
    *   **失败的尝试**：我多次尝试添加 `spring-boot-starter-httpclient` 或 `httpclient5` 依赖，但都因为下一个问题而失败。

*   **遇到的核心问题 3：私有 Maven 仓库（Nexus）问题**
    *   **问题描述**：最终，日志显示 Maven 构建失败的根源是无法从您的私有 Nexus 仓库（`http://192.168.50.16:8081`）解析 `spring-boot-starter-httpclient` 这个依赖。
    *   **问题根源**：这表明问题已超出代码范畴，是构建环境和网络配置导致了依赖无法被正确下载。

### 4. **最终的正确解决方案（由您提出）**

*   **核心思路**：**将所有必需的 Maven 依赖包预先下载到本地，然后让 `pom.xml` 直接依赖这些本地的 JAR 文件。** 这个方案完全绕过了不稳定的私有仓库，从根本上解决了依赖问题。

*   **具体解决步骤**：
    1.  **创建 `lib` 目录**：在 `https-demo` 项目根目录下创建 `lib` 文件夹。
    2.  **手动下载依赖**：使用 `curl` 从公共的 Maven 中央仓库将 `httpclient5` 及其所有相关的 JAR 包（`httpcore5`, `httpcore5-h2`, `commons-codec`）下载到 `lib` 目录。
        ```bash
        # 在 https-demo 目录下执行
        mkdir -p lib
        curl -o lib/httpclient5-5.3.1.jar https://repo1.maven.org/maven2/org/apache/httpcomponents/client5/httpclient5/5.3.1/httpclient5-5.3.1.jar
        curl -o lib/httpcore5-5.2.4.jar https://repo1.maven.org/maven2/org/apache/httpcomponents/core5/httpcore5/5.2.4/httpcore5-5.2.4.jar
        curl -o lib/httpcore5-h2-5.2.4.jar https://repo1.maven.org/maven2/org/apache/httpcomponents/core5/httpcore5-h2/5.2.4/httpcore5-h2-5.2.4.jar
        curl -o lib/commons-codec-1.15.jar https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15.jar
        ```
    3.  **修改 `pom.xml`**：删除了所有远程的 `httpclient` 依赖，改为使用 `<scope>system</scope>` 和 `<systemPath>` 来明确指向 `lib` 目录下的每一个 JAR 文件。
        ```xml
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.3.1</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/lib/httpclient5-5.3.1.jar</systemPath>
        </dependency>
        <!-- other local dependencies... -->
        ```
    4.  **重构代码，回归简洁**：在依赖问题被彻底解决后，我们得以回归到最清晰、最可靠的编程模型：
        *   创建了 `RestTemplateConfig.java`，在其中以编程方式加载 `truststore.p12`，构建自定义的 `SSLContext`，并最终生成一个正确配置的 `RestTemplate` Bean。
        *   将 `HelloController` 恢复为最初的设计，通过构造函数注入这个 `RestTemplate` Bean，代码变得干净且易于理解。
        *   移除了所有为了临时解决问题而加入的 "补丁" 代码（如 `exclude` 自动配置等）。

---

## **总结与启示**

本次实践最核心的挑战并非 SSL 配置本身，而是由构建环境（私有 Maven 仓库）引起的、难以排查的依赖问题。在遇到看似无解的连锁错误时，回归本源，采用如**本地化依赖**这样最直接、最能排除干扰的方案，是解决问题的金钥匙。

最终，我们成功地构建了一个完全符合预期的、能够进行双向 HTTPS 通信并正确处理自定义信任链的 Spring Boot 应用。 