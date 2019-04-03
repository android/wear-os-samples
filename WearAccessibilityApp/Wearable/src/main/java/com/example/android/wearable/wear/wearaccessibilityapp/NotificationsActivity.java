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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.MessagingStyle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.android.wearable.wear.wearaccessibilityapp.util.NotificationUtil;

public class NotificationsActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    public static final int NOTIFICATION_ID = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AmbientModeSupport.attach(this);

        // Display the fragment as the main content.
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new NotificationsPrefsFragment())
                .commit();
    }

    public static class NotificationsPrefsFragment extends PreferenceFragment {

        private static final String TAG = "NotificationsActivity";
        private NotificationManagerCompat mNotificationManagerCompat;
        private boolean mActionOn; // if true, displays in-line action
        private boolean mAvatarOn; // if true, displays avatar of messenger

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_notifications);

            mNotificationManagerCompat = NotificationManagerCompat.from(getActivity());

            final SwitchPreference mActionSwitchPref =
                    (SwitchPreference) findPreference(getString(R.string.key_pref_action));
            final SwitchPreference mAvatarSwitchPref =
                    (SwitchPreference) findPreference(getString(R.string.key_pref_avatar));
            Preference mPushNotificationPref =
                    findPreference(getString(R.string.key_pref_push_notification));

            initInLineAction(mActionSwitchPref);
            initAvatar(mAvatarSwitchPref);
            initPushNotification(mPushNotificationPref);
        }

        public void initInLineAction(SwitchPreference switchPref) {
            switchPref.setChecked(true);
            mActionOn = switchPref.isChecked();
            switchPref.setOnPreferenceChangeListener(
                    new OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            mActionOn = (Boolean) newValue;
                            return true;
                        }
                    });
        }

        public void initAvatar(SwitchPreference switchPref) {
            switchPref.setChecked(true);
            mAvatarOn = switchPref.isChecked();
            switchPref.setOnPreferenceChangeListener(
                    new OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            mAvatarOn = (Boolean) newValue;
                            return true;
                        }
                    });
        }

        public void initPushNotification(Preference pref) {
            pref.setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            generateMessagingStyleNotification(getContext());
                            return true;
                        }
                    });
        }

        /*
         * Generates a MESSAGING_STYLE Notification that supports both Wear 1.+ and Wear 2.0. For
         * devices on API level 24 (Wear 2.0) and after, displays MESSAGING_STYLE. Otherwise,
         * displays a basic BIG_TEXT_STYLE.
         *
         * IMPORTANT NOTE:
         * Notification Styles behave slightly different on Wear 2.0 when they are launched by a
         * native/local Wear app, i.e., they will NOT expand when the user taps them but will
         * instead take the user directly into the local app for the richest experience. In
         * contrast, a bridged Notification launched from the phone will expand with the style
         * details (whether there is a local app or not).
         *
         * If you want to enable an action on your Notification without launching the app, you can
         * do so with the setHintDisplayActionInline() feature (shown below), but this only allows
         * one action.
         *
         * If you wish to replicate the original experience of a bridged notification, please
         * review the generateBigTextStyleNotification() method above to see how.
         */
        private void generateMessagingStyleNotification(Context context) {
            Log.d(TAG, "generateMessagingStyleNotification()");

            // Main steps for building a MESSAGING_STYLE notification:
            //      0. Get your data
            //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
            //      2. Build the MESSAGING_STYLE
            //      3. Set up main Intent for notification
            //      4. Set up RemoteInput (users can input directly from notification)
            //      5. Build and issue the notification

            // 0. Get your data (everything unique per Notification).
            MockDatabase.MessagingStyleCommsAppData messagingStyleCommsAppData =
                    MockDatabase.getMessagingStyleData(getContext());

            // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
            String notificationChannelId = NotificationUtil.createNotificationChannel(
                    getContext(),
                    messagingStyleCommsAppData);


            // 2. Build the Notification.Style (MESSAGING_STYLE).
            String contentTitle = messagingStyleCommsAppData.getContentTitle();

            MessagingStyle messagingStyle =
                    new NotificationCompat.MessagingStyle(messagingStyleCommsAppData.getMe())
                            /*
                             * <p>This API's behavior was changed in SDK version
                             * {@link Build.VERSION_CODES#P}. If your application's target version
                             * is less than {@link Build.VERSION_CODES#P}, setting a conversation
                             * title to a non-null value will make {@link #isGroupConversation()}
                             * return {@code true} and passing {@code null} will make it return
                             * {@code false}.
                             * This behavior can be overridden by calling
                             * {@link #setGroupConversation(boolean)} regardless of SDK version.
                             * In {@code P} and above, this method does not affect group
                             * conversation settings.
                             *
                             * In our case, we use the same title.
                             */
                            .setConversationTitle(contentTitle);

            // Adds all Messages.
            // Note: Messages include the text, timestamp, and sender.
            for (MessagingStyle.Message message : messagingStyleCommsAppData.getMessages()) {
                messagingStyle.addMessage(message);
            }

            messagingStyle.setGroupConversation(messagingStyleCommsAppData.isGroupConversation());

            // 3. Set up main Intent for notification.
            Intent notifyIntent = new Intent(getActivity(), MessagingMainActivity.class);

            PendingIntent mainPendingIntent =
                    PendingIntent.getActivity(
                            getActivity(),
                            0,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // 4. Set up a RemoteInput Action, so users can input (keyboard, drawing, voice)
            // directly from the notification without entering the app.

            // Create the RemoteInput specifying this key.
            String replyLabel = getString(R.string.reply_label);
            RemoteInput remoteInput = new RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
                    .setLabel(replyLabel)
                    // Use machine learning to create responses based on previous messages.
                    .setChoices(messagingStyleCommsAppData.getReplyChoicesBasedOnLastMessage())
                    .build();

            // Create PendingIntent for service that handles input.
            Intent replyIntent = new Intent(getActivity(), MessagingIntentService.class);
            replyIntent.setAction(MessagingIntentService.ACTION_REPLY);
            PendingIntent replyActionPendingIntent =
                    PendingIntent.getService(getActivity(), 0, replyIntent, 0);

            // Enable action to appear inline on Wear 2.0 (24+). This means it will appear over the
            // lower portion of the Notification for easy action (only possible for one action).
            final NotificationCompat.Action.WearableExtender inlineActionForWear2 =
                    new NotificationCompat.Action.WearableExtender()
                            .setHintDisplayActionInline(true)
                            .setHintLaunchesActivity(false);

            NotificationCompat.Action replyAction =
                    new NotificationCompat.Action.Builder(
                            R.drawable.reply,
                            replyLabel,
                            replyActionPendingIntent)
                            .addRemoteInput(remoteInput)
                            // Informs system we aren't bringing up our own custom UI for a reply
                            // action.
                            .setShowsUserInterface(false)
                            // Allows system to generate replies by context of conversation.
                            .setAllowGeneratedReplies(true)
                            // Add WearableExtender to enable inline actions.
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                            .extend(inlineActionForWear2)
                            .build();


            // 5. Build and issue the notification.

            // Because we want this to be a new notification (not updating current notification), we
            // create a new Builder. Later, we update this same notification, so we need to save
            // this Builder globally (as outlined earlier).

            // Notification Channel Id is ignored for Android pre O (26).
            NotificationCompat.Builder notificationCompatBuilder =
                    new NotificationCompat.Builder(
                            context, notificationChannelId);

            GlobalNotificationBuilder.setNotificationCompatBuilderInstance(
                    notificationCompatBuilder);

            notificationCompatBuilder
                    // MESSAGING_STYLE sets title and content for Wear 1.+ and Wear 2.0 devices.
                    .setStyle(messagingStyle)
                    .setContentTitle(contentTitle)
                    .setContentText(messagingStyleCommsAppData.getContentText())
                    .setSmallIcon(R.drawable.watch)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            getResources(),
                            mAvatarOn ? R.drawable.avatar : R.drawable.watch))
                    .setContentIntent(mainPendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    // Set primary color (important for Wear 2.0 Notifications).
                    .setColor(ContextCompat.getColor(context, R.color.background))

                    // Number of new notifications for API <24 (Wear 1.+) devices.
                    .setSubText(
                            Integer.toString(messagingStyleCommsAppData.getNumberOfNewMessages()))
                    .addAction(replyAction)
                    .setCategory(Notification.CATEGORY_MESSAGE)

                    // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated
                    // for 'importance' which is set in the NotificationChannel. The integers
                    // representing 'priority' are different from 'importance', so make sure you
                    // don't mix them.
                    .setPriority(messagingStyleCommsAppData.getPriority())

                    // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                    // visibility is set in the NotificationChannel.
                    .setVisibility(messagingStyleCommsAppData.getChannelLockscreenVisibility());

            // If the phone is in "Do not disturb" mode, the user may still be notified if the
            // sender(s) are in a group allowed through "Do not disturb" by the user.
            for (Person person : messagingStyleCommsAppData.getParticipants()) {
                notificationCompatBuilder.addPerson(person.getUri());
            }

            Notification notification = notificationCompatBuilder.build();
            mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);

            // Close app to demonstrate notification in steam.
            getActivity().finish();
        }

        private String createNotificationChannel(
                Context context, MockDatabase.MessagingStyleCommsAppData mockNotificationData) {

            // NotificationChannels are required for Notifications on O (API 26) and above.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // The id of the channel.
                String channelId = mockNotificationData.getChannelId();

                // The user-visible name of the channel.
                CharSequence channelName = mockNotificationData.getChannelName();
                // The user-visible description of the channel.
                String channelDescription = mockNotificationData.getChannelDescription();
                int channelImportance = mockNotificationData.getChannelImportance();
                boolean channelEnableVibrate = mockNotificationData.isChannelEnableVibrate();
                int channelLockscreenVisibility =
                        mockNotificationData.getChannelLockscreenVisibility();

                // Initializes NotificationChannel.
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, channelName, channelImportance);
                notificationChannel.setDescription(channelDescription);
                notificationChannel.enableVibration(channelEnableVibrate);
                notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);

                // Adds NotificationChannel to system. Attempting to create an existing notification
                // channel with its original values performs no operation, so it's safe to perform
                // the below sequence.
                NotificationManager notificationManager =
                        (NotificationManager)
                                context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);

                return channelId;
            } else {
                // Returns null for pre-O (26) devices.
                return null;
            }
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
