# Istio CLI 工具安装指南

由于 `istioctl` 二进制文件过大（93MB），我们已将其从Git仓库中移除。请按照以下步骤安装：

## 方法1：直接下载（推荐）

### macOS/Linux
```bash
# 下载最新版本的Istio
curl -L https://istio.io/downloadIstio | sh -

# 进入Istio目录
cd istio-*

# 将istioctl添加到PATH
export PATH=$PWD/bin:$PATH

# 或者将istioctl复制到系统PATH中
sudo cp bin/istioctl /usr/local/bin/
```

### Windows
1. 访问 [Istio releases](https://github.com/istio/istio/releases)
2. 下载适合Windows的版本
3. 解压并将 `istioctl.exe` 添加到PATH

## 方法2：使用包管理器

### macOS (Homebrew)
```bash
brew install istioctl
```

### Linux (Snap)
```bash
sudo snap install istioctl --classic
```

## 验证安装

```bash
istioctl version
```

## 常用命令

```bash
# 检查Istio配置
istioctl analyze

# 安装Istio到Kubernetes集群
istioctl install --set values.defaultRevision=default

# 查看代理配置
istioctl proxy-config cluster <pod-name>
```

更多信息请参考 [Istio官方文档](https://istio.io/latest/docs/setup/getting-started/)。 