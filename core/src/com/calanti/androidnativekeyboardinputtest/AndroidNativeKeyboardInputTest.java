package com.calanti.androidnativekeyboardinputtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.AndroidTextInputInterface;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.VisibleView;
import com.calanti.androidnativekeyboardinputtest.interfaces.android.VisibleViewSizeChangeListener;
import com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3.CalTextArea;
import com.calanti.androidnativekeyboardinputtest.libgdxModified_1_9_3.CalTextField;

/** Test libgdx application to demonstrate a new method of Android soft keyboard input.
 *  There is no backend modification so it should be fairly easy to build a project based on this
 *  or modify an existing project to include these methods.
 *
 *  The new text input method for Android works by syncing a hidden native EditText to
 *  a (slightly modified) libgdx TextField / TextArea (see libgdxModified_1_9_3 > CalTextField / CalTextArea),
 *  therefore the actual keyboard inputs and methods are handled completely natively by the OS.
 *  This makes input much more reliable and enables many other native features such as swype, auto-correct,
 *  dictionary selection, even voice to text input if the keyboard allows (This was an awesome surprise to me!)
 *
 *  I have also added some keyboard input type customisations so please look at the relevant styles. It is pretty straight
 *  forward to add more, just add options in the styles and action them in the AndroidKeyboard class.
 *
 *  So what's going on?
 *  The Android EditText has a TextWatcher attached that simply sends the text and cursor position to the binded TextField when it
 *  detects a change. The TextField also controls certain properties in the EditText such as cursor position and selection. It
 *  is also possible for the TextField to modify the EditText content in events such as cut/delete.
 *
 *  On top of this there is another quite nice solution for detecting the presence of the Android soft keyboard.
 *  In fact it's even better, it can quite reliably detect the height of the keyboard by providing the dimensions of the
 *  applications visible view. This is quite necessary because Android does not provide any feedback when the user
 *  presses the back button to close the keyboard, so there is no event to capture to unfocus the TextField. By knowing the
 *  height of the keyboard, it's fairly safe to assume that if the height = 0, the keyboard has been closed and we can unfocus.
 *
 *  What makes this even more awesome? Well, we know the height of the keyboard, so with a little bit of logic we can also
 *  detect if the TextField is hidden by the keyboard and do something about it, such as moving the Actor upwards or
 *  the Stage camera down. I'll let you work this part out for yourself ;)
 *
 *  Many credits to "Willempie" on StackOverflow (http://stackoverflow.com/a/33188659/5862099) for providing the basis of
 *  the visible view stuff.
 *
 *  I have vigorously tested all of the above on my (very limited range of) Android phones and it all seems to work
 *  surprisingly reliably, however it probably will be riddled with problems so please do provide feedback/help on the
 *  relevant Github page (calanti) or badlogic forum topic (graham01).
 *
 *  Note min Android API is shifted up to 11 because of AndroidLauncher$View.OnLayoutChangeListener
 *
 *  Enjoy and use as you like!
 *
 * @author calanti (Graham Watson)
 * */

public class AndroidNativeKeyboardInputTest extends Game implements VisibleViewSizeChangeListener {

	VisibleView visibleView;
	AndroidTextInputInterface androidTextInputInterface;

	private Stage stage;
	private BitmapFont font;
	private Label keyboardLabel;

	private Texture white1x1, cursor;

	//scale of actual screen to game screen - for scaling of keyboard height
	private float hScale;

	public AndroidNativeKeyboardInputTest(ApplicationBundle applicationBundle) {
		this.visibleView = applicationBundle.getVisibleView();
		//make this the listener for visible screen size change
		if(this.visibleView != null) this.visibleView.setListener(this);
		this.androidTextInputInterface = applicationBundle.getAndroidTextInputInterface();
	}

