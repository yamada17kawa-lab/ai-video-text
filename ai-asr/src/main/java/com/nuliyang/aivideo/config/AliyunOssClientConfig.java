package com.nuliyang.aivideo.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AliyunOssClientConfig {


    private final AliyunOssConfig ossProperties;


    /**
     * 初始化OSS客户端
     */
    @Bean
    public OSS init() {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(),
                        ossProperties.getAccessKeyId(),
                        ossProperties.getAccessKeySecret());
    }
}
