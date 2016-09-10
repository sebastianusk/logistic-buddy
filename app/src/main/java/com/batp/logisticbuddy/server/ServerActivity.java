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

import java.util.List;

import butterknife.OnClick;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class ServerActivity extends BaseMapActivity {

    private static final String TAG = ServerActivity.class.getSimpleName();
    private List<MapData> orders;
    private List<DriverData> driverDatas;

    @OnClick(R.id.button_get_clients)
    void getClients(){
        // making sure that the maps is ready
        if(mMap != null){
            dialog.show();
            FirebaseHandler firebaseHandler = new FirebaseHandler();
            firebaseHandler.initDatabaseReferrence();
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
