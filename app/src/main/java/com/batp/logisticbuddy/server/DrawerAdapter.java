package com.batp.logisticbuddy.server;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.batp.logisticbuddy.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Toped18 on 9/11/2016.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder>{

    public static final int SET_BASE = 0;
    public static final int GET_CLIENT = 1;
    public static final int CALCULATE_ROUTE = 2;
    private final DrawerListener listener;

    public DrawerAdapter(DrawerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        String title;
        int drawableIcon;
        switch (position){
            case SET_BASE:
                drawableIcon = R.drawable.seqhack_map;
                title = "Set Base";
                break;
            case GET_CLIENT:
                drawableIcon = R.drawable.seqhack_get_client;
                title = "Get Client";
                break;
            case CALCULATE_ROUTE:
                drawableIcon = R.drawable.seqhack_calculate_route;
                title = "Calculate Route";
                break;
            default:
                drawableIcon = R.drawable.seqhack_map;
                title = "Set Base";
        }
        holder.setView(drawableIcon, title, position);
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_view, parent, false);
        return new DrawerViewHolder(v, listener);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class DrawerViewHolder extends RecyclerView.ViewHolder {
        private final DrawerListener listener;
        @BindView(R.id.icon_image)
        ImageView iconImage;

        @BindView(R.id.title_drawer)
        TextView titleDrawer;

        public DrawerViewHolder(View itemView, DrawerListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        public void setView(int drawable, String title, final int position){
            iconImage.setImageResource(drawable);
            titleDrawer.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    interface DrawerListener{
        void onItemClick(int type);
    }
}
