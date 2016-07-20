package com.calanti.androidnativekeyboardinputtest.interfaces.android;

/** Used to detect a change in Android's visible screen area.
 * This is used to make a pretty good guess of when the keyboard has been opened/closed
 * via non-catchable methods (such as back-button pressed)
 * @Author: calanti.games@gmail.com
 */

public interface VisibleView {
    void onSizeChange(float width, float height);
    void setListener(VisibleViewSizeChangeListener sizeChangeListener);
    float getWidth();
    float getHeight();
}
