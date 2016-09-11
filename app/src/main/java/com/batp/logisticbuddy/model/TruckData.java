package com.batp.logisticbuddy.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class TruckData {
    Map<String, MapData> destinations;
    String status;
    private String lastUpdate;

    public Map<String, MapData> getDestinations() {
        return destinations;
    }

    public void setDestinations(Map<String, MapData> destinations) {
        this.destinations = destinations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
