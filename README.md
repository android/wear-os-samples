Wear OS Samples Repository
======================

This repository contains a set of individual Android Studio projects to help you get started writing Wear OS apps and watch faces.

Read below for a description of each sample.


Samples
----------

* **[AlwaysOnKotlin](AlwaysOnKotlin)** (Compose/Kotlin) - Demonstrates keeping the app visible in ambient mode. Note: While this is valuable for some specific use cases, most use cases won't need this, because when the watch goes into ambient mode (and shows watch face), if the user interacts with the watch again within several minutes, it will bring the app back up when it transitions out of ambient mode. [Guide](https://developer.android.com/training/wearables/apps/always-on)

* **[ComposeStarter](ComposeStarter)** (Compose/Kotlin) - Demonstrates simple Compose for Wear OS app devs can use as a starting point for their own Compose app.

* **[ComposeAdvanced](ComposeAdvanced)** (Compose/Kotlin) - Demonstrates an advanced Compose for Wear OS app devs can use to learn about the Wear Scaffold, Navigation, ScalingLazyColumn, CurvedText, and more.

* **[DataLayer](DataLayer)** (Compose/Kotlin) - Demonstrates communicating via the data layer between the watch and the phone. [Guide](https://developer.android.com/training/wearables/data-layer)

* **[RuntimePermissionsWear](RuntimePermissionsWear)** (Kotlin) - Demonstrates requesting permissions not only on a Wear OS device and a mobile device, but also across devices, e.g., request permissions on a mobile device from your wear device and the other way around. Note: It does require you having both the Wear and Mobile version of the app installed on both devices. [Guide](https://developer.android.com/training/articles/wear-permissions)

* **[WatchFace](WatchFace)** (Java) - Shows a full implementation of multiple watch faces, complications, and settings (edit watch faces appearance). Also includes implementation of data providers, so developers can share their data to be displayed in complications on any watch face (without writing a watch face). [Watch face guide](https://developer.android.com/training/wearables/watch-faces#guides), [Data Provider Guide](https://developer.android.com/training/wearables/data-providers#guides)

* **[WatchFaceAlphaKotlin](WatchFaceAlphaKotlin)** (Kotlin) - Demos the new alpha AndroidX Watch Face APIs. The analog watch face in the sample is powered by a full Room database and uses the new settings APIs (among many other new APIs). Keep in mind that the APIs can change as the alpha proceeds. This version uses alpha4 and will have some additional updates for the next versions.

* **[WearAccessibilityApp](WearAccessibilityApp)** (Java) - Sample demonstrates how to include accessibility support for your wearable app. [Guide](https://developer.android.com/guide/topics/ui/accessibility)

* **[WearComplicationDataSourcesTestSuite](WearComplicationDataSourcesTestSuite)** (Kotlin) - If you are writing a watch face with complications, this app gives you a full suite of data sources to test against your implementation of complications to make sure it looks good. [Complication Guide](https://developer.android.com/training/wearables/watch-faces/adding-complications)

* **[WearOAuth](WearOAuth)** (Kotlin) - Demonstrates how developers can authenticate a user on their Wear OS app via the user's mobile/phone device without requiring a mobile app (Wear OS companion app handles the request on the mobile side). The sample uses OAuth. [Guide](https://developer.android.com/training/wearables/apps/auth-wear)

* **[WearSpeakerSample](WearSpeakerSample)** (Kotlin) - Demonstrates audio recording and playback if the wearable device has a speaker. [Guide](https://developer.android.com/training/wearables/wearable-sounds)

* **[WearStandaloneGoogleSignIn](WearStandaloneGoogleSignIn)** (Java) - Demonstrates using Google sign-in to authenticate user. [Guide](https://developer.android.com/training/wearables/apps/auth-wear)

* **[WearTilesKotlin](WearTilesKotlin)** (Kotlin) - Demonstrates tiles using the new AndroidX alpha library. [Guide](https://developer.android.com/training/articles/wear-tiles)

* **[WearVerifyRemoteApp](WearVerifyRemoteApp)** (Kotlin) - Verify and open your app on another device, and if it isn't installed, open the store listing to allow the user to install it (wear or mobile). [Guide](https://developer.android.com/training/wearables/data-layer/messages#SendMessage)
