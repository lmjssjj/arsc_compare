package com.android.keyguard.clock;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import java.util.TimeZone;

public class KeyguardClockContainer extends FrameLayout {
    ContentObserver mClockPositionObserver;
    /* access modifiers changed from: private */
    public MiuiKeyguardBaseClock mClockView;
    /* access modifiers changed from: private */
    public String mCurrentTimezone;
    /* access modifiers changed from: private */
    public boolean mDualClockOpen;
    ContentObserver mDualClockOpenObserver;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private int mLastSelectedClockPosition;
    /* access modifiers changed from: private */
    public String mResidentTimezone;
    ContentObserver mResidentTimezoneObserver;
    /* access modifiers changed from: private */
    public int mSelectedClockPosition;
    private boolean mShowDualClock;
    /* access modifiers changed from: private */
    public boolean mShowVerticalClock;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public Runnable mUpdateTimeRunnable;

    public KeyguardClockContainer(Context context) {
        this(context, (AttributeSet) null, 0, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardClockContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mCurrentTimezone = TimeZone.getDefault().getID();
        boolean z = false;
        this.mDualClockOpen = false;
        this.mShowDualClock = false;
        this.mSelectedClockPosition = 0;
        this.mLastSelectedClockPosition = 0;
        this.mHandler = new Handler();
        this.mUpdateTimeRunnable = new Runnable() {
            public void run() {
                if (KeyguardClockContainer.this.mClockView != null) {
                    KeyguardClockContainer.this.mClockView.updateTime();
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                    KeyguardClockContainer.this.mHandler.post(new Runnable() {
                        public void run() {
                            String unused = KeyguardClockContainer.this.mCurrentTimezone = TimeZone.getDefault().getID();
                            KeyguardClockContainer.this.updateKeyguardClock();
                        }
                    });
                } else {
                    KeyguardClockContainer.this.mHandler.post(KeyguardClockContainer.this.mUpdateTimeRunnable);
                }
            }
        };
        this.mDualClockOpenObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = keyguardClockContainer.mContext.getContentResolver();
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                boolean z2 = false;
                if (Settings.System.getIntForUser(contentResolver, "auto_dual_clock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                    z2 = true;
                }
                boolean unused2 = keyguardClockContainer.mDualClockOpen = z2;
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mResidentTimezoneObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = keyguardClockContainer.mContext.getContentResolver();
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                String unused2 = keyguardClockContainer.mResidentTimezone = Settings.System.getStringForUser(contentResolver, "resident_timezone", KeyguardUpdateMonitor.getCurrentUser());
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mClockPositionObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardClockContainer keyguardClockContainer = KeyguardClockContainer.this;
                ContentResolver contentResolver = keyguardClockContainer.mContext.getContentResolver();
                int defaultKeyguardClockPosition = MiuiKeyguardUtils.getDefaultKeyguardClockPosition(KeyguardClockContainer.this.mContext);
                KeyguardUpdateMonitor unused = KeyguardClockContainer.this.mUpdateMonitor;
                int unused2 = keyguardClockContainer.mSelectedClockPosition = Settings.System.getIntForUser(contentResolver, "selected_keyguard_clock_position", defaultKeyguardClockPosition, KeyguardUpdateMonitor.getCurrentUser());
                KeyguardClockContainer keyguardClockContainer2 = KeyguardClockContainer.this;
                boolean unused3 = keyguardClockContainer2.mShowVerticalClock = MiuiKeyguardUtils.isSupportVerticalClock(keyguardClockContainer2.mSelectedClockPosition, KeyguardClockContainer.this.mContext);
                KeyguardClockContainer.this.updateKeyguardClock();
            }
        };
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mSelectedClockPosition = Settings.System.getIntForUser(this.mContext.getContentResolver(), "selected_keyguard_clock_position", MiuiKeyguardUtils.getDefaultKeyguardClockPosition(this.mContext), KeyguardUpdateMonitor.getCurrentUser());
        this.mDualClockOpen = Settings.System.getIntForUser(this.mContext.getContentResolver(), "auto_dual_clock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0;
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "resident_timezone", KeyguardUpdateMonitor.getCurrentUser());
        this.mResidentTimezone = stringForUser;
        if (this.mDualClockOpen && stringForUser != null && !stringForUser.equals(this.mCurrentTimezone)) {
            z = true;
        }
        this.mShowDualClock = z;
        MiuiKeyguardUtils.isSupportVerticalClock(this.mSelectedClockPosition, this.mContext);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        addClockView();
        updateKeyguardClock();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        registerDualClockObserver();
        registerClockPositionObserver();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        unregisterDualClockObserver();
        unregisterClockPositionObserver();
    }

