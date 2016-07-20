package com.calanti.androidnativekeyboardinputtest;

import com.calanti.androidnativekeyboardinputtest.interfaces.android.VisibleView;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.VisibleViewSizeChangeListener;

/**
 * @Author: calanti.games@gmail.com
 */

public class AndroidVisibleView implements VisibleView {

    private VisibleViewSizeChangeListener visibleViewSizeChangeListener;

    private float screenWidth, screenHeight;

    public AndroidVisibleView(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void setListener(VisibleViewSizeChangeListener sizeChangeListener) {
        this.visibleViewSizeChangeListener = sizeChangeListener;
    }

    @Override
    public float getWidth() {
        return this.screenWidth;
    }

    @Override
    public float getHeight() {
        return this.screenHeight;
    }

    @Override
    public void onSizeChange(float width, float height) {
        this.screenHeight = height;
        this.screenWidth = width;
        if(visibleViewSizeChangeListener != null) visibleViewSizeChangeListener.onSizeChange(width, height);
    }
}
