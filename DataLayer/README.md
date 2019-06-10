
Android DataLayer Sample
========================

This sample demonstrates how to work with a WearableListenerService,
to produce and consume DataEvents and effectively work with the DataLayer.

Introduction
------------

This sample demonstrates how to make a handheld and an Wear device communicate
using the [DataApi][2].
It does this by sending a picture between connected devices.

An Activity is being used for both the connected devices which implement their parts of
the required interfaces.

It showcases how to use an [WearableListenerService][1] to consume DataEvents
as well as implementations for various required listeners when using the [DataApi][2],
[MessageApi][3] and [NodeApi][4].

[1]: https://developer.android.com/reference/com/google/android/gms/wearable/WearableListenerService.html
[2]: https://developer.android.com/reference/com/google/android/gms/wearable/DataApi.html
[3]: https://developer.android.com/reference/com/google/android/gms/wearable/MessageApi.html
[4]: https://developer.android.com/reference/com/google/android/gms/wearable/NodeApi.html

Pre-requisites
--------------

- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository

Screenshots
-------------

<img src="screenshots/phone_image.png" height="400" alt="Screenshot"/> <img src="screenshots/wearable_background_image.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Google+ Community: https://plus.google.com/communities/105153134372062985968
- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-DataLayer

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
