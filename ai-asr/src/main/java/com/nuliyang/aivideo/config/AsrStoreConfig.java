package com.nuliyang.aivideo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AsrStoreConfig {


    @Bean
    public ConcurrentHashMap<String, String> asrStore() {
        return new ConcurrentHashMap<>();
    }



}
