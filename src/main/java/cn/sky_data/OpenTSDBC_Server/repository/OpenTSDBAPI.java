package cn.sky_data.OpenTSDBC_Server.repository;

import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenhaonee on 2017/5/12.
 */
public class OpenTSDBAPI {


    public static JSONObject simpleBuild(Timestamp start, Timestamp end, List<MeasurementBindMethod> measurementBindMethods, String machine, String groupUnit) {
        JSONObject param = new JSONObject();
        param.put("start", start.getTime());
        param.put("end", end.getTime());
        param.put("showQuery",true);

        List<JSONObject> list = new ArrayList<>();
        for (MeasurementBindMethod measurementBindMethod : measurementBindMethods) {
            String name = measurementBindMethod.getName();
            for (String method:measurementBindMethod.getMethod()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("metric",name);
                //jsonObject.put("rate", true);
                jsonObject.put("downsample", groupUnit + "-" + method);
                JSONObject tags = new JSONObject();
                tags.put("machineId", machine);
                jsonObject.put("tags", tags);
                jsonObject.put("aggregator", method);
                list.add(jsonObject);
            }
        }
        JSONArray array = new JSONArray(list);
        param.put("queries", array);
        return param;
    }

    public static JSONObject simpleBuild(long start, long end, List<MeasurementBindMethod> measurementBindMethods, String machine) {
        JSONObject param = new JSONObject();
        param.put("start", start);
        param.put("end", end);
        param.put("showQuery",true);

        List<JSONObject> list = new ArrayList<>();
        for (MeasurementBindMethod measurementBindMethod : measurementBindMethods) {
            String name = measurementBindMethod.getName();
            for (String method:measurementBindMethod.getMethod()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("metric",name);
                //jsonObject.put("rate", true);
//                jsonObject.put("downsample", groupUnit + "-" + method);
                JSONObject tags = new JSONObject();
                tags.put("machineId", machine);
                jsonObject.put("tags", tags);
                jsonObject.put("aggregator", method);
                list.add(jsonObject);
            }
        }
        JSONArray array = new JSONArray(list);
        param.put("queries", array);
        return param;
    }


    public JSONObject buildTime(Timestamp start, String aggregator, JSONObject downsampler) {
        return buildTime(start, null, aggregator, downsampler, null);
    }

    public JSONObject buildTime(Timestamp start, Timestamp end, String aggregator, JSONObject downsampler) {
        return buildTime(start, end, aggregator, downsampler, false);
    }

    /**
     * @param start       The start time for the query. This may be relative, absolute human readable or absolute Unix Epoch.
     * @param end         The end time for the query. If left out, the end is now
     * @param aggregator  The global aggregation function to use for all metrics. It may be overridden on a per metric basis.
     * @param downsampler Reduces the number of data points returned. The format is defined below
     * @param rate        Whether or not to calculate all metrics as rates, i.e. value per second. This is computed before expressions.
     * @return
     */
    public JSONObject buildTime(Timestamp start, Timestamp end, String aggregator, JSONObject downsampler, Boolean rate) {
        JSONObject time = new JSONObject();
        time.put("start", start);
        time.put("aggregator", aggregator);
        if (end != null)
            time.put("end", end);
        if (downsampler != null)
            time.put("downsampler", downsampler);
        if (rate != null)
            time.put("rate", rate);
        return time;
    }

    /**
     * @param interval   A downsampling interval, i.e. what time span to rollup raw values into. The format is <#><unit>, e.g. 15m
     * @param aggregator The aggregation function to use for reducing the data points
     * @param fillPolicy A policy to use for filling buckets that are missing data points
     * @return
     */
    public JSONObject buildDownsampler(String interval, String aggregator, JSONObject fillPolicy) {
        JSONObject downsampler = new JSONObject();
        downsampler.put("interval", interval);
        downsampler.put("aggregator", aggregator);
        if (fillPolicy != null)
            downsampler.put("fillPolicy", fillPolicy);
        return downsampler;
    }

    public JSONObject buildFillPolicies(String policy) {
        return buildFillPolicies(policy, null);
    }

    /**
     * @param policy The name of a policy to use. The values are listed in the table below
     * @param value  For scalar fills, an optional value that can be used during substitution
     * @return
     */
    public JSONObject buildFillPolicies(String policy, Double value) {
        JSONObject fillPolicy = new JSONObject();
        fillPolicy.put("policy", policy);
        if (value != null)
            fillPolicy.put("value", value);
        return fillPolicy;
    }


    public JSONObject buildFilters(String id, JSONArray tags) {
        JSONObject filters = new JSONObject();
        filters.put("id", id);
        if (tags != null)
            filters.put("tags", tags);
        return filters;
    }


    public JSONObject buildMetric(String id, String filter, String metric) {
        return buildMetric(id, filter, metric, null, null);
    }

    public JSONObject buildMetric(String id, String filter, String metric, String aggregator, JSONObject fillPolicy) {
        JSONObject metricUnit = new JSONObject();
        metricUnit.put("id", id);
        metricUnit.put("filter", filter);
        metricUnit.put("metric", metric);
        if (aggregator != null)
            metricUnit.put("aggregator", aggregator);
        if (fillPolicy != null)
            fillPolicy.put("fillPolicy", fillPolicy);
        return metricUnit;
    }

    /**
     * @param id         A unique ID for the expression
     * @param expr       The expression to execute
     * @param join       The set operation or "join" to perform for series across sets.
     * @param fillPolicy An optional fill policy for the expression when it is used in a nested expression and doesn't have a value
     * @return
     */
    public JSONObject buildExpression(String id, String expr, JSONObject join, JSONObject fillPolicy) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("expr", expr);
        if (join != null)
            object.put("join", join);
        if (fillPolicy != null)
            object.put("fillPolicy", fillPolicy);
        return object;
    }

    /**
     * @param id    The ID of the metric or expression
     * @param alias An optional descriptive name for series
     * @return
     */
    public JSONObject buildOutput(String id, String alias) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        if (alias != null)
            object.put("alias", alias);
        return object;
    }
}
