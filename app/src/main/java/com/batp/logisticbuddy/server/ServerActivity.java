package com.batp.logisticbuddy.server;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.DriverData;
import com.batp.logisticbuddy.model.MapData;
import com.batp.logisticbuddy.route.RouteCalc;
import com.batp.logisticbuddy.route.RouteCalcImpl;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class ServerActivity extends BaseMapActivity {

    private static final String TAG = ServerActivity.class.getSimpleName();
    private List<MapData> orders;
    private List<DriverData> driverDatas;
    private FirebaseHandler firebaseHandler;

    @OnClick(R.id.button_get_clients)
    void getClients(){
        // making sure that the maps is ready
        if(mMap != null){
            dialog.show();
            firebaseHandler.receiveOrders(getListener());

        } else {
            Toast.makeText(this, getString(R.string.map_not_ready), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_find_routes)
    void FindFastestRoutes(){
        RouteCalc routeCalc = new RouteCalcImpl(getString(R.string.google_direction_key));
        compositeSubscription.add(routeCalc.calculateRoute(orders, driverDatas.size(), new RouteCalc.RouteCalcListener() {
            @Override
            public void onSuccess(SimpleMatrix simpleMatrix) {
                Log.d(TAG, simpleMatrix.toString());
                for(int i = 0; i < simpleMatrix.numRows(); i ++){
                    DriverData driverData = new DriverData();
                    driverData.setDriverName(driverDatas.get(i).getDriverName());
                    Map<String, MapData> mapDatas = new HashMap<String, MapData>();
                    for(int j = 0; j < simpleMatrix.numCols(); j ++){
                        mapDatas.put(String.valueOf(j), orders.get((int) simpleMatrix.get(i,j)));
                    }
                    driverData.setDestinations(mapDatas);
                    driverDatas.set(i, driverData);
                }
                dialog.show();
                firebaseHandler.storeRoute(driverDatas, new FirebaseHandler.FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailed(String error) {
                        dialog.dismiss();
                        Toast.makeText(ServerActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });


            }

            @Override
            public void onFailed(String error) {

            }
        }));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        firebaseHandler = new FirebaseHandler();
        driverDatas = new ArrayList<>();
        DriverData data = new DriverData();
        data.setDriverName("Bejo");
        driverDatas.add(data);
        data = new DriverData();
        data.setDriverName("Paijo");
        driverDatas.add(data);
        return null;
    }

    public FirebaseHandler.GetOrdersListener getListener() {
        return new FirebaseHandler.GetOrdersListener() {
            @Override
            public void onSuccess(List<MapData> mapDatas) {
                dialog.dismiss();
                ServerActivity.this.orders = mapDatas;
                for (MapData mapData : mapDatas){
                    mMap.addMarker(new MarkerOptions()
                            .position(mapData.getPosition())
                            .title(mapData.getRecipient()));
                }
            }

            @Override
            public void onFailed(String error) {

            }
        };
    }
}
