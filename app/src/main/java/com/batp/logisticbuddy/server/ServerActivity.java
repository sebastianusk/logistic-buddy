package com.batp.logisticbuddy.server;

import android.location.LocationListener;
import android.util.Log;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.TruckData;
import com.batp.logisticbuddy.model.MapData;
import com.batp.logisticbuddy.route.RouteCalc;
import com.batp.logisticbuddy.route.RouteCalcImpl;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
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
    public static final String TRUCK = "TRUCK";
    private List<MapData> orders;
    private Map<String, TruckData> driverDatas;
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
        dialog.show();
        RouteCalc routeCalc = new RouteCalcImpl(getString(R.string.google_direction_key));
        compositeSubscription.add(routeCalc.calculateRoute(orders, driverDatas.size(), new RouteCalc.RouteCalcListener() {
            @Override
            public void onSuccess(SimpleMatrix simpleMatrix) {
                Log.d(TAG, simpleMatrix.toString());
                for(int i = 0; i < simpleMatrix.numRows(); i ++){
                    TruckData driverData = new TruckData();
                    driverData.setStatus("IDLE");
                    Map<String, MapData> mapDatas = new HashMap<>();
                    for(int j = 0; j < simpleMatrix.numCols(); j ++){
                        MapData mapData = orders.get((int) simpleMatrix.get(i, j));
                        if(j != 0)
                            mapData.setTruck(TRUCK + i);
                        mapDatas.put(String.valueOf(j), mapData);
                    }
                    driverData.setDestinations(mapDatas);
                    modifyTruck(i, driverData);
                }

                firebaseHandler.updateOrders(orders, new FirebaseHandler.FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                        Toast.makeText(ServerActivity.this,
                                ServerActivity.this.getString(R.string.success_route),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String error) {
                        dialog.dismiss();
                        Toast.makeText(ServerActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });

                compositeSubscription.add(firebaseHandler.storeRoute(driverDatas, new FirebaseHandler.FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                        Toast.makeText(ServerActivity.this,
                                ServerActivity.this.getString(R.string.success_route),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String error) {
                        dialog.dismiss();
                        Toast.makeText(ServerActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }));


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
        driverDatas = new HashMap<>();
        TruckData data = new TruckData();
        data.setStatus("IDLE");
        addTruck(data);
        data = new TruckData();
        data.setStatus("IDLE");
        addTruck(data);
        return null;
    }

    private void addTruck(TruckData data) {
        driverDatas.put(TRUCK + driverDatas.size(),data);
    }

    private void modifyTruck(int i, TruckData driverData) {
        String truckCode = TRUCK + i;
        driverDatas.remove(truckCode);
        driverDatas.put(truckCode, driverData);
    }

    @Override
    protected LocationListener getLocationListener() {
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
