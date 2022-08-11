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

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.example.android.wearable.oauth.util.doGetRequest
import com.example.android.wearable.oauth.util.doPostRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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
class AuthDeviceGrantViewModel(application: Application) : AndroidViewModel(application) {
    // Status to show on the Wear OS display
    val status: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    // Dynamic content to show on the Wear OS display
    val result: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    private fun showStatus(statusString: Int, resultString: String = "") {
        status.postValue(statusString)
        result.postValue(resultString)
    }

    fun startAuthFlow() {
        viewModelScope.launch {
            // Step 1: Retrieve the verification URI
            showStatus(R.string.status_switch_to_phone)
            val verificationInfo = retrieveVerificationInfo().getOrElse {
                showStatus(R.string.status_failed)
                return@launch
            }

            // Step 2: Show the pairing code & open the verification URI on the paired device
            showStatus(R.string.status_code, verificationInfo.userCode)
            fireRemoteIntent(verificationInfo.verificationUri)

            // Step 3: Poll the Auth server for the token
            val token = retrieveToken(verificationInfo.deviceCode, verificationInfo.interval)

            // Step 4: Use the token to make an authorized request
            val userName = retrieveUserProfile(token).getOrElse {
                showStatus(R.string.status_failed)
                return@launch
            }

            showStatus(R.string.status_retrieved, userName)
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
    private suspend fun retrieveVerificationInfo(): Result<VerificationInfo> {
        return try {
            Log.d(TAG, "Retrieving verification info...")
            val responseJson = doPostRequest(
                url = "https://oauth2.googleapis.com/device/code",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "scope" to "https://www.googleapis.com/auth/userinfo.profile"
                )
            )
            Result.success(
                VerificationInfo(
                    verificationUri = responseJson.getString("verification_url"),
                    userCode = responseJson.getString("user_code"),
                    deviceCode = responseJson.getString("device_code"),
                    interval = responseJson.getInt("interval")
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Opens the verification URL on the paired device.
     *
     * When the user has the corresponding app installed on their paired Android device, the Data
     * Layer can be used instead, see https://developer.android.com/training/wearables/data-layer.
     *
     * When the user has the corresponding app installed on their paired iOS device, it should
     * use [Universal Links](https://developer.apple.com/ios/universal-links/) to intercept the
     * intent.
     */
    private fun fireRemoteIntent(verificationUri: String) {
        RemoteActivityHelper(getApplication()).startRemoteActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = Uri.parse(verificationUri)
            },
            null
        )
    }

    /**
     * Poll the Auth server for the token. This will only return when the user has finished their
     * authorization flow on the paired device.
     *
     * For this sample the various exceptions aren't handled.
     */
    private tailrec suspend fun retrieveToken(deviceCode: String, interval: Int): String {
        Log.d(TAG, "Polling for token...")
        return fetchToken(deviceCode).getOrElse {
            Log.d(TAG, "No token yet. Waiting...")
            delay(interval * 1000L)
            return retrieveToken(deviceCode, interval)
        }
    }

    private suspend fun fetchToken(deviceCode: String): Result<String> {
        return try {
            val responseJson = doPostRequest(
                url = "https://oauth2.googleapis.com/token",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "client_secret" to CLIENT_SECRET,
                    "device_code" to deviceCode,
                    "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                )
            )

            Result.success(responseJson.getString("access_token"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Using the access token, make an authorized request to the Auth server to retrieve the user's
     * profile.
     */
    private suspend fun retrieveUserProfile(token: String): Result<String> {
        return try {
            val responseJson = doGetRequest(
                url = "https://www.googleapis.com/oauth2/v2/userinfo",
                requestHeaders = mapOf(
                    "Authorization" to "Bearer $token"
                )
            )
            Result.success(responseJson.getString("name"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
