package com.batp.logisticbuddy;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.batp.logisticbuddy.client.CreateOrderActivity;
import com.batp.logisticbuddy.driverapplication.DriverMapsActivity;
import com.batp.logisticbuddy.helper.SessionHandler;
import com.batp.logisticbuddy.server.ServerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.btn_start_driving)
    Button startDriving;

    @BindView(R.id.btn_create_order)
    Button createOrder;

    @BindView(R.id.logout)
    Button logout;

    @BindView(R.id.admin)
    Button admin;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ButterKnife.bind(this);

        initializeFirebase();
        initializeMenu(this);

    }

    private void initializeFirebase() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            initView();
        }

    }

    private void initView() {
//        startDriving.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MenuActivity.this, DriverActivity.class));
//            }
//        });
        createOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, CreateOrderActivity.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            }
        });
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, ServerActivity.class));
            }
        });
        startDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, DriverMapsActivity.class));
            }
        });
//        admin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MenuActivity.this, MapActivity.class));
//            }
//        });
    }

    private void initializeMenu(Context context) {
        switch (SessionHandler.getSession(context)){
            case SessionHandler.DRIVER:
                startDriving.setVisibility(View.VISIBLE);
                createOrder.setVisibility(View.GONE);
                break;
            case SessionHandler.CLIENT:
                startDriving.setVisibility(View.GONE);
                createOrder.setVisibility(View.VISIBLE);
                break;
            case SessionHandler.SERVER:
                startDriving.setVisibility(View.GONE);
                createOrder.setVisibility(View.GONE);
                break;
            default:
                startDriving.setVisibility(View.VISIBLE);
                createOrder.setVisibility(View.VISIBLE);
                break;
        }
    }
}
