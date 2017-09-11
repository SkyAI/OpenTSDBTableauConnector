package cn.sky_data.OpenTSDBC_Server.domain;

import javax.persistence.Entity;
import java.util.*;

@Entity
public class WDCData {

    private long timeStamp;
    private String machineId;
    private Map<String, Double> values;

    public WDCData(long time, String machineId) {
        this.timeStamp = time;
        values = new HashMap<>();
        this.machineId = machineId;
    }

    public WDCData(Date time, String machineId) {
        this.timeStamp = time.getTime();
        values = new HashMap<>();
        this.machineId = machineId;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void putValue(String metricName, double value) {
        this.values.put(metricName, value);
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    @Override
    public String toString() {
        return getTimeStamp() + getValues().toString();
    }
}
