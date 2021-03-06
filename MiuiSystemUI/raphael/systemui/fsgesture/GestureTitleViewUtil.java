package com.android.systemui.fsgesture;

import android.content.Context;
import android.widget.RelativeLayout;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;

public class GestureTitleViewUtil {
    public static void setMargin(Context context, FsGestureDemoTitleView fsGestureDemoTitleView) {
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.gesture_title_view_margin_left_right_radius);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fsGestureDemoTitleView.getLayoutParams();
        if (Constants.IS_NOTCH) {
            layoutParams.setMargins(dimensionPixelSize, context.getResources().getDimensionPixelSize(R.dimen.status_bar_height) + context.getResources().getDimensionPixelSize(R.dimen.gesture_title_view_notch_top), dimensionPixelSize, 0);
        } else {
            layoutParams.setMargins(dimensionPixelSize, context.getResources().getDimensionPixelSize(R.dimen.gesture_title_view_margin_top), dimensionPixelSize, 0);
        }
        fsGestureDemoTitleView.setLayoutParams(layoutParams);
    }
}
