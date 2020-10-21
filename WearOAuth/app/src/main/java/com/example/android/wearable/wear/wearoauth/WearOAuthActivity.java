/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearoauth;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.support.wearable.authentication.OAuthClient;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Demonstrates the OAuth 2.0 flow on Wear OS by initiating an auth request that is passed to the
 * phone and triggers a browser view where the user can easily approve/deny access from a signed
 * account.
 *
 * After approval/denial, sample handles response via callback and performs the OAuth token exchange
 * before making an authenticated API call.
 *
 * If you are using a Google OAuth client ID (make sure the application type is "Web application"
 * not "Android"). You can find more details here:
 * https://developer.android.com/training/wearables/apps/auth-wear#OAuth
 *
 * The sample uses the Google and Google OAuth 2.0 APIs, but can be easily extended for any other
 * OAuth 2.0 provider.
 */
public class WearOAuthActivity extends Activity {

    public static final String TAG = "WearOAuthActivity";

    // Note that normally the redirect URL would be your own server, which would in turn
    // redirect to this URL intercepted by the Wear OS companion app after completing the
    // auth code exchange.
    private static final String HTTP_REDIRECT_URL = "https://www.android.com/wear/3p_auth";

    // TODO Add your client id and client secret here (for dev purposes only).
    private static final String CLIENT_ID = "TODO_ADD_CLIENT_ID.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "TODO_ADD_CLIENT_SECRET";

    private OAuthClient mOAuthClient;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mOAuthClient = OAuthClient.create(this);
        setContentView(R.layout.layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOAuthClient.destroy();
    }

    public void onClickStartGoogleOAuth2Flow(View view) {
        Log.d(TAG, "onClickStartGoogleOAuth2Flow()");

        if (clientIdsAreSet()) {
            performRequest(
                    "https://accounts.google.com/o/oauth2/v2/auth?response_type=code"
                            + "&client_id="
                            + CLIENT_ID
                            + "&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fplus.login"
                            + "&redirect_uri=" + redirectUrl(),
                    new GoogleOAuth2RequestCallback(this));
        } else {
            updateStatus("CLIENT_ID and CLIENT_SECRET aren't properly set.");
        }
    }

    private boolean clientIdsAreSet() {

        if (TextUtils.isEmpty(CLIENT_ID)) {
            return false;
        } else if (TextUtils.isEmpty(CLIENT_SECRET)) {
            return false;
        } else if (CLIENT_ID.toLowerCase().contains("todo")) {
            return false;
        } else if (CLIENT_SECRET.toLowerCase().contains("todo")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method should be called with any OAuth 2.0 URL scheme and for any provider. The callback
     * object is called after the user provides consent on the authorization screen on the Wear OS
     * companion app.
     */
    private void performRequest(String url, @Nullable OAuthClient.Callback callback) {
        Log.d(TAG, "performRequest()");
        mOAuthClient.sendAuthorizationRequest(Uri.parse(url), callback);
    }

    private String redirectUrl() {
        // Ensure you register the redirect URI in your Google OAuth 2.0 client configuration.
        // Normally this would be the server that would handle the token exchange after receiving
        // the authorization code.
        return HTTP_REDIRECT_URL + "/" + getApplicationContext().getPackageName();
    }

    /**
     * Helper method to update display with fetched results on the activity view.
     *
     * @param text Returned text to display
     */
    private void updateStatus(final String text) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = findViewById(R.id.text_view);
                        Log.d(TAG, text);
                        textView.setText(text);
                    }
                });
    }

    /**
     * Handles the callback from the Wear OS phone app after user consents to the OAuth
     * authorization screen. Currently this callback performs both the token exchange for the
     * authorization code, and a follow test authenticated call to the Google OAuth 2.0 API.
     */
    private class GoogleOAuth2RequestCallback extends OAuthClient.Callback {

        protected @Nullable String accessToken;
        protected Uri responseUrl;
        protected final OkHttpClient okHttpClient = new OkHttpClient();

        protected WearOAuthActivity authActivity = null;

        public GoogleOAuth2RequestCallback(WearOAuthActivity activity) {
            this.authActivity = activity;
        }

        @Override
        public void onAuthorizationError(final int error) {
            Log.e(TAG, "onAuthorizationError called: " + error);
        }

        @Override
        public void onAuthorizationResponse(Uri requestUrl, Uri responseUrl) {
            Log.d(TAG, "onAuthorizationResponse()");

            authActivity.updateStatus("Request completed. Response URL:\n" + responseUrl);
            this.responseUrl = responseUrl;

            Runnable runnable =
                    new Runnable() {
                        public void run() {
                            Request formPost = createFormPostRequest();
                            if (formPost != null) {
                                acquireToken(formPost);
                                accessAPI();
                            }
                        }
                    };
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(runnable);
        }

        private Request createFormPostRequest() {
            Log.d(TAG, "createHttpPostObject()");

            String code = responseUrl.getQueryParameter("code");
            if (TextUtils.isEmpty(code)) {
                authActivity.updateStatus(
                        "Google OAuth 2.0 API token exchange failed. No code query parameter" +
                                " in response URL");
                return null;
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("code", code)
                    .add("redirect_uri", redirectUrl())
                    .add("client_id", CLIENT_ID)
                    .add("client_secret", CLIENT_SECRET)
                    .build();

            return new Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(formBody)
                    .build();
        }

        private void acquireToken(Request formRequest) {
            Log.d(TAG, "acquireToken()");

            try {
                Response response = okHttpClient.newCall(formRequest).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                ResponseBody body = response.body();
                JSONObject jsonObject = new JSONObject(body.string());

                accessToken = jsonObject.getString("access_token");

                if (TextUtils.isEmpty(accessToken)) {
                    authActivity.updateStatus("Google OAuth 2.0 API token exchange failed. " +
                            " No access token in response.");
                } else {
                    authActivity.updateStatus("Google OAuth 2.0 API token exchange " +
                            "succeeded. Token:\n" + accessToken);
                }
            } catch (IOException | JSONException exception) {
                Log.e(TAG, "Exception: " + exception);
                authActivity.updateStatus("Google OAuth 2.0 API token exchange failed. " +
                        "Check exception");
            }
        }

        private void accessAPI() {
            Log.d(TAG, "accessAPI()");

            Request request = new Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v2/userinfo")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                Log.d(TAG, "Response: " + response);

                if (response != null) {
                    String jsonString = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonString);

                    authActivity.updateStatus("Google OAuth 2.0 API request occurred. " +
                            "Response:\n" + jsonObject);
                } else {
                    Log.e(TAG, "Could not execute HTTP request. No response returned.");
                    authActivity.updateStatus("Accessing API failed; Check logs.");
                }
            } catch (IOException | JSONException exception) {
                Log.e(TAG, "Exception occurred\n\n" + Log.getStackTraceString(exception));
                authActivity.updateStatus("Accessing API failed; Check logs.");
            }
        }
    }
}
