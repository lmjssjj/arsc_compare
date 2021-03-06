package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.widget.Switch;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import miui.os.DeviceFeature;

public class AutoBrightnessTile extends QSTileImpl<QSTile.BooleanState> {
    private static final boolean SUPPORT_AUTO_BRIGHTNESS_OPTIMIZE = DeviceFeature.SUPPORT_AUTO_BRIGHTNESS_OPTIMIZE;
    private boolean mAutoBrightnessAvailable = this.mResource.getBoolean(285474817);
    private boolean mAutoBrightnessMode;
    private ContentObserver mAutoBrightnessObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            AutoBrightnessTile.this.refreshState();
        }
    };
    private IBinder mBinder = ServiceManager.getService("display");
    private int mCurrentUserId = ActivityManager.getCurrentUser();
    private final ContentResolver mResolver = this.mContext.getContentResolver();
    private final Resources mResource = this.mContext.getResources();

    public int getMetricsCategory() {
        return -1;
    }

    public AutoBrightnessTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            if (!SUPPORT_AUTO_BRIGHTNESS_OPTIMIZE) {
                this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this.mAutoBrightnessObserver, this.mCurrentUserId);
                this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this.mAutoBrightnessObserver, this.mCurrentUserId);
            }
            this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, this.mAutoBrightnessObserver, this.mCurrentUserId);
            return;
        }
        this.mResolver.unregisterContentObserver(this.mAutoBrightnessObserver);
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        this.mCurrentUserId = i;
    }

    public Intent getLongClickIntent() {
        return longClickAutoBrightnessIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (this.mAutoBrightnessMode) {
            this.mAutoBrightnessMode = false;
            if (Build.VERSION.SDK_INT > 27) {
                resetAutoBrightnessShortModel();
            }
        } else {
            this.mAutoBrightnessMode = this.mAutoBrightnessAvailable;
        }
        String str = this.TAG;
        Log.d(str, "handleClick: from: " + ((QSTile.BooleanState) this.mState).value + ", to: " + this.mAutoBrightnessMode);
        ContentResolver contentResolver = this.mResolver;
        boolean z = this.mAutoBrightnessMode;
        Settings.System.putIntForUser(contentResolver, "screen_brightness_mode", z ? 1 : 0, this.mCurrentUserId);
        if (this.mInControlCenter) {
            this.mUiHandler.post(new ClickRunnable(this.mAutoBrightnessMode));
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_autobrightness_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        queryAutoBrightnessStatus();
        booleanState.value = this.mAutoBrightnessMode;
        booleanState.label = this.mContext.getString(R.string.quick_settings_autobrightness_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_brightness_auto), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_brightness_manual), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.activeBgColor = 2;
    }

    private Intent longClickAutoBrightnessIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.display.BrightnessActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }

    private void queryAutoBrightnessStatus() {
        boolean z = false;
        if (this.mAutoBrightnessAvailable && 1 == Settings.System.getIntForUser(this.mResolver, "screen_brightness_mode", 0, this.mCurrentUserId)) {
            z = true;
        }
        this.mAutoBrightnessMode = z;
    }

    private void resetAutoBrightnessShortModel() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("android.view.android.hardware.display.IDisplayManager");
            this.mBinder.transact(16777214, obtain, obtain2, 0);
        } catch (RemoteException e) {
            Slog.d(this.TAG, "RemoteException!", e);
        } catch (Throwable th) {
            obtain2.recycle();
            obtain.recycle();
            throw th;
        }
        obtain2.recycle();
        obtain.recycle();
    }

    protected class ClickRunnable implements Runnable {
        private boolean enabled;

        ClickRunnable(boolean z) {
            this.enabled = z;
        }

        public void run() {
            Util.showSystemOverlayToast(AutoBrightnessTile.this.mContext, this.enabled ? R.string.auto_brightness_on : R.string.auto_brightness_off, 0);
        }
    }
}
