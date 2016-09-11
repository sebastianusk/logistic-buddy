package com.batp.logisticbuddy.server;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.model.TruckData;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Toped18 on 9/11/2016.
 */
public class DriverStatusAdapter extends RecyclerView.Adapter<DriverStatusAdapter.DriverStatusViewHolder>{


    private final List<Pair<String, String>> truckDataMap;

    public DriverStatusAdapter(List<Pair<String, String>> truckDataMap) {
        this.truckDataMap = truckDataMap;
    }

    @Override
    public DriverStatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_status, parent, false);
        return new DriverStatusViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DriverStatusViewHolder holder, int position) {
        holder.setToView(truckDataMap.get(position).first, truckDataMap.get(position).second);
    }

    @Override
    public int getItemCount() {
        return truckDataMap.size();
    }

    public class DriverStatusViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.truck_name)
        TextView truckName;

        @BindView(R.id.truck_status)
        TextView truckStatus;

        public DriverStatusViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setToView(String truckName, String truckStatus){
            this.truckName.setText(truckName);
            this.truckStatus.setText(truckStatus);
        }
    }

}
