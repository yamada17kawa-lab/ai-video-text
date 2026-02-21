package com.nuliyang.aivideo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@Slf4j
@EnableFeignClients
public class AiAsrApplication {


    public static void main(String[] args) {
        SpringApplication.run(AiAsrApplication.class, args);
        log.info("ai-asr服务启动成功");
    }

}
