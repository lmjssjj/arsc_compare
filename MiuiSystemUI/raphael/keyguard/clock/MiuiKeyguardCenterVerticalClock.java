package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import miui.keyguard.clock.MiuiCenterHorizontalClock;
import miui.system.R;

public class MiuiKeyguardCenterVerticalClock extends MiuiKeyguardSingleClock {
    private MiuiCenterHorizontalClock mMiuiCenterHorizontalClock;

    public MiuiKeyguardCenterVerticalClock(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardCenterVerticalClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        try {
            this.mMiuiBaseClock = this.mLayoutInflater.inflate(R.layout.miui_vertical_clock, (ViewGroup) null, false);
            updateLunarCalendarInfo();
            this.mMiuiCenterHorizontalClock = this.mLayoutInflater.inflate(R.layout.miui_center_horizontal_clock, (ViewGroup) null, false);
            this.mMiuiCenterHorizontalClock.setShowLunarCalendar(false);
            this.mMiuiCenterHorizontalClock.setAlpha(0.0f);
            this.mMiuiCenterHorizontalClock.setVisibility(8);
        } catch (Exception e) {
            Log.e("MiuiKeyguardCenterVerticalClock", "init clock exception", e);
        }
        this.mClockContainer.addView(this.mMiuiBaseClock);
        this.mClockContainer.addView(this.mMiuiCenterHorizontalClock);
        this.mLockScreenMagazineInfo.updateViewsForClockPosition(false);
    }

    public void setDarkMode(boolean z) {
        super.setDarkMode(z);
        this.mMiuiBaseClock.setTextColorDark(z);
        this.mMiuiCenterHorizontalClock.setTextColorDark(z);
    }

    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutEnd() {
        this.mMiuiBaseClock.setVisibility(8);
        this.mMiuiCenterHorizontalClock.setAlpha(0.0f);
        this.mMiuiCenterHorizontalClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutUpdate(float f) {
        this.mMiuiBaseClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimInUpdate(float f) {
        this.mMiuiCenterHorizontalClock.setAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutEnd() {
        this.mMiuiCenterHorizontalClock.setVisibility(8);
        this.mMiuiBaseClock.setAlpha(0.0f);
        this.mMiuiBaseClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutUpdate(float f) {
        this.mMiuiCenterHorizontalClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimInUpdate(float f) {
        this.mMiuiBaseClock.setAlpha(f);
    }

    public void updateTimeZone(String str) {
        super.updateTimeZone(str);
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.updateTimeZone(str);
        }
    }

    public void updateTime() {
        super.updateTime();
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.updateTime();
        }
    }
}
