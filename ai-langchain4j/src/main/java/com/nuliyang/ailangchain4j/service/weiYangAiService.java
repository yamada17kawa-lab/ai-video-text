package com.nuliyang.ailangchain4j.service;

import com.nuliyang.common.dto.FileDto;
import com.nuliyang.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

public interface weiYangAiService {



    void ai22(MultipartFile file, String resourceId) throws IOException, SQLException;


    void weiYangAi(FileDto fileDto, String resourceId) throws IOException, SQLException;

}
