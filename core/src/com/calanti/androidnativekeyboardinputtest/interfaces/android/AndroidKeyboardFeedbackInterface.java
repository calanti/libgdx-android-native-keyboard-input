package com.calanti.androidnativekeyboardinputtest.interfaces.android;

import com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3.CalTextField;

/** Used to bind and update the TextField/TextArea with new text from the Android TextWatcher
 * @Author: calanti.games@gmail.com
 */

public interface AndroidKeyboardFeedbackInterface {

    void textChanged(CharSequence text, int cursorPosition, int selectionEnd);

    /** Used for initialising the EditText only */
    String getText();
    int getCursorPosition();
    CalTextField.TextFieldStyle getStyle();

}