	@Override
	public void create () {
		float screenRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
		float gameWidth = 1080;
		float gameHeight = gameWidth*screenRatio;
		hScale = Gdx.graphics.getHeight() / gameHeight;

		Viewport viewport = new FitViewport(gameWidth, gameHeight);
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(stage);

		//some assets to draw with
		white1x1 = new Texture("white-1px.png");
		cursor = new Texture("cursor.png");

		font = new BitmapFont();
		font.getData().setScale(2.8f);
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		NinePatch tfBackground = new NinePatch(white1x1, Color.LIGHT_GRAY);
		tfBackground.setPadding(30, 30, 30, 30);
		NinePatch selectionPatch = new NinePatch(white1x1, new Color(0.7f, 0f, 0f, 0.5f));
		NinePatch cursorPatch = new NinePatch(cursor, 4, 4, 6, 6);

		//TEXT FIELD STYLES
		CalTextField.TextFieldStyle textFieldStyle = new CalTextField.TextFieldStyle();
		textFieldStyle.font = font;
		textFieldStyle.fontColor = Color.BLACK;
		textFieldStyle.background = new NinePatchDrawable(tfBackground);
		textFieldStyle.cursor = new NinePatchDrawable(cursorPatch);
		textFieldStyle.selection = new NinePatchDrawable(selectionPatch);
		textFieldStyle.androidKeyboardNumericalOnly = false;
		textFieldStyle.androidKeyboardAutoCorrect = true;
		textFieldStyle.androidKeyboardTextSuggestions = true;

		CalTextField.TextFieldStyle numericalTextFieldStyle = new CalTextField.TextFieldStyle(textFieldStyle);
		numericalTextFieldStyle.androidKeyboardNumericalOnly = true;

		CalTextArea.TextAreaStyle calTextAreaCarriageReturnStyle = new CalTextArea.TextAreaStyle(textFieldStyle);
		calTextAreaCarriageReturnStyle.androidKeyboardCarriageReturn = true;
		calTextAreaCarriageReturnStyle.autoSizeWithLines = true;
		calTextAreaCarriageReturnStyle.maxLines = 5;

		CalTextArea.TextAreaStyle calTextAreaCompletionModeStyle = new CalTextArea.TextAreaStyle(textFieldStyle);
		calTextAreaCompletionModeStyle.androidKeyboardCarriageReturn = false;

		//TEXT FIELDS
		CalTextField calTextField = new CalTextField("Edit me!", textFieldStyle, androidTextInputInterface);
		CalTextField calNumericalTextField = new CalTextField("123", numericalTextFieldStyle, androidTextInputInterface);
		CalTextArea calTextAreaCarriageReturn = new CalTextArea("Edit me too!", calTextAreaCarriageReturnStyle, androidTextInputInterface);
		CalTextArea calTextAreaCompletionMode = new CalTextArea("Edit me as well!", calTextAreaCompletionModeStyle, androidTextInputInterface);
		calTextAreaCompletionMode.setPrefRows(4);

		//LABELS
		Label.LabelStyle genericLabelStyle = new Label.LabelStyle(font, Color.BLACK);
		Label.LabelStyle keyboardLabelStyle = new Label.LabelStyle(genericLabelStyle);
		keyboardLabelStyle.background = new NinePatchDrawable(selectionPatch);

		Label topLabel = new Label("Gdx TextField/TextArea supporting native-like Android soft keyboard input and keyboard height/status detection.", genericLabelStyle);
		topLabel.setWrap(true);
		Label calTextFieldLabel = new Label("Native like TextField", genericLabelStyle);
		Label calNumericalTextFieldLabel = new Label("Numerical Only", genericLabelStyle);
		Label textAreaCRLabel = new Label("Native like TextArea\n(carriage-return mode, auto-line increase, max lines 5)", genericLabelStyle);
		textAreaCRLabel.setAlignment(Align.center);
		textAreaCRLabel.setWrap(true);
		Label textAreaCMLabel = new Label("Native like TextArea (completion mode)", genericLabelStyle);
		Label creditsLabel = new Label("Use as you please! Brought to you by calanti (Graham Watson)", genericLabelStyle);
		creditsLabel.setWrap(true);

		//CONTENT TABLE
		Table table = new Table();
		table.top();
		table.setFillParent(true);
		table.defaults().pad(20).colspan(2).align(Align.center);

		table.add(topLabel).expandX().fillX().row();
		table.add(calTextFieldLabel).padBottom(0).colspan(1);
		table.add(calNumericalTextFieldLabel).padBottom(0).colspan(1).row();
		table.add(calTextField).expandX().fillX().colspan(1);
		table.add(calNumericalTextField).fillX().colspan(1).row();
		table.add(textAreaCRLabel).expandX().fillX().padBottom(0).row();
		table.add(calTextAreaCarriageReturn).width(900).row();
		table.add(textAreaCMLabel).padBottom(0).row();
		table.add(calTextAreaCompletionMode).width(900).row();
		table.add(creditsLabel).expandX().fillX().row();

		stage.addActor(table);

		//floating keyboard label - stays above keyboard
		keyboardLabel = new Label("", keyboardLabelStyle);
		keyboardLabel.setWidth(1080);
		keyboardLabel.setHeight(100);
		keyboardLabel.setColor(1, 1, 1, 0.5f);

		stage.addActor(keyboardLabel);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
	}
	
	@Override
	public void dispose () {
		font.dispose();
		stage.dispose();
		white1x1.dispose();
		cursor.dispose();
	}

	@Override
	public void onSizeChange(float width, float height) {
		float screenKeyboardHeight = Gdx.graphics.getHeight() - height;
		int gameKeyboardHeight = (int) (screenKeyboardHeight / hScale);
		boolean keyboardOpen = screenKeyboardHeight > 2;

		String keyboardText = "Keyboard height: "+gameKeyboardHeight+", is open: "+(keyboardOpen ? "yes" : "no, removing stage focus");

		if(keyboardLabel != null) {
			keyboardLabel.setText(keyboardText);
			keyboardLabel.setPosition(0, gameKeyboardHeight);
		}
		
		if(!keyboardOpen){
			androidTextInputInterface.keyboardHideDetected();
		        if(stage != null) stage.unfocusAll();
		}
	}

	@Override
	public void pause() {
		super.pause();
		//drop keyboard and focus on pause (otherwise it stays open is Task Manager / Home button pressed)
		androidTextInputInterface.forceHideKeyboard();
		if(stage != null) stage.unfocusAll();
	}
}
