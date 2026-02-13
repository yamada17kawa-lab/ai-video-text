package com.nuliyang.aivideo.service.serviceimpl;

import com.nuliyang.aivideo.common.TaskIdStore;
import com.nuliyang.aivideo.common.ThreadContext;
import com.nuliyang.aivideo.config.AliyunOssConfig;
import com.nuliyang.aivideo.service.AsrService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AliyunAsrServiceImpl implements AsrService {





    @Autowired
    AliyunOssConfig ossProperties;

    @Autowired
    ObjectMapper objectMapper;




    private static final String SUBMIT_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/audio/asr/transcription";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();






    @Override
    public String asr(String wavFileUrl) throws IOException {

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
                .asString();
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
                    .asString();
            if (taskStatus.equals("PENDING")){
                log.info("任务未开始，请稍后查询");
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("status", "PENDING");
                resultMap.put("message", "任务未开始，请稍后查询");
                ////不存在则存入内存
                if (!TaskIdStore.containsKey(taskId)) {
                    //从线程中取出文件的原本名字
                    String fileName = ThreadContext.getData();
                    TaskIdStore.put(taskId, fileName);
                }
                return objectMapper.writeValueAsString(resultMap);

            } else if (taskStatus.equals("RUNNING")) {
                log.info("任务正在运行，请稍后查询");
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("status", "RUNNING");
                resultMap.put("message", "任务正在运行，请稍后查询");
                ////不存在则存入内存
                if (!TaskIdStore.containsKey(taskId)) {
                    //从线程中取出文件的原本名字
                    String fileName = ThreadContext.getData();
                    TaskIdStore.put(taskId, fileName);
                }
                return objectMapper.writeValueAsString(resultMap);

            }else if (taskStatus.equals("FAILED")) {
                log.info("任务失败");
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("status", "FAILED");
                resultMap.put("message", "任务失败");
                //清除该任务
                TaskIdStore.remove(taskId);
                return objectMapper.writeValueAsString(resultMap);
            }else if (taskStatus.equals("SUCCEEDED")) {
                log.info("任务成功");
                //解析返回数据，取出结果url
                String transcriptionUrl = root2
                        .path("output")
                        .path("results")
                        .get(0)
                        .path("transcription_url")
                        .asString();


                log.info("结果url: {}", transcriptionUrl);
                Request request0 = new Request.Builder()
                        .url(transcriptionUrl)
                        .get()
                        .build();
                try (Response response0 = CLIENT.newCall(request0).execute()) {
                    assert response0.body() != null;
                    String resultText = response0.body().string();

                    // ✅ 保存到 media/document/ 目录
                    Path projectRoot = Paths.get(System.getProperty("user.dir"));
                    Path docDir = projectRoot.resolve("media").resolve("document");
                    Files.createDirectories(docDir); // 自动创建目录

                    String fileName = "transcript_" + taskId + ".txt";
                    Path filePath = docDir.resolve(fileName);
                    Files.write(filePath, resultText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

                    log.info("识别结果已保存至: {}", filePath.toAbsolutePath());



                    Map<String, String> resultMap00 = new HashMap<>();
                    resultMap00.put("status", "SUCCEEDED");
                    resultMap00.put("message", "任务成功，正在将文件喂给ai");
                    //清除该任务
                    TaskIdStore.remove(taskId);
                    return objectMapper.writeValueAsString(resultMap00);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("未知任务状态");
            Map<String, String> resultMap0 = new HashMap<>();
            resultMap0.put("status", "UNKNOW");
            resultMap0.put("message", "未知任务状态");
            resultMap0.put("data", result2);
            return objectMapper.writeValueAsString(resultMap0);
        }
    }







}



