package com.nuliyang.aivideo.service;

import java.io.File;
import java.io.IOException;

public interface AsrService {


    String asr(String wavFileUrl) throws IOException;


}
