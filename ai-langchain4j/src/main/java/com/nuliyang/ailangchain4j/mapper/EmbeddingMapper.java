package com.nuliyang.ailangchain4j.mapper;

import com.nuliyang.common.po.EmbeddingPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmbeddingMapper {


    //TODO 存储向量
    @Insert("insert into vector_data(id, resource_id, embedding) values(#{id}, #{resourceId}, #{embedding})")
    void insertEmbedding(EmbeddingPo embeddingPo);


    //TODO 获取向量



    //TODO 删除向量
}
