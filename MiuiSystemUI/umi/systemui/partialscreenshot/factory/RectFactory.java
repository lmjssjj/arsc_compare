package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;
import com.android.systemui.partialscreenshot.shape.RectScreenshot;

public class RectFactory extends ShapeFactory {
    private int mState = 1;
    private RectScreenshot rect;

    public boolean onTouch(View view, MotionEvent motionEvent) {
        PartialScreenshotView partialScreenshotView = (PartialScreenshotView) view;
        int action = motionEvent.getAction();
        if (action == 0) {
            return onActionDown(partialScreenshotView, motionEvent, view);
        }
        if (action == 1) {
            return onActionUp(partialScreenshotView, motionEvent);
        }
        if (action != 2) {
            return false;
        }
        return onActionMove(partialScreenshotView, motionEvent);
    }

    private boolean onActionDown(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent, View view) {
        if (this.mState == 1) {
            RectScreenshot rectScreenshot = new RectScreenshot(view);
            this.rect = rectScreenshot;
            rectScreenshot.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            partialScreenshotView.setProduct(this.rect);
        } else if (this.rect.getSelectionRect() != null) {
            this.rect.onActionDown(motionEvent);
        } else {
            this.mState = 1;
        }
        return true;
    }

    private boolean onActionMove(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        RectScreenshot rectScreenshot = this.rect;
        if (rectScreenshot != null) {
            if (this.mState == 1) {
                rectScreenshot.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            } else {
                rectScreenshot.onActionMove(motionEvent);
            }
            partialScreenshotView.setProduct(this.rect);
        }
        return true;
    }

    private boolean onActionUp(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        RectScreenshot rectScreenshot = this.rect;
        if (rectScreenshot != null) {
            if (rectScreenshot.getSelectionRect() == null) {
                this.mState = 1;
                partialScreenshotView.clear();
            } else {
                this.mState = 2;
            }
        }
        return true;
    }

    public int getState() {
        return this.mState;
    }

    public Rect getTrimmingFrame() {
        return this.rect.getSelectionRect();
    }

    public void notifyShapeChanged(Rect rect2, PartialScreenshotView partialScreenshotView) {
        RectScreenshot rectScreenshot = new RectScreenshot(partialScreenshotView);
        this.rect = rectScreenshot;
        rectScreenshot.startSelection(rect2.left, rect2.top);
        this.rect.updateSelection(rect2.right, rect2.bottom);
        partialScreenshotView.setProduct(this.rect);
        this.mState = 2;
    }

    public Bitmap getPartialBitmap(Bitmap bitmap) {
        return this.rect.getPartialBitmap(bitmap);
    }
}
