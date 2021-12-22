/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.android.wearable.wear.wearstandalonegooglesignin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Demonstrates using Google Sign-In on Android Wear
 */
class GoogleSignInActivity : ComponentActivity() {

    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var googleSignInAccount by remember {
                mutableStateOf(GoogleSignIn.getLastSignedInAccount(this))
            }

            val signInRequestLauncher = rememberLauncherForActivityResult(
                contract = GoogleSignInContract(googleSignInClient)
            ) {
                googleSignInAccount = it
                if (googleSignInAccount != null) {
                    Toast.makeText(
                        this,
                        R.string.google_signin_successful,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val coroutineScope = rememberCoroutineScope()

            GoogleSignInScreen(
                googleSignInAccount = googleSignInAccount,
                onSignInClicked = { signInRequestLauncher.launch(Unit) },
                onSignOutClicked = {
                    coroutineScope.launch {
                        try {
                            googleSignInClient.signOut().await()

                            googleSignInAccount = null

                            Toast.makeText(
                                this@GoogleSignInActivity,
                                R.string.signout_successful,
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (apiException: ApiException) {
                            Log.w("GoogleSignInActivity", "Sign out failed: $apiException")
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun GoogleSignInScreen(
    googleSignInAccount: GoogleSignInAccount?,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (googleSignInAccount == null) {
            AndroidView(::SignInButton) { signInButton ->
                signInButton.setOnClickListener {
                    onSignInClicked()
                }
            }
        } else {
            Chip(
                onClick = onSignOutClicked,
                label = {
                    Text(text = stringResource(id = R.string.wear_signout_button_text))
                }
            )
        }
    }
}

/**
 * An [ActivityResultContract] for signing in with the given [GoogleSignInClient].
 */
private class GoogleSignInContract(
    private val googleSignInClient: GoogleSignInClient
) : ActivityResultContract<Unit, GoogleSignInAccount?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        googleSignInClient.signInIntent

    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        // As documented, this task must be complete
        check(task.isComplete)

        return if (task.isSuccessful) {
            task.result
        } else {
            val exception = task.exception
            check(exception is ApiException)
            Log.w(
                "GoogleSignInContract",
                "Sign in failed: code=${
                exception.statusCode
                }, message=${
                GoogleSignInStatusCodes.getStatusCodeString(exception.statusCode)
                }"
            )
            null
        }
    }
}
