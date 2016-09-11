package com.batp.logisticbuddy.client;

import android.app.ProgressDialog;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.client.adapter.ItemCodeAdapter;
import com.batp.logisticbuddy.client.adapter.OrderAdapter;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;

public class OrderDetailActivity extends BaseMapActivity {


    @BindView(R.id.recipient)
    EditText recipient;

    @BindView(R.id.address)
    EditText address;

    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.driver_status)
    EditText driverStatus;

    @BindView(R.id.expected_time_of_arrival)
    EditText expectedTimeArrival;

    @BindView(R.id.map_layout)
    View mapView;

    @BindView(R.id.list_item)
    RecyclerView listItem;

    MapData mapData;
    ItemCodeAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initDriverStatus();
    }

    private void initDriverStatus() {

        if(mapData.getTruck() != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please wait...");
            progressDialog.show();
            FirebaseHandler.getTruckStatus(mapData.getTruck(), new FirebaseHandler.DriverStatusListener() {
                @Override
                public void onSuccess(String status) {
                    progressDialog.dismiss();
                    driverStatus.setText(status);
                }

                @Override
                public void onFailed(String error) {
                    progressDialog.dismiss();
                    Log.e(OrderDetailActivity.class.getSimpleName(), error);
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_detail;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        return null;
    }

    @Override
    protected LocationListener getLocationListener() {
        return null;
    }

    @Override
    protected boolean goToPosition() {
        return false;
    }

    private void initView() {
        recipient.clearFocus();
        mapData = getIntent().getExtras().getParcelable(OrderAdapter.PARAM_DETAIL);
        if(mapData != null){
            recipient.setText(mapData.getRecipient());
            address.setText(mapData.getAddress());
            phone.setText(mapData.getPhone());

            itemAdapter = ItemCodeAdapter.createInstance();
            itemAdapter.setDeleteEnabled(false);
            listItem.setLayoutManager(new LinearLayoutManager(this));
            listItem.setAdapter(itemAdapter);

            itemAdapter.setList(mapData.getItem());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapData.getPosition(), 15f));
        googleMap.addMarker(new MarkerOptions()
                .position(mapData.getPosition())
        );
    }
}
