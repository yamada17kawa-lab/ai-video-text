package com.nuliyang.ailangchain4j.controller;


import com.nuliyang.ailangchain4j.service.AiService;
import com.nuliyang.ailangchain4j.service.weiYangAiService;
import com.nuliyang.common.dto.FileDto;
import com.nuliyang.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class AiController {


    @Autowired
    private weiYangAiService weiYangAiService  ;

    @Autowired
    private AiService aiService;


    @PostMapping("/ai22")
    public Result<String> ai(@RequestPart("file") MultipartFile file,
                             @RequestParam String resourceId) throws IOException, SQLException {
        weiYangAiService.ai22(file, resourceId);
        return Result.success("处理成功");
    }



    @PostMapping("/weiyangai")
    public Result<String> weiYangAi(@RequestBody FileDto fileDto,
                             @RequestParam String resourceId) throws IOException, SQLException {
        weiYangAiService.weiYangAi(fileDto, resourceId);
        return Result.success("处理成功");
    }


    @GetMapping("/ai")
    public Flux<ServerSentEvent<String>> chat(@RequestParam String question) {
        return aiService.chat(question)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build()
                );
    }



}
