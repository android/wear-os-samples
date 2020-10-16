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

package com.example.android.wearable.watchface.config;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.BoxInsetLayout;
import androidx.wear.widget.WearableRecyclerView;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.util.DigitalWatchFaceUtil;
import com.example.android.wearable.watchface.watchface.DigitalWatchFaceService;
import com.google.android.gms.wearable.DataMap;
import java.util.concurrent.Callable;

/**
 * The watch-side config activity for {@link DigitalWatchFaceService}, which allows for setting the
 * background color.
 */
public class DigitalWatchFaceWearableConfigActivity extends Activity {
    private static final String TAG = "DigitalWatchFaceConfig";

    private WearableRecyclerView mColorSelectionRecyclerView;
    private DigitalColorRecyclerViewAdapter mDigitalColorRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_config);

        BoxInsetLayout content = (BoxInsetLayout) findViewById(R.id.content);

        // BoxInsetLayout adds padding by default on round devices. Add some on square devices.
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                if (!insets.isRound()) {
                    v.setPaddingRelative(
                            (int) getResources().getDimensionPixelSize(R.dimen.content_padding_start),
                            v.getPaddingTop(),
                            v.getPaddingEnd(),
                            v.getPaddingBottom());
                }
                return v.onApplyWindowInsets(insets);
            }
        });

        mColorSelectionRecyclerView = findViewById(R.id.color_picker_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mColorSelectionRecyclerView.setEdgeItemsCenteringEnabled(true);

        mColorSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String[] colors = getResources().getStringArray(R.array.color_array);
        mDigitalColorRecyclerViewAdapter = new DigitalColorRecyclerViewAdapter(colors);
        mColorSelectionRecyclerView.setAdapter(mDigitalColorRecyclerViewAdapter);
    }

    private void updateConfigDataItem(final int backgroundColor) {

        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putInt(
                DigitalWatchFaceUtil.KEY_BACKGROUND_COLOR,
                backgroundColor);

        DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(
                getApplicationContext(),
                configKeysToOverwrite,
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Log.d(TAG, "callback successful for datalayer write");
                        finish();
                        return null;
                    }
                });
    }

    private class DigitalColorRecyclerViewAdapter extends
            RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final String[] mColors;

        public DigitalColorRecyclerViewAdapter(String[] colors) {
            mColors = colors;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

            RecyclerView.ViewHolder viewHolder =
                    new DigitalColorViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.color_config_list_item, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            Log.d(TAG, "Element " + position + " set.");

            String colorName = mColors[position];
            int color = Color.parseColor(colorName);

            DigitalColorViewHolder colorViewHolder = (DigitalColorViewHolder) viewHolder;
            colorViewHolder.setColor(color);
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }

        /**
         * Displays color options for an item on the watch face.
         */
        public class DigitalColorViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener {

            private CircledImageView mColorCircleImageView;

            public DigitalColorViewHolder(final View view) {
                super(view);
                mColorCircleImageView =
                        (CircledImageView) view.findViewById(R.id.color);
                view.setOnClickListener(this);
            }

            public void setColor(int color) {
                mColorCircleImageView.setCircleColor(color);
            }

            @Override
            public void onClick (View view) {
                int position = getAdapterPosition();
                String colorName = mColors[position];
                Integer color = Color.parseColor(colorName);

                Log.d(TAG, "Color: " + color + " onClick() position: " + position);
                updateConfigDataItem(color);
            }
        }
    }
}
