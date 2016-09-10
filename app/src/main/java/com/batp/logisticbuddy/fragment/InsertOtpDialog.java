package com.batp.logisticbuddy.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.batp.logisticbuddy.R;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public class InsertOtpDialog extends DialogFragment{

    private static final String OTP_CODE_KEY = "otp_code_key";

    private EditText otpHolder;

    private TextView confirmOtpButton;

    public interface OtpListener {
        void onOtpDone ();
    }

    public static InsertOtpDialog createInstance(String otpCode) {
        InsertOtpDialog dialog = new InsertOtpDialog();
        Bundle bundle = new Bundle();
        bundle.putString(OTP_CODE_KEY, otpCode);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.insert_otp, container, false);
        otpHolder = (EditText) view.findViewById(R.id.otp_holder);
        confirmOtpButton = (TextView) view.findViewById(R.id.confirm_otp_button);
        confirmOtpButton.setOnClickListener(onConfirmOTPClickedListener());
        return view;
    }

    private View.OnClickListener onConfirmOTPClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getArguments().getString(OTP_CODE_KEY).equals(otpHolder.getText().toString())){
                    ((OtpListener) getActivity()).onOtpDone();
                }
            }
        };
    }


}
