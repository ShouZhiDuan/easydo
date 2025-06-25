package com.istio.demo.userservice.repository;

import com.istio.demo.userservice.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    
    // 内存存储 - 使用ConcurrentHashMap保证线程安全
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserRepository() {
        // 初始化一些mock数据
        initMockData();
    }
    
    private void initMockData() {
        // 创建一些示例用户数据
        save(new User("john_doe", "john.doe@example.com", "John Doe", "+1-555-0101"));
        save(new User("jane_smith", "jane.smith@example.com", "Jane Smith", "+1-555-0102"));
        save(new User("bob_wilson", "bob.wilson@example.com", "Bob Wilson", "+1-555-0103"));
        save(new User("alice_brown", "alice.brown@example.com", "Alice Brown", "+1-555-0104"));
        save(new User("charlie_davis", "charlie.davis@example.com", "Charlie Davis", "+1-555-0105"));
    }
    
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        users.put(user.getId(), user);
        return user;
    }
    
    public void deleteById(Long id) {
        users.remove(id);
    }
    
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
    
    public long count() {
        return users.size();
    }
    
    // 自定义查询方法
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .findFirst();
    }
    
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findFirst();
    }
} 