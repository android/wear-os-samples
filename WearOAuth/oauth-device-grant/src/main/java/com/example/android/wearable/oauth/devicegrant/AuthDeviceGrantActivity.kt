/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.oauth.devicegrant

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

/**
 * Demonstrates the OAuth 2.0 flow on Wear OS using Device Authorization Grant, as described in
 * [RFC 8628](https://datatracker.ietf.org/doc/html/rfc8628).
 *
 * This sample currently directly polls the Google OAuth 2.0 API, which means the client id and
 * client secret are included in the app. In practice you would use an intermediary server that
 * holds these values for you.
 *
 * The sample uses the Google OAuth 2.0 APIs, but can be easily extended for any other
 * OAuth 2.0 provider.
 *
 * See [AuthDeviceGrantViewModel] for the implementation of the authorization flow.
 */
class AuthDeviceGrantActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val viewModel by viewModels<AuthDeviceGrantViewModel>()

        // Start the OAuth flow when the user presses the button
        findViewById<View>(R.id.authenticateButton).setOnClickListener {
            viewModel.startAuthFlow()
        }

        // Show current status on the screen
        viewModel.status.observe(this) { statusText ->
            findViewById<TextView>(R.id.status_text_view).text = resources.getText(statusText)
        }

        // Show dynamic content on the screen
        viewModel.result.observe(this) { resultText ->
            findViewById<TextView>(R.id.result_text_view).text = resultText
        }
    }
}
