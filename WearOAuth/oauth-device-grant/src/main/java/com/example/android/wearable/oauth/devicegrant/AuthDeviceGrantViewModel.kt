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

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.wearable.oauth.util.doGetRequest
import com.example.android.wearable.oauth.util.doPostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthDeviceGrantViewModel"

// TODO Add your client id & secret here (for dev purposes only).
private const val CLIENT_ID = ""
private const val CLIENT_SECRET = ""

/**
 * The viewModel that implements the OAuth flow. The method [startAuthFlow] implements the
 * different steps of the flow. It first retrieves the URL that should be opened on the paired
 * device, then polls for the access token, and uses it to retrieve the user's name.
 */
class AuthDeviceGrantViewModel : ViewModel() {
    // Status to show on the Wear OS display
    val status: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private fun showStatus(statusString: String) = status.postValue(statusString)

    // To send a Remote Intent, we need the activity context. This Channel and Flow instance make
    // it possible to send an event to the Activity, which can then fire the Remote Intent.
    private val fireRemoteIntentChannel = Channel<String>(Channel.BUFFERED)
    val fireRemoteIntentFlow = fireRemoteIntentChannel.receiveAsFlow()

    fun startAuthFlow() {
        viewModelScope.launch(Dispatchers.IO) {
            // Step 1: Retrieve the verification URI
            showStatus("Starting authorization... Switch to your phone to authenticate.")
            val verificationInfo = retrieveVerificationInfo().getOrElse {
                showStatus("Authorization failed")
                return@launch
            }

            // Step 2: Show the pairing code & open the verification URI on the paired device
            showStatus("code: ${verificationInfo.userCode}")
            fireRemoteIntentChannel.send(verificationInfo.verificationUri)

            // Step 3: Poll the Auth server for the token
            val token = retrieveToken(verificationInfo.deviceCode, verificationInfo.interval)

            // Step 4: Use the token to make an authorized request
            val userName = retrieveUserProfile(token).getOrElse {
                showStatus("Authorization failed")
                return@launch
            }

            showStatus("User profile retrieved. Welcome $userName!")
        }
    }

    // The response data when retrieving the verification
    data class VerificationInfo(
        val verificationUri: String,
        val userCode: String,
        val deviceCode: String,
        val interval: Int
    )

    /**
     * Retrieve the information needed to verify the user. When performing this request, the server
     * generates a user & device code pair. The user code is shown to the user and opened on the
     * paired device. The device code is passed while polling the OAuth server.
     */
    private fun retrieveVerificationInfo(): Result<VerificationInfo> {
        Log.d(TAG, "Retrieving verification info...")
        return doPostRequest(
            url = "https://oauth2.googleapis.com/device/code",
            params = mapOf(
                "client_id" to CLIENT_ID,
                "scope" to "https://www.googleapis.com/auth/userinfo.profile"
            )
        ).map { responseJson ->
            VerificationInfo(
                verificationUri = responseJson.getString("verification_url"),
                userCode = responseJson.getString("user_code"),
                deviceCode = responseJson.getString("device_code"),
                interval = responseJson.getInt("interval")
            )
        }
    }

    /**
     * Poll the Auth server for the token. This will only return when the user has finished their
     * authorization flow on the paired device.
     *
     * For this sample the various exceptions aren't handled.
     */
    private suspend fun retrieveToken(deviceCode: String, interval: Int): String {
        Log.d(TAG, "Polling for token...")
        return fetchToken(deviceCode).getOrElse {
            Log.d(TAG, "No token yet. Waiting...")
            delay(interval * 1000L)
            return retrieveToken(deviceCode, interval)
        }
    }

    private fun fetchToken(deviceCode: String): Result<String> {
        return try {
            doPostRequest(
                url = "https://oauth2.googleapis.com/token",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "client_secret" to CLIENT_SECRET,
                    "device_code" to deviceCode,
                    "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                )
            ).map { responseJson ->
                if (!responseJson.has("access_token")) throw Exception("No token yet!")
                responseJson.getString("access_token")
            }
        } catch (e: Throwable) {
            return Result.failure(Exception("No token yet!"))
        }
    }

    /**
     * Using the access token, make an authorized request to the Auth server to retrieve the user's
     * profile.
     */
    private fun retrieveUserProfile(token: String): Result<String> {
        return doGetRequest(
            url = "https://www.googleapis.com/oauth2/v2/userinfo",
            requestHeaders = mapOf(
                "Authorization" to "Bearer $token"
            )
        ).map { responseJson ->
            responseJson.getString("name")
        }
    }
}
