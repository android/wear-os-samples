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
import android.content.Context;
import android.content.Intent;

public class AppItem {
    private final String mItemName;
    private final int mImageId;
    private final int mViewType;
    private final Class mClass;

    public AppItem(String itemName, int imageId, int viewType, Class<? extends Activity> clazz) {
        mItemName = itemName;
        mImageId = imageId;
        mViewType = viewType;
        mClass = clazz;
    }

    public AppItem(String itemName, int imageId, Class<? extends Activity> clazz) {
        mItemName = itemName;
        mImageId = imageId;
        mViewType = SampleAppConstants.NORMAL;
        mClass = clazz;
    }

    public String getItemName() {
        return mItemName;
    }

    public int getImageId() {
        return mImageId;
    }

    public int getViewType() {
        return mViewType;
    }

    public void launchActivity(Context context) {
        Intent intent = new Intent(context, mClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }
}
