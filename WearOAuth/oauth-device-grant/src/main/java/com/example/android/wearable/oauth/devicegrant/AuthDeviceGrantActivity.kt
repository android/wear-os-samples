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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.Button

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
        // setViewsContent()
        setContent { AuthenticateScreen() }
    }
}

@Composable
fun AuthenticateScreen() {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item { ListHeader { Text("OAuth Device Auth Grant", textAlign = TextAlign.Center) } }
            item {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxSize() ){
                    Text(text = "Get Grant from Phone",
                         modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            item {
                Text("code generation status")
            }
        }
    }
}

fun ComponentActivity.setViewsContent() {
    setContentView(R.layout.activity_auth)
    val viewModel by viewModels<AuthDeviceGrantViewModel>()

    // Start the OAuth flow when the user presses the button
    // Once you click this, the next view (status_text_view) breifly flashes "check your
    // phone", before displaying a code
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
