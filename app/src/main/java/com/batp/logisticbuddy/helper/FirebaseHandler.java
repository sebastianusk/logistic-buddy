package com.batp.logisticbuddy.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.batp.logisticbuddy.model.AccidentData;
import com.batp.logisticbuddy.model.TruckData;
import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private static final String CLIENT_TABLE = "client";

    private static final String TAG = FirebaseHandler.class.getSimpleName();
    public static final String BASE_MAP = "base";
    private static final String TRUCK = "truck";

    public static void sendOrder(final MapData param, final FirebaseListener listener) {
        final DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference orderReferrence = mFirebaseDatabaseReference.child(ORDER_TABLE);
        DatabaseReference blankRecordReferrence = orderReferrence.push();
        final String key = blankRecordReferrence.getKey();
        param.setKey(key);
        blankRecordReferrence
                .setValue(param)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mFirebaseDatabaseReference.child(CLIENT_TABLE).child(getCurrentSessionUserId()).child(key)
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

    public void getOrderLocation(final GetDriverDesignatedLocations listener, Context context) {
        DatabaseReference mFireBaseDataReference;
        mFireBaseDataReference = FirebaseDatabase.getInstance().getReference();
        mFireBaseDataReference.child("truck").child(SessionHandler.getCurrentDriver(context)).child("destinations")
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

    public static void updateOrder(final MapData param, final FirebaseListener listener) {

        if (param.getUserId() != null && param.getKey() != null) {
            final DatabaseReference mFirebaseDatabaseReference;
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseDatabaseReference.child(CLIENT_TABLE)
                    .child(param.getUserId())
                    .child(param.getKey())
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
    }

    public static void getBaseLocation(final GetOrderListener listener) {
        if (listener == null)
            return;
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(BASE_MAP)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());
                        MapData base = new MapData();
                        if (dataSnapshot.getValue() instanceof Map) {
                            Map<String, Object> mapObj = (Map<String, Object>) dataSnapshot.getValue();
                            base = MapData.convertFromFirebase(mapObj);
                        }
                        listener.onSuccess(base);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onFailed(databaseError.getMessage());
                    }
                });

    }

    public void receiveOrders(final GetOrdersListener listener) {
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(ORDER_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());
                        Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
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

    public void storeRoute(final Map<String, TruckData> truckDatas, final FirebaseListener listener) {

        for (int i = 0; i < truckDatas.size(); i++) {
            DatabaseReference mFirebaseDatabaseReference;
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseDatabaseReference
                    .child(TRUCK)
                    .setValue(truckDatas)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            listener.onSuccess();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onFailed(e.getMessage());
                }
            });
        }

//        return Observable.from(truckDatas.entrySet())
//                .flatMap(new Func1<Map.Entry<String, TruckData>, Observable<String>>() {
//                    @Override
//                    public Observable<String> call(final Map.Entry<String, TruckData> stringTruckDataEntry) {
//                        return Observable.create(new Observable.OnSubscribe<String>() {
//                            @Override
//                            public void call(final Subscriber<? super String> subscriber) {
//                                DatabaseReference mFirebaseDatabaseReference;
//                                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
//                                mFirebaseDatabaseReference.child(stringTruckDataEntry.getKey())
//                                        .setValue(stringTruckDataEntry.getValue())
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                subscriber.onNext("OK");
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        subscriber.onError(e);
//                                    }
//                                });
//                            }
//                        });
//                    }
//                })
//                .toList()
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<List<String>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(List<String> strings) {
//
//                    }
//                });
    }

    public Subscription updateOrders(List<MapData> orders, final FirebaseListener listener) {


        return Observable.from(orders)
                .flatMap(new Func1<MapData, Observable<String>>() {
                    @Override
                    public Observable<String> call(final MapData mapData) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(final Subscriber<? super String> subscriber) {
                                updateOrder(mapData, new FirebaseListener() {
                                    @Override
                                    public void onSuccess() {
                                        subscriber.onNext("OK");
                                    }

                                    @Override
                                    public void onFailed(String error) {
                                        subscriber.onError(new Throwable(error));
                                    }
                                });
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onFailed(e.getMessage());
                    }

                    @Override
                    public void onNext(String strings) {
                        listener.onSuccess();
                    }
                });
    }

    public static void storeBaseMap(MapData mapData, final FirebaseListener listener) {
        if (listener == null) {
            return;
        }

        final DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(BASE_MAP)
                .setValue(mapData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailed(e.toString());
            }
        });

    }

    public static String getCurrentSessionUserId() {
        FirebaseAuth mFirebaseAuth;
        FirebaseUser mFirebaseUser;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        return mFirebaseUser != null ? "user" + mFirebaseUser.getUid() : "";
    }

    public static String getCurrentSessionDriverId() {
        FirebaseAuth mFirebaseAuth;
        FirebaseUser mFirebaseUser;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        return mFirebaseUser != null ? "truck" + mFirebaseUser.getUid() : "";
    }

    public static void getOrderClient(String userId, final GetOrdersListener listener) {
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(CLIENT_TABLE).child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());
                        Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
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

    public static void getTruckStatus(String truck, final DriverStatusListener listener) {
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(TRUCK).child(truck)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());
                        Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();

                        listener.onSuccess(String.valueOf(objectMap.get("status")),
                                String.valueOf(objectMap.get("last_update_time")));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void updateStatus(String s, final Context context, final FirebaseListener listener) {
        if (listener == null) {
            return;
        }
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference
                .child(TRUCK)
                .child(SessionHandler.getCurrentDriver(context))
                .child("status")
                .setValue(s)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm");

                        DatabaseReference mFirebaseDatabaseReference;
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                        mFirebaseDatabaseReference
                                .child(TRUCK)
                                .child(SessionHandler.getCurrentDriver(context))
                                .child("last_update_time")
                                .setValue(sdf.format(new Date()))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.onSuccess();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onFailed(e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailed(e.getMessage());
            }
        });
    }

    public static void onAccident(AccidentData accidentData, Context context, final FirebaseListener listener) {
        if(listener == null)
            return;
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference
                .child("accidents")
                .child(SessionHandler.getCurrentDriver(context))
                .push()
                .setValue(accidentData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailed(e.getMessage());
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

    public interface DriverStatusListener {
        void onSuccess(String status, String lastUpdateTime);

        void onFailed(String error);
    }

    public interface GetOrdersListener {
        void onSuccess(List<MapData> mapData);

        void onFailed(String error);
    }

    public interface GetOrderListener {
        void onSuccess(MapData mapData);

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
