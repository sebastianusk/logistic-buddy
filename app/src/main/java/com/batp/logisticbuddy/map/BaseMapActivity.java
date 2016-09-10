package com.batp.logisticbuddy.map;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.batp.logisticbuddy.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;

import butterknife.ButterKnife;

/**
 * Created by Toped18 on 9/10/2016.
 */
public abstract class BaseMapActivity  extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 10000;
    protected GoogleMap mMap;
    protected ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        ButterKnife.bind(this);

        dialog = new ProgressDialog(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    protected abstract int getLayoutId();
    protected abstract UiSettings setMapUISetting(GoogleMap googleMap);

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMapUISetting(googleMap);
        setLocationEnabled();
    }

    private void setLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }
}
