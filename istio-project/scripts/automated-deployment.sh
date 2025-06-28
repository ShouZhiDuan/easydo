#!/bin/bash

# 自动化部署脚本 - GitOps + Argo Rollouts
# 解决手动维护策略文件的问题

set -e

# 配置参数
SERVICE_NAME=${1:-"user-service"}
NEW_IMAGE=${2}
DEPLOYMENT_TYPE=${3:-"progressive"}  # progressive, emergency, rollback

# 检查必需参数
if [ -z "$NEW_IMAGE" ]; then
    echo "❌ 错误: 必须提供新镜像版本"
    echo "用法: $0 <service-name> <new-image> [deployment-type]"
    echo "示例: $0 user-service user-service:v1.2.3 progressive"
    exit 1
fi

NAMESPACE="istio-demo"
ROLLOUT_NAME="${SERVICE_NAME}-rollout"

echo "🚀 开始自动化部署流程"
echo "服务: ${SERVICE_NAME}"
echo "新镜像: ${NEW_IMAGE}"
echo "部署类型: ${DEPLOYMENT_TYPE}"

# 检查Argo Rollouts状态
check_rollout_status() {
    echo "📊 检查当前Rollout状态..."
    if kubectl get rollout ${ROLLOUT_NAME} -n ${NAMESPACE} >/dev/null 2>&1; then
        local status=$(kubectl get rollout ${ROLLOUT_NAME} -n ${NAMESPACE} -o jsonpath='{.status.phase}')
        echo "当前状态: ${status}"
        
        if [ "$status" = "Progressing" ]; then
            echo "⚠️  检测到正在进行的部署，请先完成或中止当前部署"
            read -p "是否中止当前部署并继续? (y/N): " confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                kubectl argo rollouts abort ${ROLLOUT_NAME} -n ${NAMESPACE}
                sleep 5
            else
                exit 1
            fi
        fi
    else
        echo "📝 Rollout不存在，将创建新的Rollout"
    fi
}

# 渐进式部署
progressive_deployment() {
    echo "🔄 开始渐进式部署..."
    
    # 更新Rollout镜像
    kubectl argo rollouts set image ${ROLLOUT_NAME} \
        ${SERVICE_NAME}=${NEW_IMAGE} \
        -n ${NAMESPACE}
    
    echo "✅ 镜像已更新，Argo Rollouts将自动执行以下步骤:"
    echo "  1. 部署5%流量到新版本"
    echo "  2. 监控成功率和延迟指标"
    echo "  3. 根据分析结果自动推进或回滚"
    echo "  4. 最终完成100%流量切换"
    
    # 实时监控部署进度
    echo "📈 监控部署进度 (Ctrl+C退出监控)..."
    kubectl argo rollouts get rollout ${ROLLOUT_NAME} -n ${NAMESPACE} --watch
}

# 紧急部署（跳过金丝雀阶段）
emergency_deployment() {
    echo "🚨 紧急部署模式..."
    
    # 更新镜像并跳过分析
    kubectl argo rollouts set image ${ROLLOUT_NAME} \
        ${SERVICE_NAME}=${NEW_IMAGE} \
        -n ${NAMESPACE}
    
    # 跳过所有暂停步骤
    echo "⏭️  跳过所有暂停步骤..."
    kubectl argo rollouts promote ${ROLLOUT_NAME} -n ${NAMESPACE} --skip-all-steps
    
    echo "✅ 紧急部署完成，请密切监控系统状态"
}

# 回滚操作
rollback_deployment() {
    echo "⏪ 执行回滚操作..."
    
    local previous_revision=$(kubectl argo rollouts history rollout ${ROLLOUT_NAME} -n ${NAMESPACE} --revision=0 | tail -2 | head -1 | awk '{print $1}')
    
    if [ -z "$previous_revision" ]; then
        echo "❌ 找不到可回滚的版本"
        exit 1
    fi
    
    echo "回滚到版本: ${previous_revision}"
    kubectl argo rollouts undo ${ROLLOUT_NAME} -n ${NAMESPACE} --to-revision=${previous_revision}
    
    echo "✅ 回滚完成"
}

