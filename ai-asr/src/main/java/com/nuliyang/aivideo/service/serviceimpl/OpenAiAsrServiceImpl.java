package com.nuliyang.aivideo.service.serviceimpl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.nuliyang.aivideo.service.AsrService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@Slf4j
public class OpenAiAsrServiceImpl implements AsrService {


    private static final String API_URL = "https://api.openai.com/v1/audio/transcriptions";


    @Autowired
    private OSS ossClient;

    @Value("${openai.key}")
    private String API_KEY;



    @Override
    public void asr(String wavFileUrl) throws IOException {

        //这里代理clash，根据自己的代理配置修改
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress("127.0.0.1", 7890)
        );
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

        // 1. 解析 OSS 路径（假设格式为 "bucket:key"）
        int sep = wavFileUrl.indexOf(':');
        if (sep <= 0 || sep == wavFileUrl.length() - 1) {
            throw new IllegalArgumentException("wavFileUrl 格式应为 bucket:key");
        }
        String bucket = wavFileUrl.substring(0, sep);
        String key = wavFileUrl.substring(sep + 1);

        // 2. 下载到本地（复用 media/audio 目录，与 VideoToStringServiceImpl 一致）
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path audioDir = projectRoot.resolve("media/audio");
        Files.createDirectories(audioDir);

        Path localPath = audioDir.resolve("asr_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".wav");
        File wavFile = localPath.toFile();

        try (OSSObject object = ossClient.getObject(new GetObjectRequest(bucket, key));
             InputStream is = object.getObjectContent()) {

            Files.copy(is, localPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("音频已下载至: {}", localPath);

            // 3. 构建请求体
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", wavFile.getName(), RequestBody.create(wavFile, MediaType.parse("audio/wav")))
                    .addFormDataPart("model", "whisper-1")
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();

            log.info("ASR 请求开始，文件: {}", wavFile.getName());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("ASR 失败，状态码: {}", response.code());
                    throw new RuntimeException("ASR 请求失败: " + response);
                }

            }
        } finally {
            // 4. 清理：主动删除（非 deleteOnExit），仍保留你原逻辑风格
            if (wavFile.exists() && !wavFile.delete()) {
                log.warn("临时文件删除失败: {}", wavFile);
            }
        }
    }

    @Override
    public void task(String taskId) throws IOException {

    }


}
