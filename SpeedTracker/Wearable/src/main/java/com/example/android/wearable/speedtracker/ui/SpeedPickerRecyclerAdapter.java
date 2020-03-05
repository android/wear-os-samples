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

package com.example.android.wearable.speedtracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.wearable.speedtracker.R;

/**
 * Populates a RecyclerView with list of speeds.
 */
public class SpeedPickerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Callbacks {
        void speedLimitChange(int speedChange);
    }

    private int[] mDataSet;
    private static Callbacks parentActivitySpeedChangeCallback;

    public SpeedPickerRecyclerAdapter(int[] dataSet, Callbacks callbacks) {
        mDataSet = dataSet;
        parentActivitySpeedChangeCallback = callbacks;
    }

    /**
     * Create new views for list items (invoked by the WearableListView's layout manager)
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.speed_picker_item_layout,
                viewGroup,
                false));
    }

    /**
     * Replaces the contents of a list item. Instead of creating new views, the list tries to
     * recycle existing ones. This is invoked by the WearableListView's layout manager.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {

        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        itemHolder.setTextViewData(mDataSet[position]);
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return mDataSet.length;
    }


    /**
     * Displays all possible speed limit choices.
     */
    public static class ItemViewHolder extends
            RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTextView;
        private int speed = 25;

        ItemViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.name);
            mTextView.setOnClickListener(this);
        }

        void setTextViewData(int value) {
            speed = value;

            String speedString =
                    mTextView.getContext().getString(R.string.speed_for_list, speed);

            mTextView.setText(speedString);
        }

        @Override
        public void onClick(View v) {
            parentActivitySpeedChangeCallback.speedLimitChange(speed);

        }
    }
}
