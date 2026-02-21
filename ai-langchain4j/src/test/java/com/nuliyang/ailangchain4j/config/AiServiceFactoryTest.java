package com.nuliyang.ailangchain4j.config;

import com.nuliyang.ailangchain4j.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiServiceFactoryTest {


    @Autowired
    @Qualifier("chat")
    private AiService aiService;

    @Test
    void chat() {

    }
}