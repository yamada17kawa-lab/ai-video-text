package com.nuliyang.aivideo.service.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nuliyang.aivideo.common.OssSignedUrlUtil;
import com.nuliyang.aivideo.mapper.ResourceMapper;
import com.nuliyang.aivideo.service.AliyunOssService;
import com.nuliyang.aivideo.service.AsrService;
import com.nuliyang.aivideo.service.VideoToStringService;
import com.nuliyang.aivideo.tools.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class VideoToStringServiceImpl implements VideoToStringService {


    @Autowired
    @Qualifier("aliyunAsrServiceImpl")
    private AsrService asrService;


    @Autowired
    private ConcurrentHashMap<String, String> taskContextMap;

    @Autowired
    private AliyunOssService aliyunOssService;

    @Autowired
    private OssSignedUrlUtil ossSignedUrlUtil;


    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private RedisUtil redisUtil;



    @Override
    public String videoToString(MultipartFile video, String name)
            throws IOException {

        log.info("开始处理视频文件: {}", name);

        //将名字放到线程中
        taskContextMap.put("fileName", name);

        /////////////////////视频转音频////////////////////////////////
        // 项目根目录
        Path projectRoot = Paths.get(System.getProperty("user.dir"));

        Path videoDir = projectRoot.resolve("media/video");
        Path audioDir = projectRoot.resolve("media/audio");

        Files.createDirectories(videoDir);
        Files.createDirectories(audioDir);

        Path inputVideo = videoDir.resolve(
                "video-" + System.currentTimeMillis() + ".mp4"
        );
        video.transferTo(inputVideo.toFile());

        log.info("视频文件已创建: {}", inputVideo.toAbsolutePath());
        log.info("视频文件大小: {} bytes", Files.size(inputVideo));

        Path outputWav = audioDir.resolve(
                "audio-" + System.currentTimeMillis() + ".wav"
        );
        log.info("音频文件将输出到: {}", outputWav.toAbsolutePath());


        String url = null;

//        try {
//            ProcessBuilder pb = new ProcessBuilder(
//                    "ffmpeg",
//                    "-y",
//                    "-i", inputVideo.toAbsolutePath().toString(),
//                    "-vn",
//                    "-ac", "1",
//                    "-ar", "16000",
//                    outputWav.toAbsolutePath().toString()
//            );
//
//            pb.redirectErrorStream(true);
//            Process process = pb.start();
//
//            // 消费 FFmpeg 输出
//            try (var reader = new java.io.BufferedReader(
//                    new java.io.InputStreamReader(process.getInputStream()))) {
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    log.info("[ffmpeg] {}", line);
//                }
//            }
//
//            int exitCode = process.waitFor();
//
//            // ⭐⭐⭐ 成功判定一定要双保险
//            if (exitCode != 0 || !Files.exists(outputWav) || Files.size(outputWav) == 0) {
//                throw new RuntimeException("FFmpeg 转音频失败，exitCode=" + exitCode);
//            }
//
//            log.info("FFmpeg 音频转换成功: {}", outputWav.toAbsolutePath());
//
//
//            ////////////////上传aliyunoss服务器/////////////////////
//            // 将本地音频文件转换为 MultipartFile
//            url = aliyunOssService.uploadFile(outputWav.toFile());
//            log.info("音频文件已上传到阿里云OSS: {}", url);
//            //将原始数据存到数据库
//            Long id = IdWorker.getId();
//            taskContextMap.put("resourceId", id.toString());
//            log.info("原始数据id已保存到线程: {}", taskContextMap.get("resourceId"));
//            resourceMapper.insertResource(id, url, name);
//
//
//            // =============== 清理本地临时文件 ===============
//            try {
//                Files.deleteIfExists(inputVideo);
//                Files.deleteIfExists(outputWav);
//                log.info("本地临时文件已清理：{} 和 {}", inputVideo, outputWav);
//            } catch (IOException e) {
//                log.warn("清理本地临时文件时发生异常，已忽略：", e);
//            }
//
//        } catch (Exception e){
//            log.error("处理视频文件时发生错误: ", e);
//        }
//        finally {
//            if (Files.exists(inputVideo)) {
//                try {
//                    Files.delete(inputVideo);
//                } catch (IOException ignored) {
//                }
//            }
//            if (Files.exists(outputWav)) {
//                try {
//                    Files.delete(outputWav);
//                } catch (IOException ignored) {
//                }
//            }
//
//        }

        CompletableFuture<String> futureUrl = videoToAudio(inputVideo, outputWav, name, url);


        ///////////////asr处理音频/////////////////////////////
        futureUrl.thenCompose(ossUrl -> {
            String[] parts = ossUrl.split("/");
            String ossName = parts[parts.length - 1];
            String signedUrl = ossSignedUrlUtil.generateSignedUrl(ossName);
            log.info("开始处理音频文件: {}", signedUrl);
            try {
                return asrService.asr(signedUrl);  // 现在返回 CompletableFuture<Void>
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            log.info("ASR 处理完成");
        }).exceptionally(ex -> {
            log.error("处理链失败", ex);
            return null;
        });

        return "视频处理成功";

    }



    @Override
    public Map<String, Object> getPendingAndRunning() {

        return redisUtil.getAll();
    }



    @Async("asyncThreadBean")
    public CompletableFuture<String> videoToAudio(Path inputVideo, Path outputWav, String name, String url) {
        log.info("异步视频转换音频");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", inputVideo.toAbsolutePath().toString(),
                    "-vn",
                    "-ac", "1",
                    "-ar", "16000",
                    outputWav.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 消费 FFmpeg 输出
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[ffmpeg] {}", line);
                }
            }

            int exitCode = process.waitFor();

            // ⭐⭐⭐ 成功判定一定要双保险
            if (exitCode != 0 || !Files.exists(outputWav) || Files.size(outputWav) == 0) {
                throw new RuntimeException("FFmpeg 转音频失败，exitCode=" + exitCode);
            }

            log.info("FFmpeg 音频转换成功: {}", outputWav.toAbsolutePath());


            ////////////////上传aliyunoss服务器/////////////////////
            // 将本地音频文件转换为 MultipartFile
            url = aliyunOssService.uploadFile(outputWav.toFile());
            log.info("音频文件已上传到阿里云OSS: {}", url);
            //将原始数据存到数据库
            Long id = IdWorker.getId();
            taskContextMap.put("resourceId", id.toString());
            log.info("原始数据id已保存到线程: {}", taskContextMap.get("resourceId"));
            resourceMapper.insertResource(id, url, name);



            // =============== 清理本地临时文件 ===============
            try {
                Files.deleteIfExists(inputVideo);
                Files.deleteIfExists(outputWav);
                log.info("本地临时文件已清理：{} 和 {}", inputVideo, outputWav);
            } catch (IOException e) {
                log.warn("清理本地临时文件时发生异常，已忽略：", e);
            }
            return CompletableFuture.completedFuture(url);

        } catch (Exception e){
            log.error("处理视频文件时发生错误: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }




}
