package com.nuliyang.aivideo.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface AsrService {


    CompletableFuture<Void> asr(String wavFileUrl) throws IOException;

    void task(String taskId) throws IOException;
}
