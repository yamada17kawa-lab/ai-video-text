package com.nuliyang.ailangchain4j.service;

import reactor.core.publisher.Flux;

public interface AiService {


    Flux<String> chat(String UserMessage);
}
