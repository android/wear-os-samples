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

import android.app.Dialog;

import java.util.function.Supplier;

public class DialogsItem implements Item {
    private final int mItemId;
    private final Supplier<Dialog> mSupplier;

    public DialogsItem(int itemId, Supplier<Dialog> supplier) {
        mItemId = itemId;
        mSupplier = supplier;
    }

    public int getItemId() {
        return mItemId;
    }

    public Supplier<Dialog> getSupplier() {
        return mSupplier;
    }
}
