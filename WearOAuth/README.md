Wear OS OAuth Sample
===================================

Introduction
------------

Sample demonstrating various forms of OAuth 2.0 authentication on Wear OS.

The sample demonstrates two possible flows between a Wear OS app and the Android Wear companion app
for handling OAuth 2.0 authentication. The sample uses the Google OAuth 2.0 server, but you can use
any OAuth server instead.

## Device Authorization Grant OAuth ([RFC 8628](https://datatracker.ietf.org/doc/html/rfc8628)]

The flow starts by tapping the "authenticate" button in the Wear OS app. This retrieves a
verification URL from the OAuth server, and opens this URL on the paired phone. It then continues to
poll the OAuth server for OAuth tokens. After the user accepts and provides consent, the OAuth
tokens are filled, and the result is used to make an authenticated call to the Google OAuth 2.0 API.

## Authorization Code Grant with PKCE (Proof Key for Code Exchange) ([RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636))

The flow starts by tapping the "authenticate" button in the Wear OS app. This opens a consent web
view on the phone's Wear companion app. Once the user accepts and provides consent, flow is returned
back to the watch. The watch makes a direct HTTP call to exchange the authorization code for OAuth
tokens, and then makes a follow-up authenticated call to the Google OAuth 2.0 API.

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Setup steps:

1) Download and install Android Studio if you haven't already.

2) Note that Android Studio should already have the Wear-related support libraries and Play Services
   packages available in the SDK manager and should be able to resolve them by doing the following:

a) Right-click on the app module and select Open Module Settings... b) Click on the Dependencies tab
c) Resolve the libraries referenced as compile targets in the app/build.gradle file and add them as
dependencies to the module

In case you're unable to find the relevant Wear support libraries from the Android Studio SDK
manager, you can also download and setup the libraries from the Wear docs:

https://developer.android.com/training/wearables/get-started/creating

3) Download and install the latest Android Wear companion app also available at the preview link
   above.

4) In the Google API console, select an existing or create a new project and register it as an OAuth
   2.0 client.

    - For the Device Authorization Grant sample you must follow the instructions
      for ["TV and limited input"](https://developers.google.com/identity/protocols/oauth2/limited-input-device#creatingcred)
      .
    - For the PKCE sample you follow the instructions
      for ["web application"](https://developers.google.com/identity/protocols/oauth2/web-server#creatingcred)
      . Make sure to register https://wear.googleapis.com/3p_auth/<package_name>
      as the redirect URI for your client for this sample to work.

4) Update the AuthViewModel.kt file to include the client_id and client_secret from the Google API
   project you previously selected to configure your OAuth client.

Special Notes
---------------

In this sample, the Wear app makes direct HTTP calls to retrieve auth tokens, and thus the client_id
and client_secret are both exposed in the Wear APK.

This IS NOT the standard practice. Instead, you would normally work with an intermediary server.
That server in turn will call the OAuth server to authenticate. This way, the client id and secret
are stored on your intermediary server.

For the simplicity of this sample, the watch is handling the token exchange.

Support
-------

Stack Overflow: https://stackoverflow.com/questions/tagged/wear-os

If you've found an error in this sample, please file an issue:
https://github.com/android/wear-os-samples

Patches are encouraged, and may be submitted by forking this project and submitting a pull request
through GitHub. Please see CONTRIBUTING.md for more details.

License
-------

Copyright 2021 Google Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
See the NOTICE file distributed with this work for additional information regarding copyright
ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions and limitations under the
License.
