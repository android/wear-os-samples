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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.android.wearable.wear.wearoauth.R.id.text_view;

/**
 * Demonstrates the OAuth flow on Android Wear. This sample currently handles the callback from the
 * Android Wear companion app after receiving user consent, and the follow-up call to perform the
 * OAuth token exchange before making authenticated API calls.
 *
 * The sample uses the Google+ and Google OAuth 2.0 APIs, but can be easily extended for any other
 * OAuth 2.0 provider.
 */
public class WearOAuthActivity extends Activity {

    public static final String TAG = "WearOAuthActivity";

    // Note that normally the redirect URL would be your own server, which would in turn
    // redirect to this URL intercepted by the Android Wear companion app after completing the
    // auth code exchange.
    private static final String HTTP_REDIRECT_URL = "https://www.android.com/wear/3p_auth";

    // TODO Add your client id and client secret here (for dev purposes only).
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";

    private OAuthClient mOAuthClient;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mOAuthClient = OAuthClient.create(this);
        setContentView(com.example.android.wearable.wear.wearoauth.R.layout.layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOAuthClient.destroy();
    }

    public void onClickStartGoogleOAuth2Flow(View view) {

        performRequest(
                "https://accounts.google.com/o/oauth2/v2/auth?response_type=code"
                        + "&client_id="
                        + CLIENT_ID
                        + "&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fplus.login"
                        + "&redirect_uri=" + redirectUrl(),
                new GoogleOAuth2RequestCallback(this));
    }

    /**
     * This method should be called with any OAuth 2.0 URL scheme and for any provider. The callback
     * object is called after the user provides consent on the authorization screen on the Android
     * Wear companion app.
     */
    private void performRequest(String url, @Nullable OAuthClient.Callback callback) {
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
                        TextView textView = (TextView) findViewById(text_view);
                        Log.d(TAG, text);
                        textView.setText(text);
                    }
                });
    }

    /**
     * Handles the callback from the Android Wear phone app after user consents to the OAuth
     * authorization screen. Currently this callback performs both the token exchange for the
     * authorization code, and a follow test authenticated call to the Google OAuth 2.0 API.
     */
    private class GoogleOAuth2RequestCallback extends OAuthClient.Callback {

        protected @Nullable String accessToken;
        protected Uri responseUrl;
        protected final HttpClient httpClient = new DefaultHttpClient();
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
            Log.d(TAG, "onResult(). requestUrl:" + requestUrl + " responseUrl: " + responseUrl);
            authActivity.updateStatus("Request completed. Response URL: " + responseUrl);
            this.responseUrl = responseUrl;

            Runnable runnable =
                    new Runnable() {
                        public void run() {
                            HttpPost httpPost = createHttpPostObject();
                            if (httpPost != null) {
                                acquireToken(httpPost);
                                accessAPI();
                            }
                        }
                    };
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(runnable);
        }

        private HttpPost createHttpPostObject() {
            String code = responseUrl.getQueryParameter("code");
            if (TextUtils.isEmpty(code)) {
                authActivity.updateStatus(
                        "Google OAuth 2.0 API token exchange failed. No code query parameter in "
                                + "response URL");
                return null;
            }

            HttpPost httpPost = new HttpPost("https://www.googleapis.com/oauth2/v4/token");
            ArrayList<BasicNameValuePair> nameValuePair = new ArrayList<BasicNameValuePair>(5);
            nameValuePair.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nameValuePair.add(new BasicNameValuePair("code", code));
            nameValuePair.add(new BasicNameValuePair("redirect_uri", redirectUrl()));
            nameValuePair.add(new BasicNameValuePair("client_id", CLIENT_ID));
            nameValuePair.add(
                    new BasicNameValuePair("client_secret", CLIENT_SECRET));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding exception occurred. Stack trace:\n\n"
                        + Log.getStackTraceString(e));
            }
            return httpPost;
        }

        private void acquireToken(HttpPost httpPost) {
            try {
                HttpResponse response = httpClient.execute(httpPost);
                WearOAuthActivity.this.updateStatus(
                        "Google OAuth 2.0 API token exchange occurred. Response: "
                                + response.toString());
                String jsonString = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    String accessToken = jsonResponse.getString("access_token");
                    if (TextUtils.isEmpty(accessToken)) {
                        authActivity.updateStatus(
                                "Google OAuth 2.0 API token exchange failed. No access token in "
                                        + "response.");
                        return;
                    }
                    this.accessToken = accessToken;
                } catch (JSONException e) {
                    Log.e(TAG, "Bad JSON returned:\n\n" + Log.getStackTraceString(e));
                }
            } catch (ClientProtocolException e) {
                Log.e(TAG, "Bad protocol:\n\n" + Log.getStackTraceString(e));
            } catch (IOException e) {
                Log.e(TAG, "Exception occurred:\n\n" + Log.getStackTraceString(e));
            }
        }

        private void accessAPI() {
            HttpGet httpGet = new HttpGet("https://www.googleapis.com/oauth2/v2/userinfo");
            httpGet.setHeader("Authorization", "Bearer " + accessToken);
            try {
                HttpResponse response = httpClient.execute(httpGet);

                if(response == null) {
                    Log.e(TAG, "Could not execute HTTP request. No response returned.");
                    return;
                }

                authActivity.updateStatus(
                        "Google OAuth 2.0 API request occurred. Response: " + response.toString());
                String jsonString = EntityUtils.toString(response.getEntity());
                authActivity.updateStatus("Google OAuth 2.0 API response: " + jsonString);
            } catch (ClientProtocolException e) {
                Log.e(TAG, "Bad protocol:\n\n" + Log.getStackTraceString(e));
            } catch (IOException e) {
                Log.e(TAG, "Exception occurred\n\n" + Log.getStackTraceString(e));
            }
        }
    }
}
