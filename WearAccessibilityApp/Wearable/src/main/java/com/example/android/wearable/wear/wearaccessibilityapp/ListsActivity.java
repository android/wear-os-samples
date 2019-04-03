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

public class ListsActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {
    private List<ListsItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        AmbientModeSupport.attach(this);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(R.string.a_long_list, LongListActivity.class));
        mItems.add(new ListsItem(R.string.list_of_graphics, ListOfGraphicsActivity.class));

        // Initialize an adapter and set it to ListView listView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);

        // Set header of listView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title_layout, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.lists);
        titleView.setOnClickListener(null); // make title non-clickable.

        listView.addHeaderView(titleView);

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
