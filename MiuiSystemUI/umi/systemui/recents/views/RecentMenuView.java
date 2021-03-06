package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.HideMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.ShowMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.ShowTaskMenuEvent;
import com.android.systemui.recents.events.component.ChangeTaskLockStateEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.util.ViewAnimUtils;
import miui.util.ScreenshotUtils;
import miui.view.animation.BackEaseOutInterpolator;

public class RecentMenuView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    /* access modifiers changed from: private */
    public Bitmap mBlurBackground;
    /* access modifiers changed from: private */
    public boolean mIsShowing;
    private boolean mIsTaskViewLeft;
    boolean mIsTouchInTaskViewBound;
    Drawable mLockDrawable;
    private ImageView mMenuItemInfo;
    private FrameLayout mMenuItemInfoContainer;
    private ImageView mMenuItemLock;
    private FrameLayout mMenuItemLockContainer;
    private ImageView mMenuItemMultiWindow;
    private FrameLayout mMenuItemMultiWindowContainer;
    private TimeInterpolator mShowMenuItemAnimInterpolator;
    ValueAnimator mShowOrHideAnim;
    /* access modifiers changed from: private */
    public Task mTask;
    /* access modifiers changed from: private */
    public TaskStackView mTaskStackView;
    /* access modifiers changed from: private */
    public TaskView mTaskView;
    Rect mTaskViewBound;
    Drawable mUnlockDrawable;
    private int mVerticalMargin;

    public RecentMenuView(Context context) {
        this(context, (AttributeSet) null);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mIsTouchInTaskViewBound = false;
        this.mTaskViewBound = new Rect();
        this.mIsShowing = false;
        this.mBlurBackground = null;
        this.mShowOrHideAnim = new ValueAnimator();
        this.mShowMenuItemAnimInterpolator = new BackEaseOutInterpolator();
        this.mLockDrawable = context.getResources().getDrawable(R.drawable.ic_task_lock);
        this.mUnlockDrawable = context.getResources().getDrawable(R.drawable.ic_task_unlock);
        new ColorDrawable(getResources().getColor(R.color.recent_menu_mask_color));
        this.mVerticalMargin = context.getResources().getDimensionPixelSize(R.dimen.recents_task_menu_vertical_margin);
        setTranslationZ(10.0f);
        setVisibility(8);
        setClipChildren(false);
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mShowOrHideAnim = valueAnimator;
        valueAnimator.setDuration(180);
        this.mShowOrHideAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                if (RecentMenuView.this.mIsShowing) {
                    RecentMenuView.this.setMaskBackground();
                    RecentMenuView.this.mTaskView.setTranslationZ(10.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setAlpha(1.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setTranslationZ(5.0f);
                    RecentsEventBus.getDefault().send(new HideMemoryAndDockEvent());
                    return;
                }
                RecentsEventBus.getDefault().send(new ShowMemoryAndDockEvent());
            }

            public void onAnimationEnd(Animator animator) {
                if (!RecentMenuView.this.mIsShowing) {
                    RecentMenuView.this.mTaskView.setTranslationZ(0.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setAlpha(0.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setTranslationZ(0.0f);
                    Bitmap unused = RecentMenuView.this.mBlurBackground = null;
                    RecentMenuView.this.mTaskView.getHeaderView().setAlpha(1.0f);
                    RecentMenuView.this.setVisibility(8);
                }
            }
        });
        this.mShowOrHideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mMenuItemInfoContainer = (FrameLayout) findViewById(R.id.menu_item_info_container);
        this.mMenuItemLockContainer = (FrameLayout) findViewById(R.id.menu_item_lock_container);
        this.mMenuItemMultiWindowContainer = (FrameLayout) findViewById(R.id.menu_item_multi_window_container);
        this.mMenuItemInfo = (ImageView) findViewById(R.id.menu_item_info);
        this.mMenuItemLock = (ImageView) findViewById(R.id.menu_item_lock);
        this.mMenuItemMultiWindow = (ImageView) findViewById(R.id.menu_item_multi_window);
        this.mMenuItemInfo.setImageResource(R.drawable.ic_task_setting);
        this.mMenuItemInfo.setContentDescription(this.mContext.getString(R.string.recent_menu_item_info));
        this.mMenuItemMultiWindow.setImageResource(R.drawable.ic_task_multi);
        this.mMenuItemInfo.setOnClickListener(this);
        this.mMenuItemLock.setOnClickListener(this);
        this.mMenuItemMultiWindow.setOnClickListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
        ViewAnimUtils.mouse(this.mMenuItemInfo);
        ViewAnimUtils.mouse(this.mMenuItemLock);
        ViewAnimUtils.mouse(this.mMenuItemMultiWindow);
    }

    public boolean onLongClick(View view) {
        if (!this.mIsTouchInTaskViewBound) {
            return false;
        }
        removeMenu(true);
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mTaskView == null || !this.mIsShowing) {
            return super.onTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int i = action & 255;
        if (i == 0) {
            this.mTaskView.getHitRect(this.mTaskViewBound);
            boolean contains = this.mTaskViewBound.contains((int) rawX, (int) rawY);
            this.mIsTouchInTaskViewBound = contains;
            if (contains) {
                SpringAnimationUtils.getInstance().startTaskViewScaleUpMenuModeAnim(this.mTaskView);
            }
        } else if (i == 2 && this.mIsTouchInTaskViewBound) {
            boolean contains2 = this.mTaskViewBound.contains((int) rawX, (int) rawY);
            this.mIsTouchInTaskViewBound = contains2;
            if (!contains2) {
                SpringAnimationUtils.getInstance().startTaskViewScaleDownMenuModeAnim(this.mTaskView);
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void onClick(View view) {
        String str;
        Class cls = HapticFeedBackImpl.class;
        Task task = this.mTask;
        if (task != null) {
            Task.TaskKey taskKey = task.key;
            String packageName = (taskKey == null || taskKey.getComponent() == null) ? "" : this.mTask.key.getComponent().getPackageName();
            int id = view.getId();
            if (id == R.id.menu_item_info) {
                RecentsEventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
                RecentsPushEventHelper.sendShowAppInfoEvent(packageName);
            } else if (id == R.id.menu_item_lock) {
                Task task2 = this.mTask;
                boolean z = !task2.isLocked;
                task2.isLocked = z;
                this.mTaskView.updateLockedFlagVisible(z, true, 200);
                RecentsEventBus recentsEventBus = RecentsEventBus.getDefault();
                Task task3 = this.mTask;
                recentsEventBus.send(new ChangeTaskLockStateEvent(task3, task3.isLocked));
                if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                    if (this.mTask.isLocked) {
                        ((HapticFeedBackImpl) Dependency.get(cls)).getHapticFeedbackUtil().performHapticFeedback("switch", false);
                    } else {
                        ((HapticFeedBackImpl) Dependency.get(cls)).getHapticFeedbackUtil().performHapticFeedback("switch", false, 1);
                    }
                }
                if (this.mTask.isLocked) {
                    str = this.mContext.getString(R.string.accessibility_recent_task_locked_state);
                } else {
                    str = this.mContext.getString(R.string.accessibility_recent_task_unlocked);
                }
                announceForAccessibility(str);
                if (this.mTask.isLocked) {
                    RecentsPushEventHelper.sendLockTaskEvent(packageName);
                } else {
                    RecentsPushEventHelper.sendUnlockTaskEvent(packageName);
                }
            } else if (id == R.id.menu_item_multi_window) {
                if (!BaseRecentsImpl.toastForbidDockedWhenScreening(getContext())) {
                    final TaskStack.DockState[] dockStatesForCurrentOrientation = getDockStatesForCurrentOrientation();
                    if (dockStatesForCurrentOrientation[0] != null) {
                        this.mTaskStackView.postDelayed(new Runnable() {
                            public void run() {
                                if (!Recents.getSystemServices().hasDockedTask()) {
                                    RecentMenuView.this.mTaskStackView.addIgnoreTask(RecentMenuView.this.mTask);
                                    RecentsEventBus.getDefault().send(new DragDropTargetChangedEvent(RecentMenuView.this.mTask, dockStatesForCurrentOrientation[0]));
                                    RecentsEventBus.getDefault().send(new DragEndEvent(RecentMenuView.this.mTask, RecentMenuView.this.mTaskView, dockStatesForCurrentOrientation[0]));
                                    RecentMenuView recentMenuView = RecentMenuView.this;
                                    recentMenuView.announceForAccessibility(recentMenuView.mContext.getString(R.string.accessibility_splite_screen_primary));
                                    return;
                                }
                                RecentMenuView.this.mTaskView.onClick(RecentMenuView.this.mTaskView);
                                RecentMenuView recentMenuView2 = RecentMenuView.this;
                                recentMenuView2.announceForAccessibility(recentMenuView2.mContext.getString(R.string.accessibility_splite_screen_secondary));
                            }
                        }, 250);
                        RecentsPushEventHelper.sendClickMultiWindowMenuEvent(packageName);
                        RecentsPushEventHelper.sendEnterMultiWindowEvent("clickMenu", packageName);
                    }
                } else {
                    return;
                }
            }
        }
        removeMenu(true);
    }

    /* access modifiers changed from: private */
    public void setMaskBackground() {
        this.mBlurBackground = ScreenshotUtils.getScreenshot(getContext().getApplicationContext(), 0.25f, 0, 30000, true);
        if (Recents.getSystemServices().hasDockedTask() && this.mBlurBackground != null) {
            Rect rect = new Rect();
            this.mTaskStackView.getBoundsOnScreen(rect);
            rect.scale(0.25f);
            try {
                this.mBlurBackground = Bitmap.createBitmap(this.mBlurBackground, rect.left, rect.top, rect.width(), rect.height());
            } catch (IllegalArgumentException e) {
                Log.e("RecentMenuView", "Get blur menu background error: rect=" + rect + "   ScreenshotWidth=" + this.mBlurBackground.getWidth() + "   ScreenshotHeight=" + this.mBlurBackground.getHeight(), e);
                this.mBlurBackground = null;
            }
        }
    }

    public TaskStack.DockState[] getDockStatesForCurrentOrientation() {
        boolean z = getResources().getConfiguration().orientation == 2;
        RecentsConfiguration configuration = Recents.getConfiguration();
        return z ? configuration.isLargeScreen ? DockRegion.TABLET_LANDSCAPE : DockRegion.PHONE_LANDSCAPE : configuration.isLargeScreen ? DockRegion.TABLET_PORTRAIT : DockRegion.PHONE_PORTRAIT;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        RecentsEventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        int i15 = i3;
        int i16 = i4;
        int measuredWidth = this.mMenuItemLock.getMeasuredWidth();
        Rect rect = new Rect();
        this.mTaskView.getHitRect(rect);
        rect.top += this.mTaskView.getHeaderView().getHeight();
        rect.intersect(i, i2, i15, i16);
        int[] iArr = new int[3];
        int[] iArr2 = new int[3];
        if (this.mIsTaskViewLeft) {
            int i17 = rect.right;
            float f = (float) measuredWidth;
            int i18 = (int) (((float) i17) + (f * 0.4f));
            iArr[2] = i18;
            iArr[0] = i18;
            iArr[1] = (int) (((float) i17) + (f * 0.9f));
            i6 = i17 - measuredWidth;
            i5 = rect.centerY();
        } else {
            int i19 = rect.left;
            float f2 = (float) measuredWidth;
            int i20 = (int) (((float) i19) - (f2 * 1.4f));
            iArr[2] = i20;
            iArr[0] = i20;
            iArr[1] = (int) (((float) i19) - (f2 * 1.9f));
            i6 = i19 + measuredWidth;
            i5 = rect.centerY();
        }
        float f3 = (float) measuredWidth;
        iArr2[1] = (int) (((float) rect.centerY()) - (0.5f * f3));
        float f4 = f3;
        double d = (double) measuredWidth;
        double d2 = 1.2d * d;
        iArr2[0] = (int) (((double) iArr2[1]) - d2);
        iArr2[2] = (int) (((double) iArr2[1]) + d2);
        int i21 = iArr2[0];
        int i22 = this.mVerticalMargin;
        if (i21 < i22) {
            if (this.mIsTaskViewLeft) {
                int i23 = rect.right;
                iArr[0] = (int) (((float) i23) + (f4 * 0.6f));
                iArr[1] = (int) (((float) i23) + (f4 * 0.4f));
                iArr[2] = i23 - measuredWidth;
                i13 = measuredWidth * 2;
                i14 = i23 - i13;
                i12 = rect.bottom;
            } else {
                int i24 = rect.left;
                iArr[0] = (int) (((float) i24) - (f4 * 1.6f));
                iArr[1] = (int) (((float) i24) - (1.4f * f4));
                iArr[2] = i24;
                i13 = measuredWidth * 2;
                i14 = i24 + i13;
                i12 = rect.bottom;
            }
            int i25 = rect.bottom;
            iArr2[0] = i25 - measuredWidth;
            iArr2[1] = (int) (((float) i25) + (0.4f * f4));
            iArr2[2] = (int) (((double) i25) + (d * 0.6d));
            i7 = i12 - i13;
            i8 = i14;
        } else if (iArr2[2] + measuredWidth > i16 - i22) {
            if (this.mIsTaskViewLeft) {
                int i26 = rect.right;
                iArr[0] = i26 - measuredWidth;
                iArr[1] = (int) (((float) i26) + (f4 * 0.4f));
                iArr[2] = (int) (((float) i26) + (f4 * 0.6f));
                i10 = measuredWidth * 2;
                i11 = i26 - i10;
                i9 = rect.top;
            } else {
                int i27 = rect.left;
                iArr[0] = i27;
                iArr[1] = (int) (((float) i27) - (f4 * 1.4f));
                iArr[2] = (int) (((float) i27) - (f4 * 1.6f));
                i10 = measuredWidth * 2;
                i11 = i27 + i10;
                i9 = rect.top;
            }
            int i28 = rect.top;
            iArr2[0] = (int) (((float) i28) - (f4 * 1.6f));
            iArr2[1] = (int) (((float) i28) - (1.4f * f4));
            iArr2[2] = i28;
            i8 = i11;
            i7 = i9 + i10;
        } else {
            i7 = i5;
            i8 = i6;
        }
        int i29 = i15 - 10;
        iArr[0] = Math.max(10, Math.min(iArr[0], i29));
        iArr[1] = Math.max(10, Math.min(iArr[1], i29));
        iArr[2] = Math.max(10, Math.min(iArr[2], i29));
        int i30 = i8;
        int i31 = i7;
        int i32 = measuredWidth;
        layoutMenuItem(this.mMenuItemLockContainer, iArr[0], iArr2[0], i30, i31, i32);
        layoutMenuItem(this.mMenuItemMultiWindowContainer, iArr[1], iArr2[1], i30, i31, i32);
        layoutMenuItem(this.mMenuItemInfoContainer, iArr[2], iArr2[2], i30, i31, i32);
    }

    private void layoutMenuItem(View view, int i, int i2, int i3, int i4, int i5) {
        view.setPivotX((float) (i3 - i));
        view.setPivotY((float) (i4 - i2));
        view.layout(i, i2, i + i5, i5 + i2);
    }

    public final void onBusEvent(ShowTaskMenuEvent showTaskMenuEvent) {
        String str;
        String str2;
        if (!this.mIsShowing) {
            this.mIsShowing = true;
            this.mTaskStackView.setIsShowingMenu(true);
            TaskView taskView = showTaskMenuEvent.taskView;
            this.mTaskView = taskView;
            Task task = taskView.getTask();
            this.mTask = task;
            this.mMenuItemMultiWindow.setEnabled(task.isDockable && Utilities.supportsMultiWindow());
            this.mMenuItemLock.setImageDrawable(this.mTask.isLocked ? this.mUnlockDrawable : this.mLockDrawable);
            ImageView imageView = this.mMenuItemLock;
            if (this.mTask.isLocked) {
                str = this.mContext.getString(R.string.recent_menu_item_unlock);
            } else {
                str = this.mContext.getString(R.string.recent_menu_item_lock);
            }
            imageView.setContentDescription(str);
            ImageView imageView2 = this.mMenuItemMultiWindow;
            imageView2.setImageAlpha(imageView2.isEnabled() ? 255 : 80);
            ImageView imageView3 = this.mMenuItemMultiWindow;
            if (imageView3.isEnabled()) {
                str2 = this.mContext.getString(R.string.accessibility_menu_item_split_enable);
            } else {
                str2 = this.mContext.getString(R.string.accessibility_menu_item_split_disable);
            }
            imageView3.setContentDescription(str2);
            this.mIsTaskViewLeft = this.mTaskStackView.getTaskViews().size() > 1 && this.mTaskView.getLeft() < this.mTaskStackView.getWidth() - this.mTaskView.getRight();
            setVisibility(0);
            setFocusable(true);
            startShowItemAnim(this.mMenuItemLockContainer, 1.0f, 0);
            startShowItemAnim(this.mMenuItemMultiWindowContainer, 1.0f, 50);
            startShowItemAnim(this.mMenuItemInfoContainer, 1.0f, 100);
            this.mShowOrHideAnim.setFloatValues(new float[]{0.0f, 1.0f});
            this.mShowOrHideAnim.start();
            SpringAnimationUtils.getInstance().startShowTaskMenuAnim(this.mTaskStackView, this.mTaskView);
            for (TaskView importantForAccessibility : this.mTaskStackView.getTaskViews()) {
                importantForAccessibility.setImportantForAccessibility(4);
            }
            Task.TaskKey taskKey = this.mTask.key;
            if (taskKey != null && taskKey.getComponent() != null && this.mTaskStackView.getStack() != null) {
                RecentsPushEventHelper.sendLongCLickTaskEvent(this.mTask.key.getComponent().getPackageName(), this.mTaskStackView.getStack().indexOfStackTask(this.mTask));
            }
        }
    }

    private void startShowItemAnim(View view, float f, long j) {
        view.setAlpha(0.0f);
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.animate().alpha(f).scaleX(1.0f).scaleY(1.0f).setDuration(240).setStartDelay(j).setInterpolator(this.mShowMenuItemAnimInterpolator).start();
    }

    private void startHideItemAnim(View view) {
        view.animate().alpha(0.0f).scaleX(0.6f).scaleY(0.6f).setDuration(200).setStartDelay(0).start();
    }

    public final void onBusEvent(StartedDragingEvent startedDragingEvent) {
        removeMenu(false);
    }

    public boolean removeMenu(boolean z) {
        if (!this.mIsShowing) {
            return false;
        }
        this.mIsShowing = false;
        this.mTaskStackView.setIsShowingMenu(false);
        if (z) {
            startHideItemAnim(this.mMenuItemLockContainer);
            startHideItemAnim(this.mMenuItemMultiWindowContainer);
            startHideItemAnim(this.mMenuItemInfoContainer);
        }
        this.mShowOrHideAnim.setFloatValues(new float[]{1.0f, 0.0f});
        this.mShowOrHideAnim.start();
        SpringAnimationUtils.getInstance().startRemoveTaskMenuAnim(this.mTaskStackView, this.mTaskView);
        for (TaskView importantForAccessibility : this.mTaskStackView.getTaskViews()) {
            importantForAccessibility.setImportantForAccessibility(0);
        }
        this.mTaskView.sendAccessibilityEvent(8);
        return true;
    }

    public void setTaskStackView(TaskStackView taskStackView) {
        this.mTaskStackView = taskStackView;
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public boolean isShowOrHideAnimRunning() {
        return this.mShowOrHideAnim.isRunning();
    }
}
