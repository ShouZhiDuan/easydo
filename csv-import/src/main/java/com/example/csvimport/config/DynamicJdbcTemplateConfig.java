package com.example.csvimport.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 动态JdbcTemplate配置类
 * 根据MySQL连接信息动态创建JdbcTemplate bean
 */
@Slf4j
@Configuration
public class DynamicJdbcTemplateConfig {
    
    // 缓存多个数据源，支持多数据库连接
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();
    private final Map<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();
    
    /**
     * 创建动态JdbcTemplate工厂Bean
     * 这个Bean不会自动初始化，需要手动调用createJdbcTemplate方法
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public DynamicJdbcTemplateFactory dynamicJdbcTemplateFactory() {
        return new DynamicJdbcTemplateFactory();
    }
    
    /**
     * 动态JdbcTemplate工厂类
     */
    public class DynamicJdbcTemplateFactory {
        
        /**
         * 根据数据库配置创建JdbcTemplate
         * @param config 数据库配置
         * @return JdbcTemplate实例
         */
        public JdbcTemplate createJdbcTemplate(DatabaseConfig config) {
            String cacheKey = generateCacheKey(config);
            
            // 先从缓存中获取
            JdbcTemplate cachedTemplate = jdbcTemplateCache.get(cacheKey);
            if (cachedTemplate != null) {
                log.debug("从缓存中获取JdbcTemplate: {}", cacheKey);
                return cachedTemplate;
            }
            
            // 创建新的DataSource和JdbcTemplate
            DataSource dataSource = createDataSource(config);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 缓存起来
            dataSourceCache.put(cacheKey, dataSource);
            jdbcTemplateCache.put(cacheKey, jdbcTemplate);
            
            log.info("动态创建JdbcTemplate成功: {}", config.getUrl());
            return jdbcTemplate;
        }
        
        /**
         * 根据连接URL获取已创建的JdbcTemplate
         * @param url 数据库连接URL
         * @return JdbcTemplate实例，如果不存在则返回null
         */
        public JdbcTemplate getJdbcTemplate(String url) {
            return jdbcTemplateCache.values().stream()
                    .filter(template -> {
                        try {
                            String jdbcUrl = template.getDataSource().getConnection().getMetaData().getURL();
                            return url.equals(jdbcUrl);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);
        }
        
        /**
         * 关闭指定的数据源连接
         * @param config 数据库配置
         */
        public void closeDataSource(DatabaseConfig config) {
            String cacheKey = generateCacheKey(config);
            
            DataSource dataSource = dataSourceCache.remove(cacheKey);
            jdbcTemplateCache.remove(cacheKey);
            
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
                log.info("关闭数据源连接: {}", config.getUrl());
            }
        }
        
        /**
         * 关闭所有数据源连接
         */
        public void closeAllDataSources() {
            dataSourceCache.values().forEach(dataSource -> {
                if (dataSource instanceof HikariDataSource) {
                    ((HikariDataSource) dataSource).close();
                }
            });
            dataSourceCache.clear();
            jdbcTemplateCache.clear();
            log.info("关闭所有数据源连接");
        }
        
        /**
         * 创建HikariCP数据源
         */
        private DataSource createDataSource(DatabaseConfig config) {
            HikariConfig hikariConfig = new HikariConfig();
            
            // 基本连接配置
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClassName());
            
            // 连接池配置
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(config.getMinimumIdle());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());
            
            // MySQL性能优化配置
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
            
            // 连接池名称
            hikariConfig.setPoolName("DynamicHikariCP-" + System.currentTimeMillis());
            
            return new HikariDataSource(hikariConfig);
        }
        
        /**
         * 生成缓存键
         */
        private String generateCacheKey(DatabaseConfig config) {
            return String.format("%s_%s_%s", 
                    config.getUrl(), 
                    config.getUsername(), 
                    config.getDriverClassName());
        }
    }
}