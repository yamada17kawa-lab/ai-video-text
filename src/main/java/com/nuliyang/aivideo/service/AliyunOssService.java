package com.nuliyang.aivideo.service;

import com.nuliyang.aivideo.common.OssFileInfo;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AliyunOssService {


    String uploadFile(File file) throws IOException;


    List<OssFileInfo> listFiles(String prefix, Integer maxKeys);

    void clearDirectoryByPrefix(String prefix);

}
