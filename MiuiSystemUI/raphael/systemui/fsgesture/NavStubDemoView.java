package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.systemui.plugins.R;
import miui.util.CustomizeUtil;

public class NavStubDemoView extends View {
    public static final String TAG = "NavStubDemoView";
    /* access modifiers changed from: private */
    public Activity curActivity;
    /* access modifiers changed from: private */
    public FsGestureDemoTitleView demoTitleView;
    /* access modifiers changed from: private */
    public String demoType;
    private int fullyShowStep;
    /* access modifiers changed from: private */
    public boolean isFromPro;
    /* access modifiers changed from: private */
    public View mAppBgView;
    private Bitmap mAppIcon;
    /* access modifiers changed from: private */
    public View mAppNoteImg;
    /* access modifiers changed from: private */
    public View mBgView;
    /* access modifiers changed from: private */
    public int mBottomDec;
    /* access modifiers changed from: private */
    public float mCurAlpha;
    /* access modifiers changed from: private */
    public float mCurScale;
    /* access modifiers changed from: private */
    public float mCurrentY;
    private float mDelta;
    private int mDestPivotX;
    private int mDestPivotY;
    private int mDownNo;
    private float mDownX;
    private Bitmap mDragBitmap;
    private Bitmap mDrawBmp;
    private Bitmap mFakeBitmap;
    /* access modifiers changed from: private */
    public float mFollowTailX;
    /* access modifiers changed from: private */
    public float mFollowTailY;
    /* access modifiers changed from: private */
    public Handler mFrameHandler;
    /* access modifiers changed from: private */
    public LinearLayout mHomeIconImg;
    private boolean mIsAppToHome;
    private boolean mIsAppToRecents;
    private boolean mIsInFsgAnim;
    private int mLastDownNo;
    private Xfermode mModeSrcIn;
    private Paint mPaint;
    /* access modifiers changed from: private */
    public int mPivotLocX;
    /* access modifiers changed from: private */
    public int mPivotLocY;
    /* access modifiers changed from: private */
    public ValueAnimator mRecentsAnimator;
    /* access modifiers changed from: private */
    public View mRecentsBgView;
    /* access modifiers changed from: private */
    public LinearLayout mRecentsCardContainer;
    private Rect mRecentsFirstCardBound;
    /* access modifiers changed from: private */
    public View mRecentsFirstCardIconView;
    /* access modifiers changed from: private */
    public int mShowHeight;
    Rect mShowRect;
    /* access modifiers changed from: private */
    public int mShowWidth;
    /* access modifiers changed from: private */
    public int mStateMode;
    private Runnable mTailCatcherTask;
    /* access modifiers changed from: private */
    public float mXScale;
    /* access modifiers changed from: private */
    public float mYScale;
    /* access modifiers changed from: private */
    public FsGestureDemoSwipeView swipeView;

    static /* synthetic */ float access$016(NavStubDemoView navStubDemoView, float f) {
        float f2 = navStubDemoView.mFollowTailX + f;
        navStubDemoView.mFollowTailX = f2;
        return f2;
    }

    static /* synthetic */ float access$216(NavStubDemoView navStubDemoView, float f) {
        float f2 = navStubDemoView.mFollowTailY + f;
        navStubDemoView.mFollowTailY = f2;
        return f2;
    }

    public void setHomeIconImg(LinearLayout linearLayout) {
        this.mHomeIconImg = linearLayout;
    }

    public void setRecentsBgView(View view) {
        this.mRecentsBgView = view;
    }

    public void setRecentsCardContainer(LinearLayout linearLayout) {
        this.mRecentsCardContainer = linearLayout;
    }

    public void setAppBgView(View view) {
        this.mAppBgView = view;
    }

    public void setAppNoteImg(View view) {
        this.mAppNoteImg = view;
    }

    public void setBgView(View view) {
        this.mBgView = view;
    }

    public void setDemoType(String str) {
        this.demoType = str;
    }

    public void setFullyShowStep(int i) {
        this.fullyShowStep = i;
    }

    public void setDemoTitleView(FsGestureDemoTitleView fsGestureDemoTitleView) {
        this.demoTitleView = fsGestureDemoTitleView;
    }

    public void setSwipeView(FsGestureDemoSwipeView fsGestureDemoSwipeView) {
        this.swipeView = fsGestureDemoSwipeView;
    }

