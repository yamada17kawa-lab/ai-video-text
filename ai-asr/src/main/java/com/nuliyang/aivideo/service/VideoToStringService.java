package com.nuliyang.aivideo.service;



import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Map;


public interface VideoToStringService {

    String videoToString(MultipartFile video, String name) throws IOException, InterruptedException;


    Map<String, Object> getPendingAndRunning();

}
