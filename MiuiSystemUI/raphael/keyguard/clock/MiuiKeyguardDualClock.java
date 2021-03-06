package com.android.keyguard.clock;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import miui.keyguard.clock.MiuiDualClock;
import miui.system.R;

public class MiuiKeyguardDualClock extends MiuiKeyguardBaseClock {
    MiuiDualClock.OnLocalCityChangeListener mLocalCityChangeListener;
    private MiuiDualClock mMiuiDualClock;

    /* access modifiers changed from: protected */
    public void onUserSwitch() {
    }

    public MiuiKeyguardDualClock(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardDualClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLocalCityChangeListener = new MiuiDualClock.OnLocalCityChangeListener() {
            public void onLocalCityChanged(String str) {
                Settings.System.putString(MiuiKeyguardDualClock.this.mContext.getContentResolver(), "local_city", str);
            }
        };
        try {
            this.mMiuiDualClock = this.mLayoutInflater.inflate(R.layout.miui_dual_clock, (ViewGroup) null, false);
            this.mMiuiDualClock.setOnLocalCityChangeListener(this.mLocalCityChangeListener);
        } catch (Exception e) {
            Log.e("MiuiKeyguardDualClock", "init clock exception", e);
        }
        addView(this.mMiuiDualClock);
    }

    public void setDarkMode(boolean z) {
        super.setDarkMode(z);
        this.mMiuiDualClock.setTextColorDark(z);
    }

    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiDualClock miuiDualClock = this.mMiuiDualClock;
        if (miuiDualClock != null) {
            miuiDualClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    /* access modifiers changed from: protected */
    public void updateResidentTimeZone(String str) {
        this.mMiuiDualClock.updateResidentTimeZone(str);
    }

    /* access modifiers changed from: protected */
    public void onClockShowing() {
        MiuiDualClock miuiDualClock = this.mMiuiDualClock;
        if (miuiDualClock != null) {
            miuiDualClock.updateTime();
        }
    }

    /* access modifiers changed from: protected */
    public void updateTimeZone(String str) {
        this.mMiuiDualClock.updateTimeZone(str);
    }

    /* access modifiers changed from: protected */
    public void updateTime() {
        this.mMiuiDualClock.updateTime();
    }
}
