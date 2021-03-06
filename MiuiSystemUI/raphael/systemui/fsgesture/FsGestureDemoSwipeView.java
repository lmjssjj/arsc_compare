package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.plugins.R;

public class FsGestureDemoSwipeView extends FrameLayout {
    AnimatorSet finalAnimatorSet;
    ObjectAnimator hidingAnimator;
    /* access modifiers changed from: private */
    public int mDisplayHeight;
    /* access modifiers changed from: private */
    public int mDisplayWidth;
    private float mFinalTranslate;
    ObjectAnimator movingAnimator;
    ObjectAnimator scaleAnimator;
    ObjectAnimator showingAnimator;

    public FsGestureDemoSwipeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.fs_gesture_swipe_view, this);
        setAlpha(0.0f);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mDisplayWidth = displayMetrics.widthPixels;
        this.mDisplayHeight = displayMetrics.heightPixels;
        this.mFinalTranslate = getResources().getDimension(R.dimen.fsgesture_swipe_final_translateX);
    }

    /* access modifiers changed from: package-private */
    public void prepare(int i) {
        setAlpha(0.0f);
        setVisibility(0);
        switch (i) {
            case 0:
                setTranslationY(getResources().getDimension(R.dimen.fsgesture_swipe_translateY));
                setTranslationX((float) ((-getWidth()) / 2));
                return;
            case 1:
                setTranslationY(getResources().getDimension(R.dimen.fsgesture_swipe_translateY));
                setTranslationX((float) (this.mDisplayWidth - (getWidth() / 2)));
                return;
            case 2:
            case 4:
            case 5:
            case 6:
                setTranslationX((float) ((this.mDisplayWidth / 2) - (getLeft() + (getWidth() / 2))));
                setTranslationY((float) (this.mDisplayHeight - (getHeight() / 2)));
                return;
            case 3:
                setTranslationY(getResources().getDimension(R.dimen.fsgesture_swipe_drawer_translateY));
                setTranslationX((float) ((-getWidth()) / 2));
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(int i) {
        createShowingAnimator(i);
        createMovingAnimator(i);
        createScaleAnimator(i);
        createHidingAnimator(i);
        createFinalAnimSet(i);
        this.finalAnimatorSet.start();
    }

    private void createFinalAnimSet(final int i) {
        if (this.finalAnimatorSet == null) {
            this.finalAnimatorSet = new AnimatorSet();
            if (i != 4) {
                this.finalAnimatorSet.playSequentially(new Animator[]{this.showingAnimator, this.movingAnimator, this.hidingAnimator});
            } else {
                this.finalAnimatorSet.playSequentially(new Animator[]{this.showingAnimator, this.movingAnimator, this.scaleAnimator, this.hidingAnimator});
            }
            this.finalAnimatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    switch (i) {
                        case 0:
                        case 3:
                            FsGestureDemoSwipeView.this.setTranslationX((float) ((-FsGestureDemoSwipeView.this.getWidth()) / 2));
                            break;
                        case 1:
                            FsGestureDemoSwipeView.this.setTranslationX((float) (FsGestureDemoSwipeView.this.mDisplayWidth - (FsGestureDemoSwipeView.this.getWidth() / 2)));
                            break;
                        case 2:
                        case 4:
                            FsGestureDemoSwipeView.this.setTranslationY((float) (FsGestureDemoSwipeView.this.mDisplayHeight - (FsGestureDemoSwipeView.this.getHeight() / 2)));
                            break;
                        case 5:
                        case 6:
                            FsGestureDemoSwipeView.this.setTranslationX((float) ((FsGestureDemoSwipeView.this.mDisplayWidth / 2) - (FsGestureDemoSwipeView.this.getLeft() + (FsGestureDemoSwipeView.this.getWidth() / 2))));
                            FsGestureDemoSwipeView.this.setTranslationY((float) (FsGestureDemoSwipeView.this.mDisplayHeight - (FsGestureDemoSwipeView.this.getHeight() / 2)));
                            break;
                    }
                    FsGestureDemoSwipeView.this.finalAnimatorSet.setStartDelay(1500);
                    FsGestureDemoSwipeView.this.finalAnimatorSet.start();
                }
            });
        }
    }

    private void createScaleAnimator(int i) {
        if (this.scaleAnimator == null) {
            this.scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("scaleX", new float[]{1.0f, 1.2f}), PropertyValuesHolder.ofFloat("scaleY", new float[]{1.0f, 1.2f})});
            this.scaleAnimator.setDuration(1000);
        }
    }

    private void createShowingAnimator(int i) {
        if (this.showingAnimator == null) {
            this.showingAnimator = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("scaleX", new float[]{1.2f, 1.0f}), PropertyValuesHolder.ofFloat("scaleY", new float[]{1.2f, 1.0f}), PropertyValuesHolder.ofFloat("alpha", new float[]{0.0f, 1.0f})});
            this.showingAnimator.setDuration(200);
            this.showingAnimator.setStartDelay(300);
        }
    }

    private void createHidingAnimator(int i) {
        if (this.hidingAnimator == null) {
            if (i != 4) {
                this.hidingAnimator = ObjectAnimator.ofFloat(this, "alpha", new float[]{1.0f, 0.0f});
                this.hidingAnimator.setDuration(300);
                return;
            }
            this.hidingAnimator = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("scaleX", new float[]{1.2f, 1.5f}), PropertyValuesHolder.ofFloat("scaleY", new float[]{1.2f, 1.5f}), PropertyValuesHolder.ofFloat("alpha", new float[]{1.0f, 0.0f})});
            this.hidingAnimator.setDuration(100);
        }
    }

    private void createMovingAnimator(int i) {
        if (this.movingAnimator == null) {
            switch (i) {
                case 0:
                case 3:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", new float[]{(float) ((-getWidth()) / 2), this.mFinalTranslate - ((float) (getWidth() / 2))});
                    break;
                case 1:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", new float[]{(float) (this.mDisplayWidth - (getWidth() / 2)), ((float) this.mDisplayWidth) - this.mFinalTranslate});
                    break;
                case 2:
                case 4:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) (this.mDisplayHeight - (getHeight() / 2)), (float) (this.mDisplayHeight - 1000)});
                    break;
                case 5:
                    float left = (float) ((this.mDisplayWidth / 2) - ((getLeft() + getWidth()) / 2));
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", new float[]{left, this.mFinalTranslate + left});
                    break;
                case 6:
                    int left2 = (this.mDisplayWidth / 2) - ((getLeft() + getWidth()) / 2);
                    Path path = new Path();
                    float f = (float) left2;
                    float height = (float) (this.mDisplayHeight - (getHeight() / 2));
                    path.moveTo(f, height);
                    path.lineTo(f, height - (this.mFinalTranslate / 2.0f));
                    float f2 = this.mFinalTranslate;
                    path.lineTo(f + f2, height - (f2 / 2.0f));
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", "translationY", path);
                    break;
            }
            this.movingAnimator.setStartDelay(1000);
            if (i == 6) {
                this.movingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                this.movingAnimator.setDuration(1000);
                return;
            }
            this.movingAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            this.movingAnimator.setDuration(500);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation() {
        setVisibility(8);
        AnimatorSet animatorSet = this.finalAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.finalAnimatorSet.removeAllListeners();
            this.finalAnimatorSet = null;
        }
    }
}
