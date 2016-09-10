package com.batp.logisticbuddy.route;

import com.batp.logisticbuddy.model.MapData;

import org.ejml.simple.SimpleMatrix;

import java.util.List;

import rx.Subscription;

/**
 * Created by Toped18 on 9/10/2016.
 */
public interface RouteCalc {

    Subscription calculateRoute(List<MapData> dataList, int vehicleNumber, RouteCalcListener listener);

    interface RouteCalcListener{
        void onSuccess(SimpleMatrix simpleMatrix);
        void onFailed(String error);
    }
}
