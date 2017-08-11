package cn.sky_data.OpenTSDBC_Server.domain;

import cn.sky_data.OpenTSDBC_Server.vo.MeasurementBindMethod;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class WDCData {

    private Long timeStamp;
    private Short machineId;
    private Map<String, Double> values;

    public WDCData(Long time, Short machineId) {
        this.timeStamp = time;
        values = new HashMap<>();
        this.machineId = machineId;
    }


    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void putValue(String metricName, double value) {
        this.values.put(metricName, value);
    }

    public Short getMachineId() {
        return machineId;
    }

    public void setMachineId(Short machineId) {
        this.machineId = machineId;
    }

}
