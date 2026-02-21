package com.nuliyang.aivideo.feign;

import com.nuliyang.common.dto.FileDto;
import com.nuliyang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(
        name = "ai-langchain4j"
)
public interface AiFeign {

    @PostMapping("/weiyangai")
    Result<String> weiYangAi(@RequestBody FileDto fileDto,
                             @RequestParam String resourceId);

}
