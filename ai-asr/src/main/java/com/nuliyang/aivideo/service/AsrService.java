package com.nuliyang.aivideo.service;

import java.io.File;
import java.io.IOException;

public interface AsrService {


    void asr(String wavFileUrl) throws IOException;

    void task(String taskId) throws IOException;
}
