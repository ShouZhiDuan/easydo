.PHONY: build deploy test clean help

# 默认目标
help:
	@echo "Istio Demo 多模块项目管理"
	@echo "可用命令:"
	@echo "  build        - 构建Maven项目和Docker镜像"
	@echo "  maven-build  - 仅构建Maven多模块项目"
	@echo "  docker-build - 仅构建Docker镜像"
	@echo "  deploy       - 部署服务到Kubernetes"
	@echo "  test         - 运行功能测试"
	@echo "  canary       - 部署金丝雀版本"
	@echo "  bluegreen    - 部署蓝绿版本"
	@echo "  clean        - 清理部署环境"
	@echo "  status       - 查看部署状态"
	@echo "  logs         - 查看服务日志"
	@echo "  redeploy     - 快速重部署"
	@echo "  loadtest     - 生成负载测试"
	@echo "  metrics      - 获取监控指标"

# 构建镜像
build:
	@echo "构建Maven多模块项目和Docker镜像..."
	./scripts/build.sh

# 仅构建Maven项目
maven-build:
	@echo "构建Maven多模块项目..."
	mvn clean package -DskipTests

# 仅构建Docker镜像 (需要先执行maven-build)
docker-build:
	@echo "构建Docker镜像..."
	cd user-service && docker build -t user-service:latest .
	cd order-service && docker build -t order-service:latest .

# 部署到Kubernetes
deploy:
	@echo "部署到Kubernetes..."
	./scripts/deploy.sh

# 运行测试
test:
	@echo "运行功能测试..."
	./scripts/test.sh

# 部署金丝雀版本
canary:
	@echo "部署金丝雀版本..."
	kubectl apply -f istio/canary-deployment.yaml

# 部署蓝绿版本
bluegreen:
	@echo "部署蓝绿版本..."
	kubectl apply -f istio/blue-green-deployment.yaml

# 查看部署状态
status:
	@echo "=== Pods ==="
	kubectl get pods -n istio-demo
	@echo ""
	@echo "=== Services ==="
	kubectl get services -n istio-demo
	@echo ""
	@echo "=== VirtualServices ==="
	kubectl get virtualservices -n istio-demo
	@echo ""
	@echo "=== DestinationRules ==="
	kubectl get destinationrules -n istio-demo

# 查看服务日志
logs:
	@echo "用户服务日志:"
	kubectl logs -l app=user-service -n istio-demo --tail=50
	@echo ""
	@echo "订单服务日志:"
	kubectl logs -l app=order-service -n istio-demo --tail=50

# 清理环境
clean:
	@echo "清理部署环境..."
	kubectl delete namespace istio-demo || true
	docker rmi user-service:latest user-service:v1 order-service:latest order-service:v1 || true

# 快速重部署
redeploy: clean build deploy

# 生成负载测试
loadtest:
	@echo "生成负载测试..."
	for i in $$(seq 1 100); do \
		curl -s http://$$(kubectl get po -l istio=ingressgateway -n istio-system -o jsonpath='{.items[0].status.hostIP}'):$$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')/api/users/health > /dev/null & \
	done; \
	wait

# 监控指标
metrics:
	@echo "获取Prometheus指标..."
	@INGRESS_HOST=$$(kubectl get po -l istio=ingressgateway -n istio-system -o jsonpath='{.items[0].status.hostIP}'); \
	INGRESS_PORT=$$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}'); \
	curl -s http://$$INGRESS_HOST:$$INGRESS_PORT/api/users/actuator/prometheus | grep -E "(http_requests_total|jvm_memory_used_bytes)" | head -10 