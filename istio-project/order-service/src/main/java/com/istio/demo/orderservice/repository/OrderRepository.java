package com.istio.demo.orderservice.repository;

import com.istio.demo.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    
    // 内存存储 - 使用ConcurrentHashMap保证线程安全
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public OrderRepository() {
        logger.info("Initializing OrderRepository with mock data...");
        // 初始化一些mock数据
        initMockData();
        logger.info("OrderRepository initialized with {} orders", orders.size());
    }
    
    private void initMockData() {
        try {
            logger.info("Creating mock order data...");
            // 创建一些示例订单数据
            save(new Order(1L, "MacBook Pro", 1, new BigDecimal("2499.99")));
            save(new Order(1L, "iPhone 15", 2, new BigDecimal("999.99")));
            save(new Order(2L, "iPad Air", 1, new BigDecimal("799.99")));
            save(new Order(2L, "AirPods Pro", 1, new BigDecimal("249.99")));
            save(new Order(3L, "MacBook Air", 1, new BigDecimal("1299.99")));
            
            logger.info("Created {} initial orders", orders.size());
            
            // 设置一些不同的状态用于演示
            List<Order> allOrders = new ArrayList<>(orders.values());
            if (allOrders.size() > 0) allOrders.get(0).setStatus("COMPLETED");
            if (allOrders.size() > 1) allOrders.get(1).setStatus("SHIPPED");
            if (allOrders.size() > 2) allOrders.get(2).setStatus("PROCESSING");
            
            logger.info("Mock data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error initializing mock data", e);
        }
    }
    
    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }
    
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }
    
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());
        orders.put(order.getId(), order);
        return order;
    }
    
    public void deleteById(Long id) {
        orders.remove(id);
    }
    
    public boolean existsById(Long id) {
        return orders.containsKey(id);
    }
    
    public long count() {
        return orders.size();
    }
    
    // 自定义查询方法
    public List<Order> findByUserId(Long userId) {
        return orders.values().stream()
                .filter(order -> Objects.equals(order.getUserId(), userId))
                .collect(Collectors.toList());
    }
    
    public List<Order> findByStatus(String status) {
        return orders.values().stream()
                .filter(order -> Objects.equals(order.getStatus(), status))
                .collect(Collectors.toList());
    }
    
    public List<Order> findByUserIdAndStatus(Long userId, String status) {
        return orders.values().stream()
                .filter(order -> Objects.equals(order.getUserId(), userId) 
                        && Objects.equals(order.getStatus(), status))
                .collect(Collectors.toList());
    }
} 