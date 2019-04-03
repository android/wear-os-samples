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
import android.os.Handler;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.WearableRecyclerView;
import androidx.wear.widget.drawer.WearableActionDrawerView;

import com.example.android.wearable.wear.wearaccessibilityapp.LongListRecyclerViewAdapter.SwitchChangeListener;

import java.util.ArrayList;
import java.util.List;

public class LongListActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    private List<AppItem> mItems;
    private LongListRecyclerViewAdapter mAdapter;
    private Handler mHandler;
    private int mPreviousLastVisibleItem;
    private int mLastVisibleItem;
    private boolean mFinishLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_list);

        AmbientModeSupport.attach(this);

        mHandler = new Handler();

        // List of items to display within recyclerView (used by adapter).
        mItems = new ArrayList<AppItem>();
        mItems.add(new AppItem(null, 0, SampleAppConstants.TITLE, null)); // for title
        mItems.add(new AppItem(null, 0, SampleAppConstants.SWITCH, null)); // for switch widget
        for (int i = 1; i <= 10; i++) {
            mItems.add(
                    new AppItem(
                            getResources().getString(R.string.item_text, i),
                            R.drawable.heart_circle,
                            SampleAppConstants.NORMAL,
                            null));
        }

        // Custom adapter used so we can use custom layout for the rows within the list.
        mAdapter =
                new LongListRecyclerViewAdapter(
                        this,
                        mItems,
                        new SwitchChangeListener() {
                            @Override
                            public void onChange(boolean switchOn) {
                                WearableActionDrawerView wearableActionDrawer =
                                        findViewById(R.id.action_drawer_long_list);

                                if (switchOn) {
                                    wearableActionDrawer.setVisibility(
                                            View.VISIBLE); // Hide drawer.
                                } else {
                                    wearableActionDrawer.setVisibility(
                                            View.INVISIBLE); // Hide drawer.
                                }
                            }
                        });
        WearableRecyclerView recyclerView = findViewById(R.id.recycler_view_long_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        recyclerView.setAdapter(mAdapter); // Set adapter to the recyclerView.

        // Uncomment if you want the action drawer to show while scrolling down.
        // WearableActionDrawer mWearableActionDrawer=
        //         (WearableActionDrawer) findViewById(R.id.action_drawer_long_list);
        // mWearableActionDrawer.setShouldPeekOnScrollDown(true);

        mPreviousLastVisibleItem = 0; // default
        mLastVisibleItem = 0;
        mFinishLoad = false;

        if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            return; // invalid layout manager
        }

        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(
                new WearableRecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        mLastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        if (!mFinishLoad) {
                            int count =
                                    layoutManager
                                            .getItemCount(); // total items in the recycler view

                            // Do not notify LongListActivity if no NEW items have been displayed.
                            if (mLastVisibleItem <= mPreviousLastVisibleItem) {
                                return;
                            }

                            // Do not notify LongListActivity if not yet scrolled to threshold.
                            if (mLastVisibleItem < count - 1) {
                                return;
                            }

                            if (mLastVisibleItem % 10 == 2 && mLastVisibleItem != 2) {
                                return;
                            }

                            // End of list, no more loading.
                            if (mLastVisibleItem >= SampleAppConstants.END_OF_LONG_LIST) {
                                mFinishLoad = true; // we are done loading more items.
                                addFooter();
                                return;
                            }

                            // Load more items
                            addData();

                            // To check if new items should be displayed or not.
                            mPreviousLastVisibleItem = mLastVisibleItem;
                        }
                    }
                });
    }

    /**
     * Add items to List<AppItem> items. More items requested by adapter (used to load list in
     * batches of 10).
     */
    public void addData() {
        // Add progress bar to list.
        mItems.add(new AppItem(null, 0, SampleAppConstants.PROGRESS_BAR, null));
        mAdapter.notifyItemInserted(mItems.size() - 1);

        // Delay for 1000 milliseconds and then execute below code.
        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        // Remove progress after the delay.
                        mItems.remove(mItems.size() - 1);

                        // Add ten items to List<AppItem> items.
                        int listSize = mItems.size();
                        for (int i = listSize - 1; i <= listSize + 8; i++) {
                            mItems.add(
                                    new AppItem(
                                            getResources().getString(R.string.item_text, i),
                                            R.drawable.heart_circle,
                                            SampleAppConstants.NORMAL,
                                            null));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                1000);
    }

    /** Add footer to List<AppItem> items. Requested by adapter (called at the end of the list). */
    public void addFooter() {
        mItems.add(new AppItem(null, 0, SampleAppConstants.HEADER_FOOTER, null)); // add footer
        mAdapter.notifyItemInserted(mItems.size() - 1);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
