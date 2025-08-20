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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding

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

@Composable
fun PKCEApp(pkceViewModel: AuthPKCEViewModel) {
    AppScaffold {
        val uiState = pkceViewModel.uiState.collectAsState()
        AuthenticateScreen(
            uiState.value.statusCode,
            uiState.value.resultMessage,
            pkceViewModel::startAuthFlow
        )
    }
}

@Composable
fun AuthenticateScreen(
    statusCode: Int,
    resultMessage: String,
    startAuthFlow: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
        contentPadding = rememberResponsiveColumnPadding(
            first = ColumnItemType.ListHeader,
            last = ColumnItemType.BodyText
        )
    ) { paddingValues ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = paddingValues
        ) {
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
                    onClick = { startAuthFlow() },
                    label = {
                        Text(
                            text = stringResource(R.string.authenticate),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Text(stringResource(id = statusCode)) }
            item { Text(resultMessage) }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AuthenticateScreenPreview() {
    AuthenticateScreen(
        statusCode = R.string.status_retrieved,
        resultMessage = "Bobby Bonson",
        startAuthFlow = {}
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AuthenticateScreenFailedPreview() {
    AuthenticateScreen(
        statusCode = R.string.status_failed,
        resultMessage = "",
        startAuthFlow = {}
    )
}
