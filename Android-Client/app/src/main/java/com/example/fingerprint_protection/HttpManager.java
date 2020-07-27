package com.example.fingerprint_protection;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpManager {

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    public Response send_postRequest(String url1,String[][] data) throws IOException {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = url1;


        JSONObject postdata = new JSONObject();
        if(data == null){
            try {
                postdata.put("","");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                for(int i=0;i<data.length;i++){
                    postdata.put(data[i][0], data[i][1]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }


        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        return response;

    }


}