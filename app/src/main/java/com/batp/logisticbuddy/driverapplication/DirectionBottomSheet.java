package com.batp.logisticbuddy.driverapplication;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.widget.TextView;

import com.batp.logisticbuddy.R;

/**
 * Created by kris on 9/11/16. Tokopedia
 */
public class DirectionBottomSheet {
    TextView directionTextView;

    public DirectionBottomSheet (String direction, Context context) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.direction_bottom_sheet_layout);
        directionTextView = (TextView) dialog.findViewById(R.id.direction_text_view);
        if (directionTextView != null) {
            directionTextView.setText(direction);
        }
        dialog.show();
    }
}