# 部署后验证
post_deployment_verification() {
    echo "🔍 执行部署后验证..."
    
    # 等待Rollout完成
    kubectl argo rollouts wait ${ROLLOUT_NAME} -n ${NAMESPACE} --timeout=600s
    
    # 检查Pod状态
    echo "📋 检查Pod状态..."
    kubectl get pods -n ${NAMESPACE} -l app=${SERVICE_NAME}
    
    # 检查服务健康状态
    echo "🏥 检查服务健康状态..."
    local service_ip=$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.spec.clusterIP}')
    if kubectl run test-pod --rm -i --restart=Never --image=curlimages/curl -- \
        curl -f http://${service_ip}:8080/actuator/health; then
        echo "✅ 服务健康检查通过"
    else
        echo "❌ 服务健康检查失败"
        return 1
    fi
    
    # 检查Istio配置
    echo "🌐 验证Istio配置..."
    kubectl get virtualservice,destinationrule -n ${NAMESPACE} -l app=${SERVICE_NAME}
}

# 发送通知
send_notification() {
    local status=$1
    local message=$2
    
    echo "📢 发送部署通知..."
    
    # 这里可以集成Slack、企业微信、邮件等通知方式
    # 示例：发送到Slack
    if [ ! -z "$SLACK_WEBHOOK_URL" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"🚀 部署通知\\n服务: ${SERVICE_NAME}\\n镜像: ${NEW_IMAGE}\\n状态: ${status}\\n详情: ${message}\"}" \
            $SLACK_WEBHOOK_URL
    fi
    
    # 示例：发送到企业微信
    if [ ! -z "$WECHAT_WEBHOOK_URL" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"msgtype\":\"text\",\"text\":{\"content\":\"部署通知\\n服务: ${SERVICE_NAME}\\n镜像: ${NEW_IMAGE}\\n状态: ${status}\\n详情: ${message}\"}}" \
            $WECHAT_WEBHOOK_URL
    fi
}

# 主执行流程
main() {
    trap 'send_notification "FAILED" "部署过程中发生错误"' ERR
    
    check_rollout_status
    
    case ${DEPLOYMENT_TYPE} in
        "progressive")
            progressive_deployment
            ;;
        "emergency")
            emergency_deployment
            ;;
        "rollback")
            rollback_deployment
            ;;
        *)
            echo "❌ 不支持的部署类型: ${DEPLOYMENT_TYPE}"
            echo "支持的类型: progressive, emergency, rollback"
            exit 1
            ;;
    esac
    
    if [ "${DEPLOYMENT_TYPE}" != "rollback" ]; then
        if post_deployment_verification; then
            send_notification "SUCCESS" "部署成功完成"
            echo "🎉 部署成功完成！"
        else
            send_notification "FAILED" "部署后验证失败，考虑回滚"
            echo "❌ 部署后验证失败，请检查系统状态"
            exit 1
        fi
    else
        send_notification "SUCCESS" "回滚操作完成"
        echo "🎉 回滚操作完成！"
    fi
}

# 显示帮助信息
show_help() {
    cat << EOF
自动化部署脚本 - 基于Argo Rollouts和Istio

用法:
  $0 <service-name> <new-image> [deployment-type]

参数:
  service-name     服务名称 (如: user-service)
  new-image        新镜像版本 (如: user-service:v1.2.3)
  deployment-type  部署类型 (progressive|emergency|rollback，默认: progressive)

部署类型说明:
  progressive - 渐进式部署，自动金丝雀发布，基于指标分析
  emergency   - 紧急部署，跳过分析阶段，直接发布
  rollback    - 回滚到上一个稳定版本

示例:
  $0 user-service user-service:v1.2.3 progressive
  $0 order-service order-service:v2.1.0 emergency
  $0 user-service "" rollback

环境变量:
  SLACK_WEBHOOK_URL   - Slack通知webhook地址
  WECHAT_WEBHOOK_URL  - 企业微信通知webhook地址

注意事项:
  - 确保已安装kubectl和argocd CLI
  - 确保已配置正确的kubeconfig
  - 确保目标namespace存在Argo Rollouts资源
EOF
}

# 检查命令行参数
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    show_help
    exit 0
fi

# 执行主流程
main 