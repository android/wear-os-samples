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

package com.example.android.wearable.jumpingjack;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.android.wearable.jumpingjack.fragments.CounterFragment;
import com.example.android.wearable.jumpingjack.fragments.SettingsFragment;

import java.util.concurrent.TimeUnit;

/**
 * The main activity for the Jumping Jack application. This activity registers itself to receive
 * sensor values.
 *
 * This activity includes a {@link ViewPager} with two pages, one that
 * shows the current count and one that allows user to reset the counter. the current value of the
 * counter is persisted so that upon re-launch, the counter picks up from the last value. At any
 * stage, user can set this counter to 0.
 */
public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider, SensorEventListener {

    private static final String TAG = "MainActivity";

    // An up-down movement that takes more than 2 seconds will not be registered (in nanoseconds).
    private static final long TIME_THRESHOLD_NS = TimeUnit.SECONDS.toNanos(2);

    /**
     * Earth gravity is around 9.8 m/s^2 but user may not completely direct his/her hand vertical
     * during the exercise so we leave some room. Basically, if the x-component of gravity, as
     * measured by the Gravity sensor, changes with a variation delta > 0.03 from the hand down
     * and hand up threshold we define below, we consider that a successful count.
     *
     * This is a very rudimentary formula and is by no means production accurate. You will want to
     * take into account Y and Z gravity changes to get a truly accurate jumping jack.
     *
     * This sample is just meant to show how to easily get sensor values and use them.
     */
    private static final float HAND_DOWN_GRAVITY_X_THRESHOLD = -.040f;
    private static final float HAND_UP_GRAVITY_X_THRESHOLD = -.010f;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mLastTime = 0;
    private int mJumpCounter = 0;
    private boolean mHandDown = true;


    private ViewPager mPager;
    private CounterFragment mCounterPage;
    private SettingsFragment mSettingPage;
    private ImageView mSecondIndicator;
    private ImageView mFirstIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jumping_jack_layout);

        AmbientModeSupport.attach(this);

        setupViews();

        mJumpCounter = Utils.getCounterFromPreference(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void setupViews() {
        mPager = findViewById(R.id.pager);
        mFirstIndicator = findViewById(R.id.indicator_0);
        mSecondIndicator = findViewById(R.id.indicator_1);

        final PagerAdapter adapter = new PagerAdapter(getFragmentManager());

        mCounterPage = new CounterFragment();
        mSettingPage = new SettingsFragment();

        adapter.addFragment(mCounterPage);
        adapter.addFragment(mSettingPage);
        setIndicator(0);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                setIndicator(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully registered for the sensor updates");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unregistered for sensor events");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        detectJump(event.values[0], event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No op.
    }

    /**
     * A very simple algorithm to detect a successful up-down movement of hand(s). The algorithm
     * is based on a delta of the handing being up vs. down and taking less than TIME_THRESHOLD_NS
     * to happen.
     *
     *
     * This algorithm isn't intended to be used in production but just to show what's possible with
     * sensors. You will want to take into account other components (y and z) and other sensors to
     * get a more accurate reading.
     */
    private void detectJump(float xGravity, long timestamp) {

        if ((xGravity <= HAND_DOWN_GRAVITY_X_THRESHOLD)
                || (xGravity >= HAND_UP_GRAVITY_X_THRESHOLD)) {

            if (timestamp - mLastTime < TIME_THRESHOLD_NS) {
                // Hand is down when yValue is negative.
                onJumpDetected(xGravity <= HAND_DOWN_GRAVITY_X_THRESHOLD);
            }

            mLastTime = timestamp;
        }
    }

    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
    private void onJumpDetected(boolean handDown) {
        if (mHandDown != handDown) {
            mHandDown = handDown;

            // Only count when the hand is down (means the hand has gone up, then down).
            if (mHandDown) {
                mJumpCounter++;
                setCounter(mJumpCounter);
            }
        }
    }

    /**
     * Updates the counter on UI, saves it to preferences and vibrates the watch when counter
     * reaches a multiple of 10.
     */
    private void setCounter(int i) {
        mJumpCounter = i;
        mCounterPage.setCounter(i);
        Utils.saveCounterToPreference(this, i);
        if (i > 0 && i % 10 == 0) {
            Utils.vibrate(this, 0);
        }
    }

    public void resetCounter() {
        setCounter(0);
    }

    /**
     * Sets the page indicator for the ViewPager.
     */
    private void setIndicator(int i) {
        switch (i) {
            case 0:
                mFirstIndicator.setImageResource(R.drawable.full_10);
                mSecondIndicator.setImageResource(R.drawable.empty_10);
                break;
            case 1:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
        }
    }


    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    /** Customizes appearance for Ambient mode. (We don't do anything minus default.) */
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
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
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
        }
    }

}
