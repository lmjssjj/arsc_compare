package com.android.systemui.statusbar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.tuner.TunerService;

public abstract class HeaderView extends RelativeLayout implements View.OnClickListener, TunerService.Tunable {
    private ActivityStarter mActStarter;
    protected Clock mClock;
    protected Clock mDateView;
    protected int mLastOrientation;
    protected ImageView mShortcut;
    protected int mShortcutDestination;

    public abstract void regionChanged();

    public abstract void themeChanged();

    public HeaderView(Context context) {
        this(context, (AttributeSet) null);
    }

    public HeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLastOrientation = 1;
        this.mShortcutDestination = -1;
        this.mActStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        Clock clock = (Clock) findViewById(R.id.date_time);
        this.mDateView = clock;
        clock.setOnClickListener(this);
        Clock clock2 = (Clock) findViewById(R.id.big_time);
        this.mClock = clock2;
        clock2.setOnClickListener(this);
        ImageView imageView = (ImageView) findViewById(R.id.notification_shade_shortcut);
        this.mShortcut = imageView;
        imageView.setOnClickListener(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).useControlPanel();
        this.mShortcutDestination = 1;
        this.mShortcut.setImageResource(R.drawable.notch_settings);
        this.mShortcut.setContentDescription(getResources().getString(R.string.accessibility_settings));
    }

    public void updateShortCutVisible(boolean z) {
        int i = 8;
        if (!z) {
            this.mShortcut.setVisibility(8);
        } else if (Constants.IS_TABLET) {
            this.mShortcut.setVisibility(0);
        } else {
            ImageView imageView = this.mShortcut;
            if (this.mLastOrientation == 1) {
                i = 0;
            }
            imageView.setVisibility(i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x006a A[Catch:{ Exception -> 0x0083 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0077 A[Catch:{ Exception -> 0x0083 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.view.View r6) {
        /*
            r5 = this;
            com.android.systemui.statusbar.policy.Clock r0 = r5.mClock     // Catch:{ Exception -> 0x0083 }
            java.lang.String r1 = "android.intent.action.MAIN"
            r2 = 0
            if (r6 == r0) goto L_0x0055
            boolean r0 = com.android.systemui.Constants.IS_TABLET     // Catch:{ Exception -> 0x0083 }
            if (r0 != 0) goto L_0x0015
            int r0 = r5.mLastOrientation     // Catch:{ Exception -> 0x0083 }
            r3 = 2
            if (r0 != r3) goto L_0x0015
            com.android.systemui.statusbar.policy.Clock r0 = r5.mDateView     // Catch:{ Exception -> 0x0083 }
            if (r6 != r0) goto L_0x0015
            goto L_0x0055
        L_0x0015:
            com.android.systemui.statusbar.policy.Clock r0 = r5.mDateView     // Catch:{ Exception -> 0x0083 }
            if (r6 != r0) goto L_0x0028
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            r2.<init>(r1)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r6 = r5.getCalendarPkg()     // Catch:{ Exception -> 0x0083 }
            r2.setPackage(r6)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r6 = "date"
            goto L_0x0061
        L_0x0028:
            android.widget.ImageView r0 = r5.mShortcut     // Catch:{ Exception -> 0x0083 }
            if (r6 != r0) goto L_0x0053
            android.content.Intent r6 = r5.buildShortcutClickIntent()     // Catch:{ Exception -> 0x0083 }
            if (r6 == 0) goto L_0x0064
            java.lang.String r0 = "android.settings.ALL_APPS_NOTIFICATION_SETTINGS"
            java.lang.String r1 = r6.getAction()     // Catch:{ Exception -> 0x0083 }
            boolean r0 = r0.equals(r1)     // Catch:{ Exception -> 0x0083 }
            if (r0 == 0) goto L_0x0041
            java.lang.String r2 = "notification-settings"
            goto L_0x0064
        L_0x0041:
            java.lang.String r0 = "android.settings.SETTINGS"
            java.lang.String r1 = r6.getAction()     // Catch:{ Exception -> 0x0083 }
            boolean r0 = r0.equals(r1)     // Catch:{ Exception -> 0x0083 }
            if (r0 == 0) goto L_0x0050
            java.lang.String r2 = "settings"
            goto L_0x0064
        L_0x0050:
            java.lang.String r2 = "search"
            goto L_0x0064
        L_0x0053:
            r6 = r2
            goto L_0x0064
        L_0x0055:
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            r2.<init>(r1)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r6 = "com.android.deskclock"
            r2.setPackage(r6)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r6 = "clock"
        L_0x0061:
            r4 = r2
            r2 = r6
            r6 = r4
        L_0x0064:
            boolean r0 = android.text.TextUtils.isEmpty(r2)     // Catch:{ Exception -> 0x0083 }
            if (r0 != 0) goto L_0x0075
            java.lang.Class<com.android.systemui.miui.statusbar.analytics.SystemUIStat> r0 = com.android.systemui.miui.statusbar.analytics.SystemUIStat.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)     // Catch:{ Exception -> 0x0083 }
            com.android.systemui.miui.statusbar.analytics.SystemUIStat r0 = (com.android.systemui.miui.statusbar.analytics.SystemUIStat) r0     // Catch:{ Exception -> 0x0083 }
            r0.handleClickShortcutEvent(r2)     // Catch:{ Exception -> 0x0083 }
        L_0x0075:
            if (r6 == 0) goto L_0x0087
            r0 = 268435456(0x10000000, float:2.5243549E-29)
            r6.addFlags(r0)     // Catch:{ Exception -> 0x0083 }
            com.android.systemui.plugins.ActivityStarter r5 = r5.mActStarter     // Catch:{ Exception -> 0x0083 }
            r0 = 1
            r5.startActivity(r6, r0)     // Catch:{ Exception -> 0x0083 }
            goto L_0x0087
        L_0x0083:
            r5 = move-exception
            r5.printStackTrace()
        L_0x0087:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.HeaderView.onClick(android.view.View):void");
    }

    private Intent buildShortcutClickIntent() {
        int i = this.mShortcutDestination;
        if (i != 0) {
            if (i == 1) {
                return new Intent("android.settings.SETTINGS");
            }
            Intent intent = new Intent("android.settings.ALL_APPS_NOTIFICATION_SETTINGS");
            intent.setPackage("com.miui.notification");
            return intent;
        } else if (!Constants.IS_INTERNATIONAL) {
            Intent intent2 = new Intent("android.intent.action.SEARCH");
            intent2.setPackage("com.android.quicksearchbox");
            intent2.setData(Uri.parse("qsb://query?close_web_page=true&ref=systemui10"));
            return intent2;
        } else if (!Util.isBrowserSearchExist(getContext())) {
            return new Intent("android.intent.action.WEB_SEARCH");
        } else {
            Intent intent3 = new Intent("com.android.browser.browser_search");
            intent3.setPackage(Util.isBrowserGlobalEnabled(getContext()) ? "com.mi.globalbrowser" : "com.android.browser");
            return intent3;
        }
    }

    private String getCalendarPkg() {
        if (!Constants.IS_INTERNATIONAL) {
            return "com.android.calendar";
        }
        if (Util.isAppInstalledForUser(this.mContext, "com.xiaomi.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.xiaomi.calendar";
        }
        if (Util.isAppInstalledForUser(this.mContext, "com.android.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.android.calendar";
        }
        return "com.google.android.calendar";
    }
}
