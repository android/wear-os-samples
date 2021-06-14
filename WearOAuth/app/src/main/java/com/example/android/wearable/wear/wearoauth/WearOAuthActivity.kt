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

package com.example.android.wearable.wear.wearoauth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.wear.phone.interactions.authentication.RemoteAuthClient

private const val TAG = "WearOAuthActivity"

/**
 * Demonstrates the OAuth flow on Wear OS. This sample currently handles the callback from the
 * Wear companion app after receiving user consent, and the follow-up call to perform the
 * OAuth token exchange before making authenticated API calls.
 *
 * The sample uses the Google OAuth 2.0 APIs, but can be easily extended for any other
 * OAuth 2.0 provider.
 *
 * See [WearOAuthViewModel] for the implementation of the OAuth flow.
 */
class WearOAuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        // In a real world situation you would use some form of Dependency Injection here.
        val viewModel = WearOAuthViewModel(RemoteAuthClient.create(applicationContext))

        // Show current status on the screen
        viewModel.status.observe(this) { statusText ->
            findViewById<TextView>(R.id.text_view).text = statusText
        }

        // Start the OAuth flow when the user presses the button
        findViewById<View>(R.id.googlePlusButton).setOnClickListener {
            viewModel.startOAuthFlow(applicationContext.packageName)
        }
    }
}
