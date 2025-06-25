package com.istio.demo.orderservice.service;

import com.istio.demo.orderservice.model.Order;
import com.istio.demo.orderservice.repository.OrderRepository;
// 注释掉监控相关导入 - Istio会自动收集这些指标
// import io.micrometer.core.instrument.Counter;
// import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    // 注释掉监控相关字段 - Istio会自动收集这些指标
    // private final Counter orderCreationCounter;
    private final Random random = new Random();
    
    // 简化构造函数，专注业务逻辑
    public OrderService() {
        // 移除监控相关初始化代码
        // this.orderCreationCounter = Counter.builder("orders.created")
        //         .description("Number of orders created")
        //         .register(meterRegistry);
    }
    
    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(Long id) {
        logger.info("Fetching order with id: {}", id);
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId);
    }
    
    public List<Order> getOrdersByStatus(String status) {
        logger.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    public Order createOrder(Order order) {
        logger.info("Creating order for user: {} with product: {}", order.getUserId(), order.getProductName());
        Order savedOrder = orderRepository.save(order);
        // 注释掉监控计数器 - Istio会自动收集这些指标
        // orderCreationCounter.increment();
        return savedOrder;
    }
    
    public Order updateOrder(Long id, Order order) {
        logger.info("Updating order with id: {}", id);
        order.setId(id);
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(Long id, String status) {
        logger.info("Updating order status for id: {} to {}", id, status);
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + id);
    }
    
    public void deleteOrder(Long id) {
        logger.info("Deleting order with id: {}", id);
        orderRepository.deleteById(id);
    }
    
    // 模拟慢查询，用于测试超时
    public List<Order> simulateSlowQuery(Long userId) throws InterruptedException {
        logger.info("Simulating slow query for user orders: {}", userId);
        Thread.sleep(2000); // 2秒延迟
        return getOrdersByUserId(userId);
    }
    
    // 模拟错误，用于测试熔断
    public List<Order> simulateError(Long userId) {
        logger.info("Simulating error for user orders: {}", userId);
        if (random.nextBoolean()) {
            throw new RuntimeException("Simulated database error");
        }
        return getOrdersByUserId(userId);
    }
    
    // 模拟高负载，用于测试限流
    public List<Order> simulateHighLoad(Long userId) throws InterruptedException {
        logger.info("Simulating high load for user orders: {}", userId);
        Thread.sleep(500 + random.nextInt(1000)); // 随机延迟 0.5-1.5秒
        return getOrdersByUserId(userId);
    }
} 