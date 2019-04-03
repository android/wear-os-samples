
Android AlwaysOn Sample
===================================

A basic sample showing how to support ambient mode for native Wear apps.

Introduction
------------

[WearableActivity][1] offers methods for supporting your native app staying on the screen when the Wear device enters ambient mode.

This example extends [WearableActivity][1] and overrides onEnterAmbient, onUpdateAmbient, and onExitAmbient to allow the simple native Wear app to support ambient mode.

In ambient mode, this app follows best practices by keeping most pixels black, avoiding large blocks of white pixels, using only black and white, and disabling anti-aliasing (following the [design guidelines for Watch Faces][2]).

In addition and most importantly, the app sleeps while in ambient mode for 20 seconds between any updates to conserving battery life (processor allowed to sleep). If you can hold off on updates for a full minute, you can throw away the Handler and just use onUpdateAmbient to save even more battery life.

As always, you will still want to apply the [performance guidelines][3] outlined in the Watch Faces documention to your app.

[1]: http://developer.android.com/reference/android/support/wearable/activity/WearableActivity.html
[2]: https://developer.android.com/training/wearables/watch-faces/designing.html#DesignGuidelines
[3]: https://developer.android.com/training/wearables/watch-faces/performance.html

Pre-requisites
--------------

- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository

Screenshots
-------------

<img src="screenshots/1-main-active.png" height="400" alt="Screenshot"/> <img src="screenshots/2-main-ambient.png" height="400" alt="Screenshot"/> <img src="screenshots/3-main-active-round.png" height="400" alt="Screenshot"/> <img src="screenshots/4-main-ambient-round.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Google+ Community: https://plus.google.com/communities/105153134372062985968
- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-AlwaysOn

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.

License
-------

Copyright 2019 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
