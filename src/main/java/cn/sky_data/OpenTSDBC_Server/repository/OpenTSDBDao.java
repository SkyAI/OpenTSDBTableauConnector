package cn.sky_data.OpenTSDBC_Server.repository;

import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenhaonee on 2017/5/12.
 */
@Service
public class OpenTSDBDao {
    private String openTSDBUrl;

    private static final MediaType JSONMedia = MediaType.parse("application/json; charset=utf-8");
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
            return processResult(response, ResultType.array);
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
            return processResult(response, ResultType.array);
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
            return processResult(response, ResultType.array);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseData getVersion () {
        String url = openTSDBUrl + "/api/version";
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
            return processResult(response, ResultType.object);
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

        RequestBody requestBody = RequestBody.create(JSONMedia, query.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            logger.info(response.toString());
            return processResult(response, ResultType.array);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResponseData processResult(Response response, ResultType type) throws IOException {
        logger.warn(response.toString());
        if (response.isSuccessful()) {
            return success(response, type);
        } else {
            return error(response);
        }
    }

    private ResponseData success(Response response, ResultType type) throws IOException {
        ResponseBody  responseBody = response.body();
        InputStream is = responseBody.byteStream();
        try {
            ResponseData responseData = null;
            switch (type){
                case array:
                    JSONArray arrayData = JSONArray.parseArray(IOUtils.toString(is, "UTF-8"));
                    responseData = new ResponseData<>(arrayData);
                    break;
                case object:
                    JSONObject objectData = JSONObject.parseObject(IOUtils.toString(is, "UTF-8"));
                    responseData = new ResponseData<>(objectData);
                    break;
            }
            responseBody.close();
            return responseData;
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

    public ResponseData setOpenTSDBUrl(String host, int port) {
        String oldUrl = this.openTSDBUrl;
        this.openTSDBUrl = "http://" + host + ":" + port;
        ResponseData responseData = getVersion();
        if(responseData != null)
            return new ResponseData("success", ResponseData.RESULT_OK);
        else {
            this.openTSDBUrl = oldUrl;
            return new ResponseData("error", ResponseData.RESULT_PARAM_ERROR);
        }
    }
}

enum ResultType {
    array, object
}

