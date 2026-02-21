package com.nuliyang.aivideo.controller;


import com.nuliyang.aivideo.service.AsrService;
import com.nuliyang.aivideo.service.VideoToStringService;
import com.nuliyang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
public class ToAudioController {


    @Autowired
    @Qualifier("videoToStringServiceImpl")
    VideoToStringService videoToStringService;

    @Autowired
    @Qualifier("aliyunPendingServiceImpl")
    AsrService aliyunPendingService;


    @PostMapping("/toAudio")
    public Result<String> toAudio(@RequestParam MultipartFile video,
                                  @RequestParam String name) {
        try {
            String result = videoToStringService.videoToString(video, name);
            log.info("视频asr转换成功: {}",  result);
            return Result.success("视频asr转换成功",  result);
        } catch (IOException | InterruptedException e) {
            log.error("视频转音频过程中发生未知异常");
            return Result.fail("处理失败，请稍后重试");
        }
    }



    @GetMapping("/getPendingAndRunning")
    public Result<Map<String, Object>> getPendingAndRunning() {
        Map<String, Object> result = videoToStringService.getPendingAndRunning();
        return Result.success("查询成功", result);
    }

    @GetMapping("/getTask/{taskId}")
    public Result<String> getTask(@PathVariable String taskId) throws IOException {
        String result = aliyunPendingService.asr(taskId);
        return Result.success("查询成功", result);
    }


}
