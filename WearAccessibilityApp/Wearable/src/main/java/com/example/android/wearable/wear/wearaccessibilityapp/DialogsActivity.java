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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.AcceptDenyDialog;
import android.support.wearable.view.WearableDialogHelper.DialogBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DialogsActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    private List<DialogsItem> mItems;
    public View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        AmbientModeSupport.attach(this);

        // Supplier objects
        Supplier<Dialog> twoActionDialog =
                new Supplier<Dialog>() {
                    @Override
                    public Dialog get() {
                        AcceptDenyDialog twoActionDialog =
                                new AcceptDenyDialog(DialogsActivity.this);
                        twoActionDialog.setTitle(R.string.yes_no_dialog);
                        twoActionDialog.setMessage(getString(R.string.yes_no_dialog_description));
                        twoActionDialog.setPositiveButton(
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Add code here for onClick functionality.
                                    }
                                });
                        twoActionDialog.setNegativeButton(
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Add code here for onClick functionality.
                                    }
                                });
                        return twoActionDialog;
                    }
                };

        Supplier<Dialog> oneActionDialog =
                new Supplier<Dialog>() {
                    @Override
                    public Dialog get() {
                        AcceptDenyDialog oneActionDialog =
                                new AcceptDenyDialog(DialogsActivity.this);
                        oneActionDialog.setTitle(R.string.one_action_dialog);
                        oneActionDialog.setMessage(
                                getString(R.string.one_action_dialog_description));
                        oneActionDialog.setPositiveButton(
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Add code here for onClick functionality.
                                    }
                                });
                        return oneActionDialog;
                    }
                };

        Supplier<Dialog> multipleActionDialog =
                new Supplier<Dialog>() {
                    @Override
                    public Dialog get() {
                        DialogBuilder multipleActionBuilder =
                                new DialogBuilder(DialogsActivity.this);
                        multipleActionBuilder.setTitle(R.string.multiple_action_dialog);
                        multipleActionBuilder.setMessage(
                                R.string.multiple_action_dialog_description);

                        // OK option.
                        multipleActionBuilder.setPositiveIcon(R.drawable.accept_circle);
                        multipleActionBuilder.setPositiveButton(
                                R.string.ok,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Add code here for onClick functionality.
                                    }
                                });

                        // Close option.
                        multipleActionBuilder.setNeutralIcon(R.drawable.deny_circle);
                        multipleActionBuilder.setNeutralButton(
                                R.string.close,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Add code here for onClick functionality.
                                    }
                                });

                        // Open in phone option.
                        multipleActionBuilder.setNegativeIcon(R.drawable.open_in_phone_circle);
                        multipleActionBuilder.setNegativeButton(
                                R.string.open_on_phone,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent =
                                                new Intent(
                                                        DialogsActivity.this,
                                                        OpenOnPhoneAnimationActivity.class);
                                        startActivity(intent);
                                    }
                                });
                        return multipleActionBuilder.create();
                    }
                };

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new DialogsItem(R.string.yes_no_action, twoActionDialog));
        mItems.add(new DialogsItem(R.string.one_action, oneActionDialog));
        mItems.add(new DialogsItem(R.string.multiple_actions, multipleActionDialog));

        // Initialize an adapter and set it to ListView mListView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_dialogs);
        listView.setAdapter(adapter);

        // Set header of mListView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title_layout, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.dialogs);
        titleView.setOnClickListener(null); // make title non-clickable (will not turn grey)

        listView.addHeaderView(titleView);

        // Goes to a dialog when you click on one of the list items.
        // Dependent upon position of click.
        // Note: Keep in mind that icons will not appear on dialog unless you
        //       set an onClickListener.
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        mView = view;
                        Dialog dialog =
                                mItems.get(position - listView.getHeaderViewsCount())
                                        .getSupplier()
                                        .get();
                        dialog.show();
                    }
                });
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
