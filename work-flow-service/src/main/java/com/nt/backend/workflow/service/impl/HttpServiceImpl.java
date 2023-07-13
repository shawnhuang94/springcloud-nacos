package com.nt.backend.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nt.backend.workflow.dto.UserIdDTO;
import com.nt.backend.workflow.service.HttpService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpServiceImpl implements HttpService {

    @Value("${http.url.findLeader}")
    private String findLeaderApi;


    @Override
    public String findLeaderIdByStartUserId(String startUserId)  {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(new UserIdDTO(startUserId)));
        Request request = new Request.Builder()
                .url(findLeaderApi)
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        Response response = null;
        JSONObject object = null;
        try {
            response = client.newCall(request).execute();
            if (response != null){
                object = JSONObject.parseObject(response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String leaderId = null;
        if (object != null){
            leaderId = object.getString("data");
        }
        return leaderId;
    }
}
