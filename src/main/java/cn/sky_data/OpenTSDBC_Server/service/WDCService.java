package cn.sky_data.OpenTSDBC_Server.service;


import cn.sky_data.OpenTSDBC_Server.domain.WDCData;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBAPI;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBDao;
import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenhaonee on 2017/5/12.
 */
@Service
public class WDCService {

    @Autowired
    private OpenTSDBDao openTSDBDao;


    public ResponseData findBy(Timestamp from, Timestamp to, List<MeasurementBindMethod> measurementBindMethods, Short[] machineId) {
        StringBuffer description = new StringBuffer();
        List<WDCData> dataList = new ArrayList<>();
        Arrays.stream(machineId)
                .forEach(id -> {
                    //According to OpenTSDB Api, if a machine here misses one of the given measurement, none data will be returned but just a 400 error
                    JSONObject paramsToQuery = OpenTSDBAPI.simpleBuild(from, to, measurementBindMethods, id);
                    ResponseData responseData = openTSDBDao.findBy(paramsToQuery);
                    if (responseData == null || responseData.getResultCode() != 200)
                        description.append(Short.toString(id))
                                    .append("No data is available now\n");
                    else {
                        JSONArray result = (JSONArray) responseData.getData();
                        Map<Long, WDCData> timeMap = new HashMap<>();

                        int index = 0;
                        result.forEach(o -> {
                            JSONObject item = (JSONObject) o;
                            String metricName = item.getString("metric");
                            JSONObject dps = item.getJSONObject("dps");
                            dps.keySet().forEach(key -> {
                                Double value = dps.getDouble(key);
                                Long timeStamp = Long.parseLong(key);
                                WDCData wdcData = timeMap.get(timeStamp);
                                if (wdcData == null) {
                                    wdcData = new WDCData(timeStamp, id);
                                    timeMap.put(timeStamp, wdcData);
                                    dataList.add(wdcData);
                                }
                                wdcData.putValue(metricName, value);
                            });
                        });

                    }
                });
        return new ResponseData<List<WDCData>>(dataList, description.toString(), 200);
    }
}
