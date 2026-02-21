package com.nuliyang.ailangchain4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@Slf4j
@EnableFeignClients
public class AiLangchain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLangchain4jApplication.class, args);

        log.info("ai-langchain4j服务启动成功");
    }

}
