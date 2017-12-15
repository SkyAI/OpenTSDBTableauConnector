package cn.sky_data.OpenTSDBC_Server.vo;

import java.util.List;

/**
 * Created by chenhaonee on 2017/5/18.
 */
public class MeasurementBindMethod {

    private String name;

    private List<String> method;

    public MeasurementBindMethod() {
    }

    public MeasurementBindMethod(String name, List<String> method) {
        this.name = name;
        this.method = method;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMethod() {
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }
}
