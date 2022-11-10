
Android WearComplicationProvidersTestSuite Sample
=================================================

Complication Test Suite is a set of complication data sources that provide dummy data and it can be
used to test how different types of complications render on a watch face.

Introduction
------------

Steps for trying out the sample:
* Compile and install the wearable app onto your Wear device or emulator (for Wear scenario).

* This sample does not have a main Activity (just Services that provide the complication data).
Therefore, you may see an error next to the 'Run' button. To fix, click on the
"Wearable" dropdown next to the 'Run' button and select 'Edit Configurations'. Under the
'Launch Options', change the 'Launch' field from 'Default APK' to 'Nothing' and save.

This sample provides dummy data for testing the complications UI in your watch face. After
selecting a type from your watch face configuration Activity, you can tap on the complications to
see more options.

The Wear app demonstrates the use of [ComplicationData][1], [ComplicationDataSourceService][2], and [ComplicationText][3].

[1]: https://developer.android.com/reference/kotlin/androidx/wear/complications/data/ComplicationData
[2]: https://developer.android.com/reference/kotlin/androidx/wear/complications/datasource/ComplicationDataSourceService
[3]: https://developer.android.com/reference/kotlin/androidx/wear/complications/data/ComplicationText

Pre-requisites
--------------

- Android SDK 30

Screenshots
-------------

<img src="screenshots/wear-1.png" height="400" alt="Screenshot"/> <img src="screenshots/wear-2.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: https://stackoverflow.com/questions/tagged/wear-os

If you've found an error in this sample, please file an issue:
https://github.com/android/wear-os-samples/issues

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
