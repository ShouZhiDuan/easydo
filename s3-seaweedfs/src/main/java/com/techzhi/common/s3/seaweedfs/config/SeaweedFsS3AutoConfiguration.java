package com.techzhi.common.s3.seaweedfs.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.techzhi.common.s3.seaweedfs.service.SeaweedFsS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SeaweedFS S3 自动配置类
 * 
 * @author TechZhi
 * @version 1.0.0
 */
@Configuration
@ConditionalOnClass(AmazonS3.class)
@EnableConfigurationProperties(SeaweedFsS3Properties.class)
@ConditionalOnProperty(prefix = "seaweedfs.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SeaweedFsS3AutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SeaweedFsS3AutoConfiguration.class);

    /**
     * 配置AmazonS3客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public AmazonS3 amazonS3Client(SeaweedFsS3Properties properties) {
        logger.info("Initializing SeaweedFS S3 client with endpoint: {}", properties.getEndpoint());
        
        // 创建AWS凭证
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                properties.getAccessKey(), 
                properties.getSecretKey()
        );

        // 配置客户端
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(properties.getConnectionTimeout());
        clientConfiguration.setRequestTimeout(properties.getRequestTimeout());
        clientConfiguration.setMaxConnections(properties.getMaxConnections());
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        // 创建端点配置
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = 
                new AwsClientBuilder.EndpointConfiguration(
                        properties.getEndpoint(), 
                        properties.getRegion()
                );

        // 构建S3客户端
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withClientConfiguration(clientConfiguration)
                .withPathStyleAccessEnabled(properties.isPathStyleAccessEnabled())
                .build();

        logger.info("SeaweedFS S3 client initialized successfully");
        return amazonS3;
    }

    /**
     * 配置SeaweedFS S3服务
     */
    @Bean
    @ConditionalOnMissingBean
    public SeaweedFsS3Service seaweedFsS3Service(AmazonS3 amazonS3Client, SeaweedFsS3Properties properties) {
        logger.info("Initializing SeaweedFS S3 service with bucket: {}", properties.getBucketName());
        return new SeaweedFsS3Service(amazonS3Client, properties);
    }
}