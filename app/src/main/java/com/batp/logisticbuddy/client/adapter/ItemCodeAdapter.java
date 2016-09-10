package com.batp.logisticbuddy.client.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.batp.logisticbuddy.R;
import com.batp.logisticbuddy.model.ItemData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nisie on 9/10/16.
 */
public class ItemCodeAdapter extends RecyclerView.Adapter<ItemCodeAdapter.ViewHolder> {

    ArrayList<ItemData> listItem;

    public ItemCodeAdapter(){
        this.listItem = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_item, null);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.itemCode.setText(listItem.get(position).getId());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItem.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static ItemCodeAdapter createInstance() {
        return new ItemCodeAdapter();
    }

    public void addItem(ItemData itemData) {
        this.listItem.add(itemData);
        notifyDataSetChanged();
    }

    public ArrayList<ItemData> getList() {
        return listItem;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.item_code)
        TextView itemCode;

        @BindView(R.id.delete_button)
        ImageView deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
