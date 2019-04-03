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

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

public class ZoomImageActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        AmbientModeSupport.attach(this);

        // Check if integer was actually given.
        if (!(getIntent().hasExtra(getString(R.string.intent_extra_image)))) {
            throw new NotFoundException("Expecting extras");
        }

        // Grab the resource id from extras and set the image resource.
        int value = getIntent().getIntExtra(getString(R.string.intent_extra_image), 0);
        ImageView expandedImage = findViewById(R.id.expanded_image);
        expandedImage.setImageResource(value);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
