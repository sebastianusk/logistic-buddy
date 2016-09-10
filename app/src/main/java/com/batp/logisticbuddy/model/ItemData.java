package com.batp.logisticbuddy.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nisie on 9/10/16.
 */
public class ItemData implements Parcelable{
    String id;

    public ItemData(String id) {
        this.id = id;
    }

    public ItemData(){}

    protected ItemData(Parcel in) {
        id = in.readString();
    }

    public static final Creator<ItemData> CREATOR = new Creator<ItemData>() {
        @Override
        public ItemData createFromParcel(Parcel in) {
            return new ItemData(in);
        }

        @Override
        public ItemData[] newArray(int size) {
            return new ItemData[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
    }
}
