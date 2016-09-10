package com.batp.logisticbuddy.client;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLngBounds;

import butterknife.BindView;

public class FindAddressActivity extends BaseMapActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int DEFAULT_MAPS_PADDING = 10;
    @BindView(R.id.pointer_submit)
    View submitPointer;

    PlaceAutocompleteFragment autoComplete;
    MapData locationPass;
    private GoogleApiClient mGoogleApiClient;

    private static final String PARAM_FINDADDRESS = "PARAM_FINDADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        initAutoComplete();

    }

    private void initAutoComplete() {
        autoComplete = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete);
        ((EditText)autoComplete.getView().
                findViewById(R.id.place_autocomplete_search_input)).setTextSize(10.0f);
        autoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(FindAddressActivity.class.getSimpleName(), "Place: " + place.getName());
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(place.getLatLng());
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,DEFAULT_MAPS_PADDING));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(FindAddressActivity.class.getSimpleName(), "An error occurred: " + status);
            }
        });
        autoComplete.setHint("Find Address..");
    }

    private void setBoundaryFromCamera() {
        autoComplete.setBoundsBias(mMap.getProjection().getVisibleRegion().latLngBounds);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_find_address;
    }

    @Override
    protected UiSettings setMapUISetting(final GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                setBoundaryFromCamera();
                autoComplete.setText(googleMap.getCameraPosition().target.toString());
            }
        });
        return googleMap.getUiSettings();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
