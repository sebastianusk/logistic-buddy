package com.batp.logisticbuddy.helper;

import android.support.annotation.NonNull;

import com.batp.logisticbuddy.model.MapData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by nisie on 9/10/16.
 */
public class FirebaseHandler {
    private static final String ORDER_TABLE = "order";

    public static void sendOrder(MapData param, final FirebaseListener listener) {
        DatabaseReference mFirebaseDatabaseReference;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
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

    public interface SessionListener {
        void onAlreadyLogin();
    }

    public interface FirebaseListener {
        void onSuccess();

        void onFailed(String error);
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
