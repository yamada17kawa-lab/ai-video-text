package com.nuliyang.aivideo.common;

import lombok.Data;

import java.util.Date;

@Data
public class OssFileInfo {
    private String objectName; // 文件在OSS上的完整名称 (key)
    private Long size;         // 文件大小 (单位: 字节)
    private Date lastModified; // 最后修改时间
    // private String url;     // 可选：文件的访问URL
}
