package com.batp.logisticbuddy.client;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.model.ItemData;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateOrderActivity extends AppCompatActivity {

    private static final String ORDER_TABLE = "order";
    @BindView(R.id.recipient)
    EditText recipient;

    @BindView(R.id.address)
    EditText address;

    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.btn_submit)
    Button submitButton;

    @BindView(R.id.verify_code)
    EditText verifyCode;

    @BindView(R.id.verify_code_layout)
    View verifyCodeLayout;

    @BindView(R.id.address_layout)
    View addressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        ButterKnife.bind(this);

        initView();
        initViewListener();
    }

    private void initViewListener() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initFakeData();
                sendDataToFirebase(getParam());

            }
        });
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
    }

    private void initView() {
        verifyCodeLayout.setVisibility(View.GONE);
    }

    private MapData getParam() {

        ArrayList<ItemData> listItem = new ArrayList<>();
        listItem.add(new ItemData("Barang 6"));
        listItem.add(new ItemData("Barang 7"));

        MapData mapData = new MapData();
        mapData.setAddress(address.getText().toString());
        mapData.setRecipient(recipient.getText().toString());
        mapData.setPhone(phone.getText().toString());
        mapData.setVerifyCode(verifyCode.getText().toString());
        mapData.setItem(listItem);
        mapData.setPosition(new LatLng(-6.131138,106.824011));

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
                finish();
            }

            @Override
            public void onFailed(String error) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void initFakeData() {
        recipient.setText("Nisie");
        address.setText("Jalan 4 no 4");
        phone.setText("08999991149");

        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(999999);
        verifyCode.setText(String.valueOf(n));
        verifyCodeLayout.setVisibility(View.VISIBLE);

    }
}
