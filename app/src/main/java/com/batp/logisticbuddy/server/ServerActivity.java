package com.batp.logisticbuddy.server;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationListener;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.client.CreateOrderActivity;
import com.batp.logisticbuddy.client.FindAddressActivity;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.TruckData;
import com.batp.logisticbuddy.model.MapData;
import com.batp.logisticbuddy.route.RouteCalc;
import com.batp.logisticbuddy.route.RouteCalcImpl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class ServerActivity extends BaseMapActivity {

    private static final String TAG = ServerActivity.class.getSimpleName();
    public static final String TRUCK = "truck";
    private static final int REQUEST_LOCATION = 1000;
    private List<MapData> orders;
    private Map<String, TruckData> driverDatas;
    private FirebaseHandler firebaseHandler;
    private MapData baseMapData;


    void getClients(){
        // making sure that the maps is ready
        if(mMap != null){
            dialog.show();
            firebaseHandler.receiveOrders(getListener());

        } else {
            Toast.makeText(this, getString(R.string.map_not_ready), Toast.LENGTH_SHORT).show();
        }
    }

    void findFastestRoutes(){
        dialog.show();
        RouteCalc routeCalc = new RouteCalcImpl(getString(R.string.google_direction_key));
        List<MapData> allPoints = new ArrayList<>();
        allPoints.add(baseMapData);
        for (MapData data: orders) {
            allPoints.add(data);
        }
        compositeSubscription.add(routeCalc.calculateRoute(allPoints, driverDatas.size(), new RouteCalc.RouteCalcListener() {
            @Override
            public void onSuccess(SimpleMatrix simpleMatrix) {
                Log.d(TAG, simpleMatrix.toString());
                for(int i = 0; i < simpleMatrix.numRows(); i ++){
                    TruckData driverData = new TruckData();
                    driverData.setStatus("IDLE");
                    Map<String, MapData> mapDatas = new HashMap<>();
                    for(int j = 0; j < simpleMatrix.numCols(); j ++){
                        MapData mapData;
                        if(simpleMatrix.get(i, j) == 0){
                            mapData = baseMapData;
                        } else {
                            mapData = orders.get((int) simpleMatrix.get(i, j) - 1);
                            mapData.setTruck(TRUCK + i);
                        }
                        mapDatas.put(String.valueOf(j), mapData);
                    }
                    driverData.setDestinations(mapDatas);
                    modifyTruck(i, driverData);
                }

                firebaseHandler.storeRoute(driverDatas, new FirebaseHandler.FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ServerActivity.this,
                                ServerActivity.this.getString(R.string.success_route),
                                Toast.LENGTH_SHORT).show();
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

    void setBase(){
        Intent intent = new Intent(ServerActivity.this
                , FindAddressActivity.class);
        if (baseMapData != null &&
                baseMapData.getPosition() != null) {
            intent.putExtra(CreateOrderActivity.PARAM_LATITUDE, baseMapData.getPosition().latitude);
            intent.putExtra(CreateOrderActivity.PARAM_LONGITUDE, baseMapData.getPosition().longitude);
        }
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    @BindView(R.id.recycler_view_drawer)
    RecyclerView recyclerViewDrawer;
    DrawerAdapter adapter;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == RESULT_OK) {
                LatLng position = new LatLng(data.getDoubleExtra(CreateOrderActivity.PARAM_LATITUDE, 0),
                        data.getDoubleExtra(CreateOrderActivity.PARAM_LONGITUDE, 0));

                this.baseMapData.setPosition(position);
                if (baseMapData != null && baseMapData.getPosition() != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(baseMapData.getPosition(), 15f));
                    mMap.addMarker(new MarkerOptions()
                            .position(baseMapData.getPosition())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    );
                    baseMapData.setRecipient("home base");
                    FirebaseHandler.storeBaseMap(baseMapData, new FirebaseHandler.FirebaseListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailed(String error) {

                        }
                    });
                }

            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        firebaseHandler = new FirebaseHandler();
        driverDatas = new HashMap<>();
        adapter = new DrawerAdapter(new DrawerAdapter.DrawerListener() {
            @Override
            public void onItemClick(int type) {
                switch (type){
                    case DrawerAdapter.SET_BASE:
                        setBase();
                        break;
                    case DrawerAdapter.GET_CLIENT:
                        getClients();
                        break;
                    case DrawerAdapter.CALCULATE_ROUTE:
                        findFastestRoutes();
                        break;
                    case DrawerAdapter.TRUCK_CHECK:
                        checkTruckCondition();
                        break;
                    default:
                        setBase();
                }
            }
        });
        recyclerViewDrawer.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewDrawer.setLayoutManager(linearLayoutManager);
        TruckData data = new TruckData();
        data.setStatus("IDLE");
        addTruck(data);
        data = new TruckData();
        data.setStatus("IDLE");
        addTruck(data);
        FirebaseHandler.getBaseLocation(new FirebaseHandler.GetOrderListener() {
            @Override
            public void onSuccess(MapData mapData) {
                baseMapData = mapData;
                if(mapData != null && mapData.getPosition() != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapData.getPosition(), 15f));
                    mMap.addMarker(new MarkerOptions()
                            .position(mapData.getPosition())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }
            }

            @Override
            public void onFailed(String error) {
                baseMapData = new MapData();
            }
        });
        return null;
    }

    private void checkTruckCondition() {
        FirebaseHandler.getAllTruckStatus(new FirebaseHandler.DriverStatusesListener() {
            @Override
            public void onSuccess(List<Pair<String, String>> truckDataMap) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ServerActivity.this);
                RecyclerView recyclerView = new RecyclerView(ServerActivity.this);
                DriverStatusAdapter adapter = new DriverStatusAdapter(truckDataMap);
                recyclerView.setAdapter(adapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(ServerActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                builder.setView(recyclerView);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }

            @Override
            public void onFailed() {

            }
        });
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

    @Override
    protected boolean goToPosition() {
        return false;
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
