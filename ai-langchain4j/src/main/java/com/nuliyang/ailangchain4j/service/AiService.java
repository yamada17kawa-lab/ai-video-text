package com.nuliyang.ailangchain4j.service;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiService {


    @SystemMessage(fromResource = "System.text")
    Flux<String> chat(String UserMessage);
}
