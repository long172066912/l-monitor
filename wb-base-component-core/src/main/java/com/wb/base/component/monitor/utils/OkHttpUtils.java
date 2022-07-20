package com.wb.base.component.monitor.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
* @Title: OkHttpUtils
* @Description: 低版本无监控的okhttpUtils，请使用OkHttpClientUtils
* @author JerryLong
* @date 2022/4/8 4:21 PM
* @version V1.0
*/
@Slf4j
public class OkHttpUtils {

    private static OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, SECONDS).build();
    private static MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    /**
     * send，不需要拿到返回
     * @param uri
     * @param data
     */
    public static void send(String uri, String data) {
        client.newCall(new Request.Builder().url(uri).post(RequestBody.create(mediaType, data)).build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("OkHttpUtils send error !", e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    log.warn("OkHttpUtils send fail ! response : {}", response.toString());
                } else {
//                        LOGGER.info("OpsMonitorPusher sendOps success !");
                }
                response.close();
            }
        });
    }
}