    private void registerDualClockObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("auto_dual_clock"), false, this.mDualClockOpenObserver, -1);
        this.mDualClockOpenObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("resident_timezone"), false, this.mResidentTimezoneObserver, -1);
        this.mResidentTimezoneObserver.onChange(false);
    }

    private void unregisterDualClockObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mDualClockOpenObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mResidentTimezoneObserver);
    }

    private void registerClockPositionObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("selected_keyguard_clock_position"), false, this.mClockPositionObserver, -1);
        this.mClockPositionObserver.onChange(false);
    }

    private void unregisterClockPositionObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mClockPositionObserver);
    }

    private void addClockView() {
        MiuiKeyguardBaseClock miuiKeyguardBaseClock;
        LayoutInflater.from(this.mContext);
        if (this.mShowDualClock) {
            miuiKeyguardBaseClock = new MiuiKeyguardDualClock(this.mContext);
        } else {
            int i = this.mSelectedClockPosition;
            if (i == 2) {
                miuiKeyguardBaseClock = new MiuiKeyguardLeftTopClock(this.mContext);
            } else if (i != 3) {
                miuiKeyguardBaseClock = i != 4 ? new MiuiKeyguardCenterHorizontalClock(this.mContext) : new MiuiKeyguardLeftTopLargeClock(this.mContext);
            } else {
                miuiKeyguardBaseClock = new MiuiKeyguardCenterVerticalClock(this.mContext);
            }
        }
        addView(miuiKeyguardBaseClock);
        this.mClockView = miuiKeyguardBaseClock;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = r3.mResidentTimezone;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateKeyguardClock() {
        /*
            r3 = this;
            boolean r0 = r3.mDualClockOpen
            if (r0 == 0) goto L_0x0012
            java.lang.String r0 = r3.mResidentTimezone
            if (r0 == 0) goto L_0x0012
            java.lang.String r1 = r3.mCurrentTimezone
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x0012
            r0 = 1
            goto L_0x0013
        L_0x0012:
            r0 = 0
        L_0x0013:
            boolean r1 = r3.mShowDualClock
            if (r1 != r0) goto L_0x001d
            int r1 = r3.mSelectedClockPosition
            int r2 = r3.mLastSelectedClockPosition
            if (r1 == r2) goto L_0x0029
        L_0x001d:
            r3.mShowDualClock = r0
            int r0 = r3.mSelectedClockPosition
            r3.mLastSelectedClockPosition = r0
            r3.removeAllViews()
            r3.addClockView()
        L_0x0029:
            com.android.keyguard.clock.MiuiKeyguardBaseClock r0 = r3.mClockView
            if (r0 == 0) goto L_0x0040
            java.lang.String r1 = r3.mResidentTimezone
            r0.updateResidentTimeZone(r1)
            com.android.keyguard.clock.MiuiKeyguardBaseClock r0 = r3.mClockView
            java.lang.String r1 = r3.mCurrentTimezone
            r0.updateTimeZone(r1)
            com.android.keyguard.clock.MiuiKeyguardBaseClock r0 = r3.mClockView
            int r3 = r3.mSelectedClockPosition
            r0.setSelectedClockPosition(r3)
        L_0x0040:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.clock.KeyguardClockContainer.updateKeyguardClock():void");
    }

    public void setDarkMode(boolean z) {
        this.mClockView.setDarkMode(z);
    }

    public void updateClockView(boolean z, boolean z2) {
        this.mClockView.updateClockView(z, z2);
    }

    public void updateTime() {
        this.mClockView.updateTime();
    }

    public int getClockHeight() {
        return this.mClockView.getClockHeight();
    }

    public float getClockVisibleHeight() {
        return this.mClockView.getClockVisibleHeight();
    }

    public void onUserChanged() {
        this.mDualClockOpenObserver.onChange(false);
        this.mResidentTimezoneObserver.onChange(false);
        this.mClockPositionObserver.onChange(false);
    }

    public void setClockAlpha(float f) {
        this.mClockView.setClockAlpha(f);
    }

    public void updateLockScreenMagazineInfo() {
        this.mClockView.updateLockScreenMagazineInfo();
    }
}
