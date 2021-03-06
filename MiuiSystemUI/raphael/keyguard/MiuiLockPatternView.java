package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.view.DisplayListCanvas;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.widget.ExploreByTouchHelper;
import com.android.internal.widget.LockPatternView;
import com.android.systemui.plugins.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import miui.view.MiuiHapticFeedbackConstants;

public class MiuiLockPatternView extends View {
    private long mAnimatingPeriodStart;
    private int mAspect;
    private AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public final CellState[][] mCellStates;
    private final Path mCurrentPath;
    /* access modifiers changed from: private */
    public final int mDotSize;
    /* access modifiers changed from: private */
    public final int mDotSizeActivated;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mErrorColor;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    /* access modifiers changed from: private */
    public final Interpolator mFastOutSlowInInterpolator;
    /* access modifiers changed from: private */
    public float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Rect mInvalidate;
    private final Interpolator mLinearOutSlowInInterpolator;
    private Drawable mNotSelectedDrawable;
    private OnPatternListener mOnPatternListener;
    private final Paint mPaint;
    private final Paint mPathPaint;
    private final int mPathWidth;
    private final ArrayList<LockPatternView.Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    /* access modifiers changed from: private */
    public final boolean[][] mPatternDrawLookup;
    /* access modifiers changed from: private */
    public boolean mPatternInProgress;
    private int mRegularColor;
    private Drawable mSelectedDrawable;
    /* access modifiers changed from: private */
    public float mSquareHeight;
    /* access modifiers changed from: private */
    public float mSquareWidth;
    private int mSuccessColor;
    private final Rect mTmpInvalidateRect;
    private boolean mUseLockPatternDrawable;

    public static class CellState {
        float alpha = 1.0f;
        int col;
        boolean hwAnimating;
        CanvasProperty<Float> hwCenterX;
        CanvasProperty<Float> hwCenterY;
        CanvasProperty<Paint> hwPaint;
        CanvasProperty<Float> hwRadius;
        public ValueAnimator lineAnimator;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        float radius;
        int row;
        float translationY;
    }

    public enum DisplayMode {
        Correct,
        Animate,
        Wrong
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<LockPatternView.Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<LockPatternView.Cell> list);

