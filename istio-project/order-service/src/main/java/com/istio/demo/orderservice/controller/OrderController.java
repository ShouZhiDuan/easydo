package com.istio.demo.orderservice.controller;

import com.istio.demo.orderservice.model.Order;
import com.istio.demo.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Value("${spring.application.name:order-service}")
    private String serviceName;
    
    @Value("${app.version:v1}")
    private String version;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", serviceName,
                "version", version,
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            logger.info("Retrieved {} orders", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            Optional<Order> order = orderService.getOrderById(id);
            if (order.isPresent()) {
                logger.info("Retrieved order with id: {}", id);
                return ResponseEntity.ok(order.get());
            } else {
                logger.warn("Order not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving order with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            logger.info("Retrieved {} orders for user: {}", orders.size(), userId);
            
            Map<String, Object> response = Map.of(
                    "userId", userId,
                    "orders", orders,
                    "totalOrders", orders.size(),
                    "service", serviceName,
                    "version", version
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving orders for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            logger.info("Retrieved {} orders with status: {}", orders.size(), status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders with status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            logger.info("Created order for user: {} with product: {}", 
                    createdOrder.getUserId(), createdOrder.getProductName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            logger.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            logger.info("Updated order with id: {}", id);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            logger.info("Updated order status for id: {} to {}", id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order status for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            logger.info("Deleted order with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting order with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 测试接口
    @GetMapping("/user/{userId}/slow")
    public ResponseEntity<List<Order>> getSlowUserOrders(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.simulateSlowQuery(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error in slow query for user orders: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}/error")
    public ResponseEntity<List<Order>> getErrorUserOrders(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.simulateError(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Simulated error for user orders: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}/load")
    public ResponseEntity<List<Order>> getHighLoadUserOrders(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.simulateHighLoad(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error in high load simulation for user orders: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 获取服务信息和数据状态
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        long orderCount = orderService.getAllOrders().size();
        return ResponseEntity.ok(Map.of(
                "service", serviceName,
                "version", version,
                "description", "Order management service for Istio demo",
                "mockDataStatus", orderCount > 0 ? "Initialized" : "Empty",
                "totalOrders", orderCount,
                "features", List.of(
                        "Order CRUD operations",
                        "Order status management",
                        "User order retrieval",
                        "Performance testing endpoints",
                        "Metrics collection"
                )
        ));
    }

    @GetMapping("/trace")
    public ResponseEntity<Map<String, String>> trace() {
        logger.info("Order service trace endpoint called");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully reached order-service",
                "service", serviceName,
                "version", version
        ));
    }
} 