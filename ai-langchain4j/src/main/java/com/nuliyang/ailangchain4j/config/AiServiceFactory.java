package com.nuliyang.ailangchain4j.config;


import com.nuliyang.ailangchain4j.service.AiService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServiceFactory {

    @Resource
    private QwenChatModel qianwen;

    @Autowired
    private ContentRetriever contentRetriever;


    @Resource
    private QwenStreamingChatModel qianwenStreaming;


    @Bean(name = "chat")
    public AiService chat(){
        return AiServices.builder(AiService.class)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .chatLanguageModel(qianwen)
                .contentRetriever(contentRetriever)
                .streamingChatLanguageModel(qianwenStreaming)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }


}
