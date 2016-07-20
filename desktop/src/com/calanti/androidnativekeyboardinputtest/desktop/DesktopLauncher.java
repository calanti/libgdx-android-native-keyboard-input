package com.calanti.androidnativekeyboardinputtest.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.calanti.androidnativekeyboardinputtest.AndroidNativeKeyboardInputTest;
import com.calanti.androidnativekeyboardinputtest.ApplicationBundle;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1080/2;
		config.height = 1920/2;
		new LwjglApplication(new AndroidNativeKeyboardInputTest(new ApplicationBundle(null, null)), config);
	}
}
