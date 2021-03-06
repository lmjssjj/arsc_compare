package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.miui.systemui.annotation.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import miui.view.MiuiHapticFeedbackConstants;

public class ExpandInfoControllerImpl implements ExpandInfoController {
    private ControlCenterActivityStarter mActivityStarter = ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class));
    private HashMap<Integer, BaseInfo> mBaseInfos = new HashMap<>();
    private ArrayList<ExpandInfoController.Callback> mCallbacks = new ArrayList<>();
    private ControlPanelContentView mContentView;
    private Context mContext;
    private DataBillInfo mDataBillInfo;
    private DataUsageInfo mDataUsageInfo;
    private HealthDataInfo mHealthDataInfo;
    private HashMap<Integer, ExpandInfoController.Info> mInfosMap = new HashMap<>();
    private HashMap<Integer, ExpandInfoController.Info> mInfosMapOld = new HashMap<>();
    private ScreenTimeInfo mScreenTimeInfo;
    private int mSelectedType;
    private SuperPowerInfo mSuperPowerInfo;
    private boolean mSuperPowerMode;
    private int mUnAvailableCnt;
    private UserHandle mUserHandler = new UserHandle(KeyguardUpdateMonitor.getCurrentUser());

    public ExpandInfoControllerImpl(@Inject Context context) {
        this.mContext = context;
        if (!Constants.IS_INTERNATIONAL) {
            this.mInfosMap.put(0, new ExpandInfoController.Info());
            this.mInfosMap.put(1, new ExpandInfoController.Info());
            this.mInfosMap.put(2, new ExpandInfoController.Info());
            this.mInfosMap.put(3, new ExpandInfoController.Info());
            this.mDataUsageInfo = new DataUsageInfo(this.mContext, 0, this);
            this.mBaseInfos.put(0, this.mDataUsageInfo);
            this.mDataBillInfo = new DataBillInfo(this.mContext, 1, this);
            this.mBaseInfos.put(1, this.mDataBillInfo);
            this.mHealthDataInfo = new HealthDataInfo(this.mContext, 2, this);
            this.mBaseInfos.put(2, this.mHealthDataInfo);
            this.mScreenTimeInfo = new ScreenTimeInfo(this.mContext, 3, this);
            this.mBaseInfos.put(3, this.mScreenTimeInfo);
            this.mSuperPowerInfo = new SuperPowerInfo(this.mContext, 16, this);
            this.mBaseInfos.put(16, this.mSuperPowerInfo);
            this.mSelectedType = Settings.System.getIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", 0, KeyguardUpdateMonitor.getCurrentUser());
            setSelectedType(this.mSelectedType);
        }
    }

    public void onUserSwitched() {
        this.mUserHandler = new UserHandle(KeyguardUpdateMonitor.getCurrentUser());
        requestData();
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", 0, KeyguardUpdateMonitor.getCurrentUser());
        if (intForUser != this.mSelectedType) {
            setSelectedType(intForUser);
        }
    }

    public UserHandle getUserHandle() {
        return this.mUserHandler;
    }

    public void setContentView(ControlPanelContentView controlPanelContentView) {
        this.mContentView = controlPanelContentView;
    }

    public ControlPanelContentView getContentView() {
        return this.mContentView;
    }

    public HashMap<Integer, ExpandInfoController.Info> getInfosMap() {
        return this.mInfosMap;
    }

    public void updateInfo(int i, ExpandInfoController.Info info) {
        if (this.mSuperPowerMode && i != 16) {
            return;
        }
        if (this.mSuperPowerMode || i != 16) {
            ExpandInfoController.Info info2 = this.mInfosMap.get(Integer.valueOf(i));
            if (info2 == null) {
                this.mInfosMap.put(Integer.valueOf(i), info);
            } else if (!info2.equal(info)) {
                info2.copy(info);
            } else {
                return;
            }
            Iterator<ExpandInfoController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().updateInfo(i, info);
            }
            int i2 = this.mSelectedType;
            if (i != i2) {
                return;
            }
            if (info.available) {
                this.mUnAvailableCnt = 0;
                return;
            }
            this.mUnAvailableCnt++;
            if (this.mUnAvailableCnt >= 3) {
                setSelectedType(0);
            } else {
                requestData(this.mBaseInfos.get(Integer.valueOf(i2)));
            }
        }
    }

    public void setSelectedType(int i) {
        if (this.mSelectedType != i) {
            this.mUnAvailableCnt = 0;
        }
        this.mSelectedType = i;
        if (this.mSuperPowerMode) {
            i = 16;
        }
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", i, KeyguardUpdateMonitor.getCurrentUser());
        Iterator<ExpandInfoController.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().updateSelectedType(i);
        }
    }

    public int getSelectedType() {
        if (this.mSuperPowerMode) {
            return 16;
        }
        return this.mSelectedType;
    }

    public ExpandInfoController.Info getSuperPowerInfo() {
        return this.mSuperPowerInfo.getInfo();
    }

    public void addCallback(ExpandInfoController.Callback callback) {
        if (!Constants.IS_INTERNATIONAL) {
            this.mCallbacks.add(callback);
            for (Map.Entry next : this.mInfosMap.entrySet()) {
                callback.updateInfo(((Integer) next.getKey()).intValue(), (ExpandInfoController.Info) next.getValue());
                callback.updateInfosMap();
                callback.updateSelectedType(this.mSelectedType);
            }
        }
    }

    public void requestData() {
        if (!Constants.IS_INTERNATIONAL) {
            if (this.mSuperPowerMode) {
                this.mSuperPowerInfo.requestData(this.mUserHandler);
                return;
            }
            this.mDataUsageInfo.requestData(this.mUserHandler);
            this.mDataBillInfo.requestData(this.mUserHandler);
            this.mHealthDataInfo.requestData(this.mUserHandler);
            this.mScreenTimeInfo.requestData(this.mUserHandler);
        }
    }

    private void requestData(BaseInfo baseInfo) {
        if (baseInfo != null) {
            baseInfo.requestData(this.mUserHandler);
        }
    }

    public void setSuperPowerMode(boolean z) {
        this.mSuperPowerMode = z;
        if (z) {
            this.mInfosMapOld.clear();
            this.mInfosMapOld.putAll(this.mInfosMap);
        }
        this.mInfosMap.clear();
        if (!z) {
            this.mInfosMap.putAll(this.mInfosMapOld);
        }
    }

    public void startActivity(String str) {
        if (!TextUtils.isEmpty(str)) {
            Intent intent = new Intent();
            intent.setAction(str);
            intent.putExtra("misettings_from_page", "controller_center");
            intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent);
        }
    }

    public void startActivityByUri(String str) {
        if (str != null) {
            try {
                Intent parseUri = Intent.parseUri(str, 0);
                parseUri.putExtra("misettings_from_page", "controller_center");
                parseUri.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                this.mActivityStarter.postStartActivityDismissingKeyguard(parseUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
