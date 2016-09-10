package com.batp.logisticbuddy.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class DriverData {
    String driverName;
    Map<String, MapData> destinations;

    public Map<String, MapData> getDestinations() {
        return destinations;
    }

    public void setDestinations(Map<String, MapData> destinations) {
        this.destinations = destinations;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