        void onPatternStart();
    }

    public MiuiLockPatternView(Context context) {
        this(context, (AttributeSet) null);
    }

    /* JADX WARNING: type inference failed for: r7v3, types: [com.android.keyguard.MiuiLockPatternView$PatternExploreByTouchHelper, android.view.View$AccessibilityDelegate] */
    public MiuiLockPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDrawingProfilingStarted = false;
        this.mPaint = new Paint();
        this.mPathPaint = new Paint();
        this.mPattern = new ArrayList<>(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance(boolean.class, new int[]{3, 3});
        this.mInProgressX = -1.0f;
        this.mInProgressY = -1.0f;
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = false;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = false;
        this.mHitFactor = 0.4f;
        this.mCurrentPath = new Path();
        this.mInvalidate = new Rect();
        this.mTmpInvalidateRect = new Rect();
        this.mAspect = 0;
        setClickable(true);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        this.mRegularColor = getResources().getColor(R.color.miui_pattern_lockscreen_paint_color);
        this.mErrorColor = getResources().getColor(R.color.pattern_lockscreen_paint_error_color);
        this.mSuccessColor = getResources().getColor(R.color.miui_pattern_lockscreen_heavy_paint_color);
        this.mPathPaint.setColor(this.mRegularColor);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        this.mPathPaint.setStyle(Paint.Style.STROKE);
        this.mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPathWidth = 7;
        this.mPathPaint.setStrokeWidth((float) this.mPathWidth);
        this.mDotSize = 18;
        this.mDotSizeActivated = 27;
        this.mCellStates = (CellState[][]) Array.newInstance(CellState.class, new int[]{3, 3});
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                this.mCellStates[i][i2] = new CellState();
                CellState[][] cellStateArr = this.mCellStates;
                cellStateArr[i][i2].radius = (float) (this.mDotSize / 2);
                cellStateArr[i][i2].row = i;
                cellStateArr[i][i2].col = i2;
            }
        }
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
    }

    public CellState[][] getCellStates() {
        return this.mCellStates;
    }

    public void setInStealthMode(boolean z) {
        this.mInStealthMode = z;
    }

    public void setTactileFeedbackEnabled(boolean z) {
        this.mEnableHapticFeedback = z;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.mOnPatternListener = onPatternListener;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (this.mPattern.size() != 0) {
                this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
                LockPatternView.Cell cell = this.mPattern.get(0);
                this.mInProgressX = getCenterXForColumn(cell.getColumn());
                this.mInProgressY = getCenterYForRow(cell.getRow());
                clearPatternDrawLookup();
            } else {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
        }
        invalidate();
    }

    public void startCellStateAnimation(CellState cellState, float f, float f2, float f3, float f4, float f5, float f6, long j, long j2, Interpolator interpolator, Runnable runnable) {
        if (isHardwareAccelerated()) {
            startCellStateAnimationHw(cellState, f, f2, f3, f4, f5, f6, j, j2, interpolator, runnable);
        } else {
            startCellStateAnimationSw(cellState, f, f2, f3, f4, f5, f6, j, j2, interpolator, runnable);
        }
    }

    private void startCellStateAnimationSw(CellState cellState, float f, float f2, float f3, float f4, float f5, float f6, long j, long j2, Interpolator interpolator, Runnable runnable) {
        final CellState cellState2 = cellState;
        final float f7 = f;
        cellState2.alpha = f7;
        final float f8 = f3;
        cellState2.translationY = f8;
        cellState2.radius = ((float) (this.mDotSize / 2)) * f5;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(j2);
        ofFloat.setStartDelay(j);
        ofFloat.setInterpolator(interpolator);
        final float f9 = f2;
        final float f10 = f4;
        final float f11 = f5;
        final float f12 = f6;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                CellState cellState = cellState2;
                float f = 1.0f - floatValue;
                cellState.alpha = (f7 * f) + (f9 * floatValue);
                cellState.translationY = (f8 * f) + (f10 * floatValue);
                cellState.radius = ((float) (MiuiLockPatternView.this.mDotSize / 2)) * ((f * f11) + (floatValue * f12));
                MiuiLockPatternView.this.invalidate();
            }
        });
        final Runnable runnable2 = runnable;
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                Runnable runnable = runnable2;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        ofFloat.start();
    }

    private void startCellStateAnimationHw(final CellState cellState, float f, float f2, float f3, float f4, float f5, float f6, long j, long j2, Interpolator interpolator, Runnable runnable) {
        CellState cellState2 = cellState;
        float f7 = f4;
        cellState2.alpha = f2;
        cellState2.translationY = f7;
        cellState2.radius = ((float) (this.mDotSize / 2)) * f6;
        cellState2.hwAnimating = true;
        cellState2.hwCenterY = CanvasProperty.createFloat(getCenterYForRow(cellState2.row) + f3);
        cellState2.hwCenterX = CanvasProperty.createFloat(getCenterXForColumn(cellState2.col));
        cellState2.hwRadius = CanvasProperty.createFloat(((float) (this.mDotSize / 2)) * f5);
        this.mPaint.setColor(getCurrentColor(false));
        this.mPaint.setAlpha((int) (255.0f * f));
        cellState2.hwPaint = CanvasProperty.createPaint(new Paint(this.mPaint));
        long j3 = j;
        long j4 = j2;
        Interpolator interpolator2 = interpolator;
        startRtFloatAnimation(cellState2.hwCenterY, getCenterYForRow(cellState2.row) + f7, j3, j4, interpolator2);
        startRtFloatAnimation(cellState2.hwRadius, ((float) (this.mDotSize / 2)) * f6, j3, j4, interpolator2);
        final Runnable runnable2 = runnable;
        startRtAlphaAnimation(cellState, f2, j3, j4, interpolator2, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                cellState.hwAnimating = false;
                Runnable runnable = runnable2;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        invalidate();
    }

    private void startRtAlphaAnimation(CellState cellState, float f, long j, long j2, Interpolator interpolator, Animator.AnimatorListener animatorListener) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(cellState.hwPaint, 1, (float) ((int) (f * 255.0f)));
        renderNodeAnimator.setDuration(j2);
        renderNodeAnimator.setStartDelay(j);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setTarget(this);
        renderNodeAnimator.addListener(animatorListener);
        renderNodeAnimator.start();
    }

    private void startRtFloatAnimation(CanvasProperty<Float> canvasProperty, float f, long j, long j2, Interpolator interpolator) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(canvasProperty, f);
        renderNodeAnimator.setDuration(j2);
        renderNodeAnimator.setStartDelay(j);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setTarget(this);
        renderNodeAnimator.start();
    }

    private void notifyCellAdded() {
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternStarted() {
        sendAccessEvent(R.string.lockscreen_access_pattern_start);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(R.string.lockscreen_access_pattern_detected);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternDetected(this.mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCleared();
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mExploreByTouchHelper.dispatchHoverEvent(motionEvent) | super.dispatchHoverEvent(motionEvent);
    }

    private void resetPattern() {
        this.mPattern.clear();
        clearPatternDrawLookup();
        this.mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                this.mPatternDrawLookup[i][i2] = false;
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5 = (i - this.mPaddingLeft) - this.mPaddingRight;
        this.mSquareWidth = ((float) i5) / 3.0f;
        int i6 = (i2 - this.mPaddingTop) - this.mPaddingBottom;
        this.mSquareHeight = ((float) i6) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
        if (this.mUseLockPatternDrawable) {
            this.mNotSelectedDrawable.setBounds(this.mPaddingLeft, this.mPaddingTop, i5, i6);
            this.mSelectedDrawable.setBounds(this.mPaddingLeft, this.mPaddingTop, i5, i6);
        }
    }

    private int resolveMeasured(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (mode == Integer.MIN_VALUE) {
            return Math.max(size, i2);
        }
        if (mode != 0) {
            return size;
        }
        return i2;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int suggestedMinimumWidth = getSuggestedMinimumWidth();
        int suggestedMinimumHeight = getSuggestedMinimumHeight();
        int resolveMeasured = resolveMeasured(i, suggestedMinimumWidth);
        int resolveMeasured2 = resolveMeasured(i2, suggestedMinimumHeight);
        int i3 = this.mAspect;
        if (i3 == 0) {
            resolveMeasured = Math.min(resolveMeasured, resolveMeasured2);
            resolveMeasured2 = resolveMeasured;
        } else if (i3 == 1) {
            resolveMeasured2 = Math.min(resolveMeasured, resolveMeasured2);
        } else if (i3 == 2) {
            resolveMeasured = Math.min(resolveMeasured, resolveMeasured2);
        }
        Log.v("LockPatternView", "LockPatternView dimensions: " + resolveMeasured + "x" + resolveMeasured2);
        setMeasuredDimension(resolveMeasured, resolveMeasured2);
    }

    private LockPatternView.Cell detectAndAddHit(float f, float f2) {
        LockPatternView.Cell checkForNewHit = checkForNewHit(f, f2);
        LockPatternView.Cell cell = null;
        if (checkForNewHit == null) {
            return null;
        }
        ArrayList<LockPatternView.Cell> arrayList = this.mPattern;
        int i = 1;
        if (!arrayList.isEmpty()) {
            LockPatternView.Cell cell2 = arrayList.get(arrayList.size() - 1);
            int row = checkForNewHit.getRow() - cell2.getRow();
            int column = checkForNewHit.getColumn() - cell2.getColumn();
            int row2 = cell2.getRow();
            int column2 = cell2.getColumn();
            int i2 = -1;
            if (Math.abs(row) == 2 && Math.abs(column) != 1) {
                row2 = cell2.getRow() + (row > 0 ? 1 : -1);
            }
            if (Math.abs(column) == 2 && Math.abs(row) != 1) {
                int column3 = cell2.getColumn();
                if (column > 0) {
                    i2 = 1;
                }
                column2 = column3 + i2;
            }
            cell = LockPatternView.Cell.of(row2, column2);
        }
        if (cell != null && !this.mPatternDrawLookup[cell.getRow()][cell.getColumn()]) {
            addCellToPattern(cell);
        }
        addCellToPattern(checkForNewHit);
        if (this.mEnableHapticFeedback) {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                i = MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_MESH_NORMAL;
            }
            performHapticFeedback(i, 3);
        }
        return checkForNewHit;
    }

    private void addCellToPattern(LockPatternView.Cell cell) {
        this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        this.mPattern.add(cell);
        if (!this.mInStealthMode) {
            startCellActivatedAnimation(cell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(LockPatternView.Cell cell) {
        final CellState cellState = this.mCellStates[cell.getRow()][cell.getColumn()];
        startRadiusAnimation((float) (this.mDotSize / 2), (float) (this.mDotSizeActivated / 2), 96, this.mLinearOutSlowInInterpolator, cellState, new Runnable() {
            public void run() {
                MiuiLockPatternView miuiLockPatternView = MiuiLockPatternView.this;
                miuiLockPatternView.startRadiusAnimation((float) (miuiLockPatternView.mDotSizeActivated / 2), (float) (MiuiLockPatternView.this.mDotSize / 2), 192, MiuiLockPatternView.this.mFastOutSlowInInterpolator, cellState, (Runnable) null);
            }
        });
        startLineEndAnimation(cellState, this.mInProgressX, this.mInProgressY, getCenterXForColumn(cell.getColumn()), getCenterYForRow(cell.getRow()));
    }

    private void startLineEndAnimation(final CellState cellState, float f, float f2, float f3, float f4) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        final CellState cellState2 = cellState;
        final float f5 = f;
        final float f6 = f3;
        final float f7 = f2;
        final float f8 = f4;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                CellState cellState = cellState2;
                float f = 1.0f - floatValue;
                cellState.lineEndX = (f5 * f) + (f6 * floatValue);
                cellState.lineEndY = (f * f7) + (floatValue * f8);
                MiuiLockPatternView.this.invalidate();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                cellState.lineAnimator = null;
            }
        });
        ofFloat.setInterpolator(this.mFastOutSlowInInterpolator);
        ofFloat.setDuration(100);
        ofFloat.start();
        cellState.lineAnimator = ofFloat;
    }

    /* access modifiers changed from: private */
    public void startRadiusAnimation(float f, float f2, long j, Interpolator interpolator, final CellState cellState, final Runnable runnable) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cellState.radius = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                MiuiLockPatternView.this.invalidate();
            }
        });
        if (runnable != null) {
            ofFloat.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
        }
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(j);
        ofFloat.start();
    }

    private LockPatternView.Cell checkForNewHit(float f, float f2) {
        int columnHit;
        int rowHit = getRowHit(f2);
        if (rowHit >= 0 && (columnHit = getColumnHit(f)) >= 0 && !this.mPatternDrawLookup[rowHit][columnHit]) {
            return LockPatternView.Cell.of(rowHit, columnHit);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public int getRowHit(float f) {
        float f2 = this.mSquareHeight;
        float f3 = this.mHitFactor * f2;
        float f4 = ((float) this.mPaddingTop) + ((f2 - f3) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float f5 = (((float) i) * f2) + f4;
            if (f >= f5 && f <= f5 + f3) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public int getColumnHit(float f) {
        float f2 = this.mSquareWidth;
        float f3 = this.mHitFactor * f2;
        float f4 = ((float) this.mPaddingLeft) + ((f2 - f3) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float f5 = (((float) i) * f2) + f4;
            if (f >= f5 && f <= f5 + f3) {
                return i;
            }
        }
        return -1;
    }

    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action == 7) {
                motionEvent.setAction(2);
            } else if (action == 9) {
                motionEvent.setAction(0);
            } else if (action == 10) {
                motionEvent.setAction(1);
            }
            onTouchEvent(motionEvent);
            motionEvent.setAction(action);
        }
        return super.onHoverEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mInputEnabled || !isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            handleActionDown(motionEvent);
            return true;
        } else if (action == 1) {
            handleActionUp();
            return true;
        } else if (action == 2) {
            handleActionMove(motionEvent);
            return true;
        } else if (action != 3) {
            return false;
        } else {
            if (this.mPatternInProgress) {
                setPatternInProgress(false);
                resetPattern();
                notifyPatternCleared();
            }
            return true;
        }
    }

    private void setPatternInProgress(boolean z) {
        this.mPatternInProgress = z;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void handleActionMove(MotionEvent motionEvent) {
        float f = (float) this.mPathWidth;
        int historySize = motionEvent.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        int i = 0;
        boolean z = false;
        while (i < historySize + 1) {
            float historicalX = i < historySize ? motionEvent.getHistoricalX(i) : motionEvent.getX();
            float historicalY = i < historySize ? motionEvent.getHistoricalY(i) : motionEvent.getY();
            LockPatternView.Cell detectAndAddHit = detectAndAddHit(historicalX, historicalY);
            int size = this.mPattern.size();
            if (detectAndAddHit != null && size == 1) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float abs = Math.abs(historicalX - this.mInProgressX);
            float abs2 = Math.abs(historicalY - this.mInProgressY);
            if (abs > 0.0f || abs2 > 0.0f) {
                z = true;
            }
            if (this.mPatternInProgress && size > 0) {
                LockPatternView.Cell cell = this.mPattern.get(size - 1);
                float centerXForColumn = getCenterXForColumn(cell.getColumn());
                float centerYForRow = getCenterYForRow(cell.getRow());
                float min = Math.min(centerXForColumn, historicalX) - f;
                float max = Math.max(centerXForColumn, historicalX) + f;
                float min2 = Math.min(centerYForRow, historicalY) - f;
                float max2 = Math.max(centerYForRow, historicalY) + f;
                if (detectAndAddHit != null) {
                    float f2 = this.mSquareWidth * 0.5f;
                    float f3 = this.mSquareHeight * 0.5f;
                    float centerXForColumn2 = getCenterXForColumn(detectAndAddHit.getColumn());
                    float centerYForRow2 = getCenterYForRow(detectAndAddHit.getRow());
                    min = Math.min(centerXForColumn2 - f2, min);
                    max = Math.max(centerXForColumn2 + f2, max);
                    min2 = Math.min(centerYForRow2 - f3, min2);
                    max2 = Math.max(centerYForRow2 + f3, max2);
                }
                this.mTmpInvalidateRect.union(Math.round(min), Math.round(min2), Math.round(max), Math.round(max2));
            }
            i++;
        }
        this.mInProgressX = motionEvent.getX();
        this.mInProgressY = motionEvent.getY();
        if (z) {
            this.mInvalidate.union(this.mTmpInvalidateRect);
            invalidate(this.mInvalidate);
            this.mInvalidate.set(this.mTmpInvalidateRect);
        }
    }

    private void sendAccessEvent(int i) {
        announceForAccessibility(this.mContext.getString(i));
    }

    private void handleActionUp() {
        if (!this.mPattern.isEmpty()) {
            setPatternInProgress(false);
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
    }

    private void cancelLineAnimations() {
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                CellState cellState = this.mCellStates[i][i2];
                ValueAnimator valueAnimator = cellState.lineAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                    cellState.lineEndX = Float.MIN_VALUE;
                    cellState.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    private void handleActionDown(MotionEvent motionEvent) {
        resetPattern();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        LockPatternView.Cell detectAndAddHit = detectAndAddHit(x, y);
        if (detectAndAddHit != null) {
            setPatternInProgress(true);
            this.mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else if (this.mPatternInProgress) {
            setPatternInProgress(false);
            notifyPatternCleared();
        }
        if (detectAndAddHit != null) {
            float centerXForColumn = getCenterXForColumn(detectAndAddHit.getColumn());
            float centerYForRow = getCenterYForRow(detectAndAddHit.getRow());
            float f = this.mSquareWidth / 2.0f;
            float f2 = this.mSquareHeight / 2.0f;
            invalidate((int) (centerXForColumn - f), (int) (centerYForRow - f2), (int) (centerXForColumn + f), (int) (centerYForRow + f2));
        }
        this.mInProgressX = x;
        this.mInProgressY = y;
    }

    /* access modifiers changed from: private */
    public float getCenterXForColumn(int i) {
        float f = this.mSquareWidth;
        return ((float) this.mPaddingLeft) + (((float) i) * f) + (f / 2.0f);
    }

    /* access modifiers changed from: private */
    public float getCenterYForRow(int i) {
        float f = this.mSquareHeight;
        return ((float) this.mPaddingTop) + (((float) i) * f) + (f / 2.0f);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i;
        int i2;
        float f;
        Canvas canvas2 = canvas;
        ArrayList<LockPatternView.Cell> arrayList = this.mPattern;
        int size = arrayList.size();
        boolean[][] zArr = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int elapsedRealtime = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % ((size + 1) * 700);
            int i3 = elapsedRealtime / 700;
            clearPatternDrawLookup();
            for (int i4 = 0; i4 < i3; i4++) {
                LockPatternView.Cell cell = arrayList.get(i4);
                zArr[cell.getRow()][cell.getColumn()] = true;
            }
            if (i3 > 0 && i3 < size) {
                float f2 = ((float) (elapsedRealtime % 700)) / 700.0f;
                LockPatternView.Cell cell2 = arrayList.get(i3 - 1);
                float centerXForColumn = getCenterXForColumn(cell2.getColumn());
                float centerYForRow = getCenterYForRow(cell2.getRow());
                LockPatternView.Cell cell3 = arrayList.get(i3);
                this.mInProgressX = centerXForColumn + ((getCenterXForColumn(cell3.getColumn()) - centerXForColumn) * f2);
                this.mInProgressY = centerYForRow + (f2 * (getCenterYForRow(cell3.getRow()) - centerYForRow));
            }
            invalidate();
        }
        Path path = this.mCurrentPath;
        path.rewind();
        int i5 = 0;
        while (true) {
            int i6 = 3;
            if (i5 >= 3) {
                break;
            }
            float centerYForRow2 = getCenterYForRow(i5);
            int i7 = 0;
            while (i7 < i6) {
                CellState cellState = this.mCellStates[i5][i7];
                float centerXForColumn2 = getCenterXForColumn(i7);
                float f3 = cellState.translationY;
                if (this.mUseLockPatternDrawable) {
                    i = i7;
                    f = centerYForRow2;
                    drawCellDrawable(canvas, i5, i7, cellState.radius, zArr[i5][i7]);
                } else {
                    i = i7;
                    f = centerYForRow2;
                    if (!isHardwareAccelerated() || !cellState.hwAnimating) {
                        float f4 = ((float) ((int) f)) + f3;
                        float f5 = cellState.radius;
                        float f6 = f4;
                        float f7 = f5;
                        boolean z = zArr[i5][i];
                        i2 = i6;
                        drawCircle(canvas, (float) ((int) centerXForColumn2), f6, f7, z, cellState.alpha);
                        i7 = i + 1;
                        centerYForRow2 = f;
                        i6 = i2;
                    } else {
                        ((DisplayListCanvas) canvas2).drawCircle(cellState.hwCenterX, cellState.hwCenterY, cellState.hwRadius, cellState.hwPaint);
                    }
                }
                i2 = i6;
                i7 = i + 1;
                centerYForRow2 = f;
                i6 = i2;
            }
            i5++;
        }
        if (!this.mInStealthMode) {
            this.mPathPaint.setColor(getCurrentColor(true));
            float f8 = 0.0f;
            float f9 = 0.0f;
            int i8 = 0;
            boolean z2 = false;
            while (i8 < size) {
                LockPatternView.Cell cell4 = arrayList.get(i8);
                if (!zArr[cell4.getRow()][cell4.getColumn()]) {
                    break;
                }
                float centerXForColumn3 = getCenterXForColumn(cell4.getColumn());
                float centerYForRow3 = getCenterYForRow(cell4.getRow());
                if (i8 != 0) {
                    CellState cellState2 = this.mCellStates[cell4.getRow()][cell4.getColumn()];
                    path.rewind();
                    path.moveTo(f8, f9);
                    float f10 = cellState2.lineEndX;
                    if (f10 != Float.MIN_VALUE) {
                        float f11 = cellState2.lineEndY;
                        if (f11 != Float.MIN_VALUE) {
                            path.lineTo(f10, f11);
                            canvas2.drawPath(path, this.mPathPaint);
                        }
                    }
                    path.lineTo(centerXForColumn3, centerYForRow3);
                    canvas2.drawPath(path, this.mPathPaint);
                }
                i8++;
                f8 = centerXForColumn3;
                f9 = centerYForRow3;
                z2 = true;
            }
            if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && z2) {
                path.rewind();
                path.moveTo(f8, f9);
                path.lineTo(this.mInProgressX, this.mInProgressY);
                this.mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(this.mInProgressX, this.mInProgressY, f8, f9) * 255.0f));
                canvas2.drawPath(path, this.mPathPaint);
            }
        }
    }

    private float calculateLastSegmentAlpha(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return Math.min(1.0f, Math.max(0.0f, ((((float) Math.sqrt((double) ((f5 * f5) + (f6 * f6)))) / this.mSquareWidth) - 0.3f) * 4.0f));
    }

    private int getCurrentColor(boolean z) {
        if (!z || this.mInStealthMode || this.mPatternInProgress) {
            return this.mRegularColor;
        }
        DisplayMode displayMode = this.mPatternDisplayMode;
        if (displayMode == DisplayMode.Wrong) {
            return this.mErrorColor;
        }
        if (displayMode == DisplayMode.Correct || displayMode == DisplayMode.Animate) {
            return this.mSuccessColor;
        }
        throw new IllegalStateException("unknown display mode " + this.mPatternDisplayMode);
    }

    private void drawCircle(Canvas canvas, float f, float f2, float f3, boolean z, float f4) {
        this.mPaint.setColor(getCurrentColor(z));
        this.mPaint.setAlpha((int) (f4 * 255.0f));
        canvas.drawCircle(f, f2, f3, this.mPaint);
    }

    private void drawCellDrawable(Canvas canvas, int i, int i2, float f, boolean z) {
        int i3 = this.mPaddingLeft;
        float f2 = this.mSquareWidth;
        int i4 = this.mPaddingTop;
        float f3 = this.mSquareHeight;
        Rect rect = new Rect((int) (((float) i3) + (((float) i2) * f2)), (int) (((float) i4) + (((float) i) * f3)), (int) (((float) i3) + (((float) (i2 + 1)) * f2)), (int) (((float) i4) + (((float) (i + 1)) * f3)));
        float f4 = f / ((float) (this.mDotSize / 2));
        canvas.save();
        canvas.clipRect(rect);
        canvas.scale(f4, f4, (float) rect.centerX(), (float) rect.centerY());
        if (!z || f4 > 1.0f) {
            this.mNotSelectedDrawable.draw(canvas);
        } else {
            this.mSelectedDrawable.draw(canvas);
        }
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), (String) null, this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback);
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mPatternDisplayMode = DisplayMode.values()[savedState.getDisplayMode()];
        this.mInputEnabled = savedState.isInputEnabled();
        this.mInStealthMode = savedState.isInStealthMode();
        this.mEnableHapticFeedback = savedState.isTactileFeedbackEnabled();
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        private SavedState(Parcelable parcelable, String str, int i, boolean z, boolean z2, boolean z3) {
            super(parcelable);
            this.mSerializedPattern = str;
            this.mDisplayMode = i;
            this.mInputEnabled = z;
            this.mInStealthMode = z2;
            this.mTactileFeedbackEnabled = z3;
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.mSerializedPattern = parcel.readString();
            this.mDisplayMode = parcel.readInt();
            this.mInputEnabled = ((Boolean) parcel.readValue((ClassLoader) null)).booleanValue();
            this.mInStealthMode = ((Boolean) parcel.readValue((ClassLoader) null)).booleanValue();
            this.mTactileFeedbackEnabled = ((Boolean) parcel.readValue((ClassLoader) null)).booleanValue();
        }

        public int getDisplayMode() {
            return this.mDisplayMode;
        }

        public boolean isInputEnabled() {
            return this.mInputEnabled;
        }

        public boolean isInStealthMode() {
            return this.mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return this.mTactileFeedbackEnabled;
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeString(this.mSerializedPattern);
            parcel.writeInt(this.mDisplayMode);
            parcel.writeValue(Boolean.valueOf(this.mInputEnabled));
            parcel.writeValue(Boolean.valueOf(this.mInStealthMode));
            parcel.writeValue(Boolean.valueOf(this.mTactileFeedbackEnabled));
        }
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private HashMap<Integer, VirtualViewContainer> mItems = new HashMap<>();
        private Rect mTempRect = new Rect();

        class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(CharSequence charSequence) {
                this.description = charSequence;
            }
        }

        public PatternExploreByTouchHelper(View view) {
            super(view);
        }

        /* access modifiers changed from: protected */
        public int getVirtualViewAt(float f, float f2) {
            return getVirtualViewIdForHit(f, f2);
        }

        /* access modifiers changed from: protected */
        public void getVisibleVirtualViews(IntArray intArray) {
            if (MiuiLockPatternView.this.mPatternInProgress) {
                for (int i = 1; i < 10; i++) {
                    if (!this.mItems.containsKey(Integer.valueOf(i))) {
                        this.mItems.put(Integer.valueOf(i), new VirtualViewContainer(getTextForVirtualView(i)));
                    }
                    intArray.add(i);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            if (this.mItems.containsKey(Integer.valueOf(i))) {
                CharSequence charSequence = this.mItems.get(Integer.valueOf(i)).description;
                accessibilityEvent.getText().add(charSequence);
                accessibilityEvent.setContentDescription(charSequence);
                return;
            }
            accessibilityEvent.setContentDescription(MiuiLockPatternView.this.mContext.getResources().getString(R.string.input_pattern_hint_text));
        }

        public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            MiuiLockPatternView.super.onPopulateAccessibilityEvent(view, accessibilityEvent);
            if (!MiuiLockPatternView.this.mPatternInProgress) {
                accessibilityEvent.setContentDescription(MiuiLockPatternView.this.getContext().getText(17040308));
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfo accessibilityNodeInfo) {
            accessibilityNodeInfo.setText(getTextForVirtualView(i));
            accessibilityNodeInfo.setContentDescription(getTextForVirtualView(i));
            if (MiuiLockPatternView.this.mPatternInProgress) {
                accessibilityNodeInfo.setFocusable(true);
                if (isClickable(i)) {
                    accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                    accessibilityNodeInfo.setClickable(isClickable(i));
                }
            }
            accessibilityNodeInfo.setBoundsInParent(getBoundsForVirtualView(i));
        }

        private boolean isClickable(int i) {
            if (i == Integer.MIN_VALUE) {
                return false;
            }
            int i2 = i - 1;
            return !MiuiLockPatternView.this.mPatternDrawLookup[i2 / 3][i2 % 3];
        }

        /* access modifiers changed from: protected */
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            return onItemClicked(i);
        }

        /* access modifiers changed from: package-private */
        public boolean onItemClicked(int i) {
            invalidateVirtualView(i);
            sendEventForVirtualView(i, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int i) {
            int i2 = i - 1;
            Rect rect = this.mTempRect;
            int i3 = i2 / 3;
            int i4 = i2 % 3;
            CellState cellState = MiuiLockPatternView.this.mCellStates[i3][i4];
            float access$1000 = MiuiLockPatternView.this.getCenterXForColumn(i4);
            float access$1100 = MiuiLockPatternView.this.getCenterYForRow(i3);
            float access$1200 = MiuiLockPatternView.this.mSquareHeight * MiuiLockPatternView.this.mHitFactor * 0.5f;
            float access$1400 = MiuiLockPatternView.this.mSquareWidth * MiuiLockPatternView.this.mHitFactor * 0.5f;
            rect.left = (int) (access$1000 - access$1400);
            rect.right = (int) (access$1000 + access$1400);
            rect.top = (int) (access$1100 - access$1200);
            rect.bottom = (int) (access$1100 + access$1200);
            return rect;
        }

        private CharSequence getTextForVirtualView(int i) {
            return MiuiLockPatternView.this.getResources().getString(R.string.lockscreen_access_pattern_cell_added_verbose, new Object[]{Integer.valueOf(i)});
        }

        private int getVirtualViewIdForHit(float f, float f2) {
            int access$1600;
            int access$1500 = MiuiLockPatternView.this.getRowHit(f2);
            if (access$1500 < 0 || (access$1600 = MiuiLockPatternView.this.getColumnHit(f)) < 0) {
                return Integer.MIN_VALUE;
            }
            boolean z = MiuiLockPatternView.this.mPatternDrawLookup[access$1500][access$1600];
            int i = (access$1500 * 3) + access$1600 + 1;
            if (z) {
                return i;
            }
            return Integer.MIN_VALUE;
        }
    }
}
