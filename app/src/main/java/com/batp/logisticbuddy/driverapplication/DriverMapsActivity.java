package com.batp.logisticbuddy.driverapplication;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
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
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    private AudioManager audioManager;

    private Vibrator vibrator;

    private int steps;

    private FirebaseHandler firebaseHandler;

    private int currentDestinationIndex;

    private Location currentDriverLocation;

    private static final float CLOSE_DISTANCE = 150;

    private List<MapData> mapDatasAssigned;

    private List<Location> stepMarkersLocations;

    private List<String> stepMarkresSnippet;

    private MarkerOptions driverMarker;

    private VelocityListener velocityListener;

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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        firebaseHandler = new FirebaseHandler();

        currentDestinationIndex = 0;

        x = (TextView) findViewById(R.id.x_axis);
        y = (TextView) findViewById(R.id.y_axis);
        z = (TextView) findViewById(R.id.z_axis);

        mapDatasAssigned = new ArrayList<>();

        stepMarkersLocations = new ArrayList<>();

        stepMarkresSnippet = new ArrayList<>();

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

        Observable.create(new Observable.OnSubscribe<Double>() {
            @Override
            public void call(final Subscriber<? super Double> subscriber) {
                velocityListener = new VelocityListener() {
                    @Override
                    public void onVelocityChanged(Double velocity) {
                        subscriber.onNext(velocity);
                    }
                };
            }
        })
                .debounce(5, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Double>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Double aDouble) {
                        FirebaseHandler.updateStatus("STOPPED", new FirebaseHandler.FirebaseListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailed(String error) {

                            }
                        });
                    }
                });
    }

    private interface VelocityListener{
        void onVelocityChanged(Double velocity);
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
        steps = 0;
        stepMarkresSnippet.clear();
        stepMarkersLocations.clear();
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
                    mMap.addMarker(new MarkerOptions().position(startLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    setDestination(currentLeg);
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
                dialog.dismiss();
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

            LatLng stepLatLng = new LatLng(leg.getSteps().get(j).getStartLocation().getLat(), leg.getSteps().get(j).getEndLocation().getLng());
            mMap.addMarker(new MarkerOptions()
                    .position(stepLatLng)
                    .title("Step " + String.valueOf(j + 1))
                    .visible(false)
                    .snippet(Html.fromHtml(leg.getSteps().get(j).getHtmlInstructions()).toString()));

            Location stepLocations= new Location("dummy");
            stepLocations.setLatitude(leg.getSteps().get(j).getEndLocation().getLat());
            stepLocations.setLongitude(leg.getSteps().get(j).getEndLocation().getLng());
            stepMarkersLocations.add(stepLocations);

            stepMarkresSnippet.add(Html.fromHtml(leg.getSteps().get(j).getHtmlInstructions()).toString());

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
                if(currentDestinationIndex + 1 != mapDatasAssigned.size()-1){
                    FragmentManager fm = getFragmentManager();
                    InsertOtpDialog dialog = InsertOtpDialog.createInstance(
                            mapDatasAssigned.get(currentDestinationIndex+1).getVerifyCode());
                    dialog.show(fm, "insert_otp_dialog");
                } else {
                    confirmDeliveredButton.setVisibility(View.GONE);
                    startDriveButton.setVisibility(View.VISIBLE);
                    Toast.makeText(DriverMapsActivity.this, "ALL DELIVERY HAS BEEN SUCCESSFULLY DELIVERED", Toast.LENGTH_LONG).show();
                    mMap.clear();
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
                        startDriveButton.setVisibility(View.GONE);
                        firebaseHandler.getOrderLocation(new FirebaseHandler.GetDriverDesignatedLocations() {
                            @Override
                            public void onSuccessList(List<MapData> mapDataList) {
                                for (int i = 0; i< mapDataList.size(); i++){
                                    MapData convertedMapData = MapData.convertFromFirebase((Map<String, Object>) mapDataList.get(i));
                                    mapDatasAssigned.add(convertedMapData);
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

                Double vector = Math.sqrt(
                        Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"))
                        * Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"))
                        + Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0"))
                        * Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Y_AXIS, "0"))
                        + Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"))
                        * Double.parseDouble(resultData.getString(DriverIntentService.SENSOR_Z_AXIS, "0"))
                );

                if(vector > 30) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    vibrator.vibrate(500);
                    Toast.makeText(this, "Slow down mate", Toast.LENGTH_LONG).show();
                }

                if(vector > 30){
                    FirebaseHandler.updateStatus("ON HIT!!!", new FirebaseHandler.FirebaseListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailed(String error) {

                        }
                    });
                }


                break;
        }
    }

    @Override
    public void onOtpDone() {
        successFullyDelivered();
        Toast.makeText(this, "OTP CONFIRMED", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng realTimeLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        driverMarker.position(realTimeLatLng);
        if(stepMarkersLocations.size() > 0 && steps < stepMarkersLocations.size()-1 && location.distanceTo(stepMarkersLocations.get(steps))< CLOSE_DISTANCE){
            Toast.makeText(this, stepMarkresSnippet.get(steps), Toast.LENGTH_LONG).show();
            //new DirectionBottomSheet(stepMarkresSnippet.get(steps), this);
            steps++;
        }
        if(location.distanceTo(mapDatasAssigned.get(currentDestinationIndex + 1).convertToPosition()) <  CLOSE_DISTANCE){
            confirmDeliveredButton.setVisibility(View.VISIBLE);
            startDriveButton.setVisibility(View.GONE);
        } else {
            confirmDeliveredButton.setVisibility(View.GONE);
        }

        if(location.getSpeed() != 0){
            velocityListener.onVelocityChanged((double) location.getSpeed());
        }

        if (location.getSpeed() > 22.2222222){
            FirebaseHandler.updateStatus("TOO FAST", new FirebaseHandler.FirebaseListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailed(String error) {

                }
            });
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
