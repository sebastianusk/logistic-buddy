package com.batp.logisticbuddy.driverapplication.api;

import com.batp.logisticbuddy.driverapplication.model.DriverModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public interface GoogleMapApiInterface {
    @GET("directions/json")
    Call<DriverModel> getDistance(@QueryMap Map<String, String> driverParams);
}
