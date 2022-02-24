
Android AlwaysOn Sample (Kotlin)
=======================

A basic sample showing how to support ambient mode for native Wear apps.

Introduction
------------

The [AmbientModeSupport][1] class offers methods for supporting your native app staying on the screen when the Wear device enters ambient mode.

This example implements the [AmbientCallbackProvider][2] interface and provides behavior for onEnterAmbient, onUpdateAmbient, and onExitAmbient to allow the simple native Wear app to support ambient mode.
In ambient mode, this app follows best practices by keeping most pixels black, avoiding large blocks of white pixels, using only black and white, and disabling anti-aliasing (following the [design guidelines for Watch Faces][3]).

In addition and most importantly, the app sleeps while in ambient mode for 10 seconds between any updates to conserve battery life (processor allowed to sleep). If you can hold off on updates for a full minute, you can avoid `AlarmManager` and just use onUpdateAmbient to save even more battery life.

As always, you will still want to apply the [performance guidelines][4] outlined in the Watch Face documentation to your app.

[1]: https://developer.android.com/reference/androidx/wear/ambient/AmbientModeSupport
[2]: https://developer.android.com/reference/androidx/wear/ambient/AmbientModeSupport.AmbientCallbackProvider
[3]: https://developer.android.com/training/wearables/watch-faces/designing.html#DesignGuidelines
[4]: https://developer.android.com/training/wearables/watch-faces/performance.html

IMPORTANT NOTE
--------------
Most apps shouldn't use the always on/ambient mode APIs, as it drains battery life and the system already handles this for you.

That is, by default (without this API), the system will go into system ambient mode if the user hasn't interacted with the watch in a period of time, and if the user interacts again with the watch soon after that, the system will automatically bring up your app again in the same state it was before that point which covers most use cases.

Pre-requisites
--------------

- Android SDK 30
- Requires AC to be configured off `adb emu power ac off`

Screenshots
-------------

<img src="screenshots/1-main-active.png" height="400" alt="Screenshot"/> <img src="screenshots/2-main-ambient.png" height="400" alt="Screenshot"/> <img src="screenshots/3-main-active-round.png" height="400" alt="Screenshot"/> <img src="screenshots/4-main-ambient-round.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: https://stackoverflow.com/questions/tagged/wear-os

If you've found an error in this sample, please file an issue:
https://github.com/android/wear-os-samples

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
