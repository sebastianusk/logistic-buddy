package com.batp.logisticbuddy.helper;

import android.support.annotation.NonNull;
import android.util.Log;

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

/**
 * Created by nisie on 9/10/16.
 */
public class FirebaseHandler {
    private static final String ORDER_TABLE = "order";
    private static final String TAG = FirebaseHandler.class.getSimpleName();

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    DatabaseReference mFirebaseDatabaseReference;

    public void initDatabaseReferrence() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void sendOrder(MapData param, final FirebaseListener listener) {
        mFirebaseDatabaseReference.child(ORDER_TABLE)
                .push()
                .setValue(param)
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

    public void receiveOrders(){
        mFirebaseDatabaseReference.child(ORDER_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "data shanpshot is " + dataSnapshot.toString());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

    public void signInWithEmailAndPassword(String userName, String password, final FirebaseListener listener) {
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

    public void initializeAuth(SessionListener listener) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            listener.onAlreadyLogin();
        }
    }
}
