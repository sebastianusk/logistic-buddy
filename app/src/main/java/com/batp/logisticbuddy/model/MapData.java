package com.batp.logisticbuddy.model;

import android.location.Location;
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
    String truck;
    String userId;
    String estimatedTime;
    private String key;

    protected MapData(Parcel in) {
        position = in.readParcelable(LatLng.class.getClassLoader());
        address = in.readString();
        recipient = in.readString();
        phone = in.readString();
        item = in.readArrayList(ItemData.class.getClassLoader());
        verifyCode = in.readString();
        truck = in.readString();
        userId = in.readString();
        estimatedTime = in.readString();
        key = in.readString();
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

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
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
        marker.setVerifyCode((String) mapObj.get("verifyCode"));
        marker.setUserId((String) mapObj.get("userId"));
        marker.setKey((String) mapObj.get("key"));
        marker.setTruck((String) mapObj.get("truck"));
        marker.setEstimatedTime((String) mapObj.get("estimatedTime"));
        marker.setPosition(converPositionFromFirebase(mapObj));
        marker.setItem(convertItemsFromFirebase(mapObj));
        return marker;
    }

    private static ArrayList<ItemData> convertItemsFromFirebase(Map<String, Object> mapObj) {
        ArrayList<Object> objectMap = (ArrayList<Object>) mapObj.get("item");
        if(objectMap != null) {
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
        } else {
            return null;
        }
    }

    private static LatLng converPositionFromFirebase(Map<String, Object> mapObj) {
        if(mapObj.get("position") != null) {
            return new LatLng(
                    ((HashMap<String, Double>) mapObj.get("position")).get("latitude")
                    , ((HashMap<String, Double>) mapObj.get("position")).get("longitude")
            );
        } else {
            return null;
        }
    }

    public Location convertToPosition(){
        Location location = new Location("dummy_provider");
        location.setLatitude(position.latitude);
        location.setLongitude(position.longitude);
        return location;
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
        parcel.writeList(item);
        parcel.writeString(verifyCode);
        parcel.writeString(truck);
        parcel.writeString(userId);
        parcel.writeString(estimatedTime);
        parcel.writeString(key);

    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}