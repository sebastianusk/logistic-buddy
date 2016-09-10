package com.batp.logisticbuddy.client.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.model.MapData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nisie on 9/10/16.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    ArrayList<MapData> listOrder;

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.delivery_status)
        TextView deliveryStatus;

        @BindView(R.id.address)
        TextView address;

        @BindView(R.id.phone)
        TextView phone;

        @BindView(R.id.expected_time_arrive)
        TextView expectedTimeArrive;

        @BindView(R.id.verify_code)
        TextView verifyCode;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public OrderAdapter(){
        this.listOrder = new ArrayList<>();
    }

    public static OrderAdapter createInstance() {
        return new OrderAdapter();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_order, null);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.address.setText(listOrder.get(position).getAddress());
        holder.phone.setText(listOrder.get(position).getPhone());
        holder.verifyCode.setText(listOrder.get(position).getVerifyCode());
        holder.deliveryStatus.setText(listOrder.get(position).getTruck());
        holder.expectedTimeArrive.setText(listOrder.get(position).getTruck());
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }

    public void setList(List<MapData> mapData) {
        this.listOrder.clear();
        listOrder.addAll(mapData);
        notifyDataSetChanged();
    }


}
