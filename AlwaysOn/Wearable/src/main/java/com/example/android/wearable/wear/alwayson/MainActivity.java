/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.wear.alwayson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates support for <i>Ambient Mode</i> by attaching ambient mode support to the activity,
 * and listening for ambient mode updates (onEnterAmbient, onUpdateAmbient, and onExitAmbient) via a
 * named AmbientCallback subclass.
 *
 * <p>Also demonstrates how to update the display more frequently than every 60 seconds, which is
 * the default frequency, using an AlarmManager. The Alarm code is only necessary for the custom
 * refresh frequency; it can be ignored for basic ambient mode support where you can simply rely on
 * calls to onUpdateAmbient() by the system.
 *
 * <p>There are two modes: <i>ambient</i> and <i>active</i>. To trigger future display updates, we
 * use a custom Handler for active mode and an Alarm for ambient mode.
 *
 * <p>Why not use just one or the other? Handlers are generally less battery intensive and can be
 * triggered every second. However, they can not wake up the processor (common in ambient mode).
 *
 * <p>Alarms can wake up the processor (what we need for ambient move), but they are less efficient
 * compared to Handlers when it comes to quick update frequencies.
 *
 * <p>Therefore, we use Handler for active mode (can trigger every second and are better on the
 * battery), and we use an Alarm for ambient mode (only need to update once every 10 seconds and
 * they can wake up a sleeping processor).
 *
 * <p>The activity waits 10 seconds between doing any processing (getting data, updating display
 * etc.) while in ambient mode to conserving battery life (processor allowed to sleep). If your app
 * can wait 60 seconds for display updates, you can disregard the Alarm code and simply use
 * onUpdateAmbient() to save even more battery life.
 *
 * <p>As always, you will still want to apply the performance guidelines outlined in the Watch Faces
 * documentation to your app.
 *
 * <p>Finally, in ambient mode, this activity follows the same best practices outlined in the Watch
 * Faces API documentation: keeping most pixels black, avoiding large blocks of white pixels, using
 * only black and white, disabling anti-aliasing, etc.
 */
