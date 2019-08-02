Android Wear OAuth Sample
===================================

Introduction
------------

Sample demonstrating OAuth authentication on Wear 2.0.

The sample demonstrates the flow between a Wear app and the Android Wear
companion app for handling OAuth authentication.

The flow starts by tapping the Google+ authentication button on the app,
and the OAuth consent web view on the Android Wear companion app. Once
the user accepts and provides consent, flow is returned back to the
watch. The watch makes a direct HTTP call to exchange the
authorization code for OAuth tokens, and then makes a follow-up
authenticated call to the Google OAuth 2.0 API.

Pre-requisites
--------------

Please have Android Studio installed and configured.

Support
-------

You can find support by posting up on the Android Wear Developers Community
page at the link below:
https://plus.google.com/communities/113381227473021565406

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

3) Download and install the latest Android Wear companion app also available
at the preview link above.

4) In the Google API console, select an existing or create a new project
and register it as an OAuth 2.0 client following the first set of instructions
at the link below:

https://developers.google.com/identity/sign-in/web/server-side-flow

Note there is no need to progress beyond setting up the project as an OAuth
client and obtaining the client_id and client_secret keys.

Also, make sure to register https://android.com/wear/3p_auth/<package_name>
as the redirect URI for your client for this sample to work.

4) Update the WearOAuthActivity.java file to include the client_id and
client_secret from the Google API project you previously selected to
configure your OAuth client.

Special Notes
---------------

In this sample, the Wear app makes the direct HTTP call to exchange
the authorization code for auth tokens, and thus the client_id and
client_secret are both exposed in the Wear APK.

This IS NOT the standard practice, and you would normally want to
set the redirect URI to your own server URL, where the server can
perform the authorization code token exchange and then redirect the
user to the special https://android.com/wear/3p_auth/<package_name>
URI.

For the purposes of this sample, the watch is handling the token
exchange.
