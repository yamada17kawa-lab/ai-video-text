package com.nuliyang.aivideo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuliyang.aivideo.config.AliyunOssConfig;
import com.nuliyang.aivideo.feign.AiFeign;
import com.nuliyang.aivideo.tools.RedisUtil;
import com.nuliyang.common.dto.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessRedisTaskService {


    private final RedisUtil redisUtil;

    private final AliyunOssConfig ossProperties;

    private final ConcurrentHashMap<String, String> taskContextMap;

    private final AiFeign aiFeign;

    private final ObjectMapper objectMapper;


    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();

    @Async("asyncThreadBean")
    public void task(String taskId) throws IOException {
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
            log.info("任务结果: {}", result2);
            JsonNode root2 = objectMapper.readTree(result2);
            String taskStatus = root2
                    .path("output")
                    .path("task_status")
                    .asText();
            if (taskStatus.equals("PENDING")){
                log.info("任务未开始，请稍后查询");
            } else if (taskStatus.equals("RUNNING")) {
                log.info("任务正在运行，请稍后查询");

            } else if (taskStatus.equals("FAILED")) {
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



    /**
     * 每10秒执行一次，从Redis中获取一条数据并调用asr方法
     */
    @Scheduled(fixedRate = 10000)
    public void processRedisTask() {
        try {
            // 获取Redis中的第一条数据
            Map.Entry<String, Object> entry = redisUtil.getFirstEntry();

            if (entry != null) {
                String taskId = entry.getKey();
                String fileName = (String) entry.getValue();

                log.info("定时任务获取到任务: taskId={}, fileName={}", taskId, fileName);

                // 调用asr方法处理任务
                task(taskId);

                log.info("定时任务处理完成: taskId={}", taskId);
            } else {
                log.debug("Redis中暂无待处理任务");
            }
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }
}
