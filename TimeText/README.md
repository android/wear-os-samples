
TimeText Sample
================================

A sample that includes a pre-built [`TimeText`](timetext/src/main/java/com/example/android/wearable/timetext/TimeText.kt) `View`.

The component is included in the [timetext](timetext) library module, which includes the resources, layouts and tests for the component.

The `TimeText` `View` primarily displays the current time, [which you should show on any long-lived overlay screen to give users quick access to the time](https://developer.android.com/training/wearables/design/overlays#time).
On round screens, this time is displayed in a `CurvedTextView`, while on square screens, the time is displayed in a normal `TextView`.

Additionally, the `TimeText` supports adding a title, which is displayed alongside the time.
This title can be set via `TimeText.title`, or in XML via `app:titleText`.
The title might be ellipsized if it is too long: On a square screen, the entire `TimeText` is limited to a single line, and on a round screen, the entire `TimeText` is limited to an angle of 90 degrees.
The color of this title can be controlled via `TimeText.titleTextColor`, or in XML via `android:titleTextColor`.

To use, include the `TimeText` as an overlaying view that fills up the entire screen:

```xml
<com.example.android.wearable.timetext.TimeText
    android:id="@+id/timeText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:titleTextColor="#82b1ff"
    android:titleText="ABC" />
```

A sample is included in the [wear](wear) module which allows editing the title via an `EditText`.

Pre-requisites
--------------

- Android SDK 30

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
