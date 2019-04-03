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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.util.ArrayList;
import java.util.List;

public class ListOfGraphicsActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {
    private List<AppItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_graphics);

        AmbientModeSupport.attach(this);

        // Store all data in a list for adapter to use.
        mItems = new ArrayList<AppItem>();
        mItems.add(
                new AppItem(
                        getString(R.string.photo_carousel),
                        R.drawable.heart_circle,
                        SampleAppConstants.NORMAL,
                        PhotoCarouselActivity.class));
        mItems.add(
                new AppItem(
                        getString(R.string.images),
                        R.drawable.heart_circle,
                        SampleAppConstants.NORMAL,
                        ImagesActivity.class));

        // Set up an adapter and pass in all the items you initialized above.
        AppItemListViewAdapter adapter = new AppItemListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_graphics);
        listView.setAdapter(adapter);

        // Set header of list view to be a title.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title_layout, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.list_of_graphics); // Set the text of the title.
        listView.addHeaderView(titleView, getString(R.string.title), false); // Set header.

        // Goes to a new screen when you click on one of the list items.
        // Dependent upon position of click.
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        mItems.get(position - listView.getHeaderViewsCount())
                                .launchActivity(getApplicationContext());
                    }
                });
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
