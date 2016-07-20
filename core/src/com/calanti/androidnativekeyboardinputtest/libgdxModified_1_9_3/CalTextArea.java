package com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.AndroidTextInputInterface;

/** A multiple-line text input field, entirely based on {@link com.badlogic.gdx.scenes.scene2d.ui.TextField} */

/** Modified by calanti for:
 *  1) Extend CalTextField for native-like Android text input
 *  2) Added some new features such as auto-sizing with new lines and limiting the
 *  number of lines (for CalTextArea$AndroidOnscreenKeyboard only)
 *  3) Various bug fixes to original TextArea
 * */
public class CalTextArea extends CalTextField {


    /** calanti addition - replaced lineBreak array with this for my own sanity. */
    private class LineMeta {
        int startIndex, endIndex;

        LineMeta(int startIndex, int endIndex){
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    private Array<LineMeta> lineMetas;

    /** Last text processed. This attribute is used to avoid unnecessary computations while calculating offsets **/
    private String lastText;

    /** Current line for the cursor **/
    int cursorLine;

    /** Index of the first line showed by the text area **/
    int firstLineShowing;

    /** Number of lines showed by the text area **/
    private int linesShowing;

    /** Variable to maintain the x offset of the cursor when moving up and down. If it's set to -1, the offset is reset **/
    float moveOffset;

    private float prefRows;

    public CalTextArea(String text, Skin skin) {
        super(text, skin);
    }

    public CalTextArea(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public CalTextArea(String text, TextAreaStyle style) {
        super(text, style);
    }

    public CalTextArea(String text, TextAreaStyle style, AndroidTextInputInterface androidTextInputInterface) {
        super(text, style, androidTextInputInterface);
    }

    @Override
    protected void initialize () {
        super.initialize();
        writeEnters = true;
        //linesBreak = new IntArray();
        lineMetas = new Array<LineMeta>();
        cursorLine = 0;
        firstLineShowing = 0;
        moveOffset = -1;
        linesShowing = 0;
    }

    @Override
    protected int letterUnderCursor (float x) {
        if (lineMetas.size > 0) {
            if (cursorLine >= lineMetas.size) {
                return text.length();
            } else {
                float[] glyphPositions = this.glyphPositions.items;
                int start = lineMetas.get(cursorLine).startIndex;
                x += glyphPositions[start];
                int end = lineMetas.get(cursorLine).endIndex;
                int i = start;
                for (; i <= end; i++){
                    if (glyphPositions[i] > x) break;
                }
                return Math.max(0, i - 1);
            }
        } else {
            return 0;
        }
    }

    /** Sets the preferred number of rows (lines) for this text area. Used to calculate preferred height */
    public void setPrefRows (float prefRows) {
        this.prefRows = prefRows;
    }

    @Override
    public float getPrefHeight () {
        if (prefRows <= 0) {
            return super.getPrefHeight();
        } else {
            float prefHeight = textHeight * prefRows;
            if (style.background != null) {
                prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(),
                        style.background.getMinHeight());
            }
            return prefHeight;
        }
    }


    int changeCheck = -1;

    /** Returns total number of lines that the text occupies **/
    public int getLines () {
        int val = lineMetas.size + (newLineAtEnd() ? 1 : 0);
        if(changeCheck != val){
            changeCheck = val;
        }
        return lineMetas.size + (newLineAtEnd() ? 1 : 0);
    }

    /** Returns if there's a new line at then end of the text **/
    public boolean newLineAtEnd () {
        return text.length() != 0
                && (text.charAt(text.length() - 1) == ENTER_ANDROID || text.charAt(text.length() - 1) == ENTER_DESKTOP);
    }

    /** Moves the cursor to the given number line **/
    public void moveCursorLine (int line) {
        if (line < 0) {
            cursorLine = 0;
            cursor = 0;
            moveOffset = -1;
        } else if (line >= getLines()) {
            int newLine = getLines() - 1;
            cursor = text.length();
            if (line > getLines() || newLine == cursorLine) {
                moveOffset = -1;
            }
            cursorLine = newLine;
        } else if (line != cursorLine) {
            if (moveOffset < 0) {
                moveOffset = lineMetas.size <= cursorLine ? 0 : glyphPositions.get(cursor) - glyphPositions.get(lineMetas.get(cursorLine).startIndex);
            }
            cursorLine = line;
            cursor = cursorLine >= lineMetas.size ? text.length() : lineMetas.get(cursorLine).startIndex;
            while (cursor < text.length() && cursor <= lineMetas.get(cursorLine).endIndex - 1
                    && glyphPositions.get(cursor) - glyphPositions.get(lineMetas.get(cursorLine).startIndex) < moveOffset) {
                cursor++;
            }
            showCursor();
        }
    }

    /** Updates the current line, checking the cursor position in the text **/
    void updateCurrentLine () {
        //bug.out("CURSOR "+cursor);
        int index = calculateCurrentLineIndex(cursor);
        int line = index / 2;

        if (index % 2 == 0 || index + 1 >= lineMetas.size*2 || cursor != lineMetas.get(line).startIndex
                || lineMetas.get(line).endIndex != lineMetas.get(line).startIndex) {
            if (line < lineMetas.size || text.length() == 0 || text.charAt(text.length() - 1) == ENTER_ANDROID
                    || text.charAt(text.length() - 1) == ENTER_DESKTOP) {
                cursorLine = line;
            }
        }
    }

    /** Scroll the text area to show the line of the cursor **/
    void showCursor () {
        updateCurrentLine();
        if(!((TextAreaStyle) style).autoSizeWithLines) {
            if (cursorLine != firstLineShowing) {
                int step = cursorLine >= firstLineShowing ? 1 : -1;
                while (firstLineShowing > cursorLine || firstLineShowing + linesShowing - 1 < cursorLine) {
                    firstLineShowing += step;
                }
            }
        } else firstLineShowing = 0;
    }

    /** Calculates the text area line for the given cursor position **/
    private int calculateCurrentLineIndex (int cursor) {
        int index = 0;

        for(int i = 0; i < lineMetas.size; i++){
            if(cursor > lineMetas.get(i).startIndex) index++;
            if(cursor > lineMetas.get(i).endIndex) index++;
        }
        return index;
    }

    @Override
    protected void sizeChanged () {
        lastText = null; // Cause calculateOffsets to recalculate the line breaks.

        // The number of lines showed must be updated whenever the height is updated
        BitmapFont font = style.font;

        Drawable background = style.background;
        float availableHeight = getHeight() - (background == null ? 0 : background.getBottomHeight() + background.getTopHeight());
        linesShowing = (int)Math.ceil(availableHeight / font.getLineHeight());

    }

    @Override
    public float getTextY (BitmapFont font, Drawable background) {

        float textMid = textHeight / 2 + font.getDescent();

        float textY = getHeight() - textMid;

        if(background != null) textY -= background.getTopHeight();

        //float textY = getHeight();
        //if (background != null) {
        //    textY = (int)(textY - background.getTopHeight());
       // }
        return textY;
    }

    @Override
    protected void drawSelection (Drawable selection, Batch batch, BitmapFont font, float x, float y) {
        int i = firstLineShowing;
        float offsetY = 0;
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);
        while (i < lineMetas.size && i < (firstLineShowing + linesShowing)) {

            int lineStart = lineMetas.get(i).startIndex;
            int lineEnd = lineMetas.get(i).endIndex;

            if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
                    || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

                int start = Math.max(lineStart, minIndex);
                int end = Math.min(lineEnd, maxIndex);

                if(start < glyphPositions.size && end < glyphPositions.size){
                    float selectionX = glyphPositions.get(start) - glyphPositions.get(lineStart);
                    float selectionWidth = Math.max(glyphPositions.get(end) - glyphPositions.get(start), selection.getMinWidth());

                    selection.draw(batch, x + selectionX + fontOffset, y - textHeight - font.getDescent() - offsetY, selectionWidth,
                            font.getLineHeight());
                }
            }

            offsetY += font.getLineHeight();
            i++;
        }
    }

