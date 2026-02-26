package com.nuliyang.ailangchain4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.nuliyang.ailangchain4j", "com.nuliyang.common"})
@Slf4j
@EnableFeignClients
@EnableAsync
public class AiLangchain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLangchain4jApplication.class, args);

        log.info("ai-langchain4j服务启动成功");
    }

}
