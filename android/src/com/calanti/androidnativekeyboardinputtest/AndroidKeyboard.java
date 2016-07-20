package com.calanti.androidnativekeyboardinputtest;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.AndroidKeyboardFeedbackInterface;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.AndroidTextInputInterface;
import com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3.CalTextArea;
import com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3.CalTextField;

/** The class containing the hidden EditText and tools to manage data to/from libgdx core.
 * @Author: calanti
 */

public class AndroidKeyboard implements AndroidTextInputInterface {

    private final Activity activity;

    /** The Android EditText hidden offscreen with the keyboard focus. */
    private EditText hiddenEditText;

    /** The TextWatcher that listens to any text change on the hidden EditText */
    private TextWatcher hiddenEditTextTextWatcher;

    private boolean disableTextWatcher = true;

    /** The libgdx TextField/TextArea to bind the EditText to */
    private AndroidKeyboardFeedbackInterface keyboardFeedbackTextField;

    public AndroidKeyboard(Activity activity){
        this.activity = activity;

        this.hiddenEditText = new EditText(activity);

        //put the EditText in a silly position so it is offscreen
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 2000;
        params.topMargin = 0;

        hiddenEditText.setLayoutParams(params);

        //tell the EditText to show the keyboard when it receives focus
        hiddenEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(hiddenEditText);
                }
            }
        });

        //configure the TextWatcher to provide feedback
        hiddenEditTextTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(disableTextWatcher) return;
                hiddenEditTextTextChanged(charSequence, hiddenEditText.getSelectionStart(), hiddenEditText.getSelectionEnd());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        };

        hiddenEditText.addTextChangedListener(hiddenEditTextTextWatcher);
    }

    EditText getForView(){
        return this.hiddenEditText;
    }

    private void showKeyboard(EditText target){
        if (target == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(target, InputMethodManager.SHOW_FORCED);
    }

    private void hiddenEditTextTextChanged(final CharSequence text, final int cursorPosition, final int selectionEnd){
        System.out.println("AK: EditText change: cursor: "+cursorPosition+" selectionEnd: "+selectionEnd+" text: "+text);

        //just in case, probably shouldn't happen though
        if(keyboardFeedbackTextField == null) return;

        //sync with the gdx thread
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                //math.max needed because my asus keyboard also deletes from cursor point 0, which creates negative cursor pos..
                keyboardFeedbackTextField.textChanged(text, Math.max(0,cursorPosition), Math.max(0,selectionEnd));
            }
        });
    }

    @Override
    public void forceHideKeyboard() {
        System.out.println("AK: fired forceHideKeyboard");
        this.keyboardFeedbackTextField = null;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableTextWatcher = true;
                hiddenEditText.setSelection(0);
                hiddenEditText.setText("");
                hiddenEditText.clearFocus();

                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(hiddenEditText.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void keyboardHideDetected() {
        System.out.println("AK: fired keyboardHideDetected");
        this.keyboardFeedbackTextField = null;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableTextWatcher = true;
                hiddenEditText.setSelection(0);
                hiddenEditText.setText("");
                hiddenEditText.clearFocus();
            }
        });
    }

    @Override
    public void requestKeyboard(final AndroidKeyboardFeedbackInterface textField) {
        //allow the TextField to configure itself
        disableTextWatcher = true;
        this.keyboardFeedbackTextField = null;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                requestKeyboard(textField, textField.getText(), textField.getCursorPosition());
            }
        });
    }

    private void requestKeyboard(final AndroidKeyboardFeedbackInterface textField, final CharSequence currentText, final int cursorPosition) {
        System.out.println("AK: fired requestKeyboard, cursor position: "+cursorPosition);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //set the keyboard type depending on TextField/TextAreaStyle

                CalTextField.TextFieldStyle style = textField.getStyle();

                hiddenEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                if(style.androidKeyboardNumericalOnly){
                    hiddenEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    if(style.androidKeyboardAutoCorrect) {
                        hiddenEditText.setInputType(hiddenEditText.getInputType() | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                    }

                    if(!style.androidKeyboardTextSuggestions) {
                        hiddenEditText.setInputType(hiddenEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    }

                    if(style instanceof CalTextArea.TextAreaStyle){
                        if(((CalTextArea.TextAreaStyle) style).androidKeyboardCarriageReturn){
                            hiddenEditText.setInputType(hiddenEditText.getInputType() |
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                        }
                    }
                }

                hiddenEditText.clearFocus();
                hiddenEditText.requestFocus();
                hiddenEditText.setText(currentText);
                hiddenEditText.setSelection(cursorPosition);
                disableTextWatcher = false;
            }
        });
        this.keyboardFeedbackTextField = textField;
    }

    @Override
    public void setCursorPosition(final int position) {
        if(this.keyboardFeedbackTextField == null) return;
        System.out.println("AK: fired setCursorPosition at: "+position);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hiddenEditText.setSelection(position);
            }
        });
    }

    @Override
    public void setSelection(final int start, final int end) {
        System.out.println("AK: fired setSelection start: "+start+" end: "+end);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("AK: set selection: "+start+" to "+end);
                hiddenEditText.setSelection(start, end);
            }
        });
    }

    @Override
    public void setText(final CharSequence text, final int cursorPosition) {
        System.out.println("AK: fired setText pos: "+cursorPosition+" text: "+text);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableTextWatcher = true;
                hiddenEditText.setText(text);
                hiddenEditText.setSelection(Math.max(0, cursorPosition));
                disableTextWatcher = false;
                hiddenEditTextTextChanged(text, cursorPosition, cursorPosition);
            }
        });
    }
}