    @Override
    protected void drawText (Batch batch, BitmapFont font, float x, float y) {
        float offsetY = 0;

        for (int i = firstLineShowing; i < (firstLineShowing + linesShowing) && i < lineMetas.size; i++) {
            if(lineMetas.get(i).startIndex != lineMetas.get(i).endIndex)
            font.draw(batch, displayText, x, y + offsetY, lineMetas.get(i).startIndex, lineMetas.get(i).endIndex, 0, Align.left, false);
            offsetY -= font.getLineHeight();
        }
    }

    @Override
    protected void drawCursor (Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
        float textOffset = cursor >= glyphPositions.size || cursorLine >= lineMetas.size ? 0
                : glyphPositions.get(cursor) - glyphPositions.get(lineMetas.get(cursorLine).startIndex);

        float cx = x + textOffset + fontOffset + font.getData().cursorX-cursorPatch.getMinWidth()/2;
        float cy = y - font.getDescent()  - (cursorLine - firstLineShowing + 1) * font.getLineHeight();
        float w = cursorPatch.getMinWidth();
        float h = font.getLineHeight();

        cursorPatch.draw(batch, cx, cy, w, h);
    }

    @Override
    protected void calculateOffsets () {
        super.calculateOffsets();
        if (!this.text.equals(lastText)) {
            //bug.out("text not same!!!!");
            this.lastText = text;
            BitmapFont font = style.font;
            float maxWidthLine = this.getWidth()
                    - (style.background != null ? style.background.getLeftWidth() + style.background.getRightWidth() : 0);
            lineMetas.clear();
            int lineStart = 0;
            int lastSpace = 0;
            char lastCharacter;
            Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
            GlyphLayout layout = layoutPool.obtain();
            for (int i = 0; i < text.length(); i++) {
                lastCharacter = text.charAt(i);
                if (lastCharacter == ENTER_DESKTOP || lastCharacter == ENTER_ANDROID) {
                    lineMetas.add(new LineMeta(lineStart, i));
                    lineStart = i + 1;
                } else {
                    lastSpace = (continueCursor(i, 0) ? lastSpace : i);
                    layout.setText(font, text.subSequence(lineStart, i + 1));
                    if (layout.width > maxWidthLine) {
                        if (lineStart >= lastSpace) {
                            lastSpace = i - 1;
                        }
                        lineMetas.add(new LineMeta(lineStart, lastSpace+1));

                        lineStart = lastSpace + 1;
                        lastSpace = lineStart;
                    }
                }
            }
            layoutPool.free(layout);
            // Add last line
            if (lineStart < text.length()) {
                lineMetas.add(new LineMeta(lineStart, text.length()));
            }
            if(((TextAreaStyle) style).autoSizeWithLines){
                setPrefRows(getLines());
                invalidateHierarchy();
            }
            showCursor();
        }
    }

