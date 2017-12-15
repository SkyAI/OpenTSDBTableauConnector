package cn.sky_data.OpenTSDBC_Server.controller;

import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBDao;
import cn.sky_data.OpenTSDBC_Server.service.WDCService;
import cn.sky_data.OpenTSDBC_Server.vo.MetricBindMethod;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@Controller
public class WDCController {

    private static Logger logger = LoggerFactory.getLogger(WDCController.class);


    @Autowired
    private WDCService wdcService;
    @Autowired
    private OpenTSDBDao openTSDBDao;

    @RequestMapping(path = "/")
    public String getWDC( HttpServletRequest request) {
        return "wdc";
    }

    @RequestMapping(path = "/wdc/getData", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData getRealTimeData(@RequestParam(value = "start") long startTime,
                                        @RequestParam(value = "end") long endTime,
                                        @RequestParam(value = "metrics") String metricsJson,
                                        @RequestParam(value = "tagList", required = false) String tagListJson,
                                        HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get Real Time Data. Start: " + startTime + " End: " + endTime);

        JSONArray metrics = JSONArray.parseArray(metricsJson);
        JSONArray tagList = JSONArray.parseArray(tagListJson);

        //Build multi-measurment
        List<MetricBindMethod> mbms = new ArrayList<>();
        for (int i = 0; i < metrics.size(); ++i) {
            String metric = metrics.getString(i);
            ArrayList<String> methods = new ArrayList<>();
            methods.add("sum");
            MetricBindMethod mbm = new MetricBindMethod(metric, methods);
            mbms.add(mbm);
        }

        //OpenTSDB Query
        List<String> machineList = null;
        if (tagList != null) {
            machineList = new ArrayList<>();
            for (int i=0; i<tagList.size(); ++i) {
                JSONObject jsonObject = tagList.getJSONObject(i);
                for (String key : jsonObject.keySet()) {
                        machineList.add(jsonObject.getString(key));
                }
            }
        }
        String[] machines = null;
        if(machineList != null) {
            machines = new String[machineList.size()];
            machineList.toArray(machines);
        }

        ResponseData responseData = wdcService.findBy(startTime, endTime, mbms, machines);
        logger.info(reqIP + " : Get Real Time Data Success");
        return responseData;
    }


    @RequestMapping(path = "/wdc/metrics", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getMetrics( HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Metrics");
        ResponseData responseData = openTSDBDao.getMetrics();
        logger.info(reqIP + " : Get All Metrics Success");
        return responseData;
    }

    @RequestMapping(path = "/wdc/tagk", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getTagKeys(HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Tag Key");

        ResponseData responseData = openTSDBDao.getTagKeys();
        logger.info(reqIP + " : Get All Tag Key Success");
        return responseData;
    }

    @RequestMapping(path = "/wdc/tagv", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getTagValues(HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get All Tag Value");

        ResponseData responseData = openTSDBDao.getTagValues();
        logger.info(reqIP + " : Get All Tag Value Success");
        return responseData;
    }


    @RequestMapping(path = "/wdc/setconn", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData setConnection(@RequestParam(value = "host") String host,
                                     @RequestParam(value = "port") int port,
                                     HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Set OpenTSDB Connection.");

        ResponseData responseData = openTSDBDao.setOpenTSDBUrl(host, port);

        logger.info(reqIP + " : Set OpenTSDB Connection Success.");
        return responseData;
    }
}
