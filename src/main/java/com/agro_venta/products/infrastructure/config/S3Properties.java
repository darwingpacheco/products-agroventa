package com.agro_venta.products.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.aws.s3")
public class S3Properties {

    private String bucketName;
    private String region;
    private String publicBaseUrl;
    private String prefix;
    private long maxFileSize;
    private List<String> allowedContentTypes;
    private String accessKey;
    private String secretKey;
}
