#!/bin/bash

echo "=== NFS 挂载测试脚本 ==="
echo "开始验证 NFS StorageClass 的挂载有效性..."

# 1. 应用 StorageClass
echo "1. 应用 NFS StorageClass..."
kubectl apply -f nfs-sc.yaml
sleep 2

# 2. 创建 PVC
echo "2. 创建 PersistentVolumeClaim..."
kubectl apply -f nfs-pvc.yaml
sleep 3

# 3. 检查 PVC 状态
echo "3. 检查 PVC 状态..."
kubectl get pvc nfs-test-pvc

# 4. 部署测试 Pod
echo "4. 部署测试 Pod..."
kubectl apply -f nfs-test-pod.yaml

# 5. 等待 Pod 启动
echo "5. 等待 Pod 启动..."
kubectl wait --for=condition=Ready pod/nfs-test-pod --timeout=60s

# 6. 检查 Pod 状态
echo "6. 检查 Pod 状态..."
kubectl get pod nfs-test-pod -o wide

# 7. 执行挂载测试
echo "7. 执行 NFS 挂载测试..."

echo "检查挂载点..."
kubectl exec nfs-test-pod -- df -h | grep nfs || echo "未找到 NFS 挂载点，继续检查..."

echo "检查挂载目录..."
kubectl exec nfs-test-pod -- ls -la /mnt/nfs

echo "创建测试文件..."
kubectl exec nfs-test-pod -- sh -c 'echo "NFS测试文件 - $(date)" > /mnt/nfs/test-file.txt'

echo "读取测试文件..."
kubectl exec nfs-test-pod -- cat /mnt/nfs/test-file.txt

echo "列出 NFS 目录内容..."
kubectl exec nfs-test-pod -- ls -la /mnt/nfs/

# 8. 在本地验证文件是否同步
echo "8. 验证本地文件同步..."
echo "本地 nfs-exports 目录内容:"
ls -la nfs-exports/

# 9. 显示详细信息用于调试
echo "9. 调试信息..."
echo "Pod 详细信息:"
kubectl describe pod nfs-test-pod

echo "PVC 详细信息:"
kubectl describe pvc nfs-test-pvc

echo "=== 测试完成 ==="
echo "如果看到测试文件内容，说明 NFS 挂载成功！"
echo "清理资源请运行: kubectl delete pod nfs-test-pod && kubectl delete pvc nfs-test-pvc" 