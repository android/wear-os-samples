Android Wear Google Sign-In Sample
===================================

Introduction
------------

Sample demonstrating Google Sign-In implementation on Wear 2.0, as well as
a Google Sign-In button matching Wear material design.

The button included in this sample should be used as throwaway code for all
Wear integrations until the final version lands in Play Services, and
maintained there going forward.

Pre-requisites
--------------

Please have Android Studio installed and configured. If you're not using
Android Studio, you can still follow these instructions but will need to
adjust them according to your environment.

Support
-------

You can find support by posting up on the Android Wear Developers Community
page at the link below:

If you've found an error in the sample, please file an issue report at:
https://github.com/android/wear-os/issues

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.

Setup steps:

1) Download and install Android Studio if you haven't already.

2) Note that Android Studio should already have the Wear-related
support libraries and Play Services packages available in the SDK manager
and should be able to resolve them by doing the following:

a) Right-click on the app module and select Open Module Settings...
b) Click on the Dependencies tab
c) Resolve the libraries referenced as compile targets in the
app/build.gradle file and add them as dependencies to the module

In case you're unable to find the relevant Wear support libraries from
the Android Studio SDK manager, you can also download and setup the libraries
from the Wear 2.0 Developer Preview 4 developer docs:

https://developer.android.com/wear/preview/start.html

3) Follow the setup instructions for integrating Google Sign-in into an
Android app at the link below. Remember to use the package name
com.example.android.wearable.wear.wearstandalonegooglesignin when configuring your
project and credentials, or otherwise modify the sample appropriately if you want
to use a different package name.

https://developers.google.com/identity/sign-in/android/start-integrating

4) Update the strings.xml file for the string value 'server_client_id' to the
value for the OAuth web client id created in step 3).

Special Notes
---------------

1) For Wear integrations using Google Sign-In, please use the Wear-styled
Google Sign-In button contained in this sample as throwaway code. The final
version of this button will be released in a future Play Service version, following the
Wear 2.0 final release
