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

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.phone.interactions.authentication.CodeChallenge
import androidx.wear.phone.interactions.authentication.CodeVerifier
import androidx.wear.phone.interactions.authentication.OAuthRequest
import androidx.wear.phone.interactions.authentication.OAuthResponse
import androidx.wear.phone.interactions.authentication.RemoteAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "WearOAuthViewModel"

// TODO Add your client id & secret here (for dev purposes only).
private const val CLIENT_ID = ""
private const val CLIENT_SECRET = ""

/**
 * The viewModel that implements the OAuth flow. The method [startOAuthFlow] implements the
 * different steps of the flow. It first retrieves the OAuth code, uses it to exchange it for an
 * access token, and uses the token to retrieve the user's name.
 */
class WearOAuthViewModel(private val client: RemoteAuthClient) : ViewModel() {
    val status: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    /**
     * Start the authentication flow and do an authenticated request. This method implements
     * the steps described at
     * https://d.google.com/identity/protocols/oauth2/native-app#obtainingaccesstokens
     *
     * The [androidx.wear.phone.interactions.authentication] package helps with this implementation.
     * It can generate a code verifier and challenge, and helps to move the consent step to
     * the phone. After the user consents on their phone, the wearable app is notified and can
     * continue the authorization process.
     */
    fun startOAuthFlow(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val codeVerifier = CodeVerifier()

            // Step 1: Retrieve the OAuth code
            showStatus("Starting authorization... Switch to your phone to authenticate.")
            val code = retrieveOAuthCode(packageName, codeVerifier).getOrElse {
                showStatus("Authorization failed")
                return@launch
            }

            // Step 2: Retrieve the access token
            showStatus("Retrieving token...")
            val token = retrieveToken(code, packageName, codeVerifier).getOrElse {
                showStatus("Could not retrieve token")
                return@launch
            }

            // Step 3: Use token to perform API request
            showStatus("Retrieving user profile...")
            val userName = retrieveUserProfile(token).getOrElse {
                showStatus("Could not retrieve user profile")
                return@launch
            }

            showStatus("User profile retrieved. Welcome $userName!")
        }
    }

    private suspend fun retrieveOAuthCode(
        packageName: String,
        codeVerifier: CodeVerifier
    ): Result<String> {
        // Create the authorization Uri that will be shown to the user on the phone. This will
        // be different depending on the OAuth backend your app uses. Here we use the Google
        // OAuth backend.
        val uri = Uri.Builder()
            .encodedPath("https://accounts.google.com/o/oauth2/v2/auth")
            .appendQueryParameter("scope", "https://www.googleapis.com/auth/userinfo.profile")
            .build()

        // Create the authorization request. Make sure to fill in the CLIENT_ID retrieved from your
        // OAuth server settings.
        val request = OAuthRequest.Builder(packageName)
            .setAuthProviderUrl(uri)
            .setCodeChallenge(CodeChallenge(codeVerifier))
            .setClientId(CLIENT_ID)
            .build()

        Log.d(TAG, "Authorization requested. Request URL: ${request.getRequestUrl()}")

        return suspendCoroutine { c ->
            client.sendAuthorizationRequest(
                request,
                object : RemoteAuthClient.Callback() {
                    override fun onAuthorizationError(errorCode: Int) {
                        Log.w(TAG, "Authorization failed with errorCode $errorCode")
                        c.resume(Result.failure(IOException("Authorization failed")))
                    }

                    override fun onAuthorizationResponse(
                        request: OAuthRequest,
                        response: OAuthResponse
                    ) {
                        val responseUrl = response.getResponseUrl()
                        Log.d(TAG, "Authorization success. ResponseUrl: $responseUrl")
                        val code = responseUrl?.getQueryParameter("code")
                        if (code.isNullOrBlank()) {
                            Log.w(
                                TAG,
                                "Google OAuth 2.0 API token exchange failed. " +
                                    "No code query parameter in response URL."
                            )
                            c.resume(Result.failure(IOException("Authorization failed")))
                        } else {
                            c.resume(Result.success(code))
                        }
                    }
                }
            )
        }
    }

    private fun retrieveToken(
        code: String,
        packageName: String,
        codeVerifier: CodeVerifier
    ): Result<String> {
        // In this sample, we're using a basic implementation of a POST request to retrieve the
        // token. Normally you would probably move this code into a repository and use a library
        // for making such a request.
        Log.d(TAG, "Requesting token...")
        // We request the token from the Google OAuth server. You can replace this with any OAuth
        // server that supports the PKCE authentication flow.
        val url = URL("https://oauth2.googleapis.com/token")

        val conn = url.openConnection() as HttpURLConnection
        try {
            // See https://developers.google.com/identity/protocols/oauth2/native-app#exchange-authorization-code
            val params = mapOf(
                "client_id" to CLIENT_ID,
                "client_secret" to CLIENT_SECRET,
                "code" to code,
                "code_verifier" to codeVerifier.getValue(),
                "grant_type" to "authorization_code",
                "redirect_uri" to "https://wear.googleapis.com/3p_auth/$packageName",
            )

            val postData = StringBuilder()
            for ((key, value) in params) {
                if (postData.isNotEmpty()) postData.append('&')
                postData.append(URLEncoder.encode(key, "UTF-8"))
                postData.append('=')
                postData.append(URLEncoder.encode(value, "UTF-8"))
            }
            val postDataBytes = postData.toString().toByteArray(charset("UTF-8"))

            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("Content-Length", postDataBytes.size.toString())
            conn.doOutput = true
            conn.outputStream.write(postDataBytes)

            val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val response = inputReader.readText()

            Log.d(TAG, "Token retrieved: $response")

            val token = JSONObject(response).getString("access_token")
            return Result.success(token)
        } finally {
            conn.disconnect()
        }
    }

    private fun retrieveUserProfile(
        token: String
    ): Result<String> {
        val url = URL("https://www.googleapis.com/oauth2/v2/userinfo")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.setRequestProperty("Authorization", "Bearer $token")
            val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val response = inputReader.readText()

            Log.d(TAG, "User profile retrieved: $response")

            val username = JSONObject(response).getString("name")
            return Result.success(username)
        } finally {
            conn.disconnect()
        }
    }

    private fun showStatus(statusString: String) = status.postValue(statusString)
}
