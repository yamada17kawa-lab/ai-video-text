package com.nuliyang.aivideo.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Base64;

public class AliyunTokenUtil {

    private static final String TOKEN_URL =
            "https://nls-meta.cn-shanghai.aliyuncs.com/pop/2018-05-18/tokens";

    public static String getToken(String accessKeyId, String accessKeySecret) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String auth = Base64.getEncoder()
                .encodeToString((accessKeyId + ":" + accessKeySecret).getBytes());

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Authorization", "Basic " + auth)
                .post(RequestBody.create(new byte[0]))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("获取 Token 失败：" + response.code());
            }

            assert response.body() != null;
            String body = response.body().string();
            JSONObject json = JSON.parseObject(body);
            return json.getJSONObject("Token").getString("Id");
        }
    }
}