    public void setCurActivity(Activity activity) {
        this.curActivity = activity;
    }

    public void setIsFromPro(boolean z) {
        this.isFromPro = z;
    }

    public void setRecentsFirstCardIconView(View view) {
        this.mRecentsFirstCardIconView = view;
    }

    public NavStubDemoView(Context context) {
        this(context, (AttributeSet) null);
    }

    public NavStubDemoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NavStubDemoView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NavStubDemoView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mFrameHandler = new Handler();
        this.mRecentsFirstCardBound = new Rect();
        this.mTailCatcherTask = new Runnable() {
            public void run() {
                NavStubDemoView navStubDemoView = NavStubDemoView.this;
                NavStubDemoView.access$016(navStubDemoView, (((float) navStubDemoView.mPivotLocX) - NavStubDemoView.this.mFollowTailX) / 4.0f);
                NavStubDemoView navStubDemoView2 = NavStubDemoView.this;
                NavStubDemoView.access$216(navStubDemoView2, (((float) navStubDemoView2.mPivotLocY) - NavStubDemoView.this.mFollowTailY) / 4.0f);
                float abs = Math.abs(((float) NavStubDemoView.this.mPivotLocX) - NavStubDemoView.this.mFollowTailX);
                float abs2 = Math.abs(((float) NavStubDemoView.this.mPivotLocY) - NavStubDemoView.this.mFollowTailY);
                double sqrt = Math.sqrt((double) ((abs * abs) + (abs2 * abs2)));
                if (NavStubDemoView.this.mStateMode == 65538) {
                    if (NavStubDemoView.this.mCurrentY < ((float) (NavStubDemoView.this.mShowHeight - 320)) && sqrt < 20.0d) {
                        int unused = NavStubDemoView.this.mStateMode = 65539;
                        Log.d(NavStubDemoView.TAG, "current state mode: StateMode.STATE_TASK_HOLD");
                        NavStubDemoView.this.performHapticFeedback(1);
                        NavStubDemoView.this.mRecentsCardContainer.setVisibility(0);
                        if (NavStubDemoView.this.mRecentsAnimator.isRunning() || NavStubDemoView.this.mRecentsAnimator.isStarted()) {
                            NavStubDemoView.this.mRecentsAnimator.cancel();
                        }
                        NavStubDemoView.this.mRecentsAnimator.start();
                    }
                } else if (NavStubDemoView.this.mStateMode == 65539 && NavStubDemoView.this.mCurrentY > ((float) (NavStubDemoView.this.mShowHeight - 240))) {
                    int unused2 = NavStubDemoView.this.mStateMode = 65538;
                    if (NavStubDemoView.this.mRecentsAnimator.isRunning() || NavStubDemoView.this.mRecentsAnimator.isStarted()) {
                        NavStubDemoView.this.mRecentsAnimator.cancel();
                    }
                    NavStubDemoView.this.mRecentsAnimator.reverse();
                }
                NavStubDemoView.this.mFrameHandler.postDelayed(this, 16);
            }
        };
        this.mModeSrcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        this.mShowRect = new Rect();
        initInternal();
    }

    private void initInternal() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mShowWidth = displayMetrics.widthPixels;
        this.mShowHeight = displayMetrics.heightPixels;
        this.mStateMode = 65537;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mRecentsAnimator = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("scale", new float[]{1.1f, 1.05f}), PropertyValuesHolder.ofInt("alpha", new int[]{0, 255})});
        this.mRecentsAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        this.mRecentsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue("scale")).floatValue();
                int intValue = ((Integer) valueAnimator.getAnimatedValue("alpha")).intValue();
                NavStubDemoView.this.mRecentsCardContainer.setScaleX(floatValue);
                NavStubDemoView.this.mRecentsCardContainer.setScaleY(floatValue);
                NavStubDemoView.this.mRecentsCardContainer.setAlpha((float) intValue);
            }
        });
        this.mRecentsAnimator.setDuration(300);
        this.mFakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_note);
        int min = Math.min(this.mFakeBitmap.getHeight(), (int) (((float) this.mFakeBitmap.getWidth()) * ((((float) this.mShowHeight) * 1.0f) / ((float) this.mShowWidth))));
        Bitmap bitmap = this.mFakeBitmap;
        this.mFakeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), min);
        this.mFakeBitmap.setHasAlpha(false);
        this.mFakeBitmap.prepareToDraw();
        this.mDragBitmap = createRoundCornerBmp(this.mFakeBitmap);
        this.mDragBitmap.setHasAlpha(false);
        this.mDragBitmap.prepareToDraw();
        this.mAppIcon = BitmapFactory.decodeResource(getResources(), R.drawable.note_icon);
    }

    private Bitmap createRoundCornerBmp(Bitmap bitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawRoundRect(new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()), 50.0f, 50.0f, this.mPaint);
        this.mPaint.setXfermode(this.mModeSrcIn);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mPaint);
        this.mPaint.setXfermode((Xfermode) null);
        return createBitmap;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawBmp == null) {
            return;
        }
        if (!this.mIsAppToHome) {
            canvas.save();
            canvas.translate((float) this.mPivotLocX, (float) this.mPivotLocY);
            float f = this.mCurScale;
            canvas.scale(f, f);
            canvas.translate((float) (-this.mPivotLocX), (float) (-this.mPivotLocY));
            Rect rect = this.mShowRect;
            int i = this.mPivotLocX;
            int i2 = this.mShowWidth;
            rect.left = i - (i2 / 2);
            int i3 = this.mPivotLocY;
            int i4 = this.mShowHeight;
            rect.top = i3 - i4;
            rect.right = rect.left + i2;
            int i5 = rect.top;
            rect.bottom = i5 + i4;
            int i6 = (int) (((float) i5) + (((float) i4) * this.mCurScale));
            if (this.mIsAppToRecents) {
                i6 = this.mBottomDec + i5;
            }
            this.mPaint.setAlpha(255);
            this.mPaint.setXfermode((Xfermode) null);
            this.mPaint.setStyle(Paint.Style.FILL);
            Rect rect2 = this.mShowRect;
            float f2 = (float) rect2.right;
            float f3 = f2;
            float f4 = (float) i6;
            int saveLayer = canvas.saveLayer((float) rect2.left, (float) rect2.top, f3, f4, (Paint) null);
            Rect rect3 = this.mShowRect;
            float f5 = (float) rect3.right;
            canvas.drawRoundRect((float) rect3.left, (float) rect3.top, f5, f4, 50.0f, 50.0f, this.mPaint);
            this.mPaint.setXfermode(this.mModeSrcIn);
            canvas.drawBitmap(this.mDrawBmp, (Rect) null, this.mShowRect, this.mPaint);
            this.mPaint.setXfermode((Xfermode) null);
            canvas.restoreToCount(saveLayer);
            canvas.restore();
            return;
        }
        canvas.save();
        canvas.translate((float) this.mPivotLocX, (float) this.mPivotLocY);
        canvas.scale(this.mXScale, this.mYScale);
        canvas.translate((float) (-this.mPivotLocX), (float) (-this.mPivotLocY));
        Rect rect4 = this.mShowRect;
        int i7 = this.mPivotLocX;
        int i8 = this.mShowWidth;
        rect4.left = i7 - (i8 / 2);
        int i9 = this.mPivotLocY;
        int i10 = this.mShowHeight;
        rect4.top = i9 - (i10 / 2);
        rect4.right = rect4.left + i8;
        rect4.bottom = rect4.top + i10;
        this.mPaint.setAlpha((int) ((1.0f - this.mCurAlpha) * 255.0f));
        canvas.drawBitmap(this.mAppIcon, (Rect) null, this.mShowRect, this.mPaint);
        this.mPaint.setAlpha((int) (this.mCurAlpha * 255.0f));
        canvas.drawBitmap(this.mDrawBmp, (Rect) null, this.mShowRect, this.mPaint);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mIsInFsgAnim) {
            return false;
        }
        if (motionEvent.getAction() == 0) {
            this.mDownNo++;
        }
        if (this.mDownNo == this.mLastDownNo) {
            return false;
        }
        if (1 == motionEvent.getAction()) {
            this.mLastDownNo = this.mDownNo;
        }
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action == 0) {
            this.swipeView.cancelAnimation();
            this.mDownX = motionEvent.getRawX();
            int i = this.mShowWidth;
            this.mDelta = ((float) (i / 2)) - this.mDownX;
            int i2 = i / 2;
            this.mPivotLocX = i2;
            this.mFollowTailX = (float) i2;
            int i3 = this.mShowHeight;
            this.mPivotLocY = i3;
            this.mFollowTailY = (float) i3;
            this.mDrawBmp = this.mDragBitmap;
            this.mIsAppToHome = false;
            this.mStateMode = 65537;
        } else if (action == 1) {
            this.mIsInFsgAnim = true;
            setClickable(false);
            this.mFrameHandler.removeCallbacksAndMessages((Object) null);
            boolean z2 = this.mStateMode == 65538;
            if (this.mStateMode == 65539) {
                z = true;
            }
            if (z2 || z) {
                int i4 = this.mPivotLocY;
                float f = this.mFollowTailY;
                if (((float) i4) - f > 20.0f) {
                    performHapticFeedback(1);
                    startCancelAnim();
                } else if (((float) i4) - f < -20.0f) {
                    if ("DEMO_FULLY_SHOW".equals(this.demoType) && this.fullyShowStep == 1) {
                        startToHomeAnim();
                    } else if ("DEMO_TO_HOME".equals(this.demoType)) {
                        startToHomeAnim();
                    } else {
                        performHapticFeedback(1);
                        startCancelAnim();
                    }
                } else if (z2) {
                    performHapticFeedback(1);
                    startCancelAnim();
                } else if ("DEMO_FULLY_SHOW".equals(this.demoType) && this.fullyShowStep == 2) {
                    startRecentTaskAnim();
                } else if ("DEMO_TO_RECENTTASK".equals(this.demoType)) {
                    startRecentTaskAnim();
                } else {
                    performHapticFeedback(1);
                    startCancelAnim();
                }
            } else {
                finalization();
            }
        } else if (action == 2) {
            this.mPivotLocX = (int) (((motionEvent.getRawX() + this.mDownX) / 2.0f) + this.mDelta);
            int i5 = this.mShowHeight;
            this.mPivotLocY = (int) (((float) i5) - (linearToCubic(this.mCurrentY, (float) i5, 0.0f, 3.0f) * 444.0f));
            if (this.mStateMode == 65537) {
                this.mStateMode = 65538;
                this.mFrameHandler.post(this.mTailCatcherTask);
                setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
                this.mRecentsCardContainer.setVisibility(8);
                this.mAppNoteImg.setVisibility(8);
                this.mHomeIconImg.setVisibility(8);
                this.mRecentsFirstCardIconView.setVisibility(4);
            }
            this.mCurScale = 1.0f - (linearToCubic(this.mCurrentY, (float) this.mShowHeight, 0.0f, 3.0f) * 0.385f);
            invalidate();
        } else if (action == 3) {
            finalization();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void setDestPivot(int i, int i2) {
        this.mDestPivotX = i;
        this.mDestPivotY = i2;
    }

    private void startToHomeAnim() {
        this.mIsAppToHome = true;
        float height = (((float) this.mAppIcon.getHeight()) * 1.0f) / ((float) this.mShowHeight);
        float width = (((float) this.mAppIcon.getWidth()) * 1.0f) / ((float) this.mShowWidth);
        float f = this.mCurScale;
        int i = this.mPivotLocX;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                NavStubDemoView.this.mHomeIconImg.setVisibility(0);
                NavStubDemoView.this.mRecentsCardContainer.setVisibility(8);
                NavStubDemoView.this.mRecentsBgView.setVisibility(8);
                NavStubDemoView.this.mAppNoteImg.setVisibility(8);
                NavStubDemoView.this.mAppBgView.setVisibility(8);
            }

            public void onAnimationEnd(Animator animator) {
                NavStubDemoView.this.demoTitleView.notifyFinish();
                if ("DEMO_FULLY_SHOW".equals(NavStubDemoView.this.demoType)) {
                    NavStubDemoView.this.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent();
                            intent.setClass(NavStubDemoView.this.getContext(), HomeDemoAct.class);
                            intent.putExtra("DEMO_TYPE", "DEMO_FULLY_SHOW");
                            intent.putExtra("FULLY_SHOW_STEP", 2);
                            intent.putExtra("IS_FROM_PROVISION", NavStubDemoView.this.isFromPro);
                            NavStubDemoView.this.getContext().startActivity(intent);
                            NavStubDemoView.this.curActivity.overridePendingTransition(R.anim.activity_start_enter, R.anim.activity_start_exit);
                            NavStubDemoView.this.curActivity.finish();
                        }
                    }, 1000);
                } else if ("DEMO_TO_HOME".equals(NavStubDemoView.this.demoType)) {
                    NavStubDemoView.this.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            NavStubDemoView.this.curActivity.finish();
                        }
                    }, 1000);
                }
            }
        });
        ValueAnimator ofPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("xScale", new float[]{this.mCurScale, width}), PropertyValuesHolder.ofInt("xPivot", new int[]{i, this.mDestPivotX})});
        ofPropertyValuesHolder.setInterpolator(new DecelerateInterpolator(1.5f));
        ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubDemoView.this.mXScale = ((Float) valueAnimator.getAnimatedValue("xScale")).floatValue();
                int unused2 = NavStubDemoView.this.mPivotLocX = ((Integer) valueAnimator.getAnimatedValue("xPivot")).intValue();
            }
        });
        ofPropertyValuesHolder.setDuration(300);
        ValueAnimator ofPropertyValuesHolder2 = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("yScale", new float[]{this.mCurScale, height}), PropertyValuesHolder.ofInt("yPivot", new int[]{(int) (((float) this.mPivotLocY) - ((((float) this.mShowHeight) * f) / 2.0f)), this.mDestPivotY})});
        ofPropertyValuesHolder2.setInterpolator(new DecelerateInterpolator(2.0f));
        ofPropertyValuesHolder2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubDemoView.this.mYScale = ((Float) valueAnimator.getAnimatedValue("yScale")).floatValue();
                int unused2 = NavStubDemoView.this.mPivotLocY = ((Integer) valueAnimator.getAnimatedValue("yPivot")).intValue();
            }
        });
        ofPropertyValuesHolder2.setDuration(300);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        ofFloat.setInterpolator(new DecelerateInterpolator(1.0f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubDemoView.this.mCurAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        ofFloat.setDuration(210);
        ofFloat.setStartDelay(40);
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NavStubDemoView.this.mBgView.setBackgroundColor(Color.argb((int) ((1.0f - valueAnimator.getAnimatedFraction()) * 187.0f), 0, 0, 0));
                NavStubDemoView.this.invalidate();
            }
        });
        ofFloat2.setDuration(300);
        animatorSet.playTogether(new Animator[]{ofFloat2, ofPropertyValuesHolder2, ofPropertyValuesHolder, ofFloat});
        animatorSet.start();
    }

    private void startCancelAnim() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mCurScale, 1.0f});
        ofFloat.setInterpolator(new DecelerateInterpolator());
        final int i = this.mPivotLocX;
        final int i2 = this.mPivotLocY;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubDemoView.this.mCurScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float animatedFraction = valueAnimator.getAnimatedFraction();
                NavStubDemoView navStubDemoView = NavStubDemoView.this;
                int unused2 = navStubDemoView.mPivotLocX = (int) (((float) i) + (((float) ((navStubDemoView.mShowWidth / 2) - i)) * animatedFraction));
                NavStubDemoView navStubDemoView2 = NavStubDemoView.this;
                int unused3 = navStubDemoView2.mPivotLocY = (int) (((float) i2) + (((float) (navStubDemoView2.mShowHeight - i2)) * animatedFraction));
                NavStubDemoView.this.invalidate();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                NavStubDemoView.this.getHandler().postDelayed(new Runnable() {
                    public void run() {
                        NavStubDemoView.this.swipeView.prepare(2);
                        NavStubDemoView.this.swipeView.startAnimation(2);
                    }
                }, 300);
                NavStubDemoView.this.finalization();
            }
        });
        ofFloat.setDuration(300).start();
    }

    public void setRecentsFirstCardBound(Rect rect) {
        this.mRecentsFirstCardBound = rect;
    }

    private void startRecentTaskAnim() {
        this.mShowHeight -= CustomizeUtil.HAS_NOTCH ? getContext().getResources().getDimensionPixelSize(R.dimen.status_bar_height) : 0;
        this.mIsAppToRecents = true;
        float width = (((float) this.mRecentsFirstCardBound.width()) * 1.0f) / ((float) this.mShowWidth);
        ValueAnimator ofPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("scale", new float[]{this.mCurScale, width}), PropertyValuesHolder.ofInt("bottomDec", new int[]{(int) (this.mCurScale * ((float) this.mShowHeight)), (int) (((float) this.mRecentsFirstCardBound.height()) / width)})});
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                NavStubDemoView.this.mRecentsFirstCardIconView.setVisibility(0);
                NavStubDemoView.this.demoTitleView.notifyFinish();
                if ("DEMO_FULLY_SHOW".equals(NavStubDemoView.this.demoType)) {
                    NavStubDemoView.this.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent();
                            intent.setClass(NavStubDemoView.this.getContext(), FsGestureBackDemoActivity.class);
                            intent.putExtra("DEMO_TYPE", "DEMO_FULLY_SHOW");
                            intent.putExtra("IS_FROM_PROVISION", NavStubDemoView.this.isFromPro);
                            NavStubDemoView.this.getContext().startActivity(intent);
                            NavStubDemoView.this.curActivity.overridePendingTransition(R.anim.activity_start_enter, R.anim.activity_start_exit);
                            NavStubDemoView.this.curActivity.finish();
                        }
                    }, 1000);
                } else if ("DEMO_TO_RECENTTASK".equals(NavStubDemoView.this.demoType)) {
                    NavStubDemoView.this.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            NavStubDemoView.this.curActivity.finish();
                        }
                    }, 1000);
                }
            }
        });
        ofPropertyValuesHolder.setInterpolator(new DecelerateInterpolator());
        final int i = this.mPivotLocX;
        final int i2 = this.mPivotLocY;
        Rect rect = this.mRecentsFirstCardBound;
        final float width2 = (float) ((this.mRecentsFirstCardBound.width() / 2) + rect.left);
        final float width3 = (float) (((rect.width() * this.mShowHeight) / this.mShowWidth) + this.mRecentsFirstCardBound.top);
        ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubDemoView.this.mCurScale = ((Float) valueAnimator.getAnimatedValue("scale")).floatValue();
                int unused2 = NavStubDemoView.this.mBottomDec = ((Integer) valueAnimator.getAnimatedValue("bottomDec")).intValue();
                float animatedFraction = valueAnimator.getAnimatedFraction();
                NavStubDemoView navStubDemoView = NavStubDemoView.this;
                int i = i;
                int unused3 = navStubDemoView.mPivotLocX = (int) (((float) i) + ((width2 - ((float) i)) * animatedFraction));
                NavStubDemoView navStubDemoView2 = NavStubDemoView.this;
                int i2 = i2;
                int unused4 = navStubDemoView2.mPivotLocY = (int) (((float) i2) + ((width3 - ((float) i2)) * animatedFraction));
                NavStubDemoView.this.invalidate();
            }
        });
        ofPropertyValuesHolder.setDuration(300);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.05f, 1.0f});
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                NavStubDemoView.this.mRecentsCardContainer.setScaleX(floatValue);
                NavStubDemoView.this.mRecentsCardContainer.setScaleY(floatValue);
            }
        });
        ofFloat.setDuration(300);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{ofPropertyValuesHolder, ofFloat});
        animatorSet.start();
    }

    /* access modifiers changed from: private */
    public void finalization() {
        this.mIsAppToRecents = false;
        this.mIsInFsgAnim = false;
        setClickable(true);
        this.mIsAppToHome = false;
        this.mPivotLocY = 0;
        this.mPivotLocX = 0;
        this.mCurAlpha = 1.0f;
        this.mCurScale = 0.0f;
        this.mPaint.setAlpha(255);
        this.mStateMode = 65537;
        this.mDrawBmp = this.mFakeBitmap;
        this.mHomeIconImg.setVisibility(0);
        this.mRecentsBgView.setVisibility(0);
        this.mAppBgView.setVisibility(0);
        this.mAppNoteImg.setVisibility(0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, (int) (getContext().getResources().getDisplayMetrics().density * 20.0f));
        layoutParams.addRule(12);
        setLayoutParams(layoutParams);
        invalidate();
        Handler handler = this.mFrameHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages((Object) null);
        }
    }

    private float linearToCubic(float f, float f2, float f3, float f4) {
        if (f4 == f2) {
            return f;
        }
        float f5 = (f - f2) / (f4 - f2);
        if (f4 != 0.0f) {
            return (float) (1.0d - Math.pow((double) (1.0f - f5), (double) f4));
        }
        return 0.0f;
    }
}
