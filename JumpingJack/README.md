
Android JumpingJack Sample
===================================

A basic sample showing how to use the Gravity sensor on the wearable device
by counting how many jumping jacks you have performed.

Introduction
------------

[SensorEventListener][1] offers you methods used for receiving notifications from the
[SensorManager][2] when sensor values have changed.

This example counts how many times Jumping Jacks are performed by detecting the value
of the Gravity sensor by the following code:

```java
@Override
public void onSensorChanged(SensorEvent event) {
    detectJump(event.values[0], event.timestamp);
}

private void detectJump(float xValue, long timestamp) {
    if ((Math.abs(xValue) > GRAVITY_THRESHOLD)) {
        if(timestamp - mLastTime < TIME_THRESHOLD_NS && mUp != (xValue > 0)) {
            onJumpDetected(!mUp);
        }
        mUp = xValue > 0;
        mLastTime = timestamp;
    }
}
```

The detectJump method above assumes that when a person is wearing the watch, the x-component of gravity
as measured by the Gravity Sensor is +9.8 when the hand is downward and -9.8 when the hand
is upward (signs are reversed if the watch is worn on the right hand). Since the upward or
downward may not be completely accurate, we leave some room and instead of 9.8, we use
GRAVITY_THRESHOLD (7.0f). We also consider the up <-> down movement successful if it takes less than
TIME_THRESHOLD_NS (2000000000 nanoseconds).

[1]: http://developer.android.com/reference/android/hardware/SensorEventListener.html
[2]: http://developer.android.com/reference/android/hardware/SensorManager.html

Pre-requisites
--------------

- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository

Screenshots
-------------

<img src="screenshots/jumping_jack.gif" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Google+ Community: https://plus.google.com/communities/105153134372062985968
- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-JumpingJack

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
