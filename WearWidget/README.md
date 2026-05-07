# Wear Widget Sample

Demonstrates building Wear Widgets using Remote Compose for Wear OS.

## Introduction

Wear Widgets are partial-height, vertically scrolling surfaces that complement
apps and watch faces, providing effortless access to information and key
actions. They leverage
[Remote Compose](https://developer.android.com/jetpack/androidx/releases/compose-remote),
featuring a declarative DSL similar to Jetpack Compose.

This sample includes:

- **HelloWidget**: A simple "Hello World" widget.
- **WeatherWidget**: A widget that displays dynamic content based on stored
  state.

## Getting Started

This sample uses the Gradle build system. To build this project, use the
`./gradlew build` command or import it into Android Studio.

### Build and Install

```bash
./gradlew :app:installDebug
```

### Add and Preview your Widget

To test how your widget translates to a full-screen Tile on older Wear OS
versions, you can inject it directly into the system carousel using ADB.

**1. Add the tile:**

```bash
adb shell am broadcast \
  -a com.google.android.wearable.app.DEBUG_SURFACE \
  --es operation add-tile \
  --ecn component com.google.example.wear_widget/.HelloWidgetService
```

**2. Show the tile:**

```bash
adb shell am broadcast \
  -a com.google.android.wearable.app.DEBUG_SYSUI \
  --es operation show-tile \
  --ei index 0
```

## Using the Hello Widget

The `HelloWidget` is a simple, static widget designed to show the minimal
implementation required for a Wear Widget.

To preview it on a device or emulator, follow the steps in the **Add and Preview
your Widget** section above.

## Updating the Weather Widget

The `WeatherWidget` demonstrates how to handle dynamic content updates. You can
trigger updates in two ways:

### Via the In-App Control Panel

The sample includes a `WeatherActivity` that acts as a control panel.

1. Launch the **Weather Update** app from the app launcher on your watch.
2. Tap any of the weather condition buttons (Sunny, Cloudy, etc.) to push that
   state to the widget.

### Via ADB Broadcast

You can simulate external data updates (like a background sync) by sending a
broadcast intent via ADB.

```bash
adb shell am broadcast \
  -a com.google.example.wear_widget.UPDATE_WEATHER \
  -n com.google.example.wear_widget/.WeatherUpdateReceiver \
  --ei temp 75 \
  --es condition "'🌧️'"
```

_Note: Depending on your terminal and OS, you may need to escape the emoji or
use its unicode representation._

## Support

- Stack Overflow: <https://stackoverflow.com/questions/tagged/wear-os>

If you've found an error in this sample, please file an issue:
<https://github.com/android/wear-os-samples>

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more
details.
