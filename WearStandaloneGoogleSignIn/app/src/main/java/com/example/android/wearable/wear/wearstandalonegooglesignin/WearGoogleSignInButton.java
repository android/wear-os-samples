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

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * The Wear Google Sign-In button to authenticate the user. This button is styled to match Wear
 * material design and should be used for all integrations as throwaway code that can be replaced
 * once the final version lands in Play Services, and maintained there going forward.
 * <p>
 * Note that this class only handles the visual
 * aspects of the button. In order to trigger an action, register a listener using
 * {@link #setOnClickListener(OnClickListener)}.
 */
public class WearGoogleSignInButton extends LinearLayout {

    public WearGoogleSignInButton(Context context) {
        this(context, null);
    }

    public WearGoogleSignInButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearGoogleSignInButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WearGoogleSignInButton(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.wear_google_signin_btn_layout, this);
        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        setFocusable(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setEnabled(enabled);
        }
    }
}
