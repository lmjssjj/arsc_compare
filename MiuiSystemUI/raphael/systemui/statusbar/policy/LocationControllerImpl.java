package com.android.systemui.statusbar.policy;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.qs.tiles.TilesHelper;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LocationControllerImpl extends BroadcastReceiver implements LocationController {
    private static final int[] mHighPowerRequestAppOpArray = {42};
    private AppOpsManager mAppOpsManager;
    /* access modifiers changed from: private */
    public boolean mAreActiveLocationRequests;
    private Context mContext;
    private final H mHandler = new H();
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public ArrayList<LocationController.LocationChangeCallback> mSettingsChangeCallbacks = new ArrayList<>();
    private StatusBarManager mStatusBarManager;

    public LocationControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
        intentFilter.addAction("android.location.GPS_FIX_CHANGE");
        intentFilter.addAction("android.location.HIGH_POWER_REQUEST_CHANGE");
        intentFilter.addAction("android.location.MODE_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, new Handler(looper));
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotificationManager.cancelAsUser((String) null, 252119, UserHandle.CURRENT);
        updateActiveLocationRequests();
    }

    public void addCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mSettingsChangeCallbacks.add(locationChangeCallback);
        this.mHandler.sendEmptyMessage(1);
    }

    public void removeCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mSettingsChangeCallbacks.remove(locationChangeCallback);
    }

    public boolean setLocationEnabled(boolean z) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (isUserLocationRestricted(currentUser)) {
            return false;
        }
        return TilesHelper.updateLocationEnabled(this.mContext, z, currentUser);
    }

    public boolean isLocationEnabled() {
        return TilesHelper.isLocationEnabled(this.mContext, KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isLocationActive() {
        return this.mAreActiveLocationRequests;
    }

    private boolean isUserLocationRestricted(int i) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_share_location", UserHandleCompat.of(i));
    }

    /* access modifiers changed from: protected */
    public boolean areActiveHighPowerLocationRequests() {
        List packagesForOps = this.mAppOpsManager.getPackagesForOps(mHighPowerRequestAppOpArray);
        if (packagesForOps != null) {
            int size = packagesForOps.size();
            for (int i = 0; i < size; i++) {
                List ops = ((AppOpsManager.PackageOps) packagesForOps.get(i)).getOps();
                if (ops != null) {
                    int size2 = ops.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) ops.get(i2);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }

    private void updateActiveLocationRequests() {
        boolean z = this.mAreActiveLocationRequests;
        this.mAreActiveLocationRequests = areActiveHighPowerLocationRequests();
        if (this.mAreActiveLocationRequests != z) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void updateLocationStatus(Intent intent) {
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = 3;
        obtainMessage.obj = intent;
        this.mHandler.sendMessage(obtainMessage);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
            updateActiveLocationRequests();
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            this.mHandler.sendEmptyMessage(1);
        } else if ("android.location.GPS_ENABLED_CHANGE".equals(action) || "android.location.GPS_FIX_CHANGE".equals(action)) {
            updateLocationStatus(intent);
            updateGpsNotification(intent);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ca  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateGpsNotification(android.content.Intent r15) {
        /*
            r14 = this;
            java.lang.String r0 = r15.getAction()
            r1 = 0
            java.lang.String r2 = "enabled"
            boolean r2 = r15.getBooleanExtra(r2, r1)
            java.lang.String r3 = "android.location.GPS_FIX_CHANGE"
            boolean r3 = r0.equals(r3)
            r4 = 1
            if (r3 == 0) goto L_0x0020
            if (r2 == 0) goto L_0x0020
            r0 = 285671445(0x11070015, float:1.0649647E-28)
            r2 = 2131821294(0x7f1102ee, float:1.9275327E38)
            r3 = r2
            r2 = r0
            r0 = r1
            goto L_0x0040
        L_0x0020:
            java.lang.String r3 = "android.location.GPS_ENABLED_CHANGE"
            boolean r0 = r0.equals(r3)
            if (r0 == 0) goto L_0x002f
            if (r2 != 0) goto L_0x002f
            r0 = r1
            r2 = r0
            r3 = r2
            r4 = r3
            goto L_0x0040
        L_0x002f:
            boolean r0 = com.android.systemui.Constants.SUPPORT_DUAL_GPS
            if (r0 == 0) goto L_0x0037
            r0 = 2131233343(0x7f080a3f, float:1.808282E38)
            goto L_0x003a
        L_0x0037:
            r0 = 2131233351(0x7f080a47, float:1.8082837E38)
        L_0x003a:
            r2 = 2131821295(0x7f1102ef, float:1.927533E38)
            r3 = r2
            r2 = r0
            r0 = r4
        L_0x0040:
            r5 = 252119(0x3d8d7, float:3.53294E-40)
            r6 = 0
            if (r4 == 0) goto L_0x00f4
            java.lang.String r4 = "android.intent.extra.PACKAGES"
            java.lang.String r15 = r15.getStringExtra(r4)
            boolean r4 = android.text.TextUtils.isEmpty(r15)
            if (r4 == 0) goto L_0x0053
            return
        L_0x0053:
            android.content.Context r4 = r14.mContext
            android.content.pm.PackageManager r4 = r4.getPackageManager()
            android.content.pm.ApplicationInfo r7 = r4.getApplicationInfo(r15, r1)     // Catch:{ NameNotFoundException -> 0x0076 }
            java.lang.CharSequence r4 = r7.loadLabel(r4)     // Catch:{ NameNotFoundException -> 0x0076 }
            java.lang.Class<com.android.systemui.miui.AppIconsManager> r7 = com.android.systemui.miui.AppIconsManager.class
            java.lang.Object r7 = com.android.systemui.Dependency.get(r7)     // Catch:{ NameNotFoundException -> 0x0077 }
            com.android.systemui.miui.AppIconsManager r7 = (com.android.systemui.miui.AppIconsManager) r7     // Catch:{ NameNotFoundException -> 0x0077 }
            android.content.Context r8 = r14.mContext     // Catch:{ NameNotFoundException -> 0x0077 }
            android.graphics.Bitmap r7 = r7.getAppIconBitmap(r8, r15)     // Catch:{ NameNotFoundException -> 0x0077 }
            if (r7 == 0) goto L_0x0077
            android.graphics.drawable.Icon r7 = android.graphics.drawable.Icon.createWithBitmap(r7)     // Catch:{ NameNotFoundException -> 0x0077 }
            goto L_0x0078
        L_0x0076:
            r4 = r6
        L_0x0077:
            r7 = r6
        L_0x0078:
            android.content.Intent r10 = new android.content.Intent
            java.lang.String r8 = "package"
            android.net.Uri r8 = android.net.Uri.fromParts(r8, r15, r6)
            java.lang.String r9 = "android.settings.APPLICATION_DETAILS_SETTINGS"
            r10.<init>(r9, r8)
            r8 = 268435456(0x10000000, float:2.5243549E-29)
            r10.setFlags(r8)
            android.content.Context r8 = r14.mContext
            r9 = 0
            r11 = 0
            r12 = 0
            android.os.UserHandle r13 = android.os.UserHandle.CURRENT
            android.app.PendingIntent r8 = android.app.PendingIntent.getActivityAsUser(r8, r9, r10, r11, r12, r13)
            android.content.Context r9 = r14.mContext
            java.lang.String r10 = com.android.systemui.util.NotificationChannels.LOCATION
            android.app.Notification$Builder r9 = android.app.NotificationCompat.newBuilder(r9, r10)
            android.content.Context r10 = r14.mContext
            java.lang.CharSequence r3 = r10.getText(r3)
            android.app.Notification$Builder r3 = r9.setContentTitle(r3)
            android.app.Notification$Builder r3 = r3.setContentText(r4)
            android.app.Notification$Builder r0 = r3.setOngoing(r0)
            android.app.Notification$Builder r0 = r0.setContentIntent(r8)
            if (r7 != 0) goto L_0x00b9
            r0.setSmallIcon(r2)
            goto L_0x00bc
        L_0x00b9:
            r0.setSmallIcon(r7)
        L_0x00bc:
            android.app.Notification r2 = r0.build()
            boolean r3 = android.text.TextUtils.isEmpty(r15)
            if (r3 != 0) goto L_0x00ca
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setTargetPkg(r2, r15)
            goto L_0x00e2
        L_0x00ca:
            android.content.Context r15 = r14.mContext
            android.content.res.Resources r15 = r15.getResources()
            boolean r3 = com.android.systemui.Constants.SUPPORT_DUAL_GPS
            if (r3 == 0) goto L_0x00d8
            r3 = 2131232997(0x7f0808e5, float:1.808212E38)
            goto L_0x00db
        L_0x00d8:
            r3 = 2131232999(0x7f0808e7, float:1.8082123E38)
        L_0x00db:
            android.graphics.Bitmap r15 = android.graphics.BitmapFactory.decodeResource(r15, r3)
            r0.setLargeIcon(r15)
        L_0x00e2:
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setEnableFloat(r2, r1)
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setEnableKeyguard(r2, r1)
            r2.tickerView = r6
            r2.tickerText = r6
            android.app.NotificationManager r14 = r14.mNotificationManager
            android.os.UserHandle r15 = android.os.UserHandle.CURRENT
            r14.notifyAsUser(r6, r5, r2, r15)
            goto L_0x00fb
        L_0x00f4:
            android.app.NotificationManager r14 = r14.mNotificationManager
            android.os.UserHandle r15 = android.os.UserHandle.CURRENT
            r14.cancelAsUser(r6, r5, r15)
        L_0x00fb:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.LocationControllerImpl.updateGpsNotification(android.content.Intent):void");
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                locationSettingsChanged();
            } else if (i == 2) {
                locationActiveChanged();
            } else if (i == 3) {
                locationStatusChanged((Intent) message.obj);
            }
        }

        private void locationActiveChanged() {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer() {
                public final void accept(Object obj) {
                    LocationControllerImpl.H.this.lambda$locationActiveChanged$0$LocationControllerImpl$H((LocationController.LocationChangeCallback) obj);
                }
            });
        }

        public /* synthetic */ void lambda$locationActiveChanged$0$LocationControllerImpl$H(LocationController.LocationChangeCallback locationChangeCallback) {
            locationChangeCallback.onLocationActiveChanged(LocationControllerImpl.this.mAreActiveLocationRequests);
        }

        private void locationSettingsChanged() {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer(LocationControllerImpl.this.isLocationEnabled()) {
                private final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationSettingsChanged(this.f$0);
                }
            });
        }

        private void locationStatusChanged(Intent intent) {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer(intent) {
                private final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationStatusChanged(this.f$0);
                }
            });
        }
    }
}
