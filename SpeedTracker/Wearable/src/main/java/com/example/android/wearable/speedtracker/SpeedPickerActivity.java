/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
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

package com.example.android.wearable.speedtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.android.wearable.speedtracker.ui.SpeedPickerRecyclerAdapter;

/**
 * An activity that presents a list of speeds to user and allows user to pick one, to be used as
 * the current speed limit.
 */
public class SpeedPickerActivity extends Activity implements SpeedPickerRecyclerAdapter.Callbacks {

    public static final String EXTRA_NEW_SPEED_LIMIT =
            "com.example.android.wearable.speedtracker.extra.NEW_SPEED_LIMIT";

    /* Speeds, in mph, that will be shown on the list */
    private int[] speeds = {25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speed_picker_activity);

        final TextView header = findViewById(R.id.header);

        // Get the list component from the layout of the activity
        WearableRecyclerView recyclerView = findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        recyclerView.setEdgeItemsCenteringEnabled(true);

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Assign an adapter to the list
        recyclerView.setAdapter(new SpeedPickerRecyclerAdapter(speeds, this));
    }

    @Override
    public void speedLimitChange(int speedChange) {
        Intent resultIntent = new Intent(Intent.ACTION_PICK);
        resultIntent.putExtra(EXTRA_NEW_SPEED_LIMIT, speedChange);
        setResult(RESULT_OK, resultIntent);

        finish();
    }
/*
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        int newSpeedLimit = speeds[viewHolder.getPosition()];

        Intent resultIntent = new Intent(Intent.ACTION_PICK);
        resultIntent.putExtra(EXTRA_NEW_SPEED_LIMIT, newSpeedLimit);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

 */
}