    @Override
    protected InputListener createInputListener () {
        return new TextAreaListener();
    }

    @Override
    public void setSelection (int selectionStart, int selectionEnd) {
        super.setSelection(selectionStart, selectionEnd);
        updateCurrentLine();
    }

    @Override
    protected void moveCursor (boolean forward, boolean jump) {
        int count = forward ? 1 : -1;
      //  int index = (cursorLine * 2) + count;
        int index = (cursorLine) + count;
       // if (index >= 0 && index + 1 < linesBreak.size && linesBreak.items[index] == cursor && linesBreak.items[index + 1] == cursor) {
        if (index >= 0 && index + 1 < lineMetas.size
                && lineMetas.get(index).startIndex == cursor && lineMetas.get(index).endIndex == cursor) {
            cursorLine += count;
            if (jump) {
                super.moveCursor(forward, jump);
            }
            showCursor();
        } else {
            super.moveCursor(forward, jump);
        }
        updateCurrentLine();

    }

    @Override
    public void textChanged(CharSequence text, int cursorPosition, int selectionEnd) {
        super.textChanged(text, cursorPosition, selectionEnd);
        /** calanti addition - limit number of lines */
        if(((TextAreaStyle) style).maxLines != 0) {
            calculateOffsets();
            if (lineMetas.size > ((TextAreaStyle) style).maxLines +  (newLineAtEnd() ? 0 : 1)) {
                StringBuilder sb = new StringBuilder(text);
                sb.deleteCharAt(cursorPosition - 1);
                //if(cursorPosition == text.length()) cursorPosition--;
                androidTextInputInterface.setText(sb.toString(), cursorPosition - 1);
            }
        }

    }

