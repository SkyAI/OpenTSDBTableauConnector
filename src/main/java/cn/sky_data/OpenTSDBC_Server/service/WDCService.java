package cn.sky_data.OpenTSDBC_Server.service;


import cn.sky_data.OpenTSDBC_Server.domain.WDCData;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBAPI;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBDao;
import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by chenhaonee on 2017/5/12.
 */
@Service
public class WDCService {

    private static Logger logger = LoggerFactory.getLogger(WDCService.class);

    @Autowired
    private OpenTSDBDao openTSDBDao;


    public ResponseData findBy(long from, long to, List<MeasurementBindMethod> measurementBindMethods, String[] machineId) {
        logger.info("find by from : " + from + " to : " + to);
        StringBuffer description = new StringBuffer();
        List<WDCData> dataList = new ArrayList<>();
        Arrays.stream(machineId).forEach(id -> {
            //According to OpenTSDB Api, if a machine here misses one of the given measurement, none data will be returned but just a 400 error
            JSONObject paramsToQuery = OpenTSDBAPI.simpleBuild(from, to, measurementBindMethods, id);
            ResponseData responseData = openTSDBDao.findBy(paramsToQuery);
            if (responseData == null || responseData.getResultCode() != 200)
                description.append(id)
                        .append("No data is available now\n");
            else {
                JSONArray result = (JSONArray) responseData.getData();
                Map<Long, WDCData> timeMap = new HashMap<>();

                result.forEach(o -> {
                    JSONObject item = (JSONObject) o;
                    String metricName = item.getString("metric");
                    JSONObject dps = item.getJSONObject("dps");
                    dps.keySet().forEach(key -> {
                        Double value = dps.getDouble(key);
                        Long timeStamp = Long.parseLong(key);
                        WDCData wdcData = timeMap.get(timeStamp);
                        if (wdcData == null) {
                            Date date = (timeStamp > Long.parseLong("20000000000")) ? new Date(timeStamp) : new Date(timeStamp*1000);
                            wdcData = new WDCData(timeStamp, id);
                            timeMap.put(timeStamp, wdcData);
                            dataList.add(wdcData);
                        }
                        wdcData.putValue(metricName, value);
                    });
                });

            }
        });
        logger.info("find " + dataList.size() + " records.");
        return new ResponseData<>(dataList, description.toString(), 200);
    }
}
