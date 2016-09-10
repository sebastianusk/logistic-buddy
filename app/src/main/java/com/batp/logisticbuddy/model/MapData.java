package com.batp.logisticbuddy.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nisie on 9/10/16.
 */
@IgnoreExtraProperties
public class MapData implements Parcelable{

    LatLng position;
    String address;
    String recipient;
    String phone;
    ArrayList<ItemData> item;
    String verifyCode;

    protected MapData(Parcel in) {
        position = in.readParcelable(LatLng.class.getClassLoader());
        address = in.readString();
        recipient = in.readString();
        phone = in.readString();
        verifyCode = in.readString();
    }

    public MapData() {
    }

    public static final Creator<MapData> CREATOR = new Creator<MapData>() {
        @Override
        public MapData createFromParcel(Parcel in) {
            return new MapData(in);
        }

        @Override
        public MapData[] newArray(int size) {
            return new MapData[size];
        }
    };

    public LatLng getPosition() {
        return this.position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public ArrayList<ItemData> getItem() {
        return item;
    }

    public void setItem(ArrayList<ItemData> item) {
        this.item = item;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Exclude
    public MarkerOptions getMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(this.getPosition());
        if (this.getRecipient() != null)
            markerOptions.title(this.getRecipient());
        if (this.getAddress() != null)
            markerOptions.snippet(this.getAddress());
        return markerOptions;

    }

    @Exclude
    public static MapData convertFromFirebase(Map<String, Object> mapObj) {
        MapData marker = new MapData();
        marker.setAddress((String) mapObj.get("address"));
        marker.setPhone((String) mapObj.get("phone"));
        marker.setRecipient((String) mapObj.get("recipient"));
        marker.setPosition(converPositionFromFirebase(mapObj));
        marker.setItem(convertItemsFromFirebase(mapObj));
        return marker;
    }

    private static ArrayList<ItemData> convertItemsFromFirebase(Map<String, Object> mapObj) {
        ArrayList<Object> objectMap = (ArrayList<Object>) mapObj.get("item");
        ArrayList<ItemData> list = new ArrayList<>();
        for (Object obj : objectMap) {
            if (obj instanceof Map) {
                Map<String, Object> item = (Map<String, Object>) obj;
                ItemData itemData = new ItemData();
                itemData.setId((String) item.get("id"));
                list.add(itemData);
            }
        }
        return list;
    }

    private static LatLng converPositionFromFirebase(Map<String, Object> mapObj) {
        return new LatLng(
                ((HashMap<String, Double>) mapObj.get("position")).get("latitude")
                , ((HashMap<String, Double>) mapObj.get("position")).get("longitude")
        );
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(position, i);
        parcel.writeString(address);
        parcel.writeString(recipient);
        parcel.writeString(phone);
        parcel.writeString(verifyCode);
    }
}