1. 安装 Argo CD

# 创建命名空间
kubectl create namespace argocd

# 安装 Argo CD
wget https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -n argocd -f install.yaml


2. 访问 Argo CD API 服务器
kubectl port-forward svc/argocd-server -n argocd 8080:443



3. 安装 Argo CD CLI
下载并安装 CLI：
MacOS / Linux
# MacOS
brew install argocd
 
# Linux
curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x argocd
sudo mv argocd /usr/local/bin/

# Windows
choco install argocd


4. 登录 Argo CD
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

> eBZhaS4E7goNFI7g