public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {

    private static final String TAG = "MainActivity";

    /** Custom 'what' for Message sent to Handler. */
    private static final int MSG_UPDATE_SCREEN = 0;

    /** Milliseconds between updates based on state. */
    private static final long ACTIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);

    private static final long AMBIENT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);

    /** Action for updating the display in ambient mode, per our custom refresh cycle. */
    private static final String AMBIENT_UPDATE_ACTION =
            "com.example.android.wearable.wear.alwayson.action.AMBIENT_UPDATE";

    /** Number of pixels to offset the content rendered in the display to prevent screen burn-in. */
    private static final int BURN_IN_OFFSET_PX = 10;

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in ambient
     * mode.
     */
    private AmbientModeSupport.AmbientController mAmbientController;

    /** If the display is low-bit in ambient mode. i.e. it requires anti-aliased fonts. */
    boolean mIsLowBitAmbient;

    /**
     * If the display requires burn-in protection in ambient mode, rendered pixels need to be
     * intermittently offset to avoid screen burn-in.
     */
    boolean mDoBurnInProtection;

    private View mContentView;
    private TextView mTimeTextView;
    private TextView mTimeStampTextView;
    private TextView mStateTextView;
    private TextView mUpdateRateTextView;
    private TextView mDrawCountTextView;

    private final SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private volatile int mDrawCount = 0;

    /**
     * Since the handler (used in active mode) can't wake up the processor when the device is in
     * ambient mode and undocked, we use an Alarm to cover ambient mode updates when we need them
     * more frequently than every minute. Remember, if getting updates once a minute in ambient mode
     * is enough, you can do away with the Alarm code and just rely on the onUpdateAmbient()
     * callback.
     */
    private AlarmManager mAmbientUpdateAlarmManager;

    private PendingIntent mAmbientUpdatePendingIntent;
    private BroadcastReceiver mAmbientUpdateBroadcastReceiver;

    /**
     * This custom handler is used for updates in "Active" mode. We use a separate static class to
     * help us avoid memory leaks.
     */
    private final Handler mActiveModeUpdateHandler = new ActiveModeUpdateHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAmbientController = AmbientModeSupport.attach(this);

        mAmbientUpdateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /*
         * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
         * on an interval which we've define.
         */
        Intent ambientUpdateIntent = new Intent(AMBIENT_UPDATE_ACTION);

        /*
         * Retrieves a PendingIntent that will perform a broadcast. You could also use getActivity()
         * to retrieve a PendingIntent that will start a new activity, but be aware that actually
         * triggers onNewIntent() which causes lifecycle changes (onPause() and onResume()) which
         * might trigger code to be re-executed more often than you want.
         *
         * If you do end up using getActivity(), also make sure you have set activity launchMode to
         * singleInstance in the manifest.
         *
         * Otherwise, it is easy for the AlarmManager launch Intent to open a new activity
         * every time the Alarm is triggered rather than reusing this Activity.
         */
        mAmbientUpdatePendingIntent =
                PendingIntent.getBroadcast(
                        this, 0, ambientUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
         * An anonymous broadcast receiver which will receive ambient update requests and trigger
         * display refresh.
         */
        mAmbientUpdateBroadcastReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        refreshDisplayAndSetNextUpdate();
                    }
                };

        mContentView = findViewById(R.id.content_view);
        mTimeTextView = findViewById(R.id.time);
        mTimeStampTextView = findViewById(R.id.time_stamp);
        mStateTextView = findViewById(R.id.state);
        mUpdateRateTextView = findViewById(R.id.update_rate);
        mDrawCountTextView = findViewById(R.id.draw_count);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        IntentFilter filter = new IntentFilter(AMBIENT_UPDATE_ACTION);
        registerReceiver(mAmbientUpdateBroadcastReceiver, filter);

        refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        unregisterReceiver(mAmbientUpdateBroadcastReceiver);

        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
        mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);
    }

    /**
     * Loads data/updates screen (via method), but most importantly, sets up the next refresh
     * (active mode = Handler and ambient mode = Alarm).
     */
    private void refreshDisplayAndSetNextUpdate() {

        loadDataAndUpdateScreen();

        long timeMs = System.currentTimeMillis();

        if (mAmbientController.isAmbient()) {
            /* Calculate next trigger time (based on state). */
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;

            mAmbientUpdateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, triggerTimeMs, mAmbientUpdatePendingIntent);
        } else {
            /* Calculate next trigger time (based on state). */
            long delayMs = ACTIVE_INTERVAL_MS - (timeMs % ACTIVE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }
    }

    /** Updates display based on Ambient state. If you need to pull data, you should do it here. */
    private void loadDataAndUpdateScreen() {

        mDrawCount += 1;
        long currentTimeMs = System.currentTimeMillis();
        Log.d(
                TAG,
                "loadDataAndUpdateScreen(): "
                        + currentTimeMs
                        + "("
                        + mAmbientController.isAmbient()
                        + ")");

        if (mAmbientController.isAmbient()) {

            mTimeTextView.setText(sDateFormat.format(new Date()));
            mTimeStampTextView.setText(getString(R.string.timestamp_label, currentTimeMs));

            mStateTextView.setText(getString(R.string.mode_ambient_label));
            mUpdateRateTextView.setText(
                    getString(R.string.update_rate_label, (AMBIENT_INTERVAL_MS / 1000)));

            mDrawCountTextView.setText(getString(R.string.draw_count_label, mDrawCount));

        } else {

            mTimeTextView.setText(sDateFormat.format(new Date()));
            mTimeStampTextView.setText(getString(R.string.timestamp_label, currentTimeMs));

            mStateTextView.setText(getString(R.string.mode_active_label));
            mUpdateRateTextView.setText(
                    getString(R.string.update_rate_label, (ACTIVE_INTERVAL_MS / 1000)));

            mDrawCountTextView.setText(getString(R.string.draw_count_label, mDrawCount));
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            mIsLowBitAmbient =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
            mDoBurnInProtection =
                    ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);

            /* Clears Handler queue (only needed for updates in active mode). */
            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);

            /*
             * Following best practices outlined in WatchFaces API (keeping most pixels black,
             * avoiding large blocks of white pixels, using only black and white, and disabling
             * anti-aliasing, etc.)
             */
            mStateTextView.setTextColor(Color.WHITE);
            mUpdateRateTextView.setTextColor(Color.WHITE);
            mDrawCountTextView.setTextColor(Color.WHITE);

            if (mIsLowBitAmbient) {
                mTimeTextView.getPaint().setAntiAlias(false);
                mTimeStampTextView.getPaint().setAntiAlias(false);
                mStateTextView.getPaint().setAntiAlias(false);
                mUpdateRateTextView.getPaint().setAntiAlias(false);
                mDrawCountTextView.getPaint().setAntiAlias(false);
            }

            refreshDisplayAndSetNextUpdate();
        }

        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();

            /*
             * If the screen requires burn-in protection, views must be shifted around periodically
             * in ambient mode. To ensure that content isn't shifted off the screen, avoid placing
             * content within 10 pixels of the edge of the screen.
             *
             * Since we're potentially applying negative padding, we have ensured
             * that the containing view is sufficiently padded (see res/layout/activity_main.xml).
             *
             * Activities should also avoid solid white areas to prevent pixel burn-in. Both of
             * these requirements only apply in ambient mode, and only when this property is set
             * to true.
             */
            if (mDoBurnInProtection) {
                int x = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                int y = (int) (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX);
                mContentView.setPadding(x, y, 0, 0);
            }
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();

            /* Clears out Alarms since they are only used in ambient mode. */
            mAmbientUpdateAlarmManager.cancel(mAmbientUpdatePendingIntent);

            mStateTextView.setTextColor(Color.GREEN);
            mUpdateRateTextView.setTextColor(Color.GREEN);
            mDrawCountTextView.setTextColor(Color.GREEN);

            if (mIsLowBitAmbient) {
                mTimeTextView.getPaint().setAntiAlias(true);
                mTimeStampTextView.getPaint().setAntiAlias(true);
                mStateTextView.getPaint().setAntiAlias(true);
                mUpdateRateTextView.getPaint().setAntiAlias(true);
                mDrawCountTextView.getPaint().setAntiAlias(true);
            }

            /* Reset any random offset applied for burn-in protection. */
            if (mDoBurnInProtection) {
                mContentView.setPadding(0, 0, 0, 0);
            }

            refreshDisplayAndSetNextUpdate();
        }
    }

    /** Handler separated into static class to avoid memory leaks. */
    private static class ActiveModeUpdateHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivityWeakReference;

        ActiveModeUpdateHandler(MainActivity reference) {
            mMainActivityWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity != null) {
                switch (message.what) {
                    case MSG_UPDATE_SCREEN:
                        mainActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }
}
