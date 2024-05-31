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
package com.example.android.wearable.oauth.pkce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState

/**
 * Demonstrates the OAuth flow on Wear OS. This sample currently handles the callback from the
 * Wear companion app after receiving user consent, and the follow-up call to perform the
 * OAuth token exchange before making authenticated API calls.
 *
 * The sample uses the Google OAuth 2.0 APIs, but can be easily extended for any other
 * OAuth 2.0 provider.
 *
 * See [AuthPKCEViewModel] for the implementation of the OAuth flow.
 */
class AuthPKCEActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PKCEApp(pkceViewModel = viewModel()) }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PKCEApp(pkceViewModel: AuthPKCEViewModel) {
    AppScaffold {
        val uiState = pkceViewModel.uiState.collectAsState()
        val localContext = LocalContext.current
        val columnState = rememberResponsiveColumnState(
            contentPadding = ScalingLazyColumnDefaults.padding(
                first = ScalingLazyColumnDefaults.ItemType.Text,
                last = ScalingLazyColumnDefaults.ItemType.Text
            )
        )
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(columnState = columnState) {
                item {
                    ListHeader {
                        Text(
                            stringResource(R.string.oauth_pkce),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    Button(
                        onClick = { pkceViewModel.startAuthFlow(localContext) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.authenticate),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                item { Text(stringResource(id = uiState.value.statusCode)) }
                item { Text(uiState.value.resultMessage) }
            }
        }
    }
}
