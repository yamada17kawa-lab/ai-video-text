package com.nuliyang.ailangchain4j.service.serviceimpl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuliyang.ailangchain4j.service.weiYangAiService;
import com.nuliyang.common.dto.FileDto;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class WeiYangAiServiceImpl implements weiYangAiService {




    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddingModel qwenEmbeddingModel;



    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void ai22(MultipartFile file, String resourceId) throws IOException{

        //读取文件内容清洗数据并转换成TextSegment

        JsonNode transcriptsNode = objectMapper.readTree(file.getInputStream())
                .path("transcripts")
                .get(0);
        List<TextSegment> segments = new ArrayList<>();
        for (JsonNode sentenceNode : transcriptsNode.path("sentences")){
            Map<String, String> metadata = new HashMap<>();
            metadata.put(
                    "beginTime",
                    sentenceNode.path("begin_time").asText()
            );
            metadata.put(
                    "endTime",
                    sentenceNode.path("end_time").asText()
            );
            metadata.put(
                    "sentenceId",
                    sentenceNode.path("sentence_id").asText()
            );
            metadata.put(
                    "documentId",
                    resourceId
            );
            TextSegment textSegment = TextSegment.from(sentenceNode.path("text").asText(), Metadata.from( metadata));
            segments.add(textSegment);



        }



        // 批量存储
        for (TextSegment segment : segments){
            float[] vector = qwenEmbeddingModel.embed(segment.text()).content().vector();
            Embedding embedding = Embedding.from( vector);
            embeddingStore.add(embedding, segment);
        }

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void weiYangAi(FileDto fileDto, String resourceId) throws IOException, SQLException {
        //读取文件内容清洗数据并转换成TextSegment
        MultipartFile file = getMultipartFile(fileDto.getTaskId(), fileDto.getResultText());

        JsonNode transcriptsNode = objectMapper.readTree(file.getInputStream())
                .path("transcripts")
                .get(0);
        List<TextSegment> segments = new ArrayList<>();
        for (JsonNode sentenceNode : transcriptsNode.path("sentences")){
            Map<String, String> metadata = new HashMap<>();
            metadata.put(
                    "beginTime",
                    sentenceNode.path("begin_time").asText()
            );
            metadata.put(
                    "endTime",
                    sentenceNode.path("end_time").asText()
            );
            metadata.put(
                    "sentenceId",
                    sentenceNode.path("sentence_id").asText()
            );
            metadata.put(
                    "documentId",
                    resourceId
            );
            TextSegment textSegment = TextSegment.from(sentenceNode.path("text").asText(), Metadata.from( metadata));
            segments.add(textSegment);



        }



        // 批量存储
        for (TextSegment segment : segments){
            float[] vector = qwenEmbeddingModel.embed(segment.text()).content().vector();
            Embedding embedding = Embedding.from( vector);
            embeddingStore.add(embedding, segment);
        }
    }




    private MultipartFile getMultipartFile(String taskId, String resultText) {
        String fileName = "transcript_" + taskId + ".txt";
        byte[] fileBytes = resultText.getBytes(StandardCharsets.UTF_8);

        // 创建 MultipartFile 对象
        return new MockMultipartFile(
                "file",           // 表单字段名，要和 @RequestPart("file") 一致
                fileName,         // 文件名
                "text/plain",     // Content-Type
                fileBytes         // 文件字节内容
        );
    }


}
