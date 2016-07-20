/** Test libgdx application to demonstrate a new method of Android soft keyboard input.
There is no backend modification so it should be fairly easy to build a project based on this
or modify an existing project to include these methods.
LibGDX base project supporting native-like Android Soft Keyboard input

The new text input method for Android works by syncing a hidden native EditText to
a (slightly modified) libgdx TextField / TextArea (see libgdxModified_1_9_3 > CalTextField / CalTextArea),
therefore the actual keyboard inputs and methods are handled completely natively by the OS.
This makes input much more reliable and enables many other native features such as swype, auto-correct,
dictionary selection, even voice to text input if the keyboard allows (This was an awesome surprise to me!)
 
I have also added some keyboard input type customisations so please look at the relevant styles. It is pretty straight
forward to add more, just add options in the styles and action them in the AndroidKeyboard class.
 
So what's going on?
The Android EditText has a TextWatcher attached that simply sends the text and cursor position to the binded TextField when it
detects a change. The TextField also controls certain properties in the EditText such as cursor position and selection. It
is also possible for the TextField to modify the EditText content in events such as cut/delete.
 
On top of this there is another quite nice solution for detecting the presence of the Android soft keyboard.
In fact it's even better, it can quite reliably detect the height of the keyboard by providing the dimensions of the
applications visible view. This is quite necessary because Android does not provide any feedback when the user
presses the back button to close the keyboard, so there is no event to capture to unfocus the TextField. By knowing the
height of the keyboard, it's fairly safe to assume that if the height = 0, the keyboard has been closed and we can unfocus.
 
What makes this even more awesome? Well, we know the height of the keyboard, so with a little bit of logic we can also
detect if the TextField is hidden by the keyboard and do something about it, such as moving the Actor upwards or
the Stage camera down. I'll let you work this part out for yourself ;)
 
Many credits to "Willempie" on StackOverflow (http://stackoverflow.com/a/33188659/5862099) for providing the basis of
the visible view stuff.
 
I have vigorously tested all of the above on my (very limited range of) Android phones and it all seems to work
surprisingly reliably, however it probably will be riddled with problems so please do provide feedback/help here
or badlogic forum topic (graham01)
 
Note min Android API is shifted up to 11 because of AndroidLauncher$View.OnLayoutChangeListener
 
Enjoy and use as you like!
 
Author: calanti (Graham Watson)
