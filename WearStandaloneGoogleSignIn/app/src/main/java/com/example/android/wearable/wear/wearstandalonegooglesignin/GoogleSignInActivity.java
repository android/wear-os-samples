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
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

/**
 * Demonstrates using Google Sign-In on Android Wear, including the Wear-styled
 * {@link com.example.android.wearable.wear.wearstandalonegooglesignin.WearGoogleSignInButton}.
 */
public class GoogleSignInActivity extends WearableActivity {

    private static final String TAG = "GoogleSignInActivity";
    private GoogleSignInClient mGoogleSignInClient;

    /* Custom Wear Google Sign-In button to be used until the button is supported in a future Play
     *  Services releases, following the Wear 2.0 final release */
    private SignInButton mSignInButton;
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
        mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Set up the sign out button.
        mSignOutButton = findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        checkAlreadySignedIn();
    }

    private void checkAlreadySignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUi(account);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity request code: " + requestCode);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
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
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    protected void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUi(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG,
                    "signInResult:failed code=" + e.getStatusCode() + ". Msg=" + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()));
            updateUi(null);
        }
    }

    private void updateUi(GoogleSignInAccount account) {
        if (account != null) {
            Toast.makeText(this, R.string.google_signin_successful, Toast.LENGTH_SHORT).show();

            mUserIdToken = account.getIdToken();
            Log.d(TAG, "Google Sign-In success " + mUserIdToken);

            mSignInButton.setVisibility(View.GONE);
            mSignOutButton.setVisibility(View.VISIBLE);
        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignOutButton.setVisibility(View.GONE);
        }
    }

    /**
     * Starts Google sign in activity, response handled in onActivityResult.
     */
    private void signIn() {
        if (mGoogleSignInClient == null) {
            Log.e(TAG, "Google Sign In API client not initialized.");
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    /**
     * Signs the user out and resets the sign-in button to visible.
     */
    private void signOut() {
        if (mGoogleSignInClient == null) {
            Log.e(TAG, "Google Sign In API client not initialized.");
            return;
        }
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUi(null);
                Toast.makeText(GoogleSignInActivity.this, R.string.signout_successful, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
