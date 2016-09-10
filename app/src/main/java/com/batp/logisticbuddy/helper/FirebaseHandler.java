package com.batp.logisticbuddy.helper;

import android.support.annotation.NonNull;
import android.util.Log;

import com.batp.logisticbuddy.model.TruckData;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by nisie on 9/10/16.
 */
public class FirebaseHandler {
    private static final String ORDER_TABLE = "order";
    private static final String DRIVER_TABLE = "driver";
    private static final String TAG = FirebaseHandler.class.getSimpleName();

    public static void sendOrder(final MapData param, final FirebaseListener listener) {
        final DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(ORDER_TABLE)
                .push()
                .setValue(param)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mFirebaseDatabaseReference.child(param.getRecipient()).push()
                                .setValue(param)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.onSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        listener.onFailed(e.toString());
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailed(e.toString());
            }
        });
    }

    public void getOrderLocation(final GetDriverDesignatedLocations listener) {
        DatabaseReference mFireBaseDataReference;
        mFireBaseDataReference = FirebaseDatabase.getInstance().getReference();
        mFireBaseDataReference.child("truck100").child("destinations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<MapData> mapDataList = (List<MapData>) dataSnapshot.getValue();
                        listener.onSuccessList(mapDataList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void receiveOrders(final GetOrdersListener listener){
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(ORDER_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());
                        Map<String, Object> objectMap = (Map <String, Object>) dataSnapshot.getValue();
                        List<MapData> mapDataList = new ArrayList<MapData>();
                        for (Object obj : objectMap.values()) {
                            if (obj instanceof Map) {
                                Map<String, Object> mapObj = (Map<String, Object>) obj;
                                mapDataList.add(MapData.convertFromFirebase(mapObj));
                            }
                        }
                        listener.onSuccess(mapDataList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public Subscription storeRoute(final Map<String, TruckData> truckDatas, final FirebaseListener listener) {

        return Observable.from(truckDatas.entrySet())
                .flatMap(new Func1<Map.Entry<String, TruckData>, Observable<String>>() {
                    @Override
                    public Observable<String> call(final Map.Entry<String, TruckData> stringTruckDataEntry) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(final Subscriber<? super String> subscriber) {
                                DatabaseReference mFirebaseDatabaseReference;
                                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                                mFirebaseDatabaseReference.child(stringTruckDataEntry.getKey())
                                        .setValue(stringTruckDataEntry.getValue())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                subscriber.onNext("OK");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        subscriber.onError(e);
                                    }
                                });
                            }
                        });
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<String> strings) {

                    }
                });
    }

    public void updateOrders(List<MapData> orders, FirebaseListener listener) {
        Observable.from(orders)
                .flatMap(new Func1<MapData, Observable<String>>() {
                    @Override
                    public Observable<String> call(MapData mapData) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {

                            }
                        });
                    }
                })
        .toList()
        .subscribeOn(Schedulers.io())
        .unsubscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<String> strings) {

            }
        });
    }

    public interface SessionListener {
        void onAlreadyLogin();
    }

    public interface FirebaseListener {
        void onSuccess();

        void onFailed(String error);
    }

    public interface GetOrdersListener {
        void onSuccess(List<MapData> mapData);

        void onFailed(String error);
    }

    public interface GetDriverDesignatedLocations {
        void onSuccessList(List<MapData> mapDatas);

        void onFailed();
    }

    public static void signInWithEmailAndPassword(String userName, String password, final FirebaseListener listener) {

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseAuth.signInWithEmailAndPassword(userName, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailed(e.toString());
            }
        });
    }

    public static void initializeAuth(SessionListener listener) {

        FirebaseAuth mFirebaseAuth;
        FirebaseUser mFirebaseUser;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            listener.onAlreadyLogin();
        }
    }
}
