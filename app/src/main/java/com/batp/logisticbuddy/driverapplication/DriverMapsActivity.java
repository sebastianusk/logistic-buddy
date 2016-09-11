package com.batp.logisticbuddy.driverapplication;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.driverapplication.api.GoogleMapApiInterface;
import com.batp.logisticbuddy.driverapplication.model.DriverModel;
import com.batp.logisticbuddy.driverapplication.model.Leg;
import com.batp.logisticbuddy.driverapplication.service.GoogleMapApiService;
import com.batp.logisticbuddy.fragment.InsertOtpDialog;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public class DriverMapsActivity extends BaseMapActivity implements SpeedingResultReceiver.Receiver,
        InsertOtpDialog.OtpListener, LocationListener{

    private HashMap<String, String> googleParameters;

    private SpeedingResultReceiver receiver;

    private TextView x;
    private TextView y;
    private TextView z;
    private TextView startDriveButton;
    private TextView confirmDeliveredButton;
    private TextView testSuccessButton;

    private FirebaseHandler firebaseHandler;

    private int currentDestinationIndex;

    private Location currentDriverLocation;

    private static final float CLOSE_DISTANCE = 150;

    private List<MapData> mapDatasAssigned;

//    private List<LatLng> latLngLists;
//
//    private List<Location> locationList;
//
//    private List<String> customerOTP;

    private MarkerOptions driverMarker;

    @Override
    protected int getLayoutId() {
        return R.layout.driver_map_activity;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        return googleMap.getUiSettings();
    }

    @Override
    protected LocationListener getLocationListener() {
        return null;
    }

    @Override
    protected boolean goToPosition() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        firebaseHandler = new FirebaseHandler();

        currentDestinationIndex = 0;

        x = (TextView) findViewById(R.id.x_axis);
        y = (TextView) findViewById(R.id.y_axis);
        z = (TextView) findViewById(R.id.z_axis);

        mapDatasAssigned = new ArrayList<>();

//        latLngLists = new ArrayList<>();
//
//        locationList = new ArrayList<>();
//
//        customerOTP = new ArrayList<>();

        driverMarker = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        startDriveButton = (TextView) findViewById(R.id.driver_start_button);
        startDriveButton.setOnClickListener(onStartButtonClickedListener());

        confirmDeliveredButton = (TextView) findViewById(R.id.driver_confirm_shipment_button);
        confirmDeliveredButton.setOnClickListener(onConfirmButtonClickedListener());

        testSuccessButton = (TextView) findViewById(R.id.test_success_delivery_button);
        testSuccessButton.setOnClickListener(onDelivered());

        receiver = new SpeedingResultReceiver(new Handler());
        receiver.setReceiver(this);
        DriverIntentService.startBackgroundService(this, receiver);
    }

    private View.OnClickListener onDelivered() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successFullyDelivered();
            }
        };
    }

    private void successFullyDelivered() {
        currentDestinationIndex++;
        if(currentDestinationIndex == mapDatasAssigned.size()-1){
            mMap.clear();
            googleParameters.clear();
            Toast.makeText(DriverMapsActivity.this, "CONGRATS", Toast.LENGTH_LONG).show();
        } else {
            fetchDestinationFromGoogleMapApi();
        }
    }

    private void fetchDestinationFromGoogleMapApi () {
        GoogleMapApiInterface mapInterface = GoogleMapApiService.getClient().create(GoogleMapApiInterface.class);
        googleParameters.clear();
        mMap.clear();
        googleParameters.put("origin", String.valueOf(mapDatasAssigned.get(currentDestinationIndex).getPosition().latitude)
                + ","
                + String.valueOf(mapDatasAssigned.get(currentDestinationIndex).getPosition().longitude));
        googleParameters.put("destination", String.valueOf(mapDatasAssigned.get(currentDestinationIndex+1).getPosition().latitude) +
                ","
                + String.valueOf(mapDatasAssigned.get(currentDestinationIndex+1).getPosition().longitude));
        googleParameters.put("mode", "driving");
        googleParameters.put("key", getString(R.string.google_direction_key));
        retrofit2.Call<DriverModel> call = mapInterface.getDistance(googleParameters);
        call.enqueue(new Callback<DriverModel>() {
            @Override
            public void onResponse(Call<DriverModel> call, Response<DriverModel> response) {
                List<Leg> legs = response.body().getRoutes().get(0).getLegs();
                for(Leg currentLeg : legs) {
                    LatLng startLatLng = new LatLng(currentLeg.getStartLocation().getLat(), currentLeg.getStartLocation().getLng());
                    mMap.addMarker(new MarkerOptions().position(startLatLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    setDestination(currentLeg);
                    dialog.show();
                    MapData mapData = mapDatasAssigned.get(currentDestinationIndex + 1);
                    mapData.setEstimatedTime(currentLeg.getDuration().getText());
                    FirebaseHandler.updateOrder(mapData, new FirebaseHandler.FirebaseListener() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailed(String error) {
                            dialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DriverModel> call, Throwable t) {

            }
        });
    }

    private void setDestination(Leg leg) {
        LatLng endLatLng = new LatLng(leg.getEndLocation().getLat(), leg.getEndLocation().getLng());
        mMap.addMarker(new MarkerOptions().position(endLatLng).title("Route " + String.valueOf(currentDestinationIndex + 1)));

        for (int j =0; j < leg.getSteps().size(); j++) {

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(decodePoly(leg.getSteps().get(j).getPolyline().getPoints()))
                    .width(10)
                    .color(Color.BLUE)
                    .geodesic(true);

            mMap.addPolyline(polylineOptions);
        }
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

    private View.OnClickListener onConfirmButtonClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDestinationIndex != mapDatasAssigned.size()-1){
                    FragmentManager fm = getFragmentManager();
                    InsertOtpDialog dialog = InsertOtpDialog.createInstance(
                            mapDatasAssigned.get(currentDestinationIndex+1).getVerifyCode());
                    dialog.show(fm, "insert_otp_dialog");
                }
            }
        };
    }

    private View.OnClickListener onStartButtonClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                googleParameters = new HashMap<>();
                firebaseHandler.updateStatus("ON THE WAY", new FirebaseHandler.FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        firebaseHandler.getOrderLocation(new FirebaseHandler.GetDriverDesignatedLocations() {
                            @Override
                            public void onSuccessList(List<MapData> mapDataList) {
                                for (int i = 0; i< mapDataList.size(); i++){
                                    MapData convertedMapData = MapData.convertFromFirebase((Map<String, Object>) mapDataList.get(i));
                                    mapDatasAssigned.add(convertedMapData);
//                            latLngLists.add(convertedMapData.getPosition());
//                            customerOTP.add(convertedMapData.getVerifyCode());
//                            Location location = new Location("dummy_provider");
//                            location.setLatitude(convertedMapData.getPosition().latitude);
//                            location.setLongitude(convertedMapData.getPosition().longitude);
//                            locationList.add(location);
                                }
                                fetchDestinationFromGoogleMapApi();
                                initiateLocationListener();
                            }

                            @Override
                            public void onFailed() {

                            }
                        });
                    }

                    @Override
                    public void onFailed(String error) {

                    }
                });

            }
        };
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch(resultCode) {
            case 10:
                if(Double.parseDouble(x.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_X_AXIS, "0")))
                    x.setText(resultData.getString(DriverIntentService.SENSOR_X_AXIS, "0"));
                if(Double.parseDouble(y.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0")))
                    y.setText(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0"));
                if(Double.parseDouble(z.getText().toString()) < Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0")))
                    z.setText(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"));
                break;
            case 11:
                currentDriverLocation = resultData.getParcelable(DriverIntentService.LOCATION_KEY);
                LatLng realTimeLatLng = null;
                if (currentDriverLocation != null) {
                    realTimeLatLng = new LatLng(currentDriverLocation.getLatitude(), currentDriverLocation.getLongitude());
                    driverMarker.position(realTimeLatLng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(realTimeLatLng));
                }
                if(currentDriverLocation.distanceTo(mapDatasAssigned.get(currentDestinationIndex)
                .convertToPosition()) <  CLOSE_DISTANCE){
                    confirmDeliveredButton.setVisibility(View.VISIBLE);
                    startDriveButton.setVisibility(View.GONE);
                } else {
                    confirmDeliveredButton.setVisibility(View.GONE);
                    startDriveButton.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onOtpDone() {
        successFullyDelivered();
        Toast.makeText(this, "OTP DONE", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng realTimeLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        driverMarker.position(realTimeLatLng);
        if(location.distanceTo(mapDatasAssigned.get(currentDestinationIndex + 1).convertToPosition()) <  CLOSE_DISTANCE){
            confirmDeliveredButton.setVisibility(View.VISIBLE);
            startDriveButton.setVisibility(View.GONE);
        } else {
            confirmDeliveredButton.setVisibility(View.GONE);
            startDriveButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void initiateLocationListener() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);

        String provider = locationManager.getBestProvider(crit, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);
    }
}
