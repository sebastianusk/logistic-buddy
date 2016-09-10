package com.batp.logisticbuddy.server;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.map.BaseMapActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class ServerActivity extends BaseMapActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }

    @Override
    protected UiSettings setMapUISetting(GoogleMap googleMap) {
        return null;
    }
}
