package com.nuliyang.aivideo.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper {


    /**
     * 插入原始资源
     * @param url
     * @param name
     */
    @Insert("insert into resourcetable (id, name, url)" +
            "values " +
            "(#{id}, #{name}, #{url})")
    void insertResource(Long id, String url, String name);
}
