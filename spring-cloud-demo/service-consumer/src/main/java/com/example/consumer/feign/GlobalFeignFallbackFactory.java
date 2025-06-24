package com.example.consumer.feign;

import com.example.consumer.config.GlobalFeignConfiguration;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 全局Feign降级处理工厂
 * 使用动态代理统一处理所有FeignClient的降级逻辑
 */
@Component
public class GlobalFeignFallbackFactory<T> implements FallbackFactory<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalFeignFallbackFactory.class);
    
    @Override
    @SuppressWarnings("unchecked")
    public T create(Throwable cause) {
        logger.error("Feign调用失败，异常信息：{}", cause.getMessage(), cause);
        
        return (T) Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[]{getTargetClass()},
            new FallbackInvocationHandler(cause)
        );
    }
    
    /**
     * 获取目标接口类型，子类可以重写此方法指定具体的接口类型
     * 默认返回 Object.class，实际使用时应该通过泛型或其他方式确定
     */
    protected Class<?> getTargetClass() {
        // 这里可以通过反射获取泛型类型，或者由具体实现类重写
        return ProviderFeignClient.class;
    }
    
    /**
     * 动态代理处理器
     */
    private class FallbackInvocationHandler implements InvocationHandler {
        private final Throwable cause;
        
        public FallbackInvocationHandler(Throwable cause) {
            this.cause = cause;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return handleFallback(method.getName(), args, cause);
        }
    }
    
    /**
     * 处理降级逻辑
     * 根据异常类型返回不同的降级响应
     */
    private String handleFallback(String methodName, Object[] args, Throwable cause) {
        // 记录详细的降级信息
        logger.warn("方法 {} 触发降级，参数：{}, 异常类型：{}", methodName, args, cause.getClass().getSimpleName());
        
        // 根据异常类型提供不同的降级策略
        if (cause instanceof GlobalFeignConfiguration.ServiceUnavailableException ||
            cause instanceof FeignException.ServiceUnavailable) {
            return getServiceUnavailableResponse(methodName, args);
        } else if (cause instanceof GlobalFeignConfiguration.NotFoundException ||
                   cause instanceof FeignException.NotFound) {
            return getNotFoundResponse(methodName, args);
        } else if (cause instanceof GlobalFeignConfiguration.InternalServerErrorException ||
                   cause instanceof FeignException.InternalServerError) {
            return getInternalServerErrorResponse(methodName, args);
        } else {
            return getDefaultResponse(methodName, args);
        }
    }
    
    /**
     * 服务不可用时的降级响应
     */
    private String getServiceUnavailableResponse(String methodName, Object[] args) {
        if (methodName.contains("health") || methodName.contains("Health")) {
            return "服务健康检查失败，服务暂时不可用";
        } else if (methodName.contains("user") || methodName.contains("User")) {
            String userId = args != null && args.length > 0 ? String.valueOf(args[0]) : "unknown";
            return String.format("用户服务暂时不可用，用户ID: %s", userId);
        } else {
            return "服务暂时不可用，请稍后重试";
        }
    }
    
    /**
     * 资源未找到时的降级响应
     */
    private String getNotFoundResponse(String methodName, Object[] args) {
        if (methodName.contains("user") || methodName.contains("User")) {
            String userId = args != null && args.length > 0 ? String.valueOf(args[0]) : "unknown";
            return String.format("用户不存在，用户ID: %s", userId);
        } else {
            return "请求的资源不存在";
        }
    }
    
    /**
     * 服务内部错误时的降级响应
     */
    private String getInternalServerErrorResponse(String methodName, Object[] args) {
        return "服务内部错误，请联系管理员";
    }
    
    /**
     * 默认降级响应
     */
    private String getDefaultResponse(String methodName, Object[] args) {
        return "服务调用失败，请稍后重试";
    }
    

}