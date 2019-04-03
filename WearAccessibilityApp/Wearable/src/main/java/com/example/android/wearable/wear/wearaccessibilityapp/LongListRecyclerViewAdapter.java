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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

public class LongListRecyclerViewAdapter
        extends RecyclerView.Adapter<LongListRecyclerViewAdapter.Holder> {

    // For custom listener.
    public interface SwitchChangeListener {

        void onChange(boolean switchOn);
    }

    private final LayoutInflater mInflater;
    private List<AppItem> mItems;
    private SwitchChangeListener mSwitchChangeListener;
    private Switch mSwitchWidget;
    private Context mContext;

    public LongListRecyclerViewAdapter(
            Context context, List<AppItem> items, SwitchChangeListener switchChangeListener) {
        mContext = context;
        mItems = items;
        mInflater = LayoutInflater.from(context);

        // Forces activity to implement a SwitchChangeListener
        mSwitchChangeListener = switchChangeListener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case SampleAppConstants.TITLE:
                // Programmatically set the text of the title here.
                view = mInflater.inflate(R.layout.title_layout, parent, false);
                TextView titleView = view.findViewById(R.id.title_text);
                titleView.setText(R.string.a_long_list);
                break;
            case SampleAppConstants.SWITCH:
                // Reference the switch widget's text and view.
                view = mInflater.inflate(R.layout.long_list_switch_widget_layout, parent, false);
                TextView switchText = view.findViewById(R.id.switch_text);
                switchText.setText(R.string.bottom_action_drawer);

                mSwitchWidget = view.findViewById(R.id.switch_widget);

                view.setContentDescription(
                        mContext.getResources()
                                .getString(
                                        R.string.switch_bottom_action_drawer,
                                        getSwitchToggleString(mSwitchWidget.isChecked())));

                // Set the OnClickListener (Observer pattern used here).
                view.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mSwitchWidget.setChecked(!(mSwitchWidget.isChecked()));
                                if (mSwitchChangeListener != null) {
                                    mSwitchChangeListener.onChange(mSwitchWidget.isChecked());
                                }
                                view.setContentDescription(
                                        mContext.getResources()
                                                .getString(
                                                        R.string.switch_bottom_action_drawer,
                                                        getSwitchToggleString(
                                                                mSwitchWidget.isChecked())));
                            }
                        });
                break;
            case SampleAppConstants.HEADER_FOOTER:
                view = mInflater.inflate(R.layout.header_footer_layout, parent, false);
                break;
            case SampleAppConstants.PROGRESS_BAR:
                view = mInflater.inflate(R.layout.progress_bar_layout, parent, false);
                break;
            case SampleAppConstants.NORMAL:
                view = mInflater.inflate(R.layout.shifted_app_item_layout, parent, false);
                break;
            default:
                view = mInflater.inflate(R.layout.shifted_app_item_layout, parent, false);
                break;
        }
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (mItems.isEmpty()) {
            return;
        }

        AppItem item = mItems.get(position);
        int itemViewType = item.getViewType();

        // Return - Don't want to bind AppItem item info because item is null.
        if (itemViewType != SampleAppConstants.NORMAL) {
            return;
        }

        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    /**
     * Used to set Switch widget's content description dynamically.
     *
     * @param isChecked
     * @return "on" if true, "off" otherwise
     */
    public String getSwitchToggleString(boolean isChecked) {
        return isChecked ? mContext.getString(R.string.on) : mContext.getString(R.string.off);
    }

    // class-specific ViewHolder
    static class Holder extends ViewHolder {
        TextView mTextView;
        ImageView mImageView;

        public Holder(final View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.shifted_icon_text_view);
            mImageView = itemView.findViewById(R.id.shifted_icon_image_view);
        }

        /** Bind appItem info to main screen (displays the item). */
        public void bind(AppItem item) {
            mTextView.setText(item.getItemName());
            mImageView.setImageResource(item.getImageId());
        }
    }
}
