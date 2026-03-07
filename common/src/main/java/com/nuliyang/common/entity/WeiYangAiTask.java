package com.nuliyang.common.entity;


import com.nuliyang.common.dto.FileDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeiYangAiTask {

    private FileDto fileDto;

    private String resourceId;
}
