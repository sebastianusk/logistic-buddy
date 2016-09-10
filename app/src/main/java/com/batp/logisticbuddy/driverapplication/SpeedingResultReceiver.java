package com.batp.logisticbuddy.driverapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
@SuppressLint("ParcelCreator")

public class SpeedingResultReceiver extends ResultReceiver{
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */

    public static int SPEEDING_RESULT_CODE = 10;

    private Receiver receiver;

    public SpeedingResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(receiver !=null) {
            receiver.onReceiveResult(SPEEDING_RESULT_CODE, resultData);
        }
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle results);
    }
}
