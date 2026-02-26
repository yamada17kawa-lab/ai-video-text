package com.nuliyang.aivideo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.nuliyang.aivideo", "com.nuliyang.common"})
@Slf4j
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class AiAsrApplication {


    public static void main(String[] args) {
        SpringApplication.run(AiAsrApplication.class, args);
        log.info("ai-asr服务启动成功");
    }

}
