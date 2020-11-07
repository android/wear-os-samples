Wear OS Samples Repository
======================

This repository contains a set of individual Android Studio projects to help you get started writing Wear OS apps and watch faces.

Read below for a description of each sample.


Samples
----------

* **[AlwaysOn](https://github.com/android/wear-os-samples/tree/main/AlwaysOn)**(Java) - Demonstrates keeping the app visible in ambient mode. Note: While this is valuable for some specific use cases, most use cases won't need this, because when the watch goes into ambient mode (and shows watch face), if the user interactes with the watch again within several minutes, it will bring the app back up when it transitions out of ambient mode.

* **[DataLayer](https://github.com/android/wear-os-samples/tree/main/DataLayer)**(Java) - Demostrates communicating via the data layer between the watch and the phone.

* **[RuntimePermissionsWear](https://github.com/android/wear-os-samples/tree/main/RuntimePermissionsWear)**(Java) - Demostrates requesting permissions not only on a Wear OS device and a mobile device, but also across devices, e.g., request permissions on a mobile device from your wear device and the other way around. Note: It does require you having both the Wear and Mobile version of the app installed on both devices.

* **[WatchFace](https://github.com/android/wear-os-samples/tree/main/WatchFace)**(Java) - Shows a full implementation of multiple watch faces, complications, and settings (edit watch faces appearance). Also includes implementation of data providers, so developers can share their data to be displayed in complications on any watch face (without writing a watch face). Data providers are quite easy to implement and allow developers to own part of the screen real estate of a watch face.

* **[WearAccessibilityApp](https://github.com/android/wear-os-samples/tree/main/WearAccessibilityApp)**(Java) - Sample demonstrates how to include accessibility support for your wearable app.

* **[WearComplicationProvidersTestSuite](https://github.com/android/wear-os-samples/tree/main/WearComplicationProvidersTestSuite)**(Java) - If you are writing a watch face with complications, this app gives you a full suite of data providers to test against your implimentation of complications to make sure it looks good.

* **[WearDrawers](https://github.com/android/wear-os-samples/tree/main/WearDrawers)**(Java) - Demonstrates Navigation (Top) and Action (Bottom) Drawers, part of Material Design for Wear.

* **[WearOAuth](https://github.com/android/wear-os-samples/tree/main/WearOAuth)**(Java) - Demonstrates how developers can authenticate a user on their Wear OS app via the user's mobile/phone device without requiring a mobile app (Wear OS companion app handles the request on the mobile side). The sample uses OAuth.

* **[WearSpeakerSample](https://github.com/android/wear-os-samples/tree/main/WearSpeakerSample)**(Java) - Demonstrates audio recording and playback if the wearable device has a speaker.

* **[WearStandaloneGoogleSignIn](https://github.com/android/wear-os-samples/tree/main/WearStandaloneGoogleSignIn)**(Java) - Demonstrates using Google sign-in to authenticate user. 

* **[WearVerifyRemoteApp](https://github.com/android/wear-os-samples/tree/main/WearVerifyRemoteApp)**(Java) - Verify and open your app on another device, and if it isn't installed, open the store listing to allow the user to install it (wear or mobile).
