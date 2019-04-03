/*
 * Copyright (C) 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearaccessibilityapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.wear.ambient.AmbientMode;


public class AccessibilityActivity extends Activity implements AmbientMode.AmbientCallbackProvider {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);

        AmbientMode.attachAmbientSupport(this);

        ImageView accessibilityImage = findViewById(R.id.icon_image_view);
        accessibilityImage.setImageDrawable(getDrawable(R.drawable.settings_circle));

        TextView accessibilityText = findViewById(R.id.icon_text_view);
        accessibilityText.setText(R.string.accessibility_settings);

        findViewById(R.id.accessibility_button_include)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivityForResult(
                                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                            }
                        });
    }

    @Override
    public AmbientMode.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientMode.AmbientCallback {}
}
