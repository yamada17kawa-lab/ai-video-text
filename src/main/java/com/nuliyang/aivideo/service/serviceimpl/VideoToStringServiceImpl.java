package com.nuliyang.aivideo.service.serviceimpl;

import com.nuliyang.aivideo.common.OssSignedUrlUtil;
import com.nuliyang.aivideo.common.TaskIdStore;
import com.nuliyang.aivideo.common.ThreadContext;
import com.nuliyang.aivideo.service.AliyunOssService;
import com.nuliyang.aivideo.service.AsrService;
import com.nuliyang.aivideo.service.VideoToStringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class VideoToStringServiceImpl implements VideoToStringService {


    @Autowired
    @Qualifier("aliyunAsrServiceImpl")
    private AsrService asrService;

    @Autowired
    private AliyunOssService aliyunOssService;

    @Autowired
    private OssSignedUrlUtil ossSignedUrlUtil;


    @Override
    public String videoToString(MultipartFile video)
            throws IOException, InterruptedException {

        log.info("开始处理视频文件: {}", video.getOriginalFilename());

        //将名字放到线程中
        ThreadContext.setData(video.getOriginalFilename());

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


            // =============== 清理本地临时文件 ===============
            try {
                Files.deleteIfExists(inputVideo);
                Files.deleteIfExists(outputWav);
                log.info("本地临时文件已清理：{} 和 {}", inputVideo, outputWav);
            } catch (IOException e) {
                log.warn("清理本地临时文件时发生异常，已忽略：", e);
            }

        } finally {
            if (Files.exists(inputVideo)) {
                try { Files.delete(inputVideo); } catch (IOException ignored) {}
            }
            if (Files.exists(outputWav)) {
                try { Files.delete(outputWav); } catch (IOException ignored) {}
            }
        }


        ///////////////asr处理音频/////////////////////////////
        try {
            String result = null;
            String[] parts = url.split("/");
            String ossName = parts[parts.length - 1];
            url = ossSignedUrlUtil.generateSignedUrl(ossName);
            log.info("开始处理音频文件: {}", url);
            result = asrService.asr(url);
            return result;
        } catch (IOException e) {
            return "处理音频文件时发生错误: " + e.getMessage();
        }finally {
            //清理线程数据
            ThreadContext.clear();
        }




    }



    @Override
    public Map<String, Object> getPendingAndRunning() {
        return TaskIdStore.getAll();
    }




}
