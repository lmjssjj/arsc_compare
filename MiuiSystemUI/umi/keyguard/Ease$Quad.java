package com.android.keyguard;

import android.animation.TimeInterpolator;

public class Ease$Quad {
    public static final TimeInterpolator easeInOut = new TimeInterpolator() {
        public float getInterpolation(float f) {
            float f2 = f / 0.5f;
            if (f2 < 1.0f) {
                return (0.5f * f2 * f2) + 0.0f;
            }
            float f3 = f2 - 1.0f;
            return (((f3 * (f3 - 2.0f)) - 1.0f) * -0.5f) + 0.0f;
        }
    };
}
