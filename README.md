Wear OS Samples Repository
======================

This repository contains a set of individual Android Studio projects to help you get started writing Wear OS apps and watch faces.

Read below for a description of each sample.


Samples
----------

* **[AlwaysOnKotlin](AlwaysOnKotlin)** (Compose/Kotlin) - Demonstrates keeping the app visible in ambient mode. Note: While this is valuable for some specific use cases, most use cases won't need this, because when the watch goes into ambient mode (and shows watch face), if the user interacts with the watch again within several minutes, it will bring the app back up when it transitions out of ambient mode. [Guide](https://developer.android.com/training/wearables/apps/always-on)

* **[ComposeStarter](ComposeStarter)** (Compose/Kotlin) - Demonstrates simple Compose for Wear OS app devs can use as a starting point for their own Compose app.

* **[DataLayer](DataLayer)** (Compose/Kotlin) - Demonstrates communicating via the data layer between the watch and the phone. [Guide](https://developer.android.com/training/wearables/data-layer)

* **[WatchFaceFormat](WatchFaceFormat)** - Demos the new Watch Face Format which allows quick development of performant watch
    faces in XML [Guide](https://developer.android.com/training/wearables/wff)

* **[WatchFaceKotlin](WatchFaceKotlin)** (Kotlin) - Demos the AndroidX Watch Face APIs which provide their own storage mechanism for watch face preference values.

* **[WearComplicationDataSourcesTestSuite](WearComplicationDataSourcesTestSuite)** (Kotlin) - If you are writing a watch face with complications, this app gives you a full suite of data sources to test against your implementation of complications to make sure it looks good. [Complication Guide](https://developer.android.com/training/wearables/watch-faces/adding-complications)

* **[WearOAuth](WearOAuth)** (Kotlin) - Demonstrates how developers can authenticate a user on their Wear OS app via the user's mobile/phone device without requiring a mobile app (Wear OS companion app handles the request on the mobile side). The sample uses OAuth. [Guide](https://developer.android.com/training/wearables/apps/auth-wear)

* **[WearSpeakerSample](WearSpeakerSample)** (Compose/Kotlin) - Demonstrates audio recording and playback if the wearable device has a speaker. This is also an advanced Compose sample, handling permissions, use of [effects](https://developer.android.com/jetpack/compose/side-effects), animations and [ConstraintLayout for Compose](https://developer.android.com/jetpack/compose/layouts/constraintlayout). [Guide](https://developer.android.com/training/wearables/wearable-sounds)

* **[WearTilesKotlin](WearTilesKotlin)** (Kotlin) - Demonstrates tiles using the new AndroidX library. [Guide](https://developer.android.com/training/articles/wear-tiles)
