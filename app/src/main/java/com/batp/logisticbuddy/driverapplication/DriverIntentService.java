package com.batp.logisticbuddy.driverapplication;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public class DriverIntentService extends IntentService implements SensorEventListener, LocationListener {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public static final String RECEIVER_KEY = "receiver";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private Criteria crit;
    private String provider;
    private ResultReceiver receiverListener;

    public static final String SENSOR_X_AXIS = "xSensorKey";
    public static final String SENSOR_Y_AXIS = "ySensorKey";
    public static final String SENSOR_Z_AXIS = "zSensorKey";

    public static final String LOCATION_KEY = "location_key";

    public DriverIntentService() {
        super("Location");
    }

    public static void startBackgroundService(Context context, SpeedingResultReceiver receiver) {
        Intent intent = new Intent(context, DriverIntentService.class);
        intent.putExtra(RECEIVER_KEY, receiver);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiverListener = intent.getParcelableExtra(RECEIVER_KEY);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);

        provider = locationManager.getBestProvider(crit, true);

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

    @Override
    public void onSensorChanged(SensorEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString(SENSOR_X_AXIS, String.valueOf(Math.abs(event.values[0])));
        bundle.putString(SENSOR_Y_AXIS, String.valueOf(Math.abs(event.values[1])));
        bundle.putString(SENSOR_Z_AXIS, String.valueOf(Math.abs(event.values[2])));
        receiverListener.send(SpeedingResultReceiver.SPEEDING_RESULT_CODE, bundle);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(LOCATION_KEY, location);
        receiverListener.send(SpeedingResultReceiver.LOCATION_CHANGE_RESULT_CODE, bundle);
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
}
