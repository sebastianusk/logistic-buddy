package com.batp.logisticbuddy.client;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.client.adapter.ItemCodeAdapter;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.batp.logisticbuddy.model.ItemData;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateOrderActivity extends BaseMapActivity {

    private static final String ORDER_TABLE = "order";
    public static final int REQUEST_LOCATION = 123;
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";

    @BindView(R.id.recipient)
    EditText recipient;

    @BindView(R.id.address)
    EditText address;

    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.btn_submit)
    Button submitButton;

    @BindView(R.id.address_layout)
    View addressView;

    @BindView(R.id.map_layout)
    View mapView;

    @BindView(R.id.add_button)
    ImageView addButton;

    @BindView(R.id.item_code)
    EditText itemCode;

    @BindView(R.id.list_item)
    RecyclerView listItem;

    MapData mapData;
    ItemCodeAdapter itemAdapter;

    @BindView(R.id.choose_location)
    TextView chooseLocation;

    @BindView(R.id.change_location)
    TextView changeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        mapData = new MapData();
        initView();
        initViewListener();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_order;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setBuildingsEnabled(true);
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

    private void initViewListener() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateCode();
                sendDataToFirebase(getParam());

            }
        });
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateOrderActivity.this
                        , FindAddressActivity.class);
                if (mapData.getPosition() != null) {
                    intent.putExtra(PARAM_LATITUDE, mapData.getPosition().latitude);
                    intent.putExtra(PARAM_LATITUDE, mapData.getPosition().longitude);
                }
                startActivityForResult(intent, REQUEST_LOCATION);
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listItem.setVisibility(View.VISIBLE);
                if(itemCode.getText().toString().length() > 0){
                    itemAdapter.addItem(new ItemData(itemCode.getText().toString()));
                    itemCode.setText("");
                }else{
                    itemCode.setError("Need to insert item code");
                    itemCode.requestFocus();
                }
            }
        });
    }

    private void initView() {
        if (mapData.getPosition() == null) {
            mapView.setVisibility(View.GONE);
            changeLocation.setVisibility(View.GONE);
        }
        itemAdapter = ItemCodeAdapter.createInstance();
        listItem.setLayoutManager(new LinearLayoutManager(this));
        listItem.setAdapter(itemAdapter);

        listItem.setVisibility(View.GONE);
    }

    private MapData getParam() {

        mapData.setAddress(address.getText().toString());
        mapData.setRecipient(recipient.getText().toString());
        mapData.setPhone(phone.getText().toString());
        mapData.setItem(itemAdapter.getList());
        mapData.setUserId(FirebaseHandler.getCurrentSessionUserId());
        return mapData;
    }

    private void sendDataToFirebase(MapData param) {
        final ProgressDialog dialog = new ProgressDialog(CreateOrderActivity.this);
        dialog.setTitle("Please wait...");
        dialog.show();
        FirebaseHandler.sendOrder(param, new FirebaseHandler.FirebaseListener() {
            @Override
            public void onSuccess() {
                dialog.dismiss();
                Dialog finishDialog = new Dialog(CreateOrderActivity.this);
                finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                finishDialog.setCancelable(false);
                finishDialog.setContentView(R.layout.finish_order_layout);

                EditText text = (EditText) finishDialog.findViewById(R.id.verify_code);
                text.setText(mapData.getVerifyCode());

                Button dialogButton = (Button) finishDialog.findViewById(R.id.back_button);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        finish();
                    }
                });

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                finishDialog.getWindow().setAttributes(lp);

                finishDialog.show();
            }

            @Override
            public void onFailed(String error) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void generateCode() {

        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(999999);
        mapData.setVerifyCode(String.valueOf(n));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == RESULT_OK) {
                chooseLocation.setText("Location has been set.");
                changeLocation.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.VISIBLE);
                LatLng position = new LatLng(data.getDoubleExtra(PARAM_LATITUDE, 0),
                        data.getDoubleExtra(PARAM_LONGITUDE, 0));

                this.mapData.setPosition(position);
                if (mapData != null && mapData.getPosition() != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapData.getPosition(), 15f));
                    mMap.addMarker(new MarkerOptions()
                            .position(mapData.getPosition())
                    );
                }

            }
        }
    }

}
