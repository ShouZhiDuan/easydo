# vsFTP 服务器配置优化说明

本项目已针对 vsFTP (Very Secure FTP Daemon) 服务器进行了客户端优化配置。

## 主要优化内容

### 1. 连接超时配置
- **连接超时**: 30秒 (connect-timeout: 30000)
- **数据超时**: 60秒 (data-timeout: 60000)
- **控制连接保活**: 5分钟 (control-keep-alive-timeout: 300)

### 2. 传输模式优化
- **被动模式**: 启用 (passive-mode: true)
- **二进制模式**: 启用 (binary-mode: true)
- **字符编码**: UTF-8 (encoding: UTF-8)
- **缓冲区大小**: 8KB (buffer-size: 8192)

### 3. 连接状态检查
- 增加了连接状态验证
- 增加了登录成功验证
- 改进了错误处理和日志记录

## vsFTP 服务器建议配置

为了与客户端配置最佳匹配，建议在 vsFTP 服务器端进行以下配置：

```bash
# /etc/vsftpd.conf 建议配置

# 基本配置
local_enable=YES
write_enable=YES
local_umask=022

# 被动模式配置
pasv_enable=YES
pasv_min_port=21000
pasv_max_port=21010
pasv_address=你的服务器IP

# 超时配置
idle_session_timeout=600
data_connection_timeout=120
accept_timeout=60
connect_timeout=60

# 安全配置
chroot_local_user=YES
allow_writeable_chroot=YES
seccomp_sandbox=NO

# 日志配置
xferlog_enable=YES
xferlog_std_format=YES
log_ftp_protocol=YES

# 用户配置
local_root=/data
user_sub_token=$USER
local_root=/data/$USER
```

## 测试连接

启动服务后，可以通过以下方式测试 FTP 连接：

1. **启动服务**:
   ```bash
   ./start-services.sh
   ```

2. **测试 Service A**:
   ```bash
   curl -X POST http://localhost:8080/api/send \
     -H "Content-Type: application/json" \
     -d '{"message": "Hello vsFTP!"}'
   ```

3. **检查日志**:
   ```bash
   tail -f service-a/logs/service-a.log
   tail -f service-b/logs/service-b.log
   ```

## 故障排除

### 常见问题

1. **连接超时**:
   - 检查防火墙设置
   - 确认被动模式端口范围开放
   - 验证网络连通性

2. **登录失败**:
   - 检查用户名密码
   - 确认用户权限
   - 查看 vsFTP 日志

3. **传输失败**:
   - 检查目录权限
   - 确认磁盘空间
   - 验证文件路径

### 调试模式

在 application.yml 中启用详细日志：

```yaml
logging:
  level:
    org.apache.commons.net: DEBUG
    com.example: DEBUG
```

## 性能优化建议

1. **网络优化**:
   - 使用千兆网络
   - 减少网络跳数
   - 优化 MTU 设置

2. **服务器优化**:
   - 增加 vsFTP 最大连接数
   - 优化磁盘 I/O
   - 调整系统文件描述符限制

3. **客户端优化**:
   - 适当调整缓冲区大小
   - 使用连接池
   - 实现重试机制