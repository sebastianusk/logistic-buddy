package com.batp.logisticbuddy.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Toped18 on 9/11/2016.
 */
public class AccidentData {

    String time;
    LatLng location;

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
