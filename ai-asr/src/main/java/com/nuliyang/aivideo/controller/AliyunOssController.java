package com.nuliyang.aivideo.controller;


import com.nuliyang.aivideo.common.OssFileInfo;
import com.nuliyang.aivideo.service.AliyunOssService;
import com.nuliyang.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AliyunOssController {


    @Autowired
    private AliyunOssService aliyunOssService;

    @GetMapping("/listAudioFiles")
    public Result<List<OssFileInfo>> listFiles() {
        List<OssFileInfo> ossFileInfos = aliyunOssService.listFiles("audio", 100);
        return Result.success("获取oss音频文件成功", ossFileInfos);
    }


    @GetMapping("/deletePre/{pre}")
    public Result<String> deletePre(@PathVariable String pre) {
        aliyunOssService.clearDirectoryByPrefix(pre);
        return Result.success("清空oss音频文件成功");
    }

}
