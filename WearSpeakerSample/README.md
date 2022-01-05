
WearSpeakerSample
================================

A sample that shows how you can record voice using the microphone on a wearable and
play the recorded voice or an mp3 file, if the wearable device is connected to a speaker
(bluetooth or built-in).

This sample is also written entirely with Jetpack Compose, using [Wear-specific components provided
by AndroidX](https://developer.android.com/jetpack/androidx/releases/wear-compose).
The Compose UI is fairly advanced, using [ConstraintLayout for Compose](https://developer.android.com/jetpack/compose/layouts/constraintlayout) and animations.
The sample also handles permissions and uses the [effect APIs](https://developer.android.com/jetpack/compose/side-effects) where necessary.
If you are looking for simpler examples of Compose on [WearOS], refer to [ComposeStarter](../ComposeStarter) and [ComposeAdvanced](../ComposeAdvanced).

This sample doesn't have any companion phone app so you need to install this directly
on your watch using `adb` or Android Studio.

Pre-requisites
--------------

- Android SDK 30

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/android/wear-os-samples/issues

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
