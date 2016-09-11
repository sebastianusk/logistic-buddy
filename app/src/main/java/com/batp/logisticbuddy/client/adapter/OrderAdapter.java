package com.batp.logisticbuddy.client.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.client.OrderDetailActivity;
import com.batp.logisticbuddy.model.MapData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nisie on 9/10/16.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public static final String PARAM_DETAIL = "PARAM_DETAIL";
    private final Context context;
    ArrayList<MapData> listOrder;

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.position)
        TextView position;

        @BindView(R.id.address)
        TextView address;

        @BindView(R.id.order_view)
        View orderView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public OrderAdapter(Context context){
        this.listOrder = new ArrayList<>();
        this.context = context;
    }

    public static OrderAdapter createInstance(Context context) {
        return new OrderAdapter(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_order, null);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.address.setText(listOrder.get(position).getAddress());

        String s = String.format("%02d", position + 1);
        holder.position.setText(s);

        holder.orderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra(PARAM_DETAIL,listOrder.get(position));
                context.startActivity(intent);
            }
        });
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
