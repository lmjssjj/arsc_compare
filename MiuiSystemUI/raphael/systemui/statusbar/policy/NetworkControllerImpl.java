package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.ConnectivityManagerCompat;
import android.net.NetworkCapabilities;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.ConfigurationChangedReceiver;
import com.android.systemui.Constants;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.MCCUtils;
import com.android.systemui.VirtualSimUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.WifiSignalController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import miui.os.Build;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;
import miui.util.ObjectReference;
import miui.util.ReflectionUtils;

public class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, DataUsageController.NetworkNameProvider, ConfigurationChangedReceiver, Dumpable, NetworkController.MobileTypeListener {
    static final boolean CHATTY = Log.isLoggable("NetworkControllerChat", 3);
    static final boolean DEBUG = Log.isLoggable("NetworkController", 3);
    private final AccessPointControllerImpl mAccessPoints;
    private boolean mAirplaneMode;
    /* access modifiers changed from: private */
    public final CallbackHandler mCallbackHandler;
    private Config mConfig;
    private final BitSet mConnectedTransports;
    /* access modifiers changed from: private */
    public final ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private List<SubscriptionInfo> mCurrentSubscriptions;
    private int mCurrentUserId;
    /* access modifiers changed from: private */
    public boolean[] mDataConnedInMMsForOperators;
    private final DataSaverController mDataSaverController;
    private final DataUsageController mDataUsageController;
    private MobileSignalController mDefaultSignalController;
    private boolean mDemoInetCondition;
    private boolean mDemoMode;
    private WifiSignalController.WifiState mDemoWifiState;
    private int mEmergencySource;
    @VisibleForTesting
    final EthernetSignalController mEthernetSignalController;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSims;
    /* access modifiers changed from: private */
    public boolean[] mHideVolteForOperators;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    @VisibleForTesting
    ServiceState[] mLastServiceState;
    @VisibleForTesting
    boolean mListening;
    private Locale mLocale;
    @VisibleForTesting
    final SparseArray<MobileSignalController> mMobileSignalControllers;
    private String[] mMobileTypeList;
    private String[] mNetworkNameList;
    private String mNetworkNameSeparator;
    private final NetworkScoreManager mNetworkScoreManager;
    /* access modifiers changed from: private */
    public String[] mOperators;
    /* access modifiers changed from: private */
    public final TelephonyManager mPhone;
    private int mPhoneCount;
    /* access modifiers changed from: private */
    public final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    private boolean mShowPlmnSPn;
    private NetworkController.SignalState mSignalState;
    private SlaveWifiSignalController mSlaveWifiSignalController;
    private final SubscriptionDefaults mSubDefaults;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener;
    /* access modifiers changed from: private */
    public final SubscriptionManager mSubscriptionManager;
    private boolean mSupportSlaveWifi;
    private boolean mUserSetup;
    private final CurrentUserTracker mUserTracker;
    private final BitSet mValidatedTransports;
    /* access modifiers changed from: private */
    public final WifiManager mWifiManager;
    @VisibleForTesting
    final WifiSignalController mWifiSignalController;
    /* access modifiers changed from: private */
    public boolean[] misMobileTypeShownWhenWifiOn;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NetworkControllerImpl(@com.miui.systemui.annotation.Inject android.content.Context r16, @com.miui.systemui.annotation.Inject(tag = "SysUiNetBg") android.os.Looper r17, @com.miui.systemui.annotation.Inject com.android.systemui.statusbar.policy.DeviceProvisionedController r18) {
        /*
            r15 = this;
            r14 = r15
            r1 = r16
            java.lang.String r0 = "connectivity"
            java.lang.Object r0 = r1.getSystemService(r0)
            r2 = r0
            android.net.ConnectivityManager r2 = (android.net.ConnectivityManager) r2
            java.lang.Class<android.net.NetworkScoreManager> r0 = android.net.NetworkScoreManager.class
            java.lang.Object r0 = r1.getSystemService(r0)
            r3 = r0
            android.net.NetworkScoreManager r3 = (android.net.NetworkScoreManager) r3
            java.lang.String r0 = "phone"
            java.lang.Object r0 = r1.getSystemService(r0)
            r4 = r0
            android.telephony.TelephonyManager r4 = (android.telephony.TelephonyManager) r4
            java.lang.String r0 = "wifi"
            java.lang.Object r0 = r1.getSystemService(r0)
            r5 = r0
            android.net.wifi.WifiManager r5 = (android.net.wifi.WifiManager) r5
            miui.telephony.SubscriptionManager r6 = miui.telephony.SubscriptionManager.getDefault()
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r7 = com.android.systemui.statusbar.policy.NetworkControllerImpl.Config.readConfig(r16)
            com.android.systemui.statusbar.policy.CallbackHandler r9 = new com.android.systemui.statusbar.policy.CallbackHandler
            r9.<init>()
            com.android.systemui.statusbar.policy.AccessPointControllerImpl r10 = new com.android.systemui.statusbar.policy.AccessPointControllerImpl
            r8 = r17
            r10.<init>(r1, r8)
            com.android.settingslib.net.DataUsageController r11 = new com.android.settingslib.net.DataUsageController
            r11.<init>(r1)
            r12 = 0
            r0 = r15
            r13 = r18
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            android.os.Handler r0 = r14.mReceiverHandler
            java.lang.Runnable r1 = r14.mRegisterListeners
            r0.post(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.<init>(android.content.Context, android.os.Looper, com.android.systemui.statusbar.policy.DeviceProvisionedController):void");
    }

    @VisibleForTesting
    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, NetworkScoreManager networkScoreManager, TelephonyManager telephonyManager, WifiManager wifiManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults, DeviceProvisionedController deviceProvisionedController) {
        CallbackHandler callbackHandler2 = callbackHandler;
        final DeviceProvisionedController deviceProvisionedController2 = deviceProvisionedController;
        this.mMobileSignalControllers = new SparseArray<>();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mRegisterListeners = new Runnable() {
            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        int i = this.mPhoneCount;
        this.mNetworkNameList = new String[i];
        this.mMobileTypeList = new String[i];
        this.mOperators = new String[i];
        this.mHideVolteForOperators = new boolean[i];
        this.misMobileTypeShownWhenWifiOn = new boolean[i];
        this.mDataConnedInMMsForOperators = new boolean[i];
        String string = context.getString(R.string.status_bar_network_name_separator);
        this.mNetworkNameSeparator = string;
        this.mNetworkNameSeparator = string;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler2;
        this.mDataSaverController = new DataSaverControllerImpl(context);
        this.mSubscriptionManager = subscriptionManager;
        this.mSubDefaults = new SubscriptionDefaults();
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = this.mConnectivityManager.isNetworkSupported(0);
        this.mPhone = telephonyManager;
        this.mShowPlmnSPn = context.getResources().getBoolean(R.bool.show_plmn_and_spn_in_carrier);
        this.mWifiManager = wifiManager;
        this.mNetworkScoreManager = networkScoreManager;
        this.mLastServiceState = new ServiceState[this.mPhoneCount];
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointControllerImpl;
        this.mDataUsageController = dataUsageController;
        this.mDataUsageController.setNetworkController(this);
        this.mDataUsageController.setCallback(new DataUsageController.Callback() {
            public void onMobileDataEnabled(boolean z) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(z);
            }
        });
        this.mWifiSignalController = new WifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this, this.mNetworkScoreManager);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int i) {
                NetworkControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker.startTracking();
        deviceProvisionedController2.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() {
            public void onDeviceProvisionedChanged() {
            }

            public void onUserSwitched() {
                onUserSetupChanged();
            }

            public void onUserSetupChanged() {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                DeviceProvisionedController deviceProvisionedController = deviceProvisionedController2;
                networkControllerImpl.setUserSetupComplete(deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser()));
            }
        });
        this.mSignalState = new NetworkController.SignalState();
        this.mSupportSlaveWifi = NetworkControllerCompat.supportSlaveWifi(context);
        Log.d("NetworkController", "NetworkControllerImpl: mSupportSlaveWifi = " + this.mSupportSlaveWifi);
        this.mSlaveWifiSignalController = new SlaveWifiSignalController(this.mContext, this.mSupportSlaveWifi, callbackHandler2, this);
    }

    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    private boolean isCustomizationTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST;
    }

    /* access modifiers changed from: private */
    public void registerListeners() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).registerListener();
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        if (this.mSupportSlaveWifi) {
            NetworkControllerCompat.addSlaveWifiBroadcast(intentFilter);
        }
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        for (int i2 = 1; i2 < this.mPhoneCount; i2++) {
            intentFilter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED" + i2);
        }
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.conn.INET_CONDITION_ACTION");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_IMS_REGISTED");
        intentFilter.addAction("android.intent.action.ACTION_SPEECH_CODEC_IS_HD");
        intentFilter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        if (miui.telephony.TelephonyManager.getDefault().getCtVolteSupportedMode() > 0) {
            intentFilter.addAction("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1");
            intentFilter.addAction("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2");
        }
        this.mContext.registerReceiver(this, intentFilter, (String) null, this.mReceiverHandler);
        this.mListening = true;
        updateMobileControllers();
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
            valueAt.unregisterListener();
            if (isCustomizationTest()) {
                valueAt.setMobileTypeListener((NetworkController.MobileTypeListener) null);
            }
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mContext.unregisterReceiver(this);
    }

    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    public void addEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    public void removeEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, false);
    }

    public void addCarrierNameListener(NetworkController.CarrierNameListener carrierNameListener) {
        this.mCallbackHandler.setListening(carrierNameListener, true);
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCallbackHandler.updateCarrierName(i, this.mNetworkNameList[i]);
        }
    }

    public void removeCarrierNameListener(NetworkController.CarrierNameListener carrierNameListener) {
        this.mCallbackHandler.setListening(carrierNameListener, false);
    }

    public void addMobileTypeListener(NetworkController.MobileTypeListener mobileTypeListener) {
        this.mCallbackHandler.setListening(mobileTypeListener, true);
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCallbackHandler.updateMobileTypeName(i, this.mMobileTypeList[i]);
        }
    }

    public void removeMobileTypeListener(NetworkController.MobileTypeListener mobileTypeListener) {
        this.mCallbackHandler.setListening(mobileTypeListener, false);
    }

    public void updateMobileTypeName(int i, String str) {
        if (i < this.mPhoneCount) {
            String[] strArr = this.mMobileTypeList;
            if (strArr[i] == null || !strArr[i].equals(str)) {
                this.mMobileTypeList[i] = str;
                this.mCallbackHandler.updateMobileTypeName(i, str);
            }
        }
    }

    public boolean hasMobileDataFeature() {
        return this.mHasMobileDataFeature;
    }

    public boolean isMobileDataSupported(int i) {
        return hasMobileDataFeature() && this.mPhone.getSimState(i) == 5;
    }

    public boolean hasVoiceCallingFeature() {
        return this.mPhone.getPhoneType() != 0;
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            this.mEmergencySource = 0;
            if (this.mLastServiceState == null) {
                return false;
            }
            boolean z = false;
            for (int i = 0; i < this.mPhoneCount; i++) {
                if (!z) {
                    ServiceState[] serviceStateArr = this.mLastServiceState;
                    if (serviceStateArr[i] == null || !serviceStateArr[i].isEmergencyOnly()) {
                        z = false;
                    }
                }
                z = true;
            }
            return z;
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i2);
                if (!((MobileSignalController.MobileState) valueAt.getState()).isEmergency) {
                    this.mEmergencySource = valueAt.mSubscriptionInfo.getSubscriptionId() + 100;
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + valueAt.mTag);
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.indexOfKey(defaultVoiceSubId) >= 0) {
            this.mEmergencySource = defaultVoiceSubId + 200;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + defaultVoiceSubId);
            }
            return ((MobileSignalController.MobileState) this.mMobileSignalControllers.get(defaultVoiceSubId).getState()).isEmergency;
        } else if (this.mMobileSignalControllers.size() == 1) {
            this.mEmergencySource = this.mMobileSignalControllers.keyAt(0) + 400;
            if (DEBUG) {
                Log.d("NetworkController", "Getting assumed emergency from " + this.mMobileSignalControllers.keyAt(0));
            }
            return ((MobileSignalController.MobileState) this.mMobileSignalControllers.valueAt(0).getState()).isEmergency;
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for voice sub: " + defaultVoiceSubId);
            }
            this.mEmergencySource = defaultVoiceSubId + 300;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void recalculateEmergency() {
        this.mIsEmergency = isEmergencyOnly();
        this.mCallbackHandler.setEmergencyCallsOnly(this.mIsEmergency);
    }

    public void addCallback(NetworkController.SignalCallback signalCallback) {
        signalCallback.setSubs(this.mCurrentSubscriptions);
        signalCallback.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, (int) R.drawable.stat_sys_signal_flightmode, (int) R.string.accessibility_airplane_mode, this.mContext));
        signalCallback.setNoSims(this.mHasNoSims);
        this.mWifiSignalController.notifyListeners(signalCallback);
        this.mEthernetSignalController.notifyListeners(signalCallback);
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners(signalCallback);
        }
        if (this.mSupportSlaveWifi) {
            this.mSlaveWifiSignalController.notifyListeners(signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, true);
        this.mCallbackHandler.setSubs(this.mCurrentSubscriptions);
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, (int) R.drawable.stat_sys_signal_flightmode, (int) R.string.accessibility_airplane_mode, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSims);
    }

    public void removeCallback(NetworkController.SignalCallback signalCallback) {
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    public void setWifiEnabled(final boolean z) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                int wifiApState = NetworkControllerImpl.this.mWifiManager.getWifiApState();
                if (z) {
                    NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                    if (!networkControllerImpl.getWifiStaSapConcurrency(networkControllerImpl.mWifiManager) && (wifiApState == 12 || wifiApState == 13)) {
                        if (Build.VERSION.SDK_INT < 24) {
                            WifiManager unused = NetworkControllerImpl.this.mWifiManager;
                        } else {
                            ConnectivityManagerCompat.stopTethering(NetworkControllerImpl.this.mConnectivityManager, 0);
                        }
                    }
                }
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(z);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public boolean getWifiStaSapConcurrency(WifiManager wifiManager) {
        if (Build.VERSION.SDK_INT > 27) {
            return true;
        }
        ObjectReference tryCallMethod = ReflectionUtils.tryCallMethod(wifiManager, "getWifiStaSapConcurrency", Boolean.class, new Object[0]);
        if (tryCallMethod == null) {
            return false;
        }
        return ((Boolean) tryCallMethod.get()).booleanValue();
    }

    /* access modifiers changed from: private */
    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        this.mAccessPoints.onUserSwitched(i);
        updateConnectivity();
    }

    public void onReceive(Context context, Intent intent) {
        if (CHATTY) {
            Log.d("NetworkController", "onReceive: intent=" + intent);
        }
        int intExtra = intent.getIntExtra("subscription", SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        String action = intent.getAction();
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.conn.INET_CONDITION_ACTION")) {
            this.mWifiSignalController.updateWifiNoNetwork();
            updateConnectivity();
        } else if (!"android.intent.action.ANY_DATA_STATE".equals(action)) {
            if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                refreshLocale();
                updateAirplaneMode(false);
            } else if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
                recalculateEmergency();
            } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                    this.mMobileSignalControllers.valueAt(i).handleBroadcast(intent);
                }
            } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                int i2 = 0;
                for (int i3 = 0; i3 < phoneCount; i3++) {
                    if (TelephonyManager.getDefault().hasIccCard(i3)) {
                        i2++;
                    }
                }
                if (Constants.SUPPORT_DISABLE_USB_BY_SIM && i2 > 0) {
                    Log.d("NetworkController", "has sim");
                    Settings.System.putInt(this.mContext.getContentResolver(), "disable_usb_by_sim", 0);
                }
                if (SubscriptionManager.isValidSubscriptionId(intExtra) && this.mMobileSignalControllers.indexOfKey(intExtra) >= 0) {
                    this.mMobileSignalControllers.get(intExtra).handleBroadcast(intent);
                }
                updateMobileControllers();
                String stringExtra = intent.getStringExtra("ss");
                if ("CARD_IO_ERROR".equals(stringExtra) || "ABSENT".equals(stringExtra)) {
                    this.mSignalState.speedHdMap.remove(Integer.valueOf(intExtra));
                    this.mSignalState.imsMap.remove(Integer.valueOf(intExtra));
                    this.mSignalState.vowifiMap.remove(Integer.valueOf(intExtra));
                }
            } else if (action.equals("android.intent.action.SERVICE_STATE")) {
                int intExtra2 = intent.getIntExtra("slot", SubscriptionManager.INVALID_SLOT_ID);
                if (intExtra2 != SubscriptionManager.INVALID_SLOT_ID) {
                    this.mLastServiceState[intExtra2] = ServiceState.newFromBundle(intent.getExtras());
                    if (this.mMobileSignalControllers.size() == 0) {
                        recalculateEmergency();
                    }
                }
            } else if (isSpnUpdateActionSlot(action)) {
                int intExtra3 = intent.getIntExtra(SubscriptionManager.SLOT_KEY, SubscriptionManager.INVALID_SLOT_ID);
                if (intExtra3 != SubscriptionManager.INVALID_SLOT_ID) {
                    TelephonyIcons.updateDataTypeMcc(this.mContext, this.mPhone.getSimOperatorNumericForPhone(intExtra3), intExtra3);
                    TelephonyIcons.updateDataTypeMccMnc(this.mContext, this.mPhone.getSimOperatorNumericForPhone(intExtra3), intExtra3);
                    String str = null;
                    if (VirtualSimUtils.isVirtualSim(context, intExtra3)) {
                        str = VirtualSimUtils.getVirtualSimCarrierName(context);
                    }
                    boolean booleanExtra = intent.getBooleanExtra("showSpn", false);
                    boolean booleanExtra2 = intent.getBooleanExtra("showPlmn", false);
                    if (TextUtils.isEmpty(str)) {
                        str = getNetworkName(intExtra3, booleanExtra, intent.getStringExtra("spn"), intent.getStringExtra("spnData"), booleanExtra2, intent.getStringExtra("plmn"));
                    }
                    if (intExtra3 < this.mPhoneCount) {
                        this.mNetworkNameList[intExtra3] = str;
                    }
                    if (str == null) {
                        return;
                    }
                    if (booleanExtra2 || booleanExtra) {
                        this.mCallbackHandler.updateCarrierName(intExtra3, str);
                    }
                }
            } else if (action.equals("android.intent.action.ACTION_IMS_REGISTED")) {
                Log.d("NetworkController", "onReceive: TelephonyConstants.ACTION_IMS_REGISTED, TelephonyConstants.EXTRA_IMS_REGISTED_STATE = " + intent.getBooleanExtra("state", false));
                setImsRegister(intExtra, intent.getBooleanExtra("state", false));
                setVowifi(intExtra, intent.getBooleanExtra("wfc_state", false));
            } else if (action.equals("android.intent.action.ACTION_SPEECH_CODEC_IS_HD")) {
                setSpeechHd(intExtra, intent.getBooleanExtra("is_hd", false));
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                ((CallStateController) Dependency.get(CallStateController.class)).setCallState(intent.getStringExtra("state"));
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action) || "android.net.wifi.STATE_CHANGE".equals(action) || "android.net.wifi.RSSI_CHANGED".equals(action)) {
                this.mWifiSignalController.handleBroadcast(intent);
            } else if (this.mSupportSlaveWifi && NetworkControllerCompat.isSlaveWifiBroadcast(intent)) {
                this.mSlaveWifiSignalController.handleBroadcast(intent);
            } else if (!SubscriptionManager.isValidSubscriptionId(intExtra)) {
            } else {
                if (this.mMobileSignalControllers.indexOfKey(intExtra) >= 0) {
                    this.mMobileSignalControllers.get(intExtra).handleBroadcast(intent);
                } else {
                    updateMobileControllers();
                }
            }
        } else if (this.mMobileSignalControllers.indexOfKey(intExtra) >= 0) {
            this.mMobileSignalControllers.get(intExtra).handleBroadcast(intent);
        }
    }

    public boolean hideVolteForOperation(int i) {
        return this.mHideVolteForOperators[i];
    }

    public boolean isMobileTypeShownWhenWifiOn(int i) {
        return this.misMobileTypeShownWhenWifiOn[i];
    }

    public boolean dataConnedInMMsForOperation(int i) {
        return this.mDataConnedInMMsForOperators[i];
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable() {
            public void run() {
                NetworkControllerImpl.this.handleConfigurationChanged();
            }
        });
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleConfigurationChanged() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    /* access modifiers changed from: private */
    public void updateMobileControllers() {
        if (this.mListening) {
            doUpdateMobileControllers();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void doUpdateMobileControllers() {
        List activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            activeSubscriptionInfoList = Collections.emptyList();
        }
        if (hasCorrectMobileControllers(activeSubscriptionInfoList)) {
            updateNoSims();
            return;
        }
        setCurrentSubscriptions(activeSubscriptionInfoList);
        updateNoSims();
        recalculateEmergency();
        if (isCustomizationTest()) {
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                this.mMobileSignalControllers.valueAt(i).setMobileTypeListener(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateNoSims() {
        boolean z = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        if (z != this.mHasNoSims) {
            this.mHasNoSims = z;
            this.mCallbackHandler.setNoSims(this.mHasNoSims);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setCurrentSubscriptions(List<SubscriptionInfo> list) {
        int i;
        int i2;
        ArrayList arrayList;
        List<SubscriptionInfo> list2 = list;
        Collections.sort(list2, new Comparator<SubscriptionInfo>() {
            public int compare(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
                int i;
                int i2;
                if (subscriptionInfo.getSlotId() == subscriptionInfo2.getSlotId()) {
                    i2 = subscriptionInfo.getSubscriptionId();
                    i = subscriptionInfo2.getSubscriptionId();
                } else {
                    i2 = subscriptionInfo.getSlotId();
                    i = subscriptionInfo2.getSlotId();
                }
                return i2 - i;
            }
        });
        this.mCurrentSubscriptions = list2;
        SparseArray sparseArray = new SparseArray();
        for (int i3 = 0; i3 < this.mMobileSignalControllers.size(); i3++) {
            sparseArray.put(this.mMobileSignalControllers.keyAt(i3), this.mMobileSignalControllers.valueAt(i3));
        }
        this.mMobileSignalControllers.clear();
        ArrayList arrayList2 = new ArrayList();
        int size = list.size();
        ((CallStateController) Dependency.get(CallStateController.class)).setSimCount(size);
        int i4 = 0;
        while (i4 < size) {
            int subscriptionId = list2.get(i4).getSubscriptionId();
            int slotId = list2.get(i4).getSlotId();
            arrayList2.add(Integer.valueOf(subscriptionId));
            if (sparseArray.indexOfKey(subscriptionId) < 0 || !((MobileSignalController) sparseArray.get(subscriptionId)).getSubscriptionInfo().getDisplayName().equals(list2.get(i4).getDisplayName())) {
                Context context = this.mContext;
                Config config = this.mConfig;
                boolean z = this.mHasMobileDataFeature;
                TelephonyManager createForSubscriptionId = this.mPhone.createForSubscriptionId(subscriptionId);
                CallbackHandler callbackHandler = this.mCallbackHandler;
                SubscriptionDefaults subscriptionDefaults = this.mSubDefaults;
                i = size;
                MobileSignalController mobileSignalController = r0;
                int i5 = slotId;
                arrayList = arrayList2;
                int i6 = subscriptionId;
                i2 = i4;
                MobileSignalController mobileSignalController2 = new MobileSignalController(context, config, z, createForSubscriptionId, callbackHandler, this, list2.get(i4), subscriptionDefaults, this.mReceiverHandler.getLooper());
                mobileSignalController.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(i6, mobileSignalController);
                if (Constants.IS_MEDIATEK) {
                    setVowifi(i6, miui.telephony.TelephonyManager.getDefault().isWifiCallingAvailable(i5));
                }
                if (i5 == 0) {
                    this.mDefaultSignalController = mobileSignalController;
                }
                if (this.mListening) {
                    mobileSignalController.registerListener();
                }
            } else {
                this.mMobileSignalControllers.put(subscriptionId, (MobileSignalController) sparseArray.get(subscriptionId));
                sparseArray.remove(subscriptionId);
                i2 = i4;
                arrayList = arrayList2;
                i = size;
            }
            i4 = i2 + 1;
            list2 = list;
            arrayList2 = arrayList;
            size = i;
        }
        ArrayList arrayList3 = arrayList2;
        if (this.mListening) {
            for (int i7 = 0; i7 < sparseArray.size(); i7++) {
                int keyAt = sparseArray.keyAt(i7);
                if (sparseArray.get(keyAt) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) sparseArray.get(keyAt)).unregisterListener();
            }
        }
        NetworkController.SignalState signalState = this.mSignalState;
        ArrayList arrayList4 = arrayList3;
        signalState.updateMap(arrayList4, signalState.imsMap);
        NetworkController.SignalState signalState2 = this.mSignalState;
        signalState2.updateMap(arrayList4, signalState2.vowifiMap);
        NetworkController.SignalState signalState3 = this.mSignalState;
        signalState3.updateMap(arrayList4, signalState3.speedHdMap);
        this.mCallbackHandler.setSubs(list);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    /* access modifiers changed from: private */
    public void setUserSetupComplete(final boolean z) {
        this.mReceiverHandler.post(new Runnable() {
            public void run() {
                NetworkControllerImpl.this.handleSetUserSetupComplete(z);
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleSetUserSetupComplete(boolean z) {
        this.mUserSetup = z;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setUserSetupComplete(this.mUserSetup);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0018  */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasCorrectMobileControllers(java.util.List<miui.telephony.SubscriptionInfo> r5) {
        /*
            r4 = this;
            int r0 = r5.size()
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r4.mMobileSignalControllers
            int r1 = r1.size()
            r2 = 0
            if (r0 == r1) goto L_0x000e
            return r2
        L_0x000e:
            java.util.Iterator r5 = r5.iterator()
        L_0x0012:
            boolean r0 = r5.hasNext()
            if (r0 == 0) goto L_0x003f
            java.lang.Object r0 = r5.next()
            miui.telephony.SubscriptionInfo r0 = (miui.telephony.SubscriptionInfo) r0
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r4.mMobileSignalControllers
            int r3 = r0.getSubscriptionId()
            int r1 = r1.indexOfKey(r3)
            if (r1 < 0) goto L_0x003e
            java.lang.CharSequence r1 = r0.getDisplayName()
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r3 = r4.mMobileSignalControllers
            int r0 = r0.getSubscriptionId()
            java.lang.Object r0 = r3.get(r0)
            boolean r0 = r1.equals(r0)
            if (r0 != 0) goto L_0x0012
        L_0x003e:
            return r2
        L_0x003f:
            r4 = 1
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.hasCorrectMobileControllers(java.util.List):boolean");
    }

    private void updateAirplaneMode(boolean z) {
        boolean z2 = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z2 = false;
        }
        if (z2 != this.mAirplaneMode || z) {
            this.mAirplaneMode = z2;
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                this.mMobileSignalControllers.valueAt(i).setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (!locale.equals(this.mLocale)) {
            this.mLocale = locale;
            notifyAllListeners();
        }
    }

    private void setImsRegister(int i, boolean z) {
        Log.d("NetworkController", "set imsRegisterd, subId:" + i + " imsRegister:" + z);
        if (SubscriptionManager.isValidSubscriptionId(i) && this.mMobileSignalControllers.indexOfKey(i) >= 0) {
            this.mSignalState.imsMap.put(Integer.valueOf(i), Boolean.valueOf(z));
            this.mMobileSignalControllers.get(i).setImsRegister(this.mCallbackHandler, z);
        }
    }

    private void setSpeechHd(int i, boolean z) {
        if (SubscriptionManager.isValidSubscriptionId(i) && this.mMobileSignalControllers.indexOfKey(i) >= 0) {
            this.mSignalState.speedHdMap.put(Integer.valueOf(i), Boolean.valueOf(z));
            this.mMobileSignalControllers.get(i).setSpeechHd(this.mCallbackHandler, z);
        }
    }

    private void setVowifi(int i, boolean z) {
        if (SubscriptionManager.isValidSubscriptionId(i) && this.mMobileSignalControllers.indexOfKey(i) >= 0) {
            this.mSignalState.vowifiMap.put(Integer.valueOf(i), Boolean.valueOf(z));
            this.mMobileSignalControllers.get(i).setVowifi(this.mCallbackHandler, z);
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    private void notifyListeners() {
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, (int) R.drawable.stat_sys_signal_flightmode, (int) R.string.accessibility_airplane_mode, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSims);
    }

    private void updateConnectivity() {
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        for (NetworkCapabilities networkCapabilities : this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId)) {
            for (int i : networkCapabilities.getTransportTypes()) {
                this.mConnectedTransports.set(i);
                if (networkCapabilities.hasCapability(16)) {
                    this.mValidatedTransports.set(i);
                }
            }
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        this.mInetCondition = !this.mValidatedTransports.isEmpty();
        pushConnectivityToSignals();
    }

    private void pushConnectivityToSignals() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
    }

    private boolean isSpnUpdateActionSlot(String str) {
        if (str.equals("android.provider.Telephony.SPN_STRINGS_UPDATED")) {
            return true;
        }
        for (int i = 1; i < this.mPhoneCount; i++) {
            if (str.equals("android.provider.Telephony.SPN_STRINGS_UPDATED" + i)) {
                return true;
            }
        }
        return false;
    }

    private String getNetworkName(int i, boolean z, String str, String str2, boolean z2, String str3) {
        StringBuilder sb = new StringBuilder();
        boolean z3 = this.mShowPlmnSPn;
        TelephonyManager telephonyManager = this.mPhone;
        boolean z4 = true;
        if (telephonyManager != null) {
            String simOperatorNumericForPhone = telephonyManager.getSimOperatorNumericForPhone(i);
            MCCUtils.checkOperation(this.mContext, simOperatorNumericForPhone);
            z3 = z3 || MCCUtils.isShowPlmnAndSpn(this.mContext, simOperatorNumericForPhone, true) || MCCUtils.isShowPlmnAndSpn(this.mContext, simOperatorNumericForPhone, false);
        }
        if (!z2 || str3 == null) {
            z4 = false;
        } else {
            sb.append(str3);
        }
        if ((z3 || !z4) && z && str != null) {
            if (sb.length() != 0) {
                sb.append(this.mNetworkNameSeparator);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NetworkController state:");
        printWriter.println("  - telephony ------");
        printWriter.print("  hasVoiceCallingFeature()=");
        printWriter.println(hasVoiceCallingFeature());
        printWriter.println("  - connectivity ------");
        printWriter.print("  mConnectedTransports=");
        printWriter.println(this.mConnectedTransports);
        printWriter.print("  mValidatedTransports=");
        printWriter.println(this.mValidatedTransports);
        printWriter.print("  mInetCondition=");
        printWriter.println(this.mInetCondition);
        printWriter.print("  mAirplaneMode=");
        printWriter.println(this.mAirplaneMode);
        printWriter.print("  mHasNoSims:");
        printWriter.println(this.mHasNoSims);
        printWriter.print("  mLocale=");
        printWriter.println(this.mLocale);
        printWriter.print("  mLastServiceState=");
        printWriter.println(this.mLastServiceState);
        printWriter.print("  mIsEmergency=");
        printWriter.println(this.mIsEmergency);
        printWriter.print("  mEmergencySource=");
        printWriter.println(emergencyToString(this.mEmergencySource));
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
    }

    private static final String emergencyToString(int i) {
        if (i > 300) {
            return "ASSUMED_VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i > 300) {
            return "NO_SUB(" + (i - 300) + ")";
        } else if (i > 200) {
            return "VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i <= 100) {
            return i == 0 ? "NO_CONTROLLERS" : "UNKNOWN_SOURCE";
        } else {
            return "FIRST_CONTROLLER(" + (i - 100) + ")";
        }
    }

    public void dispatchDemoCommand(final String str, final Bundle bundle) {
        this.mReceiverHandler.post(new Runnable() {
            public void run() {
                NetworkControllerImpl.this.handleDemoCommand(str, bundle);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x0364  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0378  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0160  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0178  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleDemoCommand(java.lang.String r18, android.os.Bundle r19) {
        /*
            r17 = this;
            r0 = r17
            r1 = r18
            r2 = r19
            boolean r3 = r0.mDemoMode
            java.lang.String r4 = "NetworkController"
            r5 = 1
            if (r3 != 0) goto L_0x0039
            java.lang.String r3 = "enter"
            boolean r3 = r1.equals(r3)
            if (r3 == 0) goto L_0x0039
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x001e
            java.lang.String r1 = "Entering demo mode"
            android.util.Log.d(r4, r1)
        L_0x001e:
            r17.unregisterListeners()
            r0.mDemoMode = r5
            boolean r1 = r0.mInetCondition
            r0.mDemoInetCondition = r1
            com.android.systemui.statusbar.policy.WifiSignalController r1 = r0.mWifiSignalController
            com.android.systemui.statusbar.policy.SignalController$State r1 = r1.getState()
            com.android.systemui.statusbar.policy.WifiSignalController$WifiState r1 = (com.android.systemui.statusbar.policy.WifiSignalController.WifiState) r1
            r0.mDemoWifiState = r1
            com.android.systemui.statusbar.policy.WifiSignalController$WifiState r0 = r0.mDemoWifiState
            java.lang.String r1 = "DemoMode"
            r0.ssid = r1
            goto L_0x03b1
        L_0x0039:
            boolean r3 = r0.mDemoMode
            r6 = 0
            if (r3 == 0) goto L_0x007b
            java.lang.String r3 = "exit"
            boolean r3 = r1.equals(r3)
            if (r3 == 0) goto L_0x007b
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x004f
            java.lang.String r1 = "Exiting demo mode"
            android.util.Log.d(r4, r1)
        L_0x004f:
            r0.mDemoMode = r6
            r17.updateMobileControllers()
        L_0x0054:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r0.mMobileSignalControllers
            int r1 = r1.size()
            if (r6 >= r1) goto L_0x006a
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r0.mMobileSignalControllers
            java.lang.Object r1 = r1.valueAt(r6)
            com.android.systemui.statusbar.policy.MobileSignalController r1 = (com.android.systemui.statusbar.policy.MobileSignalController) r1
            r1.resetLastState()
            int r6 = r6 + 1
            goto L_0x0054
        L_0x006a:
            com.android.systemui.statusbar.policy.WifiSignalController r1 = r0.mWifiSignalController
            r1.resetLastState()
            android.os.Handler r1 = r0.mReceiverHandler
            java.lang.Runnable r2 = r0.mRegisterListeners
            r1.post(r2)
            r17.notifyAllListeners()
            goto L_0x03b1
        L_0x007b:
            boolean r3 = r0.mDemoMode
            if (r3 == 0) goto L_0x03b1
            java.lang.String r3 = "network"
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L_0x03b1
            java.lang.String r1 = "airplane"
            java.lang.String r1 = r2.getString(r1)
            java.lang.String r3 = "show"
            if (r1 == 0) goto L_0x00a7
            boolean r1 = r1.equals(r3)
            com.android.systemui.statusbar.policy.CallbackHandler r4 = r0.mCallbackHandler
            com.android.systemui.statusbar.policy.NetworkController$IconState r7 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            r8 = 2131233867(0x7f080c4b, float:1.8083884E38)
            r9 = 2131820588(0x7f11002c, float:1.9273895E38)
            android.content.Context r10 = r0.mContext
            r7.<init>((boolean) r1, (int) r8, (int) r9, (android.content.Context) r10)
            r4.setIsAirplaneMode(r7)
        L_0x00a7:
            java.lang.String r1 = "fully"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x00ea
            boolean r1 = java.lang.Boolean.parseBoolean(r1)
            r0.mDemoInetCondition = r1
            java.util.BitSet r1 = new java.util.BitSet
            r1.<init>()
            boolean r4 = r0.mDemoInetCondition
            if (r4 == 0) goto L_0x00c5
            com.android.systemui.statusbar.policy.WifiSignalController r4 = r0.mWifiSignalController
            int r4 = r4.mTransportType
            r1.set(r4)
        L_0x00c5:
            com.android.systemui.statusbar.policy.WifiSignalController r4 = r0.mWifiSignalController
            r4.updateConnectivity(r1, r1)
            r4 = r6
        L_0x00cb:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r7 = r0.mMobileSignalControllers
            int r7 = r7.size()
            if (r4 >= r7) goto L_0x00ea
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r7 = r0.mMobileSignalControllers
            java.lang.Object r7 = r7.valueAt(r4)
            com.android.systemui.statusbar.policy.MobileSignalController r7 = (com.android.systemui.statusbar.policy.MobileSignalController) r7
            boolean r8 = r0.mDemoInetCondition
            if (r8 == 0) goto L_0x00e4
            int r8 = r7.mTransportType
            r1.set(r8)
        L_0x00e4:
            r7.updateConnectivity(r1, r1)
            int r4 = r4 + 1
            goto L_0x00cb
        L_0x00ea:
            java.lang.String r1 = "wifi"
            java.lang.String r1 = r2.getString(r1)
            java.lang.String r4 = "out"
            java.lang.String r7 = "in"
            r8 = 100357129(0x5fb5409, float:2.3634796E-35)
            r9 = 110414(0x1af4e, float:1.54723E-40)
            r10 = 3365(0xd25, float:4.715E-42)
            java.lang.String r11 = "null"
            java.lang.String r12 = "activity"
            java.lang.String r13 = "level"
            if (r1 == 0) goto L_0x018e
            boolean r1 = r1.equals(r3)
            java.lang.String r14 = r2.getString(r13)
            if (r14 == 0) goto L_0x0132
            com.android.systemui.statusbar.policy.WifiSignalController$WifiState r6 = r0.mDemoWifiState
            boolean r16 = r14.equals(r11)
            if (r16 == 0) goto L_0x0119
            r14 = -1
            goto L_0x0125
        L_0x0119:
            int r14 = java.lang.Integer.parseInt(r14)
            int r16 = com.android.systemui.statusbar.policy.WifiIcons.WIFI_LEVEL_COUNT
            int r15 = r16 + -1
            int r14 = java.lang.Math.min(r14, r15)
        L_0x0125:
            r6.level = r14
            com.android.systemui.statusbar.policy.WifiSignalController$WifiState r6 = r0.mDemoWifiState
            int r14 = r6.level
            if (r14 < 0) goto L_0x012f
            r14 = r5
            goto L_0x0130
        L_0x012f:
            r14 = 0
        L_0x0130:
            r6.connected = r14
        L_0x0132:
            java.lang.String r6 = r2.getString(r12)
            if (r6 == 0) goto L_0x017f
            int r14 = r6.hashCode()
            if (r14 == r10) goto L_0x0155
            if (r14 == r9) goto L_0x014d
            if (r14 == r8) goto L_0x0143
            goto L_0x015d
        L_0x0143:
            java.lang.String r14 = "inout"
            boolean r6 = r6.equals(r14)
            if (r6 == 0) goto L_0x015d
            r6 = 0
            goto L_0x015e
        L_0x014d:
            boolean r6 = r6.equals(r4)
            if (r6 == 0) goto L_0x015d
            r6 = 2
            goto L_0x015e
        L_0x0155:
            boolean r6 = r6.equals(r7)
            if (r6 == 0) goto L_0x015d
            r6 = r5
            goto L_0x015e
        L_0x015d:
            r6 = -1
        L_0x015e:
            if (r6 == 0) goto L_0x0178
            if (r6 == r5) goto L_0x0172
            r14 = 2
            if (r6 == r14) goto L_0x016c
            com.android.systemui.statusbar.policy.WifiSignalController r6 = r0.mWifiSignalController
            r15 = 0
            r6.setActivity(r15)
            goto L_0x0185
        L_0x016c:
            com.android.systemui.statusbar.policy.WifiSignalController r6 = r0.mWifiSignalController
            r6.setActivity(r14)
            goto L_0x0185
        L_0x0172:
            com.android.systemui.statusbar.policy.WifiSignalController r6 = r0.mWifiSignalController
            r6.setActivity(r5)
            goto L_0x0185
        L_0x0178:
            com.android.systemui.statusbar.policy.WifiSignalController r6 = r0.mWifiSignalController
            r14 = 3
            r6.setActivity(r14)
            goto L_0x0185
        L_0x017f:
            com.android.systemui.statusbar.policy.WifiSignalController r6 = r0.mWifiSignalController
            r14 = 0
            r6.setActivity(r14)
        L_0x0185:
            com.android.systemui.statusbar.policy.WifiSignalController$WifiState r6 = r0.mDemoWifiState
            r6.enabled = r1
            com.android.systemui.statusbar.policy.WifiSignalController r1 = r0.mWifiSignalController
            r1.notifyListeners()
        L_0x018e:
            java.lang.String r1 = "sims"
            java.lang.String r1 = r2.getString(r1)
            r6 = 8
            if (r1 == 0) goto L_0x01cc
            int r1 = java.lang.Integer.parseInt(r1)
            int r1 = android.util.MathUtils.constrain(r1, r5, r6)
            java.util.ArrayList r14 = new java.util.ArrayList
            r14.<init>()
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r15 = r0.mMobileSignalControllers
            int r15 = r15.size()
            if (r1 == r15) goto L_0x01cc
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r15 = r0.mMobileSignalControllers
            r15.clear()
            miui.telephony.SubscriptionManager r15 = r0.mSubscriptionManager
            int r15 = r15.getSubscriptionInfoCount()
            r8 = r15
        L_0x01b9:
            int r9 = r15 + r1
            if (r8 >= r9) goto L_0x01c7
            miui.telephony.SubscriptionInfo r9 = r0.addSignalController(r8, r8)
            r14.add(r9)
            int r8 = r8 + 1
            goto L_0x01b9
        L_0x01c7:
            com.android.systemui.statusbar.policy.CallbackHandler r1 = r0.mCallbackHandler
            r1.setSubs(r14)
        L_0x01cc:
            java.lang.String r1 = "nosim"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x01e1
            boolean r1 = r1.equals(r3)
            r0.mHasNoSims = r1
            com.android.systemui.statusbar.policy.CallbackHandler r1 = r0.mCallbackHandler
            boolean r8 = r0.mHasNoSims
            r1.setNoSims(r8)
        L_0x01e1:
            java.lang.String r1 = "mobile"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x038e
            boolean r1 = r1.equals(r3)
            java.lang.String r8 = "datatype"
            java.lang.String r8 = r2.getString(r8)
            java.lang.String r9 = "slot"
            java.lang.String r9 = r2.getString(r9)
            boolean r14 = android.text.TextUtils.isEmpty(r9)
            if (r14 == 0) goto L_0x0201
            r9 = 0
            goto L_0x0205
        L_0x0201:
            int r9 = java.lang.Integer.parseInt(r9)
        L_0x0205:
            r14 = 0
            int r6 = android.util.MathUtils.constrain(r9, r14, r6)
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
        L_0x020f:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r14 = r0.mMobileSignalControllers
            int r14 = r14.size()
            if (r14 > r6) goto L_0x0225
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r14 = r0.mMobileSignalControllers
            int r14 = r14.size()
            miui.telephony.SubscriptionInfo r14 = r0.addSignalController(r14, r14)
            r9.add(r14)
            goto L_0x020f
        L_0x0225:
            boolean r14 = r9.isEmpty()
            if (r14 != 0) goto L_0x0230
            com.android.systemui.statusbar.policy.CallbackHandler r14 = r0.mCallbackHandler
            r14.setSubs(r9)
        L_0x0230:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r9 = r0.mMobileSignalControllers
            java.lang.Object r6 = r9.valueAt(r6)
            com.android.systemui.statusbar.policy.MobileSignalController r6 = (com.android.systemui.statusbar.policy.MobileSignalController) r6
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            if (r8 == 0) goto L_0x0242
            r14 = r5
            goto L_0x0243
        L_0x0242:
            r14 = 0
        L_0x0243:
            r9.dataSim = r14
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            if (r8 == 0) goto L_0x024f
            r14 = r5
            goto L_0x0250
        L_0x024f:
            r14 = 0
        L_0x0250:
            r9.isDefault = r14
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            if (r8 == 0) goto L_0x025c
            r14 = r5
            goto L_0x025d
        L_0x025c:
            r14 = 0
        L_0x025d:
            r9.dataConnected = r14
            if (r8 == 0) goto L_0x02da
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            java.lang.String r14 = "1x"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x0273
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.ONE_X
            goto L_0x02d8
        L_0x0273:
            java.lang.String r14 = "3g"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x027e
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            goto L_0x02d8
        L_0x027e:
            java.lang.String r14 = "4g"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x0289
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G
            goto L_0x02d8
        L_0x0289:
            java.lang.String r14 = "4g+"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x0294
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G_PLUS
            goto L_0x02d8
        L_0x0294:
            java.lang.String r14 = "e"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x029f
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.E
            goto L_0x02d8
        L_0x029f:
            java.lang.String r14 = "g"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x02aa
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.G
            goto L_0x02d8
        L_0x02aa:
            java.lang.String r14 = "h"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x02b5
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.H
            goto L_0x02d8
        L_0x02b5:
            java.lang.String r14 = "lte"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x02c0
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE
            goto L_0x02d8
        L_0x02c0:
            java.lang.String r14 = "lte+"
            boolean r14 = r8.equals(r14)
            if (r14 == 0) goto L_0x02cb
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE_PLUS
            goto L_0x02d8
        L_0x02cb:
            java.lang.String r14 = "dis"
            boolean r8 = r8.equals(r14)
            if (r8 == 0) goto L_0x02d6
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.DATA_DISABLED
            goto L_0x02d8
        L_0x02d6:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r8 = com.android.systemui.statusbar.policy.TelephonyIcons.UNKNOWN
        L_0x02d8:
            r9.iconGroup = r8
        L_0x02da:
            java.lang.String r8 = "roam"
            boolean r8 = r2.containsKey(r8)
            if (r8 == 0) goto L_0x02f4
            com.android.systemui.statusbar.policy.SignalController$State r8 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r8 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r8
            java.lang.String r9 = "roam"
            java.lang.String r9 = r2.getString(r9)
            boolean r9 = r3.equals(r9)
            r8.roaming = r9
        L_0x02f4:
            java.lang.String r8 = r2.getString(r13)
            if (r8 == 0) goto L_0x0328
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            boolean r11 = r8.equals(r11)
            if (r11 == 0) goto L_0x0308
            r14 = -1
            goto L_0x0311
        L_0x0308:
            int r8 = java.lang.Integer.parseInt(r8)
            r11 = 5
            int r14 = java.lang.Math.min(r8, r11)
        L_0x0311:
            r9.level = r14
            com.android.systemui.statusbar.policy.SignalController$State r8 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r8 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r8
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            int r9 = r9.level
            if (r9 < 0) goto L_0x0325
            r9 = r5
            goto L_0x0326
        L_0x0325:
            r9 = 0
        L_0x0326:
            r8.connected = r9
        L_0x0328:
            java.lang.String r8 = r2.getString(r12)
            if (r8 == 0) goto L_0x037e
            com.android.systemui.statusbar.policy.SignalController$State r9 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r9 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r9
            r9.dataConnected = r5
            int r9 = r8.hashCode()
            if (r9 == r10) goto L_0x0359
            r10 = 110414(0x1af4e, float:1.54723E-40)
            if (r9 == r10) goto L_0x0351
            r7 = 100357129(0x5fb5409, float:2.3634796E-35)
            if (r9 == r7) goto L_0x0347
            goto L_0x0361
        L_0x0347:
            java.lang.String r4 = "inout"
            boolean r4 = r8.equals(r4)
            if (r4 == 0) goto L_0x0361
            r4 = 0
            goto L_0x0362
        L_0x0351:
            boolean r4 = r8.equals(r4)
            if (r4 == 0) goto L_0x0361
            r4 = 2
            goto L_0x0362
        L_0x0359:
            boolean r4 = r8.equals(r7)
            if (r4 == 0) goto L_0x0361
            r4 = r5
            goto L_0x0362
        L_0x0361:
            r4 = -1
        L_0x0362:
            if (r4 == 0) goto L_0x0378
            if (r4 == r5) goto L_0x0373
            r7 = 2
            if (r4 == r7) goto L_0x036e
            r15 = 0
            r6.setActivity(r15)
            goto L_0x0382
        L_0x036e:
            r15 = 0
            r6.setActivity(r7)
            goto L_0x0382
        L_0x0373:
            r15 = 0
            r6.setActivity(r5)
            goto L_0x0382
        L_0x0378:
            r15 = 0
            r4 = 3
            r6.setActivity(r4)
            goto L_0x0382
        L_0x037e:
            r15 = 0
            r6.setActivity(r15)
        L_0x0382:
            com.android.systemui.statusbar.policy.SignalController$State r4 = r6.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r4 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r4
            r4.enabled = r1
            r6.notifyListeners()
            goto L_0x038f
        L_0x038e:
            r15 = 0
        L_0x038f:
            java.lang.String r1 = "carriernetworkchange"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x03b1
            boolean r1 = r1.equals(r3)
        L_0x039b:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r2 = r0.mMobileSignalControllers
            int r2 = r2.size()
            if (r15 >= r2) goto L_0x03b1
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r2 = r0.mMobileSignalControllers
            java.lang.Object r2 = r2.valueAt(r15)
            com.android.systemui.statusbar.policy.MobileSignalController r2 = (com.android.systemui.statusbar.policy.MobileSignalController) r2
            r2.setCarrierNetworkChangeMode(r1)
            int r15 = r15 + 1
            goto L_0x039b
        L_0x03b1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.handleDemoCommand(java.lang.String, android.os.Bundle):void");
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfoForSlot = SubscriptionManager.getDefault().getSubscriptionInfoForSlot(i2);
        MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, subscriptionInfoForSlot, this.mSubDefaults, this.mReceiverHandler.getLooper());
        this.mMobileSignalControllers.put(i, mobileSignalController);
        ((MobileSignalController.MobileState) mobileSignalController.getState()).userSetup = true;
        return subscriptionInfoForSlot;
    }

    public boolean hasEmergencyCryptKeeperText() {
        return EncryptionHelper.IS_DATA_ENCRYPTED;
    }

    public boolean isRadioOn() {
        return !this.mAirplaneMode;
    }

    private class SubListener implements SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        public void onSubscriptionsChanged() {
            NetworkControllerImpl.this.mReceiverHandler.post(new Runnable() {
                public void run() {
                    List<SubscriptionInfo> activeSubscriptionInfoList = NetworkControllerImpl.this.mSubscriptionManager.getActiveSubscriptionInfoList();
                    if (activeSubscriptionInfoList == null) {
                        activeSubscriptionInfoList = Collections.emptyList();
                    }
                    for (SubscriptionInfo slotId : activeSubscriptionInfoList) {
                        int slotId2 = slotId.getSlotId();
                        String simOperatorNumericForPhone = NetworkControllerImpl.this.mPhone.getSimOperatorNumericForPhone(slotId2);
                        NetworkControllerImpl.this.mOperators[slotId2] = simOperatorNumericForPhone;
                        NetworkControllerImpl.this.mHideVolteForOperators[slotId2] = MCCUtils.isHideVolte(NetworkControllerImpl.this.mContext, simOperatorNumericForPhone);
                        NetworkControllerImpl.this.misMobileTypeShownWhenWifiOn[slotId2] = MCCUtils.isMobileTypeShownWhenWifiOn(NetworkControllerImpl.this.mContext, simOperatorNumericForPhone);
                        NetworkControllerImpl.this.mDataConnedInMMsForOperators[slotId2] = MCCUtils.isShowMobileInMMS(NetworkControllerImpl.this.mContext, simOperatorNumericForPhone);
                    }
                    NetworkControllerImpl.this.updateMobileControllers();
                }
            });
        }
    }

    public class SubscriptionDefaults {
        public SubscriptionDefaults() {
        }

        public int getDefaultVoiceSubId() {
            return NetworkControllerImpl.this.mSubscriptionManager.getDefaultVoiceSubscriptionId();
        }

        public int getDefaultDataSubId() {
            return NetworkControllerImpl.this.mSubscriptionManager.getDefaultDataSubscriptionId();
        }
    }

    public NetworkController.SignalState getSignalState() {
        return this.mSignalState;
    }

    @VisibleForTesting
    static class Config {
        boolean alwaysShowCdmaRssi = false;
        boolean hideLtePlus = false;
        boolean hspaDataDistinguishable;
        boolean readIconsFromXml;
        boolean show4gForLte = false;
        boolean showAtLeast3G = false;
        boolean showRsrpSignalLevelforLTE;

        Config() {
        }

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources resources = context.getResources();
            config.showAtLeast3G = resources.getBoolean(R.bool.config_showMin3G);
            config.alwaysShowCdmaRssi = resources.getBoolean(17891358);
            config.show4gForLte = resources.getBoolean(R.bool.config_show4GForLTE);
            config.hspaDataDistinguishable = resources.getBoolean(R.bool.config_hspa_data_distinguishable) && !miui.os.Build.IS_CM_CUSTOMIZATION;
            config.hideLtePlus = resources.getBoolean(R.bool.config_hideLtePlus);
            config.readIconsFromXml = resources.getBoolean(R.bool.config_read_icons_from_xml);
            config.showRsrpSignalLevelforLTE = resources.getBoolean(R.bool.config_showRsrpSignalLevelforLTE);
            return config;
        }
    }
}
