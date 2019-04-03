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

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AmbientModeSupport.attach(this);

        // Menu items
        List<AppItem> items = new ArrayList<AppItem>();
        items.add(
                new AppItem(
                        getString(R.string.lists), R.drawable.lists_circle, ListsActivity.class));
        items.add(
                new AppItem(
                        getString(R.string.dialogs),
                        R.drawable.dialogs_circle,
                        DialogsActivity.class));
        items.add(
                new AppItem(
                        getString(R.string.progress),
                        R.drawable.progress_circle,
                        ProgressActivity.class));
        items.add(
                new AppItem(
                        getString(R.string.controls),
                        R.drawable.controls_circle,
                        ControlsActivity.class));
        items.add(
                new AppItem(
                        getString(R.string.notifications),
                        R.drawable.notifications_circle,
                        NotificationsActivity.class));
        items.add(
                new AppItem(
                        getString(R.string.accessibility),
                        R.drawable.accessibility_circle,
                        AccessibilityActivity.class));

        MenuRecyclerViewAdapter appListAdapter = new MenuRecyclerViewAdapter(this, items);

        WearableRecyclerView recyclerView = findViewById(R.id.main_recycler_view);

        // Customizes scrolling so items farther away form center are smaller.
        ScalingScrollLayoutCallback scalingScrollLayoutCallback = new ScalingScrollLayoutCallback();
        recyclerView.setLayoutManager(
                new WearableLinearLayoutManager(this, scalingScrollLayoutCallback));

        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setAdapter(appListAdapter);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
