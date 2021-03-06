package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.List;
import miui.telephony.SubscriptionInfo;

public class WifiTile extends QSTileImpl<QSTile.BooleanState> {
    /* access modifiers changed from: private */
    public static final Intent WIFI_SETTINGS = new Intent("android.settings.WIFI_SETTINGS");
    private boolean mConnected;
    protected final NetworkController mController;
    /* access modifiers changed from: private */
    public final WifiDetailAdapter mDetailAdapter;
    protected final WifiSignalCallback mSignalCallback = new WifiSignalCallback();
    private final QSTile.SignalState mStateBeforeClick = newTileState();
    /* access modifiers changed from: private */
    public boolean mTargetEnable;
    /* access modifiers changed from: private */
    public Boolean mTargetStatus;
    /* access modifiers changed from: private */
    public final NetworkController.AccessPointController mWifiController;

    public int getMetricsCategory() {
        return 126;
    }

    public WifiTile(QSHost qSHost) {
        super(qSHost);
        NetworkController networkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mController = networkController;
        this.mWifiController = networkController.getAccessPointController();
        this.mDetailAdapter = (WifiDetailAdapter) createDetailAdapter();
    }

    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mController.addCallback((NetworkController.SignalCallback) this.mSignalCallback);
        } else {
            this.mController.removeCallback((NetworkController.SignalCallback) this.mSignalCallback);
        }
    }

    public void setDetailListening(boolean z) {
        if (z) {
            this.mWifiController.addAccessPointCallback(this.mDetailAdapter);
        } else {
            this.mWifiController.removeAccessPointCallback(this.mDetailAdapter);
        }
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new WifiDetailAdapter();
    }

    public Intent getLongClickIntent() {
        return WIFI_SETTINGS;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        boolean z = this.mTargetEnable;
        TState tstate = this.mState;
        if (z != ((QSTile.BooleanState) tstate).value) {
            Log.d(this.TAG, "handleClick: not ready, ignore");
            return;
        }
        ((QSTile.BooleanState) tstate).copyTo(this.mStateBeforeClick);
        boolean z2 = !this.mTargetEnable;
        this.mTargetEnable = z2;
        this.mTargetStatus = Boolean.valueOf(z2);
        Log.d(this.TAG, "handleClick: to " + this.mTargetEnable);
        this.mController.setWifiEnabled(this.mTargetEnable);
        refreshState();
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        if (!this.mWifiController.canConfigWifi()) {
            postStartActivityDismissingKeyguard(new Intent("android.settings.WIFI_SETTINGS"), 0);
        }
        if (!this.mTargetEnable) {
            this.mTargetEnable = true;
            this.mTargetStatus = Boolean.TRUE;
            this.mController.setWifiEnabled(true);
        }
        refreshState();
        this.mDetailAdapter.updateItems();
        showDetail(true);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_wifi_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        CallbackInfo callbackInfo = this.mSignalCallback.mInfo;
        boolean z = (this.mTargetEnable && callbackInfo.enabled && !callbackInfo.isTransient) || (!this.mTargetEnable && (callbackInfo.enabled || callbackInfo.isTransient));
        booleanState.value = z;
        Boolean bool = this.mTargetStatus;
        if (bool != null && z == bool.booleanValue()) {
            this.mTargetStatus = null;
        }
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.dualTarget = true;
        booleanState.withAnimation = this.mTargetEnable && !booleanState.value;
        Resources resources = this.mContext.getResources();
        if (!callbackInfo.enabled || !callbackInfo.connected) {
            this.mConnected = false;
            booleanState.label = resources.getString(R.string.quick_settings_wifi_label);
        } else {
            this.mConnected = true;
            booleanState.label = removeDoubleQuotes(callbackInfo.ssid);
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(booleanState.value ? R.drawable.ic_qs_wifi_on : R.drawable.ic_qs_wifi_off), this.mInControlCenter));
        StringBuilder sb = new StringBuilder();
        sb.append(this.mContext.getString(R.string.quick_settings_wifi_label));
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        sb.append(",");
        if (booleanState.value && callbackInfo.connected) {
            sb.append(callbackInfo.wifiSignalContentDescription);
            sb.append(",");
            sb.append(removeDoubleQuotes(callbackInfo.ssid));
        }
        if (isShowingDetail()) {
            this.mDetailAdapter.setItemsVisible(booleanState.value);
        }
        booleanState.contentDescription = sb.toString();
        booleanState.dualLabelContentDescription = resources.getString(R.string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnnouncementBeDelayed() {
        return this.mStateBeforeClick.value == ((QSTile.BooleanState) this.mState).value;
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_wifi_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_wifi_changed_off);
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi");
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    private static String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length <= 1 || str.charAt(0) != '\"') {
            return str;
        }
        int i = length - 1;
        return str.charAt(i) == '\"' ? str.substring(1, i) : str;
    }

    public Boolean getTargetEnable() {
        return this.mTargetStatus;
    }

    protected static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean connected;
        boolean enabled;
        boolean isTransient;
        String ssid;
        String wifiSignalContentDescription;
        int wifiSignalIconId;

        protected CallbackInfo() {
        }

        public boolean isChanged(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
            boolean z5;
            this.activityIn = z2;
            this.activityOut = z3;
            if (this.enabled != z) {
                Log.d("WifiTile", "isChanged: enabled from: " + this.enabled + ", to: " + z);
                this.enabled = z;
                z5 = true;
            } else {
                z5 = false;
            }
            if (this.connected != iconState2.visible) {
                Log.d("WifiTile", "isChanged: connected from: " + this.connected + ", to: " + iconState2.visible);
                this.connected = iconState2.visible;
                z5 = true;
            }
            if (this.isTransient != z4) {
                Log.d("WifiTile", "isChanged: isTransient from: " + this.isTransient + ", to: " + z4);
                this.isTransient = z4;
                z5 = true;
            }
            int i = this.wifiSignalIconId;
            int i2 = iconState2.icon;
            if (i != i2) {
                this.wifiSignalIconId = i2;
                z5 = true;
            }
            if (!TextUtils.equals(this.ssid, str)) {
                this.ssid = str;
                z5 = true;
            }
            if (TextUtils.equals(this.wifiSignalContentDescription, iconState2.contentDescription)) {
                return z5;
            }
            this.wifiSignalContentDescription = iconState2.contentDescription;
            return true;
        }

        public String toString() {
            return "CallbackInfo[" + "enabled=" + this.enabled + ",connected=" + this.connected + ",wifiSignalIconId=" + this.wifiSignalIconId + ",ssid=" + this.ssid + ",activityIn=" + this.activityIn + ",activityOut=" + this.activityOut + ",wifiSignalContentDescription=" + this.wifiSignalContentDescription + ",isTransient=" + this.isTransient + ']';
        }
    }

    protected final class WifiSignalCallback implements NetworkController.SignalCallback {
        final CallbackInfo mInfo = new CallbackInfo();

        public void setEthernetIndicators(NetworkController.IconState iconState) {
        }

        public void setIsAirplaneMode(NetworkController.IconState iconState) {
        }

        public void setIsDefaultDataSim(int i, boolean z) {
        }

        public void setIsImsRegisted(int i, boolean z) {
        }

        public void setMobileDataEnabled(boolean z) {
        }

        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
        }

        public void setNetworkNameVoice(int i, String str) {
        }

        public void setNoSims(boolean z) {
        }

        public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        }

        public void setSpeechHd(int i, boolean z) {
        }

        public void setSubs(List<SubscriptionInfo> list) {
        }

        public void setVolteNoService(int i, boolean z) {
        }

        public void setVowifi(int i, boolean z) {
        }

        protected WifiSignalCallback() {
        }

        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
            boolean z5 = z;
            if (QSTileImpl.DEBUG) {
                String access$200 = WifiTile.this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onWifiSignalChanged enabled = ");
                sb.append(z);
                sb.append(", connected = ");
                sb.append(iconState.visible);
                sb.append(", wifiSignalIconId = ");
                sb.append(iconState2.icon);
                sb.append(", activityIn = ");
                boolean z6 = z2;
                sb.append(z2);
                sb.append(", activityOut = ");
                boolean z7 = z3;
                sb.append(z3);
                sb.append(", isTransient = ");
                sb.append(z4);
                Log.d(access$200, sb.toString());
            } else {
                NetworkController.IconState iconState3 = iconState;
                NetworkController.IconState iconState4 = iconState2;
                boolean z8 = z2;
                boolean z9 = z3;
                boolean z10 = z4;
            }
            boolean unused = WifiTile.this.mTargetEnable = z;
            if (this.mInfo.isChanged(z, iconState, iconState2, z2, z3, str, z4)) {
                WifiTile.this.refreshState();
                if (WifiTile.this.isShowingDetail()) {
                    WifiTile.this.mDetailAdapter.updateItems();
                    WifiTile wifiTile = WifiTile.this;
                    wifiTile.fireToggleStateChanged(wifiTile.mTargetEnable);
                }
            }
        }
    }

    protected class WifiDetailAdapter implements DetailAdapter, NetworkController.AccessPointController.AccessPointCallback, QSDetailItems.Callback {
        private AccessPoint[] mAccessPoints;
        private QSDetailItems mItems;

        public int getMetricsCategory() {
            return 152;
        }

        public boolean hasHeader() {
            return true;
        }

        public void onDetailItemDisconnect(QSDetailItems.Item item) {
        }

        protected WifiDetailAdapter() {
        }

        public CharSequence getTitle() {
            return WifiTile.this.mContext.getString(R.string.quick_settings_wifi_label);
        }

        public Intent getSettingsIntent() {
            return WifiTile.WIFI_SETTINGS;
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(WifiTile.this.mTargetEnable);
        }

        public boolean getToggleEnabled() {
            return WifiTile.this.mTargetEnable == ((QSTile.BooleanState) WifiTile.this.mState).value;
        }

        public void setToggleState(boolean z) {
            if (QSTileImpl.DEBUG) {
                String access$1000 = WifiTile.this.TAG;
                Log.d(access$1000, "setToggleState " + z);
            }
            MetricsLogger.action(WifiTile.this.mContext, 153, z);
            boolean unused = WifiTile.this.mTargetEnable = z;
            Boolean unused2 = WifiTile.this.mTargetStatus = Boolean.valueOf(z);
            WifiTile.this.mController.setWifiEnabled(z);
            WifiTile.this.refreshState();
            WifiTile.this.fireToggleStateChanged(z);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            if (QSTileImpl.DEBUG) {
                String access$1400 = WifiTile.this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("createDetailView convertView=");
                sb.append(view != null);
                Log.d(access$1400, sb.toString());
            }
            this.mAccessPoints = null;
            QSDetailItems convertOrInflate = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Wifi");
            this.mItems.setCallback(this);
            WifiTile.this.mWifiController.updateVerboseLoggingLevel();
            WifiTile.this.mWifiController.scanForAccessPoints();
            setItemsVisible(((QSTile.BooleanState) WifiTile.this.mState).value);
            return this.mItems;
        }

        public void onAccessPointsChanged(List<AccessPoint> list) {
            AccessPoint[] accessPointArr = (AccessPoint[]) list.toArray(new AccessPoint[list.size()]);
            this.mAccessPoints = accessPointArr;
            this.mAccessPoints = WifiTileHelper.filterUnreachableAPs(accessPointArr);
            if (WifiTile.this.isShowingDetail()) {
                updateItems();
            }
        }

        public void onSettingsActivityTriggered(Intent intent) {
            WifiTile.this.postStartActivityDismissingKeyguard(intent, 0);
        }

        public void onConnectionStart(AccessPoint accessPoint) {
            if (!accessPoint.isSaved() || accessPoint.getSecurity() == 0) {
                WifiTile.this.mHost.collapsePanels();
                WifiTile.this.showDetail(false);
            }
        }

        public void onDetailItemClick(QSDetailItems.Item item) {
            Object obj;
            if (item != null && (obj = item.tag) != null) {
                WifiTile.this.mWifiController.connect((AccessPoint) obj);
                this.mItems.setItemClicked(true);
            }
        }

        public void setItemsVisible(boolean z) {
            QSDetailItems qSDetailItems = this.mItems;
            if (qSDetailItems != null) {
                qSDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x002f  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0073  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updateItems() {
            /*
                r7 = this;
                com.android.systemui.qs.QSDetailItems r0 = r7.mItems
                if (r0 != 0) goto L_0x0005
                return
            L_0x0005:
                com.android.settingslib.wifi.AccessPoint[] r0 = r7.mAccessPoints
                r1 = 0
                if (r0 == 0) goto L_0x000d
                int r0 = r0.length
                if (r0 > 0) goto L_0x0017
            L_0x000d:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r2 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r2 = r2.mInfo
                boolean r2 = r2.enabled
                if (r2 != 0) goto L_0x001d
            L_0x0017:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                r0.fireScanStateChanged(r1)
                goto L_0x0021
            L_0x001d:
                r2 = 1
                r0.fireScanStateChanged(r2)
            L_0x0021:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r0 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r0 = r0.mInfo
                boolean r0 = r0.enabled
                r2 = 0
                r3 = 2131232599(0x7f080757, float:1.8081312E38)
                if (r0 == 0) goto L_0x0073
                com.android.settingslib.wifi.AccessPoint[] r0 = r7.mAccessPoints
                if (r0 == 0) goto L_0x0065
                int r0 = r0.length
                if (r0 <= 0) goto L_0x0065
                java.util.ArrayList r0 = new java.util.ArrayList
                r0.<init>()
                r2 = r1
            L_0x003c:
                com.android.settingslib.wifi.AccessPoint[] r3 = r7.mAccessPoints
                int r4 = r3.length
                if (r2 >= r4) goto L_0x0057
                r3 = r3[r2]
                com.android.systemui.qs.tiles.WifiTile r4 = com.android.systemui.qs.tiles.WifiTile.this
                android.content.Context r4 = r4.mContext
                com.android.systemui.qs.tiles.WifiTile r5 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.statusbar.policy.NetworkController$AccessPointController r5 = r5.mWifiController
                com.android.systemui.qs.QSDetailItems r6 = r7.mItems
                com.android.systemui.qs.tiles.SlaveWifiHelper.updateItem(r4, r5, r6, r0, r3)
                int r2 = r2 + 1
                goto L_0x003c
            L_0x0057:
                com.android.systemui.qs.QSDetailItems r7 = r7.mItems
                com.android.systemui.qs.QSDetailItems$Item[] r1 = new com.android.systemui.qs.QSDetailItems.Item[r1]
                java.lang.Object[] r0 = r0.toArray(r1)
                com.android.systemui.qs.QSDetailItems$Item[] r0 = (com.android.systemui.qs.QSDetailItems.Item[]) r0
                r7.setItems(r0)
                goto L_0x0080
            L_0x0065:
                com.android.systemui.qs.QSDetailItems r0 = r7.mItems
                r1 = 2131822134(0x7f110636, float:1.927703E38)
                r0.setEmptyState(r3, r1)
                com.android.systemui.qs.QSDetailItems r7 = r7.mItems
                r7.setItems(r2)
                goto L_0x0080
            L_0x0073:
                com.android.systemui.qs.QSDetailItems r0 = r7.mItems
                r1 = 2131822633(0x7f110829, float:1.9278043E38)
                r0.setEmptyState(r3, r1)
                com.android.systemui.qs.QSDetailItems r7 = r7.mItems
                r7.setItems(r2)
            L_0x0080:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.WifiTile.WifiDetailAdapter.updateItems():void");
        }
    }
}
