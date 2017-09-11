package cn.sky_data.OpenTSDBC_Server.controller;

import cn.sky_data.OpenTSDBC_Server.domain.WDCData;
import cn.sky_data.OpenTSDBC_Server.repository.OpenTSDBDao;
import cn.sky_data.OpenTSDBC_Server.service.WDCService;
import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;
import cn.sky_data.OpenTSDBC_Server.vo.ResponseData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
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

    @RequestMapping(path = "/wdc")
//    @ResponseBody
    public String getWDC( HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        //Set allow to cross-domain access.
//        rsp.setHeader("Access-Control-Allow-Origin", "*");
//        rsp.addHeader("Access-Control-Allow-Methods", "GET, POST");
        return "wdc";
    }

    @RequestMapping(path = "/wdc/getData", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData getRealTimeData(@RequestParam(value = "start") long startTime,
                                        @RequestParam(value = "end") long endTime,
                                        @RequestParam(value = "metrics") JSONArray metrics,
                                        @RequestParam(value = "tagList", required = false) JSONArray tagList,
                                        HttpServletRequest request) {
        String reqIP = request.getRemoteAddr();
        logger.info(reqIP + " : Request Get Real Time Data. Start: " + startTime + " End: " + endTime);

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
        List<String> machineList = null;
        if (tagList != null) {
            machineList = new ArrayList<>();
            for (int i=0; i<tagList.length(); ++i) {
                JSONObject jsonObject = tagList.getJSONObject(i);
                for (String key : jsonObject.keySet()) {
//                    if (key.equals("machineId")) {
                        machineList.add(jsonObject.getString(key));
//                    }
                }
            }
        }
        String[] machines = null;
        if(machineList != null) {
            machines = new String[machineList.size()];
            machineList.toArray(machines);
        }

        ResponseData responseData = wdcService.findBy(startTime, endTime, mbms, machines);
//        WDCData data = new WDCData((long)1501486474, new Short("1"));
//        data.putValue("wind.lalala", 1.1);
//        List<WDCData> res = new ArrayList<>();
//        res.add(data);
        logger.info(reqIP + " : Get Real Time Data Success");
//        return new ResponseData<>(res);
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
}
