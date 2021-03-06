package com.android.keyguard.charge.rapid;

import android.graphics.PointF;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

class FireworksManager {
    private int mDistance;
    private List<PointF> mFireList = new LinkedList();
    private int mLastIndex;
    private Random mRandom;
    private float mSpeed;

    FireworksManager(int i, float f) {
        this.mDistance = i;
        this.mSpeed = f;
        this.mRandom = new Random(System.currentTimeMillis());
    }

    /* access modifiers changed from: package-private */
    public void updateDistanceAndSpeed(int i, float f) {
        this.mDistance = i;
        this.mSpeed = f;
        this.mFireList.clear();
    }

    /* access modifiers changed from: package-private */
    public void freshPositions(List<PointF> list, long j) {
        if (list != null) {
            float f = ((float) j) * this.mSpeed;
            ListIterator<PointF> listIterator = this.mFireList.listIterator();
            while (listIterator.hasNext()) {
                PointF next = listIterator.next();
                next.y -= f;
                if (next.y <= 0.0f) {
                    listIterator.remove();
                }
            }
            list.clear();
            list.addAll(this.mFireList);
        }
    }

    /* access modifiers changed from: package-private */
    public void fire() {
        int nextInt = this.mRandom.nextInt(5);
        int i = 1;
        while (Math.abs(nextInt - this.mLastIndex) <= 1 && i < 6) {
            nextInt = this.mRandom.nextInt(5);
            i++;
        }
        if (nextInt >= 0 && nextInt < 5) {
            PointF pointF = new PointF();
            pointF.x = (float) nextInt;
            pointF.y = (float) this.mDistance;
            this.mLastIndex = nextInt;
            this.mFireList.add(pointF);
        }
    }
}