    @Override
    protected boolean continueCursor (int index, int offset) {
        int pos = calculateCurrentLineIndex(index + offset);

        int line = pos/2;

        //pos < 0 not possible. pos >= lineBreaks.size - 2 is last line
       // return super.continueCursor(index, offset) && (pos < 0 || pos >= linesBreak.size - 2 || (linesBreak.items[pos + 1] != index)
        // || (linesBreak.items[pos + 1] == linesBreak.items[pos + 2]));
        return super.continueCursor(index, offset) && (pos < 0 || pos >= lineMetas.size*2 - 2 || (lineMetas.get(line).endIndex != index)
                || (lineMetas.get(line).endIndex == lineMetas.get(line+1).startIndex));
    }

    public int getCursorLine () {
        return cursorLine;
    }

    public int getFirstLineShowing () {
        return firstLineShowing;
    }

    public int getLinesShowing () {
        return linesShowing;
    }

    public float getCursorX () {
        return textOffset + fontOffset + style.font.getData().cursorX;
    }

    public float getCursorY () {
        BitmapFont font = style.font;
        return -(-font.getDescent() / 2 - (cursorLine - firstLineShowing + 1) * font.getLineHeight());
    }

    /** Input listener for the text area **/
    public class TextAreaListener extends TextFieldClickListener {

        @Override
        protected void setCursorPosition (float x, float y) {
            moveOffset = -1;

            Drawable background = style.background;
            BitmapFont font = style.font;

            float height = getHeight();

            if (background != null) {
                height -= background.getTopHeight();
                x -= background.getLeftWidth();
            }
            x = Math.max(0, x);
            if (background != null) {
                y -= background.getTopHeight();
            }

            cursorLine = (int)Math.floor((height - y) / font.getLineHeight()) + firstLineShowing;
            cursorLine = Math.max(0, Math.min(cursorLine, getLines() - 1));

            super.setCursorPosition(x, y);
            updateCurrentLine();
        }

        @Override
        public boolean keyDown (InputEvent event, int keycode) {
            boolean result = super.keyDown(event, keycode);
            Stage stage = getStage();
            if (stage != null && stage.getKeyboardFocus() == CalTextArea.this) {
                boolean repeat = false;
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                if (keycode == Input.Keys.DOWN) {
                    if (shift) {
                        if (!hasSelection) {
                            selectionStart = cursor;
                            hasSelection = true;
                        }
                    } else {
                        clearSelection();
                    }
                    moveCursorLine(cursorLine + 1);
                    repeat = true;

                } else if (keycode == Input.Keys.UP) {
                    if (shift) {
                        if (!hasSelection) {
                            selectionStart = cursor;
                            hasSelection = true;
                        }
                    } else {
                        clearSelection();
                    }
                    moveCursorLine(cursorLine - 1);
                    repeat = true;

                } else {
                    moveOffset = -1;
                }
                if (repeat) {
                    scheduleKeyRepeatTask(keycode);
                }
              //  if(keycode == Input.Keys.ENTER){
             //       setText(getText()+"\n");
             //   }
                showCursor();
                return true;
            }
            return result;
        }

        @Override
        public boolean keyTyped (InputEvent event, char character) {
            if(character == ENTER_ANDROID || character == ENTER_DESKTOP){
                if(hasSelection) cursor = delete(false);
                text = insert(cursor++, character+"", text);
                updateDisplayText();
                return true;
            }
            boolean result = super.keyTyped(event, character);
            showCursor();
            return result;
        }

        @Override
        protected void goHome (boolean jump) {
            if (jump) {
                cursor = 0;
            } else if (cursorLine < lineMetas.size) {
                cursor = lineMetas.get(cursorLine).startIndex;
            }
        }

        @Override
        protected void goEnd (boolean jump) {
            if (jump || cursorLine >= getLines()) {
                cursor = text.length();
            } else if (cursorLine < lineMetas.size) {
                cursor = lineMetas.get(cursorLine).endIndex;
            }
        }
    }

    static public class TextAreaStyle extends CalTextField.TextFieldStyle{

        /** calanti addition */
        public int maxLines;
        public boolean autoSizeWithLines;
        public boolean androidKeyboardCarriageReturn;

        public TextAreaStyle(){}

        public TextAreaStyle(TextFieldStyle style){
            super(style);
        }

        public TextAreaStyle(TextAreaStyle style){
            super(style);
            this.maxLines = style.maxLines;
            this.autoSizeWithLines = style.autoSizeWithLines;
            this.androidKeyboardCarriageReturn = style.androidKeyboardCarriageReturn;
        }
    }


}