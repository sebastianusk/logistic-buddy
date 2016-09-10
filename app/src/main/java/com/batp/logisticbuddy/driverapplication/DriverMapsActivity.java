package com.batp.logisticbuddy.driverapplication;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.driverapplication.api.GoogleMapApiInterface;
import com.batp.logisticbuddy.driverapplication.model.DriverModel;
import com.batp.logisticbuddy.driverapplication.model.Leg;
import com.batp.logisticbuddy.driverapplication.service.GoogleMapApiService;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public class DriverMapsActivity extends BaseMapActivity implements SpeedingResultReceiver.Receiver{

    private HashMap<String, String> googleParameters;

    private SpeedingResultReceiver receiver;

    TextView x;
    TextView y;
    TextView z;
    TextView startDriveButton;

    @Override
    protected int getLayoutId() {
        return R.layout.driver_map_activity;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        return googleMap.getUiSettings();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        x = (TextView) findViewById(R.id.x_axis);
        y = (TextView) findViewById(R.id.y_axis);
        z = (TextView) findViewById(R.id.z_axis);

        startDriveButton = (TextView) findViewById(R.id.driver_start_button);
        startDriveButton.setOnClickListener(onStartButtonClickedListener());

        receiver = new SpeedingResultReceiver(new Handler());
        receiver.setReceiver(this);
        DriverIntentService.startBackgroundService(this, receiver);
    }

    private void fetchDataFromGoogleMapApi(List<Location> locationList) {
        GoogleMapApiInterface mapInterface = GoogleMapApiService.getClient().create(GoogleMapApiInterface.class);
        googleParameters.clear();
        googleParameters.put("origin", "-6.1904982,106.7976599");
        googleParameters.put("destination", "-6.190699,106.7975051");
        googleParameters.put("mode", "driving");
        googleParameters.put("key", "AIzaSyDhAonRJef0uzBDxcznv9gyi5TT33Aei6M");
        String waypoints = "";

        for(int waypointIndex = 0; waypointIndex < locationList.size(); waypointIndex++) {
            waypoints = waypoints + locationList.get(waypointIndex).getLatitude()
                    + ","
                    + locationList.get(waypointIndex).getLongitude();
            if(waypointIndex != locationList.size() -1) {
                waypoints = waypoints + "|";
            }
        }
        googleParameters.put("waypoints", waypoints);

        retrofit2.Call<DriverModel> call = mapInterface.getDistance(googleParameters);
        call.enqueue(new Callback<DriverModel>() {
            @Override
            public void onResponse(retrofit2.Call<DriverModel> call, Response<DriverModel> response) {
                Leg startLeg = response.body().getRoutes().get(0).getLegs().get(0);
                LatLng startLatLng = new LatLng(startLeg.getStartLocation().getLat(), startLeg.getStartLocation().getLng());
                mMap.addMarker(new MarkerOptions().position(startLatLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                for(int i = 0; i<response.body().getRoutes().get(0).getLegs().size(); i++) {
                    Leg leg = response.body().getRoutes().get(0).getLegs().get(i);
                    LatLng endLatLng = new LatLng(leg.getEndLocation().getLat(), leg.getEndLocation().getLng());
                    mMap.addMarker(new MarkerOptions().position(endLatLng).title("Route " + String.valueOf(i)));

                    for (int j =0; j < leg.getSteps().size(); j++) {

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(decodePoly(leg.getSteps().get(j).getPolyline().getPoints()))
                                .width(15)
                                .color(Color.BLUE)
                                .geodesic(true);

                        mMap.addPolyline(polylineOptions);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DriverModel> call, Throwable t) {

            }
        });
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode == 10){
            if(Double.parseDouble(x.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_X_AXIS, "0")))
                x.setText(resultData.getString(DriverIntentService.SENSOR_X_AXIS, "0"));
            if(Double.parseDouble(y.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0")))
                y.setText(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0"));
            if(Double.parseDouble(z.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0")))
                z.setText(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"));
        }
    }

    private View.OnClickListener onStartButtonClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleParameters = new HashMap<>();
                List<Location> dummyLocationLists = new ArrayList<>();
                Location location1 = new Location("dummyLocation1");
                location1.setLatitude(-6.1915241);
                location1.setLongitude(106.7975194);
                Location location2 = new Location("dummyLocation2");
                location2.setLatitude(-6.1922012);
                location2.setLongitude(106.7968999);
                dummyLocationLists.add(location1);
                dummyLocationLists.add(location2);
                fetchDataFromGoogleMapApi(dummyLocationLists);
            }
        };
    }
}
