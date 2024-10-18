package com.atguigu.lease.common.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")//自动映射application.yml中的前缀为minio中的值
@Data
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;
    
    private String bucketName;
}