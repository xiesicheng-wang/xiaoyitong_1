package com.youlinbang.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置：启动时确保 bucket 存在并开放公共读（图片 URL 可直接访问）
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "youlinbang.minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /** 应用启动后检查/创建 bucket，并设置公共读策略；MinIO 未启动时只告警不阻断应用 */
    @Bean
    public CommandLineRunner ensureBucket(MinioClient client) {
        return args -> {
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("MinIO bucket 已创建: {}", bucket);
                }
                String policy = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [{
                            "Effect": "Allow",
                            "Principal": {"AWS": ["*"]},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                          }]
                        }""".formatted(bucket);
                client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
                log.info("MinIO 就绪: endpoint={}, bucket={} (公共读)", endpoint, bucket);
            } catch (Exception e) {
                log.warn("MinIO 暂不可用（文件上传将失败，其余功能不受影响）: {}", e.getMessage());
            }
        };
    }
}
