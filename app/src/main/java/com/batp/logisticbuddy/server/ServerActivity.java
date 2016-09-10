package com.batp.logisticbuddy.server;

import android.widget.Button;
import android.widget.Toast;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class ServerActivity extends BaseMapActivity {

    @OnClick(R.id.button_get_clients)
    void getClients(){
        // making sure that the maps is ready
        if(mMap != null){

            FirebaseHandler firebaseHandler = new FirebaseHandler();
            firebaseHandler.initDatabaseReferrence();
            firebaseHandler.receiveOrders();

        } else {
            Toast.makeText(this, getString(R.string.map_not_ready), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        return null;
    }
}
