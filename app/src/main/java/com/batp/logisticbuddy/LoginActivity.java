package com.batp.logisticbuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.batp.logisticbuddy.helper.FirebaseHandler;
import com.batp.logisticbuddy.helper.SessionHandler;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.btn_login)
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        FirebaseHandler.initializeAuth(new FirebaseHandler.SessionListener() {
            @Override
            public void onAlreadyLogin() {
                startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
                dialog.setTitle("Please wait...");
                dialog.show();
                username.setText("master@batp.com");
                password.setText("master");
                checkRole();
                FirebaseHandler.signInWithEmailAndPassword(username.getText().toString(),
                        password.getText().toString(), new FirebaseHandler.FirebaseListener() {
                            @Override
                            public void onSuccess() {
                                dialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailed(String error) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private void checkRole() {
        switch (username.getText().toString()){
            case SessionHandler.DRIVER:
                SessionHandler.setSession(this, SessionHandler.DRIVER);
                break;
            case SessionHandler.CLIENT:
                SessionHandler.setSession(this, SessionHandler.CLIENT);
                break;
            case SessionHandler.SERVER:
                SessionHandler.setSession(this,SessionHandler.SERVER);
                break;
            default:
                SessionHandler.setSession(this, SessionHandler.MASTER);
                break;
        }
    }
}
