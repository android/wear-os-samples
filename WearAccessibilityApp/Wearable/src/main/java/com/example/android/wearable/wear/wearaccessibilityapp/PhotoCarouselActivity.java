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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

public class PhotoCarouselActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider, OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_carousel);

        AmbientModeSupport.attach(this);

        // Grab reference to each image in activity_photo_carousel.
        ImageView catImage = findViewById(R.id.cat_image);
        catImage.setTag(R.drawable.cats);
        catImage.setOnClickListener(this);

        ImageView dogImage = findViewById(R.id.dog_image);
        dogImage.setTag(R.drawable.dog);
        dogImage.setOnClickListener(this);

        ImageView hamsterImage = findViewById(R.id.hamster_image);
        hamsterImage.setTag(R.drawable.hamster);
        hamsterImage.setOnClickListener(this);

        ImageView birdImage = findViewById(R.id.bird_image);
        birdImage.setTag(R.drawable.birds);
        birdImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ZoomImageActivity.class);
        intent.putExtra(getString(R.string.intent_extra_image), (int) v.getTag());
        startActivity(intent);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
