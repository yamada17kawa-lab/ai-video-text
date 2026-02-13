package com.nuliyang.aivideo.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssConfig {


    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String apiKey;

}