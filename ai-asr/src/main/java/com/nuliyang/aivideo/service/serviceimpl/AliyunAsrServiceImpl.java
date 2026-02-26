package com.nuliyang.aivideo.service.serviceimpl;


import com.nuliyang.aivideo.config.AliyunOssConfig;
import com.nuliyang.aivideo.feign.AiFeign;
import com.nuliyang.aivideo.mapper.ResourceMapper;
import com.nuliyang.aivideo.service.AsrService;
import com.nuliyang.aivideo.tools.RedisUtil;
import com.nuliyang.common.dto.FileDto;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class AliyunAsrServiceImpl implements AsrService {





    @Autowired
    AliyunOssConfig ossProperties;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ConcurrentHashMap<String, String> taskContextMap;


    @Autowired
    private AiFeign aiFeign;




    private static final String SUBMIT_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/audio/asr/transcription";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();






    @Async("asyncThreadBean")
    @Override
    public void asr(String wavFileUrl) throws IOException {

        ///////////将音频扔给ai进行asr处理////////////////
        String jsonBody = """
        {
          "model": "paraformer-v2",
          "input": {
            "file_urls": ["%s"]
          },
          "parameters": {
            "language_hints": ["zh", "en"],
            "timestamp_alignment_enabled": true
          }
        }
        """.formatted(wavFileUrl);


        Request request = new Request.Builder()
                .url(SUBMIT_URL)
                .post(RequestBody.create(
                        jsonBody,
                        MediaType.parse("application/json")
                ))
                .addHeader("Authorization", "Bearer " + ossProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-Async", "enable")
                .build();

        String result = "";
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response);
            }

            assert response.body() != null;
            result = response.body().string();
            log.info("提交任务成功，返回结果: {}", result);


        }



        //////////////////获取结果///////////////
        JsonNode root = objectMapper.readTree(result);
        String taskId = root
                .path("output")
                .path("task_id")
                .asText();
        log.info("任务ID: {}", taskId);

        String url = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;

        log.info("查询任务结果 URL: {}", url);
        Request request2 = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0])) // POST + 空 body
                .addHeader("Authorization", "Bearer " + ossProperties.getApiKey())
                .build();

        try (Response response = CLIENT.newCall(request2).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("查询任务失败: " + response);
            }
            assert response.body() != null;
            //判断该任务的状态
            String result2 = response.body().string();
            log.info("查询任务结果: {}", result2);
            JsonNode root2 = objectMapper.readTree(result2);
            String taskStatus = root2
                    .path("output")
                    .path("task_status")
                    .asText();
            if (taskStatus.equals("PENDING")){
                log.info("任务未开始，请稍后查询");
                ////不存在则存入redis
                if (!redisUtil.hasKey(taskId)){
                    //从线程中取出文件的自定义名字
                    String fileName = taskContextMap.get("fileName");
                    redisUtil.add(taskId, fileName);
                }

            } else if (taskStatus.equals("RUNNING")) {
                log.info("任务正在运行，请稍后查询");
                ////不存在则存入redis
                if (!redisUtil.hasKey(taskId)){
                    //从线程中取出文件的原本名字
                    String fileName = taskContextMap.get("fileName");
                    redisUtil.add(taskId, fileName);
                }

            }else if (taskStatus.equals("FAILED")) {
                log.info("任务失败");
            }else if (taskStatus.equals("SUCCEEDED")) {
                log.info("任务成功");
                //解析返回数据，取出结果url
                String transcriptionUrl = root2
                        .path("output")
                        .path("results")
                        .get(0)
                        .path("transcription_url")
                        .asText();


                log.info("结果url: {}", transcriptionUrl);
                Request request0 = new Request.Builder()
                        .url(transcriptionUrl)
                        .get()
                        .build();
                try (Response response0 = CLIENT.newCall(request0).execute()) {
                    assert response0.body() != null;
                    String resultText = response0.body().string();

//                    // ✅ 保存到 media/document/ 目录
//                    Path projectRoot = Paths.get(System.getProperty("user.dir"));
//                    Path docDir = projectRoot.resolve("media").resolve("document");
//                    Files.createDirectories(docDir); // 自动创建目录

//                    String fileName = "transcript_" + taskId + ".txt";
//                    Path filePath = docDir.resolve(fileName);
//                    Files.write(filePath, resultText.getBytes(java.nio.charset.StandardCharsets.UTF_8));


//                    log.info("识别结果已保存至: {}", filePath.toAbsolutePath());



                    //清除该任务
                    redisUtil.delete(taskId);

                    //调用langchain4j服务将识别结果喂给ai
                    String resourceId = taskContextMap.get("resourceId");
                    log.info("取得原始数据id: {}", resourceId);
                    FileDto fileDto =new FileDto();
                    fileDto.setResultText(resultText);
                    fileDto.setTaskId(taskId);
                    aiFeign.weiYangAi(fileDto, resourceId);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    taskContextMap.clear();
                }
            }else {
                log.info("任务状态未知");
            }
        }
    }

    @Override
    public void task(String taskId) throws IOException {

    }


}



