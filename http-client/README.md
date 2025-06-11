# High-Performance HTTP Client Utility for Spring Boot

This project provides a reusable, high-performance HTTP client utility built with OkHttp and designed for easy integration into Spring Boot applications.

## Features

-   Powered by **OkHttp**: Leverages OkHttp's connection pooling, GZIP compression, and response caching (if configured) for efficient network operations.
-   **Spring Boot Auto-configuration**: Automatically configures the `HttpClientService` bean, making it ready to inject and use.
-   **Configurable Properties**: Allows customization of common HTTP client parameters (timeouts, redirects, retries, SSL) via `application.properties` or `application.yml`.
-   **Common HTTP Methods**: Provides convenient methods for `GET`, `POST` (JSON and Form), `PUT`, `DELETE`.
-   **File Uploads**: Supports multipart file uploads.
-   **File Downloads**: Supports downloading files as an `InputStream`.
-   **HTTPS SSL Bypass**: Option to bypass SSL certificate validation for development or testing against servers with self-signed certificates (use with extreme caution).
-   **Extensible**: Provides a generic `execute` method for more complex or less common HTTP operations.

## Prerequisites

-   Java 11 or higher
-   Maven 3.6.x or higher

## How to Use

### 1. Add as a Maven Dependency

To include this utility in your Spring Boot project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId> <!-- Replace with your actual groupId if you deploy it -->
    <artifactId>http-client-util</artifactId>
    <version>0.0.1-SNAPSHOT</version> <!-- Replace with the desired version -->
</dependency>
```

(Note: You'll need to install this `http-client-util` artifact to your local Maven repository (`mvn clean install`) or deploy it to a shared repository like Nexus or Artifactory for other projects to resolve it.)

### 2. Inject and Use `HttpClientService`

Once the dependency is added, Spring Boot's auto-configuration will create an `HttpClientService` bean. You can inject it into your services or components:

```java
import com.example.httpclient.service.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MyApiService {

    private final HttpClientService httpClientService;

    @Autowired
    public MyApiService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public String fetchDataFromExternalApi() {
        String url = "https://api.example.com/data";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", "your-api-key");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("filter", "active");

        try {
            return httpClientService.get(url, headers, queryParams);
        } catch (IOException e) {
            // Handle exception (e.g., log it, throw a custom exception)
            e.printStackTrace();
            return "Error fetching data: " + e.getMessage();
        }
    }

    public String postDataToExternalApi(Object data) {
        String url = "https://api.example.com/submit";
        try {
            // Assuming OkHttpHttpClientService is used, which has a toJson helper
            // Or use your preferred JSON library to serialize 'data'
            String jsonPayload = ((com.example.httpclient.service.impl.OkHttpHttpClientService) httpClientService).toJson(data);
            return httpClientService.postJson(url, null, jsonPayload);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error posting data: " + e.getMessage();
        }
    }
}
```

### 3. Configuration (Optional)

You can customize the HTTP client's behavior by setting properties in your `application.properties` or `application.yml` file. All properties are prefixed with `custom.http.client`.

**Available Properties:**

| Property                         | Default Value | Description                                                                 |
| -------------------------------- | ------------- | --------------------------------------------------------------------------- |
| `connect-timeout`                | `10000` (ms)  | Connection timeout in milliseconds.                                         |
| `read-timeout`                   | `30000` (ms)  | Read timeout in milliseconds.                                               |
| `write-timeout`                  | `30000` (ms)  | Write timeout in milliseconds.                                              |
| `follow-redirects`               | `true`        | Whether to follow HTTP redirects automatically.                             |
| `retry-on-connection-failure`    | `true`        | Whether to retry automatically if a connection failure occurs.              |
| `max-idle-connections`           | `5`           | Maximum number of idle connections to keep in the connection pool.          |
| `keep-alive-duration`            | `300000` (ms) | Keep-alive duration for idle connections in milliseconds (e.g., 5 minutes). |
| `bypass-ssl-validation`          | `false`       | **DANGER**: Set to `true` to bypass SSL certificate validation.             |
| `proxy-host`                     | `null`        | Proxy host address.                                                         |
| `proxy-port`                     | `0`           | Proxy port number.                                                          |
| `proxy-username`                 | `null`        | Username for proxy authentication.                                          |
| `proxy-password`                 | `null`        | Password for proxy authentication.                                          |

**Example `application.properties`:**

```properties
custom.http.client.connect-timeout=15000
custom.http.client.read-timeout=60000
custom.http.client.bypass-ssl-validation=false # Keep this false in production!
```

**Example `application.yml`:**

```yaml
custom:
  http:
    client:
      connect-timeout: 15000
      read-timeout: 60000
      bypass-ssl-validation: false # Keep this false in production!
```

#### SSL Bypass Warning

Setting `custom.http.client.bypass-ssl-validation=true` disables important security checks. This should **ONLY** be used in controlled development or testing environments where you are connecting to a trusted server with a self-signed or invalid certificate. **NEVER** use this in production environments connecting to untrusted or public servers, as it exposes your application to man-in-the-middle attacks.

## Building the Utility

To build the `http-client-util` JAR and install it into your local Maven repository:

```bash
mvn clean install
```

This will make it available for other local projects.

## Running Tests

To run the unit tests:

```bash
mvn test
```

## Further Enhancements (TODO)

-   More sophisticated response handling (e.g., mapping to DTOs directly).
-   Support for different request/response content types beyond JSON/Form/Plain Text.
-   Circuit breaker integration (e.g., Resilience4j).
-   More detailed logging options.
-   OAuth 2.0 support.
-   Custom interceptors for request/response manipulation (e.g., adding default headers, logging).
-   Asynchronous request execution returning `CompletableFuture`.