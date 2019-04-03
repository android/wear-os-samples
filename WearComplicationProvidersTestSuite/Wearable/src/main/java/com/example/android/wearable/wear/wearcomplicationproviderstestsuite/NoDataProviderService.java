/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;

/** A complication provider that always returns {@link ComplicationData#TYPE_NO_DATA}. */
public class NoDataProviderService extends ComplicationProviderService {

    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        ComplicationData data = new ComplicationData.Builder(ComplicationData.TYPE_NO_DATA).build();
        manager.updateComplicationData(complicationId, data);
    }
}