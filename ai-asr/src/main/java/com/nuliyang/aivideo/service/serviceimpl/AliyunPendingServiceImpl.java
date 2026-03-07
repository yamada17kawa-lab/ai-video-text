package com.nuliyang.aivideo.service.serviceimpl;



import com.nuliyang.aivideo.config.AliyunOssConfig;
import com.nuliyang.aivideo.feign.AiFeign;
import com.nuliyang.aivideo.service.AsrService;
import com.nuliyang.aivideo.tools.RedisUtil;
import com.nuliyang.common.dto.FileDto;
import com.nuliyang.common.entity.WeiYangAiTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service("aliyunPendingServiceImpl")
@RequiredArgsConstructor
public class AliyunPendingServiceImpl implements AsrService {


    private final RedisUtil redisUtil;

    private final RabbitTemplate rabbitTemplate;

    private final AliyunOssConfig ossProperties;

    private final ConcurrentHashMap<String, String> taskContextMap;

    private final AiFeign aiFeign;

    private final ObjectMapper objectMapper;

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();


    @Override
    public CompletableFuture<Void> asr(String wavFileUrl) throws IOException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        return future;
    }



    @Async("asyncThreadBean")
    @Override
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
                    //发到队列里面
                    log.info("发送喂养ai任务到队列");
                    WeiYangAiTask weiYangAiTask = new WeiYangAiTask(fileDto, resourceId);
                    rabbitTemplate.convertAndSend("weiYangAiEx", "weiYangAiRoutingKey", weiYangAiTask);

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




}
