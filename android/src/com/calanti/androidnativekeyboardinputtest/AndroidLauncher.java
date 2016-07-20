package com.calanti.androidnativekeyboardinputtest;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

	/** Used for visible view size change detection */
	private View rootView;
	private AndroidVisibleView androidVisibleView;
	private int visibleWidth, visibleHeight;

	private AndroidKeyboard androidKeyboard;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//create the layout to contain the gdx game and EditText
		RelativeLayout mainLayout = new RelativeLayout(this);

		//do the stuff that initialize() would do for you
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		//initialise Android Keyboard / EditText methods
		androidKeyboard = new AndroidKeyboard(this);

		//set up the visible view
		rootView = this.getWindow().getDecorView().getRootView();
		Rect rect = new Rect();
		rootView.getWindowVisibleDisplayFrame(rect);
		visibleWidth = rect.width();
		visibleHeight = rect.height();
		androidVisibleView = new AndroidVisibleView(visibleWidth, visibleHeight);

		//this shifts min api to 11
		rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				Rect rect = new Rect();
				rootView.getWindowVisibleDisplayFrame(rect);

				if (!(visibleWidth == rect.width() && visibleHeight == rect.height())) {
					visibleWidth = rect.width();
					visibleHeight = rect.height();
					androidVisibleView.onSizeChange(visibleWidth, visibleHeight);
				}
			}
		});

		//init application bridge
		ApplicationBundle applicationBundle = new ApplicationBundle(androidVisibleView, androidKeyboard);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		//get the main gdx view
		View gdxView = initializeForView(new AndroidNativeKeyboardInputTest(applicationBundle), config);

		//combine
		mainLayout.addView(gdxView);
		mainLayout.addView(androidKeyboard.getForView());

		setContentView(mainLayout);
	}

}
