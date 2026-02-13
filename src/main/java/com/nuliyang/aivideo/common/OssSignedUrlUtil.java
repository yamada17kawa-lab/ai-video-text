package com.nuliyang.aivideo.common;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.nuliyang.aivideo.config.AliyunOssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;

@Component
public class OssSignedUrlUtil {


    @Autowired
    private AliyunOssConfig ossProperties;

    /**
     * 生成带签名的 URL
     *
     * @param objectKey
     * @return
     */
    public String generateSignedUrl(String objectKey) {

        String endpoint = ossProperties.getEndpoint();
        String bucketName = ossProperties.getBucketName();
        String accessKeyId = ossProperties.getAccessKeyId();
        String accessKeySecret = ossProperties.getAccessKeySecret();

        OSS ossClient = new OSSClientBuilder().build(
                endpoint,
                accessKeyId,
                accessKeySecret
        );

        // 过期时间：10 分钟
        Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000);

        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucketName, objectKey);

        request.setExpiration(expiration);

        URL signedUrl = ossClient.generatePresignedUrl(request);

        ossClient.shutdown();

        return signedUrl.toString();
    }
}
