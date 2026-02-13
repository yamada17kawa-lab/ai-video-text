package com.nuliyang.aivideo;

import com.nuliyang.aivideo.common.TaskIdStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AiVideoApplication {


    public static void main(String[] args) {
        SpringApplication.run(AiVideoApplication.class, args);
        TaskIdStore.clear();
        log.info("初始化TaskIdStore");
        log.info("启动成功");
    }

}
