package com.android.systemui.miui.controlcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.QSControlDetail;
import com.android.systemui.miui.controlcenter.policy.QCBrightnessMirrorController;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSTileView;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowView;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSControlCenterPanel extends FrameLayout implements ConfigurationController.ConfigurationListener {
    private boolean DEBUG = Constants.DEBUG;
    protected IStateStyle mAnim;
    protected AnimConfig mAnimConfig;
    private AutoBrightnessView mAutoBrightnessView;
    private float mBaseTransitionY;
    private QSBigTileView mBigTile0;
    private QSBigTileView mBigTile1;
    private QSBigTileView mBigTile2;
    private QSBigTileView mBigTile3;
    private HashMap<Integer, ArrayList<IStateStyle>> mBigTileAnimArr;
    private HashMap<Integer, ArrayList<IStateStyle>> mBigTileTransAnimArr;
    private BrightnessController mBrightnessController;
    private QCBrightnessMirrorController mBrightnessMirrorController;
    private QCToggleSliderView mBrightnessView;
    private Context mContext;
    private QSControlFooter mControlFooter;
    private ControlPanelContentView mControlPanelContentView;
    private ControlPanelWindowView mControlPanelWindowView;
    private QSControlDetail.QSPanelCallback mDetailCallback;
    private QSPanel.Record mDetailRecord;
    private View mEditTiles;
    /* access modifiers changed from: private */
    public int mExpandHeightThres;
    private ImageView mExpandIndicator;
    private ImageView mExpandIndicatorBottom;
    private QSControlExpandTileView mExpandTileView;
    private boolean mExpanded;
    private LinearLayout mFootPanel;
    private int mFootPanelBaseIdx;
    protected IStateStyle mFootPanelTransAnim;
    private final H mHandler = new H();
    private QSControlCenterHeaderView mHeader;
    protected AnimState mHideAnim;
    protected QSControlTileHost mHost;
    private float mInitialTouchX = -1.0f;
    private float mInitialTouchY = -1.0f;
    private AutoBrightnessView mLandAutoBrightnessView;
    private BrightnessController mLandBrightnessController;
    private QCToggleSliderView mLandBrightnessView;
    private QSControlFooter mLandCtrFooter;
    private LinearLayout mLandFootPanel;
    private QCBrightnessMirrorController mLandMirrorController;
    private boolean mListening;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private int mOrientation;
    protected AnimConfig mPanelAnimConfig;
    private HashMap<Integer, IStateStyle> mPanelAnimMap;
    private ControlPanelController mPanelController;
    protected AnimState mPanelHideAnim;
    protected AnimState mPanelShowAnim;
    private HashMap<Integer, IStateStyle> mPanelTransAnimMap;
    private LinearLayout mQSBrightnessLayout;
    private FrameLayout mQSContainer;
    private QSControlScrollView mQsControlScrollView;
    /* access modifiers changed from: private */
    public QSControlCenterTileLayout mQuickQsControlCenterTileLayout;
    private int mScreenHeight;
    protected AnimState mShowAnim;
    private View mTileView0;
    private int mTransLineNum;
    private ArrayList<View> mTransViews;
    private VelocityTracker mVelocityTracker;
    private ArrayList<View> mViews;

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public QSControlCenterPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Display display = null;
        this.mDetailCallback = null;
        this.mViews = new ArrayList<>();
        this.mPanelAnimMap = new HashMap<>();
        this.mBigTileAnimArr = new HashMap<>();
        this.mPanelTransAnimMap = new HashMap<>();
        this.mBigTileTransAnimArr = new HashMap<>();
        this.mTransViews = new ArrayList<>();
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        WindowManager windowManager = (WindowManager) this.mContext.getApplicationContext().getSystemService("window");
        display = windowManager != null ? windowManager.getDefaultDisplay() : display;
        if (display != null) {
            display.getRealSize(new Point());
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHeader = (QSControlCenterHeaderView) findViewById(R.id.header);
        this.mEditTiles = findViewById(R.id.tiles_edit);
        this.mQSContainer = (FrameLayout) findViewById(R.id.qs_container);
        this.mQsControlScrollView = (QSControlScrollView) findViewById(R.id.scroll_container);
        this.mQuickQsControlCenterTileLayout = (QSControlCenterTileLayout) findViewById(R.id.quick_tile_layout);
        this.mQuickQsControlCenterTileLayout.setQSControlCenterPanel(this);
        this.mFootPanel = (LinearLayout) findViewById(R.id.foot_panel);
        this.mControlFooter = (QSControlFooter) findViewById(R.id.settings_footer);
        this.mLandFootPanel = (LinearLayout) findViewById(R.id.foot_panel_land);
        this.mLandCtrFooter = (QSControlFooter) findViewById(R.id.land_footer);
        this.mQSBrightnessLayout = (LinearLayout) findViewById(R.id.qs_brightness_container);
        this.mBrightnessView = (QCToggleSliderView) findViewById(R.id.qs_brightness);
        this.mLandBrightnessView = (QCToggleSliderView) findViewById(R.id.qs_brightness_land);
        this.mBrightnessController = new BrightnessController(getContext(), this.mBrightnessView);
        this.mLandBrightnessController = new BrightnessController(getContext(), this.mLandBrightnessView);
        this.mExpandIndicator = (ImageView) findViewById(R.id.qs_expand_indicator);
        this.mExpandIndicator.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        this.mExpandIndicatorBottom = (ImageView) findViewById(R.id.qs_expand_indicator_bottom);
        this.mAutoBrightnessView = (AutoBrightnessView) findViewById(R.id.auto_brightness);
        this.mLandAutoBrightnessView = (AutoBrightnessView) findViewById(R.id.auto_brightness_land);
        this.mExpandTileView = (QSControlExpandTileView) findViewById(R.id.expand_tile);
        if (Constants.IS_INTERNATIONAL) {
            this.mExpandTileView.setVisibility(8);
            this.mBigTile0 = (QSBigTileView) findViewById(R.id.big_tile_0);
            this.mBigTile0.setVisibility(0);
            QSBigTileView qSBigTileView = this.mBigTile0;
            this.mTileView0 = qSBigTileView;
            qSBigTileView.init(this, "cell", 0);
            this.mBigTile1 = (QSBigTileView) findViewById(R.id.big_tile_1);
            this.mBigTile1.init(this, "wifi", 1);
            this.mBigTile2 = (QSBigTileView) findViewById(R.id.big_tile_2);
            this.mBigTile2.init(this, "bt", 2);
            this.mBigTile3 = (QSBigTileView) findViewById(R.id.big_tile_3);
            this.mBigTile3.init(this, "flashlight", 3);
        } else {
            this.mTileView0 = this.mExpandTileView;
            this.mBigTile1 = (QSBigTileView) findViewById(R.id.big_tile_1);
            this.mBigTile1.init(this, "bt", 1);
            this.mBigTile2 = (QSBigTileView) findViewById(R.id.big_tile_2);
            this.mBigTile2.init(this, "cell", 2);
            this.mBigTile3 = (QSBigTileView) findViewById(R.id.big_tile_3);
            this.mBigTile3.init(this, "wifi", 3);
        }
        initAnimState();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mMinimumVelocity = 500;
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    public void setControlPanelContentView(ControlPanelContentView controlPanelContentView) {
        this.mControlPanelContentView = controlPanelContentView;
        this.mBrightnessMirrorController = new QCBrightnessMirrorController(controlPanelContentView);
        this.mLandMirrorController = new QCBrightnessMirrorController(controlPanelContentView);
        this.mControlPanelContentView.getDetailView().setQsPanel(this);
        this.mBrightnessView.setMirror((QCToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(R.id.brightness_slider));
        this.mBrightnessView.setMirrorController(this.mBrightnessMirrorController);
        this.mLandBrightnessView.setMirror((QCToggleSliderView) this.mLandMirrorController.getMirror().findViewById(R.id.brightness_slider));
        this.mLandBrightnessView.setMirrorController(this.mLandMirrorController);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mExpandIndicatorBottom.getLayoutParams();
        layoutParams.bottomMargin = windowInsets.getStableInsetBottom();
        this.mExpandIndicatorBottom.setLayoutParams(layoutParams);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), windowInsets.getStableInsetBottom());
        return super.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    public void onUserSwitched(int i) {
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView2 = this.mBigTile1;
        if (qSBigTileView2 != null) {
            qSBigTileView2.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView3 = this.mBigTile2;
        if (qSBigTileView3 != null) {
            qSBigTileView3.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView4 = this.mBigTile3;
        if (qSBigTileView4 != null) {
            qSBigTileView4.onUserSwitched(i);
        }
        this.mAutoBrightnessView.onUserSwitched(i);
        this.mLandAutoBrightnessView.onUserSwitched(i);
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
        this.mControlFooter.setHostEnvironment(qSControlTileHost);
        this.mLandCtrFooter.setHostEnvironment(qSControlTileHost);
        this.mQuickQsControlCenterTileLayout.setHost(qSControlTileHost);
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.setHost(qSControlTileHost);
        }
        this.mBigTile1.setHost(qSControlTileHost);
        this.mBigTile2.setHost(qSControlTileHost);
        this.mBigTile3.setHost(qSControlTileHost);
        this.mAutoBrightnessView.setHost(qSControlTileHost);
        this.mLandAutoBrightnessView.setHost(qSControlTileHost);
        this.mOrientation = getResources().getConfiguration().orientation;
        onOrientationChanged(this.mOrientation, true);
    }

    public void clickTile(ComponentName componentName) {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.mQuickQsControlCenterTileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.clickTile(componentName);
        }
    }

    private boolean isOrientationPortrait() {
        return this.mOrientation == 1;
    }

    public void onConfigChanged(Configuration configuration) {
        onOrientationChanged(getResources().getConfiguration().orientation, false);
    }

    public void onOrientationChanged(int i, boolean z) {
        if (z || this.mOrientation != i) {
            updateScreenHeight();
            updateFootPanelLayout();
            this.mOrientation = i;
            this.mExpandIndicatorBottom.setAlpha(0.0f);
            this.mFootPanel.setAlpha(1.0f);
            int i2 = 0;
            this.mFootPanel.setVisibility(0);
            setBrightnessListening(this.mExpanded);
            setPadding(getPaddingLeft(), this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_header_paddingTop), getPaddingRight(), getPaddingBottom());
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mQSContainer.getLayoutParams();
            this.mQuickQsControlCenterTileLayout.setTranslationY(0.0f);
            int i3 = 5;
            if (i == 1) {
                this.mFootPanel.setVisibility(0);
                this.mControlFooter.setForceHide(false);
                this.mControlFooter.refreshState();
                this.mLandFootPanel.setVisibility(8);
                this.mLandCtrFooter.setForceHide(true);
                this.mLandCtrFooter.refreshState();
                this.mQSBrightnessLayout.setVisibility(0);
                this.mQuickQsControlCenterTileLayout.setBaseLineIdx(4);
                if (!this.mPanelController.isSuperPowerMode()) {
                    i3 = 6;
                }
                this.mFootPanelBaseIdx = i3;
                this.mTransLineNum = 4;
                this.mBrightnessMirrorController.updateResources();
                this.mQuickQsControlCenterTileLayout.setExpanded(false);
                this.mFootPanel.setTranslationY(0.0f);
                ImageView imageView = this.mExpandIndicator;
                if (this.mPanelController.isSuperPowerMode()) {
                    i2 = 8;
                }
                imageView.setVisibility(i2);
                this.mExpandIndicator.setAlpha(1.0f);
                layoutParams.height = -2;
                this.mQSContainer.setLayoutParams(layoutParams);
                this.mEditTiles.setScaleX(1.0f);
                this.mEditTiles.setScaleY(1.0f);
                this.mEditTiles.setAlpha(1.0f);
                this.mAutoBrightnessView.setScaleX(1.0f);
                this.mAutoBrightnessView.setScaleY(1.0f);
                this.mAutoBrightnessView.setAlpha(1.0f);
                this.mBrightnessView.setScaleX(1.0f);
                this.mBrightnessView.setScaleY(1.0f);
                this.mBrightnessView.setAlpha(1.0f);
                this.mControlFooter.resetViews();
            } else {
                this.mFootPanel.setVisibility(8);
                this.mControlFooter.setForceHide(true);
                this.mControlFooter.refreshState();
                this.mLandFootPanel.setVisibility(0);
                this.mLandCtrFooter.setForceHide(false);
                this.mLandCtrFooter.refreshState();
                this.mFootPanelBaseIdx = 4;
                this.mTransLineNum = 5;
                this.mQuickQsControlCenterTileLayout.setBaseLineIdx(5);
                this.mQuickQsControlCenterTileLayout.setExpanded(true);
                this.mLandMirrorController.updateResources();
                this.mQSBrightnessLayout.setVisibility(8);
                this.mExpandIndicator.setVisibility(8);
                this.mFootPanel.setTranslationY(0.0f);
                layoutParams.height = (int) (((float) this.mQuickQsControlCenterTileLayout.getMaxHeight()) + this.mQuickQsControlCenterTileLayout.getTranslationY());
                this.mQSContainer.setLayoutParams(layoutParams);
                this.mLandAutoBrightnessView.setScaleX(1.0f);
                this.mLandAutoBrightnessView.setScaleY(1.0f);
                this.mLandAutoBrightnessView.setAlpha(1.0f);
                this.mLandCtrFooter.resetViews();
            }
            updateViews();
        }
    }

    public void updateResources() {
        this.mHeader.updateResources();
        this.mExpandTileView.updateResources();
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.updateResources();
        }
        this.mBigTile1.updateResources();
        this.mBigTile2.updateResources();
        this.mBigTile3.updateResources();
        this.mAutoBrightnessView.updateResources();
        this.mLandAutoBrightnessView.updateResources();
        this.mBrightnessView.updateResources();
        this.mLandBrightnessView.updateResources();
        this.mExpandIndicator.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_tiles_indicator));
        this.mExpandIndicatorBottom.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_tiles_indicator));
        this.mQuickQsControlCenterTileLayout.updateResources();
        this.mControlFooter.updateResources();
        this.mLandCtrFooter.updateResources();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "dispatchTouchEvent " + motionEvent.getAction());
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "onInterceptTouchEvent " + motionEvent.getAction());
        }
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        if (!isOrientationPortrait()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialTouchX = rawX;
            this.mInitialTouchY = rawY;
            this.mQuickQsControlCenterTileLayout.startMove();
            this.mBaseTransitionY = this.mFootPanel.getTranslationY();
            this.mExpandIndicatorBottom.getBoundsOnScreen(new Rect());
        } else if (actionMasked == 2) {
            if (!isBigTileTouched() && this.mQuickQsControlCenterTileLayout.isCollapsed() && Math.abs(motionEvent.getRawY() - this.mInitialTouchY) > Math.abs(motionEvent.getRawX() - this.mInitialTouchX)) {
                return true;
            }
            if ((motionEvent.getRawY() < this.mInitialTouchY && this.mQuickQsControlCenterTileLayout.isExpanded() && (this.mQsControlScrollView.isScrolledToBottom() || (this.mQsControlScrollView.isScrolledToTop() && !this.mQuickQsControlCenterTileLayout.canScroll()))) || this.mQuickQsControlCenterTileLayout.isExpanding()) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        if (r2 != 3) goto L_0x00ed;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            boolean r0 = r5.DEBUG
            if (r0 == 0) goto L_0x001e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onTouchEvent "
            r0.append(r1)
            int r1 = r6.getAction()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "QSControlCenterPanel"
            android.util.Log.d(r1, r0)
        L_0x001e:
            float r0 = r6.getRawX()
            float r1 = r6.getRawY()
            boolean r2 = r5.isOrientationPortrait()
            r3 = 1
            if (r2 != 0) goto L_0x002e
            return r3
        L_0x002e:
            r5.initVelocityTrackerIfNotExists()
            android.view.VelocityTracker r2 = r5.mVelocityTracker
            r2.addMovement(r6)
            int r2 = r6.getActionMasked()
            if (r2 == 0) goto L_0x00d2
            r0 = 2
            if (r2 == r3) goto L_0x007a
            if (r2 == r0) goto L_0x0046
            r6 = 3
            if (r2 == r6) goto L_0x007a
            goto L_0x00ed
        L_0x0046:
            float r0 = r6.getRawY()
            float r2 = r5.mInitialTouchY
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 >= 0) goto L_0x005f
            com.android.systemui.miui.controlcenter.QSControlCenterTileLayout r0 = r5.mQuickQsControlCenterTileLayout
            boolean r0 = r0.isCollapsed()
            if (r0 == 0) goto L_0x005f
            com.android.systemui.miui.statusbar.phone.ControlPanelWindowView r5 = r5.mControlPanelWindowView
            r5.onTouchEvent(r6)
            goto L_0x00ed
        L_0x005f:
            float r6 = r5.mInitialTouchY
            float r1 = r1 - r6
            miuix.animation.IStateStyle r6 = r5.mFootPanelTransAnim
            miuix.animation.property.FloatProperty[] r0 = new miuix.animation.property.FloatProperty[r3]
            r2 = 0
            miuix.animation.property.ViewProperty r4 = miuix.animation.property.ViewProperty.TRANSLATION_Y
            r0[r2] = r4
            r6.cancel(r0)
            float r6 = r5.mBaseTransitionY
            float r6 = r6 + r1
            int r0 = r5.mExpandHeightThres
            float r0 = (float) r0
            float r6 = r6 / r0
            r5.setTransRatio(r6)
            goto L_0x00ed
        L_0x007a:
            com.android.systemui.miui.controlcenter.QSControlCenterTileLayout r6 = r5.mQuickQsControlCenterTileLayout
            boolean r6 = r6.isExpanding()
            if (r6 == 0) goto L_0x00ce
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r1 = 1000(0x3e8, float:1.401E-42)
            int r2 = r5.mMaximumVelocity
            float r2 = (float) r2
            r6.computeCurrentVelocity(r1, r2)
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            float r6 = r6.getYVelocity()
            float r1 = java.lang.Math.abs(r6)
            int r2 = r5.mMinimumVelocity
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            r2 = 0
            if (r1 <= 0) goto L_0x00a6
            int r1 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r1 <= 0) goto L_0x00a6
            r5.toBottomAnimation()
            goto L_0x00ce
        L_0x00a6:
            float r1 = java.lang.Math.abs(r6)
            int r4 = r5.mMinimumVelocity
            float r4 = (float) r4
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 <= 0) goto L_0x00b9
            int r6 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r6 >= 0) goto L_0x00b9
            r5.toTopAnimation()
            goto L_0x00ce
        L_0x00b9:
            android.widget.LinearLayout r6 = r5.mFootPanel
            float r6 = r6.getTranslationY()
            int r1 = r5.mExpandHeightThres
            int r1 = r1 / r0
            float r0 = (float) r1
            int r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
            if (r6 < 0) goto L_0x00cb
            r5.toBottomAnimation()
            goto L_0x00ce
        L_0x00cb:
            r5.toTopAnimation()
        L_0x00ce:
            r5.recycleVelocityTracker()
            goto L_0x00ed
        L_0x00d2:
            r5.mInitialTouchX = r0
            r5.mInitialTouchY = r1
            com.android.systemui.miui.controlcenter.QSControlCenterTileLayout r6 = r5.mQuickQsControlCenterTileLayout
            r6.startMove()
            android.widget.LinearLayout r6 = r5.mFootPanel
            float r6 = r6.getTranslationY()
            r5.mBaseTransitionY = r6
            android.graphics.Rect r6 = new android.graphics.Rect
            r6.<init>()
            android.widget.ImageView r5 = r5.mExpandIndicatorBottom
            r5.getBoundsOnScreen(r6)
        L_0x00ed:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.QSControlCenterPanel.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void toBottomAnimation() {
        this.mFootPanelTransAnim.cancel(ViewProperty.TRANSLATION_Y);
        AnimState animState = new AnimState("foot_panel_trans");
        animState.add(ViewProperty.TRANSLATION_Y, this.mExpandHeightThres, new long[0]);
        AnimConfig animConfig = new AnimConfig();
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                qSControlCenterPanel.setTransRatio(f / ((float) qSControlCenterPanel.mExpandHeightThres));
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setTransRatio(1.0f);
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent("expand_quick_tiles");
                QSControlCenterPanel.this.mQuickQsControlCenterTileLayout.setExpanded(true);
            }
        });
        this.mFootPanelTransAnim.to(animState, animConfig);
    }

    private void toTopAnimation() {
        this.mFootPanelTransAnim.cancel(ViewProperty.TRANSLATION_Y);
        AnimState animState = new AnimState("foot_panel_trans");
        animState.add(ViewProperty.TRANSLATION_Y, 0, new long[0]);
        AnimConfig animConfig = new AnimConfig();
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                qSControlCenterPanel.setTransRatio(f / ((float) qSControlCenterPanel.mExpandHeightThres));
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setTransRatio(0.0f);
                QSControlCenterPanel.this.mQuickQsControlCenterTileLayout.setExpanded(false);
            }
        });
        this.mFootPanelTransAnim.to(animState, animConfig);
    }

    /* access modifiers changed from: private */
    public void setTransRatio(float f) {
        if (!this.mPanelController.isSuperPowerMode()) {
            float min = Math.min(1.0f, Math.max(0.0f, f));
            float f2 = 1.0f - min;
            this.mFootPanel.setAlpha(f2);
            this.mFootPanel.setTranslationY(((float) this.mExpandHeightThres) * min);
            if ((this.mFootPanel.getVisibility() == 0 && f2 == 0.0f) || (this.mFootPanel.getVisibility() != 0 && f2 > 0.0f)) {
                this.mFootPanel.setVisibility(f2 == 0.0f ? 4 : 0);
            }
            this.mQuickQsControlCenterTileLayout.setExpandRatio(min);
            this.mQsControlScrollView.srcollTotratio(min);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r1.mBigTile0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isBigTileTouched() {
        /*
            r1 = this;
            com.android.systemui.miui.controlcenter.QSControlExpandTileView r0 = r1.mExpandTileView
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.miui.controlcenter.QSBigTileView r0 = r1.mBigTile0
            if (r0 == 0) goto L_0x0012
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
        L_0x0012:
            com.android.systemui.miui.controlcenter.QSBigTileView r0 = r1.mBigTile1
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.miui.controlcenter.QSBigTileView r0 = r1.mBigTile2
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.miui.controlcenter.QSBigTileView r1 = r1.mBigTile3
            boolean r1 = r1.isClicked()
            if (r1 == 0) goto L_0x002b
            goto L_0x002d
        L_0x002b:
            r1 = 0
            goto L_0x002e
        L_0x002d:
            r1 = 1
        L_0x002e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.QSControlCenterPanel.isBigTileTouched():boolean");
    }

    public void setControlPanelWindowView(ControlPanelWindowView controlPanelWindowView) {
        this.mControlPanelWindowView = controlPanelWindowView;
    }

    private void updateScreenHeight() {
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenHeight = Math.max(point.y, point.x);
    }

    public void setQSDetailCallback(QSControlDetail.QSPanelCallback qSPanelCallback) {
        this.mDetailCallback = qSPanelCallback;
    }

    private void updateFootPanelLayout() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mFootPanel.getLayoutParams();
        int footPanelMarginTop = getFootPanelMarginTop() - (this.mPanelController.isSuperPowerMode() ? this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_height) + CCQSTileView.getTextHeight(getContext()) : 0);
        layoutParams.topMargin = footPanelMarginTop;
        this.mFootPanel.setLayoutParams(layoutParams);
        updateExpandHeightThres();
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "updateFootPanelLayout screenHeight:" + this.mScreenHeight + " topMargin:" + footPanelMarginTop + "  thres:" + this.mExpandHeightThres + "mOrientation:" + this.mOrientation);
        }
    }

    public void updateExpandHeightThres() {
        this.mExpandHeightThres = (this.mScreenHeight - getFootPanelMarginTop()) - this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_header_paddingTop);
        this.mExpandHeightThres = Math.min(this.mExpandHeightThres, this.mQuickQsControlCenterTileLayout.caculateExpandHeight());
        this.mQuickQsControlCenterTileLayout.setExpandHeightThres(this.mExpandHeightThres);
    }

    private int getFootPanelMarginTop() {
        TextView textView = new TextView(getContext());
        textView.setTextAppearance(R.style.TextAppearance_QSControl_Clock);
        Paint.FontMetrics fontMetrics = textView.getPaint().getFontMetrics();
        return this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_foot_panel_margin_top) + ((int) (fontMetrics.descent - fontMetrics.ascent));
    }

    public void showDetail(boolean z, QSPanel.Record record) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, record).sendToTarget();
    }

    public void closeDetail(boolean z) {
        QSPanel.Record record = this.mDetailRecord;
        if (record == null || !(record instanceof QSPanel.TileRecord)) {
            QSPanel.Record record2 = this.mDetailRecord;
            if (record2 != null) {
                showDetail(false, record2);
                return;
            }
            return;
        }
        QSTile qSTile = ((QSPanel.TileRecord) record).tile;
        if (qSTile instanceof QSTileImpl) {
            ((QSTileImpl) qSTile).showDetail(false);
        }
    }

    /* access modifiers changed from: protected */
    public void handleShowDetail(QSPanel.Record record, boolean z) {
        View view;
        if (record instanceof QSPanel.TileRecord) {
            handleShowDetailTile((QSPanel.TileRecord) record, z);
            return;
        }
        View view2 = record.wholeView;
        if (view2 != null && (view = record.translateView) != null) {
            handleShowDetailImpl(record, z, view2, view);
        }
    }

    private void handleShowDetailTile(QSPanel.TileRecord tileRecord, boolean z) {
        if ((this.mDetailRecord != null) != z || this.mDetailRecord != tileRecord) {
            if (z) {
                tileRecord.detailAdapter = tileRecord.tile.getDetailAdapter();
                if (tileRecord.detailAdapter == null) {
                    return;
                }
            }
            tileRecord.tile.setDetailListening(z);
            handleShowDetailImpl(tileRecord, z, tileRecord.tileView, tileRecord.expandIndicator);
        }
    }

    private void handleShowDetailImpl(QSPanel.Record record, boolean z, View view, View view2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? record : null);
        if (z) {
            detailAdapter = record.detailAdapter;
        }
        fireShowingDetail(detailAdapter, view, view2);
        if (z) {
            this.mAnim.cancel();
            this.mAnim.to(this.mHideAnim, this.mAnimConfig);
            return;
        }
        this.mAnim.cancel();
        this.mAnim.to(this.mShowAnim, this.mAnimConfig);
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(QSPanel.Record record) {
        if (record != this.mDetailRecord) {
            this.mDetailRecord = record;
            QSPanel.Record record2 = this.mDetailRecord;
            fireScanStateChanged((record2 instanceof QSPanel.TileRecord) && ((QSPanel.TileRecord) record2).scanState);
        }
    }

    public void fireShowingDetail(DetailAdapter detailAdapter, View view, View view2) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onShowingDetail(detailAdapter, view, view2);
        }
    }

    public void fireToggleStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onToggleStateChanged(z);
        }
    }

    public void fireScanStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onScanStateChanged(z);
        }
    }

    public void setExpand(boolean z, boolean z2) {
        if (z != this.mExpanded) {
            this.mQuickQsControlCenterTileLayout.visStartInit();
        }
        this.mExpanded = z;
        if (isOrientationPortrait()) {
            this.mFootPanel.setVisibility(0);
        }
        if (this.mQuickQsControlCenterTileLayout.isExpanded() && !z) {
            onOrientationChanged(this.mOrientation, true);
        }
        if (z) {
            if (isOrientationPortrait()) {
                setTransRatio(0.0f);
            }
            this.mExpandIndicatorBottom.setAlpha(0.0f);
        }
        setListening(z);
        if (z2) {
            panelAnimateOn(z);
        }
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            QSBigTileView qSBigTileView = this.mBigTile0;
            if (qSBigTileView != null) {
                qSBigTileView.handleSetListening(z);
            }
            this.mBigTile1.handleSetListening(z);
            this.mBigTile2.handleSetListening(z);
            this.mBigTile3.handleSetListening(z);
            setBrightnessListening(z);
            boolean z2 = true;
            this.mLandCtrFooter.setListening(z && !isOrientationPortrait());
            QSControlFooter qSControlFooter = this.mControlFooter;
            if (!z || !isOrientationPortrait()) {
                z2 = false;
            }
            qSControlFooter.setListening(z2);
        }
    }

    public void setBrightnessListening(boolean z) {
        if (isOrientationPortrait()) {
            this.mLandAutoBrightnessView.handleSetListening(false);
            this.mLandBrightnessController.unregisterCallbacks();
            this.mAutoBrightnessView.handleSetListening(z);
            if (z) {
                this.mBrightnessController.registerCallbacks();
            } else {
                this.mBrightnessController.unregisterCallbacks();
            }
        } else {
            this.mAutoBrightnessView.handleSetListening(false);
            this.mBrightnessController.unregisterCallbacks();
            this.mLandAutoBrightnessView.handleSetListening(z);
            if (z) {
                this.mLandBrightnessController.registerCallbacks();
            } else {
                this.mLandBrightnessController.unregisterCallbacks();
            }
        }
    }

    public void updateTransHeight(float f) {
        float f2 = f;
        if (isOrientationPortrait()) {
            Set<Integer> keySet = this.mPanelTransAnimMap.keySet();
            Set<Integer> keySet2 = this.mBigTileTransAnimArr.keySet();
            if (this.mPanelTransAnimMap.size() > 0) {
                for (Integer num : keySet) {
                    HashMap<Integer, IStateStyle> hashMap = this.mPanelTransAnimMap;
                    hashMap.get(num).cancel(ViewProperty.TRANSLATION_Y);
                }
            }
            if (this.mBigTileTransAnimArr.size() > 0) {
                for (Integer num2 : keySet2) {
                    Iterator it = this.mBigTileTransAnimArr.get(num2).iterator();
                    while (it.hasNext()) {
                        ((IStateStyle) it.next()).cancel(ViewProperty.TRANSLATION_Y);
                    }
                }
            }
            if (f2 == 0.0f) {
                float f3 = 0.1f;
                float f4 = 0.5f;
                float f5 = 0.2f;
                float f6 = 0.7f;
                if (this.mPanelTransAnimMap.size() > 0) {
                    for (Integer next : keySet) {
                        AnimState animState = new AnimState("control_panel_trans");
                        animState.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
                        float intValue = ((((float) next.intValue()) * f3) / ((float) this.mTransLineNum)) + f4;
                        AnimConfig animConfig = new AnimConfig();
                        animConfig.setEase(EaseManager.getStyle(-2, ((((float) next.intValue()) * 0.2f) / ((float) this.mTransLineNum)) + 0.7f, intValue));
                        this.mPanelTransAnimMap.get(next).to(animState, animConfig);
                        f3 = 0.1f;
                        f4 = 0.5f;
                    }
                }
                if (this.mBigTileTransAnimArr.size() > 0) {
                    for (Integer next2 : keySet2) {
                        Iterator it2 = this.mBigTileTransAnimArr.get(next2).iterator();
                        while (it2.hasNext()) {
                            AnimState animState2 = new AnimState("control_panel_trans");
                            animState2.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
                            float intValue2 = ((((float) next2.intValue()) * f5) / ((float) this.mTransLineNum)) + f6;
                            AnimConfig animConfig2 = new AnimConfig();
                            animConfig2.setEase(EaseManager.getStyle(-2, intValue2, ((((float) next2.intValue()) * 0.1f) / ((float) this.mTransLineNum)) + 0.5f));
                            ((IStateStyle) it2.next()).to(animState2, animConfig2);
                            f5 = 0.2f;
                            f6 = 0.7f;
                        }
                    }
                }
            } else {
                int i = this.mScreenHeight;
                this.mTransViews.forEach(new Consumer(Math.max(0.0f, Math.min(f2, (float) i)), i) {
                    private final /* synthetic */ float f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void accept(Object obj) {
                        QSControlCenterPanel.this.lambda$updateTransHeight$0$QSControlCenterPanel(this.f$1, this.f$2, (View) obj);
                    }
                });
            }
            this.mQuickQsControlCenterTileLayout.updateTransHeight(f2, this.mScreenHeight, this.mTransLineNum);
        }
    }

    public /* synthetic */ void lambda$updateTransHeight$0$QSControlCenterPanel(float f, int i, View view) {
        view.setTranslationY(Utils.getTranslationY(((Integer) view.getTag(R.id.tag_control_center_trans)).intValue(), this.mTransLineNum, f, (float) i));
    }

    /* access modifiers changed from: protected */
    public void initAnimState() {
        this.mAnim = Folme.useAt(this).state();
        this.mFootPanelTransAnim = Folme.useAt(this.mFootPanel).state();
        AnimState animState = new AnimState("control_center_detail_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        this.mShowAnim = animState;
        AnimState animState2 = new AnimState("control_center_detail_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        this.mHideAnim = animState2;
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
        animConfig.addListeners(new TransitionListener() {
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlCenterPanel.this.setLayerType(2, (Paint) null);
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setLayerType(0, (Paint) null);
            }
        });
        this.mAnimConfig = animConfig;
        AnimState animState3 = new AnimState("control_panel_show");
        animState3.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState3.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState3.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        this.mPanelShowAnim = animState3;
        AnimState animState4 = new AnimState("control_panel_hide");
        animState4.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState4.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState4.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        this.mPanelHideAnim = animState4;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(EaseManager.getStyle(-2, 300.0f, 0.99f, 0.6666f));
        this.mPanelAnimConfig = animConfig2;
        updateViews();
    }

    private void updateViews() {
        this.mViews.clear();
        this.mTransViews.clear();
        this.mPanelAnimMap.clear();
        this.mBigTileAnimArr.clear();
        this.mPanelTransAnimMap.clear();
        this.mBigTileTransAnimArr.clear();
        this.mTransLineNum = (isOrientationPortrait() ? 6 : 5) + this.mQuickQsControlCenterTileLayout.getShowLines();
        addAnimateView(findViewById(R.id.carrier_text), 0);
        addAnimateView(findViewById(R.id.system_icon_area), 0);
        if (!this.mPanelController.isSuperPowerMode()) {
            addAnimateView(findViewById(R.id.notification_shade_shortcut), 1);
        }
        addAnimateView(findViewById(R.id.date_time), 1);
        addAnimateView(findViewById(R.id.big_time), 1);
        addAnimateView(this.mTileView0, 2);
        addAnimateView(this.mBigTile1, 2);
        addAnimateView(this.mBigTile2, 3);
        addAnimateView(this.mBigTile3, 3);
        if (isOrientationPortrait()) {
            addAnimateView(findViewById(R.id.auto_brightness), this.mFootPanelBaseIdx);
            addAnimateView(findViewById(R.id.qs_brightness), this.mFootPanelBaseIdx);
            addAnimateView(this.mControlFooter.findViewById(R.id.footer_text), this.mFootPanelBaseIdx);
            addAnimateView(this.mControlFooter.findViewById(R.id.footer_icon), this.mFootPanelBaseIdx);
            if (!this.mPanelController.isSuperPowerMode()) {
                addAnimateView(findViewById(R.id.qs_expand_indicator), this.mFootPanelBaseIdx - 1);
            }
        } else {
            addAnimateView(findViewById(R.id.auto_brightness_land), this.mFootPanelBaseIdx);
            addAnimateView(findViewById(R.id.qs_brightness_land), this.mFootPanelBaseIdx);
            addAnimateView(this.mLandCtrFooter.findViewById(R.id.footer_text), this.mFootPanelBaseIdx);
            addAnimateView(this.mLandCtrFooter.findViewById(R.id.footer_icon), this.mFootPanelBaseIdx);
        }
        for (int i = 0; i < 7; i++) {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            Iterator<View> it = this.mViews.iterator();
            while (it.hasNext()) {
                View next = it.next();
                if (((Integer) next.getTag(R.id.tag_control_center)).intValue() == i) {
                    if (i == 2 || i == 3) {
                        arrayList2.add(Folme.useAt(next).state());
                        this.mBigTileAnimArr.put(Integer.valueOf(i), arrayList2);
                    } else {
                        arrayList.add(next);
                    }
                }
            }
            if (arrayList.size() > 0) {
                this.mPanelAnimMap.put(Integer.valueOf(i), Folme.useAt((View[]) arrayList.toArray(new View[arrayList.size()])).state());
            }
        }
        addTransAnimateView(this.mHeader.findViewById(R.id.panel_header), 0);
        addTransAnimateView(this.mHeader.findViewById(R.id.tiles_header), 1);
        addTransAnimateView(this.mTileView0, 2);
        addTransAnimateView(this.mBigTile1, 2);
        addTransAnimateView(this.mBigTile2, 3);
        addTransAnimateView(this.mBigTile3, 3);
        if (isOrientationPortrait()) {
            addTransAnimateView(findViewById(R.id.auto_brightness), this.mTransLineNum - 2);
            addTransAnimateView(findViewById(R.id.qs_brightness), this.mTransLineNum - 2);
            addTransAnimateView(this.mControlFooter.findViewById(R.id.footer_text), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mControlFooter.findViewById(R.id.footer_icon), this.mFootPanelBaseIdx);
            if (!this.mPanelController.isSuperPowerMode()) {
                addTransAnimateView(findViewById(R.id.qs_expand_indicator), this.mTransLineNum - 1);
            }
        } else {
            addTransAnimateView(findViewById(R.id.auto_brightness_land), this.mFootPanelBaseIdx);
            addTransAnimateView(findViewById(R.id.qs_brightness_land), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mLandCtrFooter.findViewById(R.id.footer_text), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mLandCtrFooter.findViewById(R.id.footer_icon), this.mFootPanelBaseIdx);
        }
        for (int i2 = 0; i2 < this.mTransLineNum; i2++) {
            ArrayList arrayList3 = new ArrayList();
            ArrayList arrayList4 = new ArrayList();
            Iterator<View> it2 = this.mTransViews.iterator();
            while (it2.hasNext()) {
                View next2 = it2.next();
                if (((Integer) next2.getTag(R.id.tag_control_center_trans)).intValue() == i2) {
                    if (i2 == 2 || i2 == 3) {
                        arrayList3.add(Folme.useAt(next2).state());
                        this.mBigTileTransAnimArr.put(Integer.valueOf(i2), arrayList3);
                    } else {
                        arrayList4.add(next2);
                    }
                }
            }
            if (arrayList4.size() > 0) {
                this.mPanelTransAnimMap.put(Integer.valueOf(i2), Folme.useAt((View[]) arrayList4.toArray(new View[arrayList4.size()])).state());
            }
        }
    }

    private void addAnimateView(View view, int i) {
        if (view != null && !this.mViews.contains(view)) {
            view.setTag(R.id.tag_control_center, Integer.valueOf(i));
            this.mViews.add(view);
        }
    }

    private void addTransAnimateView(View view, int i) {
        if (view != null && !this.mTransViews.contains(view)) {
            view.setTag(R.id.tag_control_center_trans, Integer.valueOf(i));
            this.mTransViews.add(view);
        }
    }

    private void panelAnimateOn(boolean z) {
        boolean z2 = z;
        Set<Integer> keySet = this.mPanelAnimMap.keySet();
        Set<Integer> keySet2 = this.mBigTileAnimArr.keySet();
        this.mQuickQsControlCenterTileLayout.visAnimOn(z2);
        if (z2) {
            if (!isOrientationPortrait() || this.mPanelController.isSuperPowerMode()) {
                Folme.useAt(this.mEditTiles).state().clean();
            } else {
                Folme.useAt(this.mEditTiles).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
            }
            if (this.mPanelAnimMap.size() > 0) {
                for (Integer num : keySet) {
                    HashMap<Integer, IStateStyle> hashMap = this.mPanelAnimMap;
                    hashMap.get(num).end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
                }
            }
            if (this.mBigTileAnimArr.size() > 0) {
                for (Integer num2 : keySet2) {
                    Iterator it = this.mBigTileAnimArr.get(num2).iterator();
                    while (it.hasNext()) {
                        ((IStateStyle) it.next()).end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
                    }
                }
            }
            if (!isOrientationPortrait()) {
                this.mPanelAnimMap.get(Integer.valueOf(this.mFootPanelBaseIdx)).cancel();
                int size = this.mPanelAnimMap.size();
                int i = this.mFootPanelBaseIdx;
                if (size > i + 1) {
                    this.mPanelAnimMap.get(Integer.valueOf(i + 1)).cancel();
                }
            }
            float f = 0.5f;
            float f2 = 0.1f;
            float f3 = 0.7f;
            float f4 = 0.2f;
            float f5 = 5.0f;
            if (this.mPanelAnimMap.size() > 0) {
                for (Integer next : keySet) {
                    float intValue = ((((float) next.intValue()) * 0.2f) / f5) + f3;
                    float intValue2 = ((((float) next.intValue()) * f2) / f5) + f;
                    AnimState animState = this.mPanelHideAnim;
                    AnimState animState2 = this.mPanelShowAnim;
                    AnimConfig animConfig = new AnimConfig();
                    animConfig.setEase(EaseManager.getStyle(-2, intValue, intValue2));
                    this.mPanelAnimMap.get(next).fromTo(animState, animState2, animConfig);
                    f = 0.5f;
                    f2 = 0.1f;
                    f3 = 0.7f;
                    f5 = 5.0f;
                }
            }
            if (this.mBigTileAnimArr.size() > 0) {
                for (Integer next2 : keySet2) {
                    Iterator it2 = this.mBigTileAnimArr.get(next2).iterator();
                    while (it2.hasNext()) {
                        AnimState animState3 = this.mPanelHideAnim;
                        AnimState animState4 = this.mPanelShowAnim;
                        AnimConfig animConfig2 = new AnimConfig();
                        animConfig2.setEase(EaseManager.getStyle(-2, ((((float) next2.intValue()) * f4) / 5.0f) + 0.7f, ((((float) next2.intValue()) * 0.1f) / 5.0f) + 0.5f));
                        ((IStateStyle) it2.next()).fromTo(animState3, animState4, animConfig2);
                        f4 = 0.2f;
                    }
                }
            }
            if (isOrientationPortrait() && !this.mPanelController.isSuperPowerMode()) {
                IStateStyle state = Folme.useAt(this.mEditTiles).state();
                AnimState animState5 = this.mPanelHideAnim;
                AnimState animState6 = this.mPanelShowAnim;
                AnimConfig animConfig3 = new AnimConfig();
                animConfig3.setEase(EaseManager.getStyle(-2, 0.74f, 0.52f));
                state.fromTo(animState5, animState6, animConfig3);
                return;
            }
            return;
        }
        if (!isOrientationPortrait() || this.mPanelController.isSuperPowerMode()) {
            Folme.useAt(this.mEditTiles).state().clean();
        } else {
            Folme.useAt(this.mEditTiles).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
        }
        if (this.mPanelAnimMap.size() > 0) {
            for (Integer num3 : keySet) {
                HashMap<Integer, IStateStyle> hashMap2 = this.mPanelAnimMap;
                hashMap2.get(num3).end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
            }
        }
        if (this.mBigTileAnimArr.size() > 0) {
            for (Integer num4 : keySet2) {
                Iterator it3 = this.mBigTileAnimArr.get(num4).iterator();
                while (it3.hasNext()) {
                    ((IStateStyle) it3.next()).end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
                }
            }
        }
        if (this.mPanelAnimMap.size() > 0) {
            for (Integer num5 : keySet) {
                AnimConfig animConfig4 = new AnimConfig();
                animConfig4.setEase(EaseManager.getStyle(-2, 0.99f, 0.2f));
                HashMap<Integer, IStateStyle> hashMap3 = this.mPanelAnimMap;
                hashMap3.get(num5).fromTo(this.mPanelShowAnim, this.mPanelHideAnim, animConfig4);
            }
        }
        if (this.mBigTileAnimArr.size() > 0) {
            for (Integer num6 : keySet2) {
                Iterator it4 = this.mBigTileAnimArr.get(num6).iterator();
                while (it4.hasNext()) {
                    AnimConfig animConfig5 = new AnimConfig();
                    animConfig5.setEase(EaseManager.getStyle(-2, 0.99f, 0.2f));
                    ((IStateStyle) it4.next()).fromTo(this.mPanelShowAnim, this.mPanelHideAnim, animConfig5);
                }
            }
        }
        if (isOrientationPortrait() && !this.mPanelController.isSuperPowerMode()) {
            IStateStyle state2 = Folme.useAt(this.mEditTiles).state();
            AnimState animState7 = this.mPanelShowAnim;
            AnimState animState8 = this.mPanelHideAnim;
            AnimConfig animConfig6 = new AnimConfig();
            animConfig6.setEase(EaseManager.getStyle(-2, 0.99f, 0.2f));
            state2.fromTo(animState7, animState8, animConfig6);
        }
    }

    public void finishCollapse() {
        if (this.mControlPanelContentView.getVisibility() == 0) {
            this.mControlPanelContentView.setVisibility(4);
        }
        if (isOrientationPortrait()) {
            this.mQuickQsControlCenterTileLayout.setExpanded(false);
        }
    }

    class H extends Handler {
        H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                QSPanel.Record record = (QSPanel.Record) message.obj;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSControlCenterPanel.handleShowDetail(record, z);
            } else if (i != 4 && i == 3) {
                QSControlCenterPanel.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }
}
