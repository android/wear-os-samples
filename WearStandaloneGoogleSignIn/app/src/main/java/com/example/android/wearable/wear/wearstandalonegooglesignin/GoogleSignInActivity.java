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
package com.example.android.wearable.wear.wearstandalonegooglesignin;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Demonstrates using Google Sign-In on Android Wear, including the Wear-styled
 * {@link com.example.android.wearable.wear.wearstandalonegooglesignin.WearGoogleSignInButton}.
 */
public class GoogleSignInActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private static final String TAG = "GoogleSignInActivity";
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    /* Custom Wear Google Sign-In button to be used until the button is supported in a future Play
     *  Services releases, following the Wear 2.0 final release */
    private WearGoogleSignInButton mSignInButton;
    private Button mSignOutButton;

    // Used to verify the user on the server
    protected String mUserIdToken;

    public static final int REQUEST_CODE_SIGN_IN = 8001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupGoogleApiClient();

        // Set up the sign in button.
        mSignInButton = (WearGoogleSignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Set up the sign out button.
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity request code: " + requestCode);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Configures the GoogleApiClient used for sign in. Requests scopes profile and email.
     */
    protected void setupGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    protected void handleSignInResult(GoogleSignInResult result) {
        if (result == null) {
            Log.d(TAG, "Google Sign-In result is null");
            onGoogleSignInFailure();
        }

        if (result.isSuccess()) {
            mGoogleSignInAccount = result.getSignInAccount();
            if (mGoogleSignInAccount != null) {
                Toast.makeText(this, R.string.google_signin_successful, Toast.LENGTH_SHORT).show();

                mUserIdToken = mGoogleSignInAccount.getIdToken();
                Log.d(TAG, "Google Sign-In success " + mUserIdToken);

                mSignInButton.setVisibility(View.GONE);
                mSignOutButton.setVisibility(View.VISIBLE);

            }
        } else {
            Log.d(TAG, "Google Sign-In failure: " + result.getStatus());
            onGoogleSignInFailure();
        }
    }

    /**
     * Try to silently retrieve sign-in information for a user who is already signed into the app.
     */
    private void refreshSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            handleSignInResult(pendingResult.get());
        } else {
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    /**
     * Starts Google sign in activity, response handled in onActivityResult.
     */
    private void signIn() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            Log.e(TAG, "Google API client not initialized or not connected.");
            return;
        }
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    /**
     * Signs the user out and resets the sign-in button to visible.
     */
    private void signOut() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            Log.e(TAG, "Google API client not initialized or not connected");
            return;
        }
        mGoogleApiClient.connect();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(this);
    }

    /**
     * If the user isn't signed in, enable the sign-in button.
     */
    protected void onGoogleSignInFailure() {
        mSignInButton.setEnabled(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed.");
        CharSequence success = "Connection failed.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, success, duration);
        toast.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
        refreshSignIn();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @Override
    public void onResult(Status status) {
        if (status != null && status.isSuccess()) {
            Log.d(TAG, "Successfully signed out");
            Toast.makeText(this, R.string.signout_successful,
                    Toast.LENGTH_SHORT).show();
            mSignOutButton.setVisibility(View.GONE);
            mSignInButton.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Sign out not successful.");
        }
    }
}
