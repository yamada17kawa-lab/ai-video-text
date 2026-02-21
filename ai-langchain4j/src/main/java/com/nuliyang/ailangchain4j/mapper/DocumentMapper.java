package com.nuliyang.ailangchain4j.mapper;


import com.nuliyang.common.po.DocumentPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper {



    //TODO 存储文档
    @Insert("insert into documenttable(id,resource_id,meta_data,text_words) values(#{id},#{resourceId},#{metaData},#{textWords})")
    void insertDocument(DocumentPo documentPo);






    //TODO 获取文档



    //TODO 删除文档
}
