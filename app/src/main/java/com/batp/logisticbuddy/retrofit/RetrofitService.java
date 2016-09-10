package com.batp.logisticbuddy.retrofit;


import com.batp.logisticbuddy.model.distanceMatrixGoogle.DistanceMatrix;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by Toped18 on 9/10/2016.
 */
public interface RetrofitService {

    @GET(RetrofitConstant.DISTANCE_MATRIX)
    Observable<DistanceMatrix> getDistanceMatrix(@QueryMap Map<String, String> params);

}
