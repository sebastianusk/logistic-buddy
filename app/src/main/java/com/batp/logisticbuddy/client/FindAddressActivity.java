package com.batp.logisticbuddy.client;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;

import butterknife.BindView;

public class FindAddressActivity extends BaseMapActivity {

    @BindView(R.id.mapview)
    MapView mapView;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.pointer_text)
    TextView textPointer;
    @BindView(R.id.pointer_submit)
    View submitPointer;
    @BindView(R.id.fab)
    FloatingActionButton fab;


    PlaceAutocompleteFragment autoComplete;

    private static final String PARAM_FINDADDRESS = "PARAM_FINDADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_address);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_find_address;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        return googleMap.getUiSettings();
    }

}
