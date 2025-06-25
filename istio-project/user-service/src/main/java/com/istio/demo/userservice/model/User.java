package com.istio.demo.userservice.model;

// 移除JPA相关导入 - 改用纯POJO
// import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

// 移除JPA注解 - 现在是纯POJO类用于Mock数据
// @Entity
// @Table(name = "users")
public class User {
    
    // 移除JPA注解
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username cannot be blank")
    // @Column(unique = true)
    private String username;
    
    @Email(message = "Email should be valid")
    // @Column(unique = true, nullable = false)
    private String email;
    
    // @Column(nullable = false)
    private String fullName;
    
    private String phone;
    
    // @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String username, String email, String fullName, String phone) {
        this();
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
    }
    
    // 移除JPA生命周期注解 - 在setter中直接处理时间更新
    // @PrePersist
    // protected void onCreate() {
    //     createdAt = LocalDateTime.now();
    //     updatedAt = LocalDateTime.now();
    // }
    
    // @PreUpdate
    // protected void onUpdate() {
    //     updatedAt = LocalDateTime.now();
    // }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 