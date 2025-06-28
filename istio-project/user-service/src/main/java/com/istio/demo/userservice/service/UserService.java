package com.istio.demo.userservice.service;

import com.istio.demo.userservice.model.User;
import com.istio.demo.userservice.repository.UserRepository;
// 注释掉Istio已提供的功能相关导入 - 避免代码污染
// import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
// import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
// import io.github.resilience4j.retry.annotation.Retry;
// import io.micrometer.core.instrument.Counter;
// import io.micrometer.core.instrument.MeterRegistry;
// import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${order.service.url:http://order-service:8080}")
    private String orderServiceUrl;
    
    // 注释掉监控相关字段 - Istio会自动收集这些指标
    // private final Counter userCreationCounter;
    // private final Timer userServiceTimer;
    
    // 简化构造函数，专注业务逻辑
    public UserService() {
        // 移除监控相关初始化代码
        // this.userCreationCounter = Counter.builder("users.created")
        //         .description("Number of users created")
        //         .register(meterRegistry);
        // this.userServiceTimer = Timer.builder("user.service.duration")
        //         .description("User service operation duration")
        //         .register(meterRegistry);
    }
    
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with id: {}", id);
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username);
    }
    
    public User createUser(User user) {
        logger.info("Creating user: {}", user.getUsername());
        User savedUser = userRepository.save(user);
        // 注释掉监控计数器 - Istio会自动收集这些指标
        // userCreationCounter.increment();
        return savedUser;
    }
    
    public User updateUser(Long id, User user) {
        logger.info("Updating user with id: {}", id);
        user.setId(id);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }
    
    // 注释掉Istio已提供的功能注解 - 熔断、重试、限流都由Istio处理
    // @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackGetUserOrders")
    // @Retry(name = "order-service")
    // @RateLimiter(name = "order-service")
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getUserOrders(Long userId) {
        logger.info("Fetching orders for user: {}", userId);
        
        return webClient.get()
                .uri(orderServiceUrl + "/api/orders/user/" + userId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(orders -> logger.info("Successfully fetched orders for user: {}", userId))
                .doOnError(error -> logger.error("Failed to fetch orders for user: {}", userId, error));
    }
    
    public Mono<Map<String, Object>> fallbackGetUserOrders(Long userId, Exception ex) {
        logger.warn("Fallback triggered for getUserOrders with userId: {}, error: {}", userId, ex.getMessage());
        return Mono.just(Map.of(
                "userId", userId,
                "orders", List.of(),
                "message", "Orders service temporarily unavailable",
                "fallback", true
        ));
    }
    
    // 模拟慢查询，用于测试超时和降级
    public User simulateSlowQuery(Long id) throws InterruptedException {
        logger.info("Simulating slow query for user: {}", id);
        Thread.sleep(3000); // 3秒延迟
        return getUserById(id).orElse(null);
    }
    
    // 模拟错误，用于测试熔断
    public User simulateError(Long id) {
        logger.info("Simulating error for user: {}", id);
        throw new RuntimeException("Simulated service error");
    }

    public Mono<Map> traceOrderService() {
        logger.info("Calling trace endpoint on order-service");
        return webClient.get()
                .uri(orderServiceUrl + "/api/orders/trace")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> logger.info("Successfully received response from order-service trace endpoint"))
                .doOnError(error -> logger.error("Failed to call trace endpoint on order-service", error));
    }
} 