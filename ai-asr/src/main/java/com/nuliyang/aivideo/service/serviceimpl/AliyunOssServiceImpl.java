package com.nuliyang.aivideo.service.serviceimpl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.nuliyang.aivideo.common.OssFileInfo;
import com.nuliyang.aivideo.config.AliyunOssConfig;
import com.nuliyang.aivideo.service.AliyunOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class AliyunOssServiceImpl implements AliyunOssService {




    private final AliyunOssConfig ossProperties;

    private final OSS ossClient;


    @Override
    public String uploadFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("传入的File对象无效或不存在");
        }

        String originalFilename = file.getName();
        if (originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // --- 生成唯一文件名 (可选) ---
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }
        String fileName = "audio_" + UUID.randomUUID().toString().replace("-", "") + extension;
        // --- 生成唯一文件名 END ---

        try {
            // 核心：直接将 File 对象传递给 putObject
            ossClient.putObject(ossProperties.getBucketName(), fileName, file);

            String url = "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint().substring(8) + "/" + fileName;
            log.info("本地File上传成功，原始文件名: {}, OSS文件名: {}, URL: {}", originalFilename, fileName, url);
            return url;

        } catch (Exception e) {
            log.error("上传本地File到OSS失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }



    /**
     * 获取指定前缀下的文件列表
     * @param prefix 文件前缀，用于模拟目录，例如 "images/", "audios/2026/02/"
     * @param maxKeys 本次查询返回的最大文件数量，默认值为100，最大值为1000
     * @return 包含文件信息的列表
     */
    @Override
    public List<OssFileInfo> listFiles(String prefix, Integer maxKeys) {
        // 设置默认值
        if (maxKeys == null || maxKeys <= 0) {
            maxKeys = 100; // 默认获取100个
        }
        if (maxKeys > 1000) {
            maxKeys = 1000; // 最大值不能超过1000
        }

        List<OssFileInfo> fileInfoList = new ArrayList<>();
        String delimiter = ""; // 设置为空字符串，表示列出所有匹配前缀的对象，不分隔层级

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(ossProperties.getBucketName())
                .withPrefix(prefix) // 指定前缀
                .withMaxKeys(maxKeys) // 指定每次最多返回多少个
                .withDelimiter(delimiter); // 不分隔

        ObjectListing objectListing;
        do {
            objectListing = ossClient.listObjects(listObjectsRequest);

            // 遍历返回的对象摘要，构建我们需要的文件信息
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                OssFileInfo info = new OssFileInfo();
                info.setObjectName(objectSummary.getKey()); // 文件名 (完整路径)
                info.setSize(objectSummary.getSize());       // 文件大小 (字节)
                info.setLastModified(objectSummary.getLastModified()); // 最后修改时间
                // info.setUrl("https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint().substring(8) + "/" + objectSummary.getKey()); // 可选：生成访问URL
                fileInfoList.add(info);
            }

            // 如果列表被截断，更新 marker 以获取下一页
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        log.info("获取文件列表完成，前缀: {}, 数量: {}", prefix, fileInfoList.size());
        return fileInfoList;
    }



    @Override
    public void clearDirectoryByPrefix(String prefix) {
        try {
            List<String> objectsToDelete = new ArrayList<>();
            String delimiter = "";

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(ossProperties.getBucketName())
                    .withPrefix(prefix)
                    .withDelimiter(delimiter);

            ObjectListing objectListing;
            do {
                objectListing = ossClient.listObjects(listObjectsRequest);

                for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    objectsToDelete.add(objectSummary.getKey());
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

            if (objectsToDelete.isEmpty()) {
                log.info("指定前缀下没有找到文件: {}", prefix);
                return;
            }

            log.info("开始批量删除 {} 个文件，前缀: {}", objectsToDelete.size(), prefix);
            // 创建 DeleteObjectsRequest 对象
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(ossProperties.getBucketName());
            deleteObjectsRequest.setKeys(objectsToDelete);
            ossClient.deleteObjects(deleteObjectsRequest);
            log.info("批量删除完成，前缀: {}", prefix);

        } catch (Exception e) {
            log.error("清空OSS目录失败，前缀: {}", prefix, e);
            throw new RuntimeException("清空目录失败: " + e.getMessage(), e);
        }
    }


}
