package cn.sky_data.OpenTSDBC_Server.repository;

import cn.sky_data.OpenTSDBC_Server.domain.WDCData;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenhaonee on 2017/5/12.
 */
@Service
public class OpenTSDBDao {
    @Value("${openTSDBUrl}")
    private String openTSDBUrl;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static Logger logger = LoggerFactory.getLogger(OpenTSDBDao.class);

    public ResponseData getMetrics () {
        String url = openTSDBUrl + "/api/suggest?type=metrics&max=1000";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(240, TimeUnit.SECONDS)
                .readTimeout(240, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return processResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseData getTagKeys () {
        String url = openTSDBUrl + "/api/suggest?type=tagk";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(240, TimeUnit.SECONDS)
                .readTimeout(240, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return processResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseData getTagValues () {
        String url = openTSDBUrl + "/api/suggest?type=tagv&max=1000";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(240, TimeUnit.SECONDS)
                .readTimeout(240, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return processResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseData findBy(JSONObject query) {
        String url = openTSDBUrl + "/api/query?details";
        logger.info("Param : " + query.toString());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(240, TimeUnit.SECONDS)
                .readTimeout(240, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = RequestBody.create(JSON, query.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            logger.info(response.toString());
            return processResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResponseData processResult(Response response) throws IOException {
        logger.warn(response.toString());
        if (response.isSuccessful()) {
            return success(response);
        } else {
            return error(response);
        }
    }

    private ResponseData success(Response response) throws IOException {
        ResponseBody  responseBody = response.body();
        InputStream is = responseBody.byteStream();
        try {
            JSONArray data = new JSONArray(IOUtils.toString(is, "UTF-8"));
            responseBody.close();
            return new ResponseData<>(data);
        } catch (IOException e) {
            logger.error("IOException occurred in OpenTSDBDao.findBy() for:" + e.getMessage());
            return new ResponseData("", 400);
        }

    }

    private ResponseData error(Response response) {
        int code = response.code();
        return new ResponseData("", code);
    }

    public String getOpenTSDBUrl() {
        return openTSDBUrl;
    }

    public void setOpenTSDBUrl(String openTSDBUrl) {
        this.openTSDBUrl = openTSDBUrl;
    }

}

