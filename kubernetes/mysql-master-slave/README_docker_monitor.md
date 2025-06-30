# Docker 监控脚本使用说明

## 📖 概述

`docker_monitor.sh` 是一个功能强大的Docker资源监控脚本，可以帮助您实时监控Docker环境中的各种资源使用情况，包括镜像、容器、卷、网络和构建缓存等。

## 🚀 功能特性

- **📊 系统概览**: 显示Docker版本、容器数量、镜像数量等基本信息
- **💾 磁盘使用**: 详细显示各类资源的磁盘占用情况
- **🖼️ 镜像管理**: 列出镜像详情，检测悬空镜像
- **📦 容器监控**: 分别显示运行中和停止的容器
- **💿 卷管理**: 监控Docker卷的使用情况
- **🌐 网络信息**: 显示Docker网络配置
- **🔧 构建缓存**: 监控Docker构建缓存使用
- **📈 实时统计**: 显示容器的CPU、内存使用情况
- **👀 持续监控**: 支持实时监控模式
- **🧹 资源清理**: 一键清理未使用的资源
- **📄 数据导出**: 支持JSON格式数据导出
- **📝 日志记录**: 支持日志记录功能

## 📋 系统要求

- Docker 已安装并运行
- Bash Shell 环境
- 可选：`jq` 命令（用于JSON导出功能）

## 🛠️ 安装与使用

### 1. 下载脚本
```bash
# 脚本已在当前目录中创建
chmod +x docker_monitor.sh
```

### 2. 基本使用

#### 显示帮助信息
```bash
./docker_monitor.sh --help
```

#### 显示摘要信息（默认）
```bash
./docker_monitor.sh
# 或
./docker_monitor.sh -s
```

#### 显示所有详细信息
```bash
./docker_monitor.sh -a
```

### 3. 分类监控

#### 监控镜像
```bash
./docker_monitor.sh -i
```

#### 监控容器
```bash
./docker_monitor.sh -c
```

#### 监控卷
```bash
./docker_monitor.sh -v
```

#### 监控网络
```bash
./docker_monitor.sh -n
```

#### 监控构建缓存
```bash
./docker_monitor.sh -b
```

### 4. 高级功能

#### 持续监控模式
```bash
./docker_monitor.sh -w
```
*按 Ctrl+C 退出监控*

#### 清理未使用资源
```bash
./docker_monitor.sh --clean-unused
```

#### 导出JSON数据
```bash
./docker_monitor.sh --export-json
```

#### 记录日志
```bash
./docker_monitor.sh -l
```

## 📊 输出示例

### 系统概览
```
📊 Docker 系统概览
==================================
Docker 版本: 25.0.3
存储路径: /var/lib/docker
容器总数: 18 (运行中: 18)
镜像总数: 10
卷总数: 0
网络总数: 3
```

### 磁盘使用情况
```
💾 磁盘使用情况
==================================
Docker 磁盘使用统计:
TYPE            TOTAL     ACTIVE    SIZE      RECLAIMABLE
Images          10        9         1.068GB   425.9MB (39%)
Containers      18        18        180B      0B (0%)
Local Volumes   0         0         0B        0B
Build Cache     0         0         0B        0B
```

## ⚙️ 配置选项

脚本顶部可以修改以下配置：

```bash
ALERT_THRESHOLD_GB=50  # 磁盘使用告警阈值（GB）
CONTAINER_LIMIT=10     # 显示容器数量限制
IMAGE_LIMIT=15         # 显示镜像数量限制
LOG_FILE="/tmp/docker_monitor.log"  # 日志文件路径
```

## 🎨 颜色说明

- 🔴 **红色**: 警告信息、错误状态
- 🟢 **绿色**: 正常状态、成功信息
- 🟡 **黄色**: 注意信息、警告
- 🔵 **蓝色**: 标题、分类信息
- 🟣 **紫色**: 时间戳、特殊标记
- 🔵 **青色**: 数值、统计信息

## 📝 日志功能

使用 `-l` 参数时，所有输出会同时保存到日志文件：
```bash
./docker_monitor.sh -l
```

日志文件位置：`/tmp/docker_monitor.log`

## 🧹 资源清理

使用 `--clean-unused` 参数可以清理：
- 未使用的容器
- 未使用的镜像
- 未使用的卷
- 未使用的网络

**注意**: 清理操作不可逆，请谨慎使用！

## 📄 JSON导出

使用 `--export-json` 参数可以导出包含以下信息的JSON文件：
- Docker系统信息
- 磁盘使用统计
- 镜像列表
- 容器列表
- 卷列表
- 网络列表

导出文件命名格式：`docker_monitor_YYYYMMDD_HHMMSS.json`

## 🔧 故障排除

### 常见问题

1. **权限问题**
   ```bash
   chmod +x docker_monitor.sh
   ```

2. **Docker未运行**
   ```bash
   sudo systemctl start docker  # Linux
   # 或启动Docker Desktop
   ```

3. **命令不存在**
   - 确保Docker已正确安装
   - 检查PATH环境变量

### 调试模式

在脚本开头添加以下行启用调试：
```bash
set -x  # 启用调试模式
```

## 📈 性能监控

脚本会显示以下性能指标：
- CPU使用率
- 内存使用量
- 网络I/O
- 磁盘I/O

## 🤝 贡献

欢迎提交问题和改进建议！

## 📄 许可证

本脚本采用MIT许可证。

---

*最后更新时间: $(date)* 