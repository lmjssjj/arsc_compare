package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import miui.view.animation.SineEaseInInterpolator;
import miui.view.animation.SineEaseInOutInterpolator;
import miui.view.animation.SineEaseOutInterpolator;

public class KeyguardBouncerMessageView extends RelativeLayout {
    private final int MAX_SHAKE_TIMES;
    private TextView mContent;
    private Resources mResources;
    private int mShakeDistance;
    private int mShakeDuration;
    /* access modifiers changed from: private */
    public int mShakeTimes;
    private TextView mTitle;

    public KeyguardBouncerMessageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardBouncerMessageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mShakeDuration = 25;
        this.MAX_SHAKE_TIMES = 2;
        this.mResources = getResources();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.secure_keyguard_bouncer_message_title);
        this.mContent = (TextView) findViewById(R.id.secure_keyguard_bouncer_message_content);
        resetAnimValue();
    }

    public void showMessage(int i, int i2) {
        if (getVisibility() != 8) {
            String str = "";
            this.mTitle.setText(i == 0 ? str : this.mResources.getString(i));
            TextView textView = this.mContent;
            if (i2 != 0) {
                str = this.mResources.getString(i2);
            }
            textView.setText(str);
        }
    }

    public void showMessage(String str, String str2) {
        if (getVisibility() == 8) {
            return;
        }
        if (!TextUtils.isEmpty(str) || !TextUtils.isEmpty(str2)) {
            this.mTitle.setText(str);
            this.mContent.setText(str2);
        }
    }

    public void showMessage(String str, String str2, int i) {
        if (getVisibility() == 8) {
            return;
        }
        if (!TextUtils.isEmpty(str) || !TextUtils.isEmpty(str2)) {
            this.mTitle.setText(str);
            this.mContent.setText(str2);
            this.mContent.setTextColor(i);
        }
    }

    public void applyHintAnimation(long j) {
        if (getVisibility() != 8 && !TextUtils.isEmpty(this.mContent.getText())) {
            this.mShakeTimes++;
            int i = this.mShakeDistance;
            this.mShakeDistance = i - (i / 2);
            float x = this.mContent.getX();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mContent, "X", new float[]{x, ((float) this.mShakeDistance) + x});
            ofFloat.setInterpolator(new SineEaseOutInterpolator());
            ofFloat.setDuration((long) this.mShakeDuration);
            TextView textView = this.mContent;
            int i2 = this.mShakeDistance;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(textView, "X", new float[]{((float) i2) + x, x - ((float) i2)});
            ofFloat2.setInterpolator(new SineEaseInOutInterpolator());
            ofFloat2.setDuration((long) (this.mShakeDuration * 2));
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.mContent, "X", new float[]{x - ((float) this.mShakeDistance), x});
            ofFloat3.setInterpolator(this.mShakeTimes == 2 ? new SineEaseOutInterpolator() : new SineEaseInInterpolator());
            ofFloat3.setDuration((long) this.mShakeDuration);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(new Animator[]{ofFloat, ofFloat2, ofFloat3});
            animatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    if (KeyguardBouncerMessageView.this.mShakeTimes > 2) {
                        KeyguardBouncerMessageView.this.resetAnimValue();
                    } else {
                        KeyguardBouncerMessageView.this.applyHintAnimation(0);
                    }
                }
            });
            animatorSet.setStartDelay(j);
            animatorSet.start();
        }
    }

    public void resetAnimValue() {
        this.mShakeTimes = 0;
        this.mShakeDistance = this.mContext.getResources().getDimensionPixelSize(R.dimen.miui_common_unlock_screen_tip_shake_distance);
    }
}
