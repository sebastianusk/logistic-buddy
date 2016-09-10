package com.batp.logisticbuddy.route;

import android.util.Log;

import com.batp.logisticbuddy.model.MapData;
import com.batp.logisticbuddy.model.distanceMatrixGoogle.DistanceMatrix;
import com.batp.logisticbuddy.retrofit.RetrofitConnection;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import routing.VehicleRoutingImpl;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class RouteCalcImpl implements RouteCalc {

    private static final String TAG = RouteCalcImpl.class.getSimpleName();
    private String GOOGLE_API_KEY;
    RetrofitConnection retrofitConnection;

    public RouteCalcImpl(String GOOGLE_API_KEY) {
        retrofitConnection = RetrofitConnection.getInstance();
        this.GOOGLE_API_KEY = GOOGLE_API_KEY;
    }

    @Override
    public Subscription calculateRoute(List<MapData> dataList, final int vehicleNumber, final RouteCalcListener listener) {
        if(listener == null)
            return null;

        String nodes = "";
        for(MapData data : dataList){
            nodes += data.getPosition().latitude
                    + ","
                    + data.getPosition().longitude
                    + "|";
        }
        nodes = nodes.substring(0, nodes.length() - 1);

        Map<String, String> params = new HashMap<>();
        params.put("origins", nodes);
        params.put("destinations", nodes);
        params.put("key", GOOGLE_API_KEY);

        return retrofitConnection.createService()
                .getDistanceMatrix(params)
                .flatMap(new Func1<DistanceMatrix, Observable<SimpleMatrix>>() {
                    @Override
                    public Observable<SimpleMatrix> call(DistanceMatrix distanceMatrix) {
                        return calculateRouteMatrix(distanceMatrix, vehicleNumber);
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SimpleMatrix>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SimpleMatrix simpleMatrix) {
                        listener.onSuccess(simpleMatrix);

                    }
                });
    }

    private Observable<SimpleMatrix> calculateRouteMatrix(DistanceMatrix distanceMatrix, int vehicleNumber) {
        int distanceMatrixSize = distanceMatrix.getDestinationAddresses().size();
        SimpleMatrix distanceSimpleMatrix = new SimpleMatrix(distanceMatrixSize, distanceMatrixSize);

        Log.i(TAG, distanceMatrix.toString());

        for(int i = 0; i < distanceMatrixSize; i ++){
            for (int j = 0; j < distanceMatrixSize; j ++){
                distanceSimpleMatrix.set(i, j, distanceMatrix.getRows().get(i).getElements().get(j).getDistance().getValue());
            }
        }

        Log.i(TAG, distanceSimpleMatrix.toString());

        List<Double> clientDemand = new ArrayList<>();
        clientDemand.add(0.0);
        for(int i = 0; i < distanceMatrix.getRows().size(); i++){
            clientDemand.add(5.0);
        }


        return Observable.just(
                new VehicleRoutingImpl(vehicleNumber, 15, 5)
                        .setClient(distanceSimpleMatrix, clientDemand)
                        .computeBestRoute());
    }
}
