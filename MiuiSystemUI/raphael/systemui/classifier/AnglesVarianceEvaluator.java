package com.android.systemui.classifier;

public class AnglesVarianceEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d > 0.05d ? 1.0f : 0.0f;
        if (d > 0.1d) {
            f2 += 1.0f;
        }
        if (d > 0.2d) {
            f2 += 1.0f;
        }
        if (d > 0.4d) {
            f2 += 1.0f;
        }
        if (d > 0.8d) {
            f2 += 1.0f;
        }
        return d > 1.5d ? f2 + 1.0f : f2;
    }
}
