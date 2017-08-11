package cn.sky_data.OpenTSDBC_Server.controller;

import cn.sky_data.OpenTSDBC_Server.domain.WDCData;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBAPI;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBDao;
import cn.sky_data.OpenTSDBC_Server.service.RealTimeDataService;
import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenhaonee on 2017/5/18.
 */
@CrossOrigin
@RestController
public class RealTimeDataController {

    private static Logger logger = LoggerFactory.getLogger(RealTimeDataController.class);


    @Autowired
    private RealTimeDataService realTimeDataService;
    @Autowired
    private OpenTSDBDao openTSDBDao;

    @RequestMapping(path = "/wdc/getData", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData getRealTimeData(@RequestParam(value = "start") long startTime,
                                        @RequestParam(value = "end") long endTime,
                                        @RequestParam(value = "metrics") JSONArray metrics,
                                        @RequestParam(value = "machines") Short[] machines,
                                        HttpServletResponse rsp, HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get Real Time Data");
        //Set allow to cross-domain access.
        rsp.setHeader("Access-Control-Allow-Origin", "*");
        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST");

        //Build multi-measurment
        List<MeasurementBindMethod> mbms = new ArrayList<>();
        for (int i = 0; i < metrics.length(); ++i) {
            String metric = metrics.getString(i);
            ArrayList<String> methods = new ArrayList<>();
            methods.add("sum");
            MeasurementBindMethod mbm = new MeasurementBindMethod(metric, methods);
            mbms.add(mbm);
        }

        //OpenTSDB Query
        ResponseData responseData = realTimeDataService.findBy(new Timestamp(startTime), new Timestamp(endTime), mbms, machines);
//        WDCData data = new WDCData(new Long(1501486474));
//        data.putValue("wind.lalala", 1.1);
//        List<WDCData> res = new ArrayList<>();
//        res.add(data);
        logger.info(reqIP + " : Get Real Time Data Success");
        return responseData;
    }


    @RequestMapping(path = "/wdc/metrics", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getMetrics(HttpServletResponse rsp, HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Metrics");
        //Set allow to cross-domain access.
        rsp.setHeader("Access-Control-Allow-Origin", "*");
        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST");

        ResponseData responseData = openTSDBDao.getMetrics();
        logger.info(reqIP + " : Get All Metrics Success");
        return responseData;
    }

    @RequestMapping(path = "/wdc/tagk", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getTagKeys(HttpServletResponse rsp, HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Metrics");
        //Set allow to cross-domain access.
        rsp.setHeader("Access-Control-Allow-Origin", "*");
        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST");

        ResponseData responseData = openTSDBDao.getTagKeys();
        logger.info(reqIP + " : Get All Metrics Success");
        return responseData;
    }

    @RequestMapping(path = "/wdc/tagv", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getTagValues(HttpServletResponse rsp, HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Metrics");
        //Set allow to cross-domain access.
        rsp.setHeader("Access-Control-Allow-Origin", "*");
        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST");

        ResponseData responseData = openTSDBDao.getTagValues();
        logger.info(reqIP + " : Get All Metrics Success");
        return responseData;
    }

}
