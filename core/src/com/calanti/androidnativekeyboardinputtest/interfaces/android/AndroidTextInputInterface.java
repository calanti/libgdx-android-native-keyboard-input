package com.calanti.androidnativekeyboardinputtest.interfaces.android;

/** Used to make requests / status updates to the native Android EditText
 * @Author: calanti.games@gmail.com
 */

public interface AndroidTextInputInterface {

    /** Request the keyboard for the specific TextField/TextArea */
    void requestKeyboard(AndroidKeyboardFeedbackInterface textField);

    /** Force hide the keyboard, e.g. when the user unfocuses the TextField */
    void forceHideKeyboard();

    /** Tell the Android app we detected the keyboard closing through non-catchable methods (such as back-button pressed) */
    void keyboardHideDetected();

    void setCursorPosition(int position);

    void setSelection(int start, int end);

    /** Force a (reverse) change of text in the EditText (for example when using Cut/Paste/Delete) */
    void setText(CharSequence text, int cursorPosition);
}
