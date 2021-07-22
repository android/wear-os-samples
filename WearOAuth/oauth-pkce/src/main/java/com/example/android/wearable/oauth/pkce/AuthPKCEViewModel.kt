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
import com.example.android.wearable.oauth.util.doGetRequest
import com.example.android.wearable.oauth.util.doPostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "WearOAuthViewModel"

// TODO Add your client id & secret here (for dev purposes only).
private const val CLIENT_ID = ""
private const val CLIENT_SECRET = ""

/**
 * The viewModel that implements the OAuth flow. The method [startAuthFlow] implements the
 * different steps of the flow. It first retrieves the OAuth code, uses it to exchange it for an
 * access token, and uses the token to retrieve the user's name.
 */
class AuthPKCEViewModel(private val client: RemoteAuthClient) : ViewModel() {
    // Status to show on the Wear OS display
    val status: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private fun showStatus(statusString: String) = status.postValue(statusString)

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
    fun startAuthFlow(packageName: String) {
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

    /**
     * Use the [RemoteAuthClient] class to authorize the user. The library will handle the
     * communication with the paired device, where the user can log in.
     */
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

        Log.d(TAG, "Authorization requested. Request URL: ${request.requestUrl}")

        // Wrap the callback-based request inside a coroutine wrapper
        return suspendCoroutine { c ->
            client.sendAuthorizationRequest(
                request = request,
                executor = { command -> command?.run() },
                clientCallback = object : RemoteAuthClient.Callback() {
                    override fun onAuthorizationError(request: OAuthRequest, errorCode: Int) {
                        Log.w(TAG, "Authorization failed with errorCode $errorCode")
                        c.resume(Result.failure(IOException("Authorization failed")))
                    }

                    override fun onAuthorizationResponse(
                        request: OAuthRequest,
                        response: OAuthResponse
                    ) {
                        val responseUrl = response.responseUrl
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
        return doPostRequest(
            // We request the token from the Google OAuth server. You can replace this with any OAuth
            // server that supports the PKCE authentication flow.
            url = "https://oauth2.googleapis.com/token",
            // See https://developers.google.com/identity/protocols/oauth2/native-app#exchange-authorization-code
            params = mapOf(
                "client_id" to CLIENT_ID,
                "client_secret" to CLIENT_SECRET,
                "code" to code,
                "code_verifier" to codeVerifier.value,
                "grant_type" to "authorization_code",
                "redirect_uri" to "https://wear.googleapis.com/3p_auth/$packageName",
            )
        ).map { jsonObject ->
            jsonObject.getString("access_token")
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
