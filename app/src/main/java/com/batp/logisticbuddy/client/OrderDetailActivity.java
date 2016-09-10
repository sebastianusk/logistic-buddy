package com.batp.logisticbuddy.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.client.adapter.OrderAdapter;
import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.helper.SessionHandler;
import com.batp.logisticbuddy.model.MapData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.list_order)
    RecyclerView listOrder;

    OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initData() {
        FirebaseHandler.getOrderClient(SessionHandler.getUserId(this), new FirebaseHandler.GetOrdersListener() {
            @Override
            public void onSuccess(List<MapData> mapData) {
                orderAdapter.setList(mapData);
            }

            @Override
            public void onFailed(String error) {

            }
        });
    }

    private void initView() {
        orderAdapter = OrderAdapter.createInstance();
        listOrder.setLayoutManager(new LinearLayoutManager(this));
        listOrder.setAdapter(orderAdapter);
    }
}
