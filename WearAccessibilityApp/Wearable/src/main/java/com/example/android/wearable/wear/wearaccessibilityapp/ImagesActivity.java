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

import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

public class ImagesActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {
    private AnimatedVectorDrawable mAnimatedVectorDrawableSwipe;
    private AnimatedVectorDrawable mAnimatedVectorDrawableTap;
    private AnimationCallback mAnimationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        AmbientModeSupport.attach(this);

        // Used to repeat animation from the beginning.
        mAnimationCallback =
                new AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        super.onAnimationEnd(drawable);
                        ((AnimatedVectorDrawable) drawable).start();
                    }
                };

        // Play 'swipe left' animation on loop.
        ImageView mSwipeLeftImage = findViewById(R.id.swipe_left_image);
        mAnimatedVectorDrawableSwipe = (AnimatedVectorDrawable) mSwipeLeftImage.getDrawable();
        mAnimatedVectorDrawableSwipe.start();
        mAnimatedVectorDrawableSwipe.registerAnimationCallback(mAnimationCallback);

        // Play 'tap' animation on loop.
        ImageView mTapImage = findViewById(R.id.tap_image);
        mAnimatedVectorDrawableTap = (AnimatedVectorDrawable) mTapImage.getDrawable();
        mAnimatedVectorDrawableTap.start();
        mAnimatedVectorDrawableTap.registerAnimationCallback(mAnimationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnimatedVectorDrawableSwipe.unregisterAnimationCallback(mAnimationCallback);
        mAnimatedVectorDrawableTap.unregisterAnimationCallback(mAnimationCallback);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
