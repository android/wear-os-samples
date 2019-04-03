
Android RuntimePermissionsWear Sample
===================================

A sample that shows how you can handle remote data that requires permissions both on
a wearable device and a mobile device.

Introduction
------------

Steps for trying out this sample:
* Compile and install the mobile app onto your mobile device or emulator.
* Compile and install the wearable app onto your Wear device or emulator.
(**Note:** wearable apps are not automatically pushed from your mobile device
unless you build a production release, see [here][3] for more info).
* Start the mobile or wear app. Each app contains two buttons: one for showing
local data and another for showing remote data.
* Click either button to view the data. Both local and remote data require
[dangerous permissions][4] to be approved before displaying the data for
devices running 23 or above. You will be asked to approve the access if you
do not have the proper permissions.
* The happy icon signifies you have access to the data while the sad icon
signifies you do or may not have access (and may be asked to approve access).

This sample demonstrates how to access data and trigger permission approval
on remote devices. It uses [Services][5] and the [Wearable MessageApi][2] to
communicate between devices.

To find out more about wear, visit our [developer Wear page][1].

[1]: http://developer.android.com/wear/
[2]: https://developer.android.com/reference/com/google/android/gms/wearable/MessageApi.html
[3]: https://developer.android.com/training/wearables/apps/creating.html#Install
[4]: http://developer.android.com/guide/topics/security/permissions.html#normal-dangerous
[5]: http://developer.android.com/guide/components/services.html

Pre-requisites
--------------

- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository

Screenshots
-------------

<img src="screenshots/screenshot-wear.png" height="400" alt="Screenshot"/> <img src="screenshots/screenshot-phone.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Google+ Community: https://plus.google.com/communities/105153134372062985968
- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-RuntimePermissionsWear

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
