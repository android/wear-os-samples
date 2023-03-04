/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.alwayson

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
import com.example.android.wearable.wear.alwayson.databinding.ActivityMainBinding
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * IMPORTANT NOTE: Most apps shouldn't use always on ambient mode, as it drains battery life. Unless
 * required, it's much better to allow the system to take over after the user stops interacting
 * with your app.
 *
 * Demonstrates support for *Ambient Mode* by attaching ambient mode support to the activity,
 * and listening for ambient mode updates (onEnterAmbient, onUpdateAmbient, and onExitAmbient) via a
 * named AmbientCallback subclass.
 *
 * Also demonstrates how to update the display more frequently than every 60 seconds, which is
 * the default frequency, using an AlarmManager. The Alarm code is only necessary for the custom
 * refresh frequency; it can be ignored for basic ambient mode support where you can simply rely on
 * calls to onUpdateAmbient() by the system.
 *
 * There are two modes: *ambient* and *active*. To trigger future display updates, we
 * use coroutines for active mode and an Alarm for ambient mode.
 *
 * Why not use just one or the other? Coroutines are generally less battery intensive and can be
 * triggered every second. However, they can not wake up the processor (common in ambient mode).
 *
 * Alarms can wake up the processor (what we need for ambient move), but they are less efficient
 * compared to coroutines when it comes to quick update frequencies.
 *
 * Therefore, we use coroutines for active mode (can trigger every second and are better on the
 * battery), and we use an Alarm for ambient mode (only need to update once every 10 seconds and
 * they can wake up a sleeping processor).
 *
 * The activity waits 10 seconds between doing any processing (getting data, updating display
 * etc.) while in ambient mode to conserving battery life (processor allowed to sleep). If your app
 * can wait 60 seconds for display updates, you can disregard the Alarm code and simply use
 * onUpdateAmbient() to save even more battery life.
 *
 * As always, you will still want to apply the performance guidelines outlined in the Watch Faces
 * documentation to your app.
 *
 * Finally, in ambient mode, this activity follows the same best practices outlined in the Watch
 * Faces API documentation: keeping most pixels black, avoiding large blocks of white pixels, using
 * only black and white, disabling anti-aliasing, etc.
 */
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var binding: ActivityMainBinding

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in ambient
     * mode.
     */
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    /**
     * Since the coroutine-based update (used in active mode) can't wake up the processor when the
     * device is in ambient mode and undocked, we use an Alarm to cover ambient mode updates when we
     * need them more frequently than every minute. Remember, if getting updates once a minute in
     * ambient mode is enough, you can do away with the Alarm code and just rely on the
     * onUpdateAmbient() callback.
     */
    private lateinit var ambientUpdateAlarmManager: AlarmManager
    private lateinit var ambientUpdatePendingIntent: PendingIntent
    private lateinit var ambientUpdateBroadcastReceiver: BroadcastReceiver

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)

    @Volatile
    private var drawCount = 0

    /**
     * The [Job] associated with the updates performed while in active mode.
     */
    private var activeUpdateJob: Job = Job().apply { complete() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ambientController = AmbientModeSupport.attach(this)
        ambientUpdateAlarmManager = getSystemService()!!

        /*
         * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
         * on an interval which we've define.
         */
        val ambientUpdateIntent = Intent(AMBIENT_UPDATE_ACTION)

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
        ambientUpdatePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            ambientUpdateIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*
         * An anonymous broadcast receiver which will receive ambient update requests and trigger
         * display refresh.
         */
        ambientUpdateBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refreshDisplayAndSetNextUpdate()
            }
        }
    }

    public override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        val filter = IntentFilter(AMBIENT_UPDATE_ACTION)
        registerReceiver(
            this,
            ambientUpdateBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        refreshDisplayAndSetNextUpdate()
    }

    public override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        unregisterReceiver(ambientUpdateBroadcastReceiver)
        activeUpdateJob.cancel()
        ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent)
    }

    /**
     * Loads data/updates screen (via method), but most importantly, sets up the next refresh
     * (active mode = coroutines and ambient mode = Alarm).
     */
    private fun refreshDisplayAndSetNextUpdate() {
        loadDataAndUpdateScreen()
        val instant = Instant.now(clock)
        if (ambientController.isAmbient) {
            val triggerTime = instant.getNextInstantWithInterval(AMBIENT_INTERVAL)
            ambientUpdateAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime.toEpochMilli(),
                ambientUpdatePendingIntent
            )
        } else {
            val delay = instant.getDelayToNextInstantWithInterval(ACTIVE_INTERVAL)
            activeUpdateJob.cancel()
            activeUpdateJob = lifecycleScope.launch {
                withContext(activeDispatcher) {
                    // Delay on the active dispatcher for testability
                    delay(delay.toMillis())
                }

                refreshDisplayAndSetNextUpdate()
            }
        }
    }

    /**
     * Returns the delay from this [Instant] to the next one that is aligned with the given
     * [interval].
     */
    private fun Instant.getDelayToNextInstantWithInterval(interval: Duration): Duration =
        Duration.ofMillis(interval.toMillis() - toEpochMilli() % interval.toMillis())

    /**
     * Returns the next [Instant] that is aligned with the given [interval].
     */
    private fun Instant.getNextInstantWithInterval(interval: Duration): Instant =
        plus(getDelayToNextInstantWithInterval(interval))

    /**
     * Updates display based on Ambient state. If you need to pull data, you should do it here.
     */
    private fun loadDataAndUpdateScreen() {
        drawCount += 1

        val currentInstant = Instant.now(clock)

        Log.d(
            TAG,
            "loadDataAndUpdateScreen(): " +
                "${currentInstant.toEpochMilli()} (${ambientController.isAmbient})"
        )

        val currentTime = LocalTime.now(clock)

        binding.time.text = dateFormat.format(currentTime)
        binding.timeStamp.text = getString(R.string.timestamp_label, currentInstant.toEpochMilli())
        binding.state.text = getString(
            if (ambientController.isAmbient) {
                R.string.mode_ambient_label
            } else {
                R.string.mode_active_label
            }
        )
        binding.updateRate.text = getString(
            R.string.update_rate_label,
            if (ambientController.isAmbient) {
                AMBIENT_INTERVAL.seconds
            } else {
                ACTIVE_INTERVAL.seconds
            }
        )
        binding.drawCount.text = getString(R.string.draw_count_label, drawCount)
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {

        /**
         * If the display is low-bit in ambient mode. i.e. it requires anti-aliased fonts.
         */
        private var isLowBitAmbient = false

        /**
         * If the display requires burn-in protection in ambient mode, rendered pixels need to be
         * intermittently offset to avoid screen burn-in.
         */
        private var doBurnInProtection = false

        /**
         * Prepares the UI for ambient mode.
         */
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
            isLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false)
            doBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false)

            // Cancel any active updates
            activeUpdateJob.cancel()

            /*
             * Following best practices outlined in WatchFaces API (keeping most pixels black,
             * avoiding large blocks of white pixels, using only black and white, and disabling
             * anti-aliasing, etc.)
             */
            binding.state.setTextColor(Color.WHITE)
            binding.updateRate.setTextColor(Color.WHITE)
            binding.drawCount.setTextColor(Color.WHITE)
            if (isLowBitAmbient) {
                binding.time.paint.isAntiAlias = false
                binding.timeStamp.paint.isAntiAlias = false
                binding.state.paint.isAntiAlias = false
                binding.updateRate.paint.isAntiAlias = false
                binding.drawCount.paint.isAntiAlias = false
            }
            refreshDisplayAndSetNextUpdate()
        }

        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        override fun onUpdateAmbient() {
            super.onUpdateAmbient()

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
            if (doBurnInProtection) {
                binding.container.translationX =
                    Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
                binding.container.translationY =
                    Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
            }
        }

        /**
         * Restores the UI to active (non-ambient) mode.
         */
        override fun onExitAmbient() {
            super.onExitAmbient()

            /* Clears out Alarms since they are only used in ambient mode. */
            ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent)
            binding.state.setTextColor(Color.GREEN)
            binding.updateRate.setTextColor(Color.GREEN)
            binding.drawCount.setTextColor(Color.GREEN)
            if (isLowBitAmbient) {
                binding.time.paint.isAntiAlias = true
                binding.timeStamp.paint.isAntiAlias = true
                binding.state.paint.isAntiAlias = true
                binding.updateRate.paint.isAntiAlias = true
                binding.drawCount.paint.isAntiAlias = true
            }

            /* Reset any random offset applied for burn-in protection. */
            if (doBurnInProtection) {
                binding.container.translationX = 0f
                binding.container.translationY = 0f
            }
            refreshDisplayAndSetNextUpdate()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Duration between updates while in active mode.
         */
        private val ACTIVE_INTERVAL = Duration.ofSeconds(1)

        /**
         * Duration between updates while in ambient mode.
         */
        private val AMBIENT_INTERVAL = Duration.ofSeconds(10)

        /**
         * Action for updating the display in ambient mode, per our custom refresh cycle.
         */
        const val AMBIENT_UPDATE_ACTION =
            "com.example.android.wearable.wear.alwayson.action.AMBIENT_UPDATE"

        /**
         * Number of pixels to offset the content rendered in the display to prevent screen burn-in.
         */
        private const val BURN_IN_OFFSET_PX = 10
    }
}

/**
 * The [Clock] driving the time information. Overridable only for testing.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal var clock: Clock = Clock.systemDefaultZone()

/**
 * The dispatcher used for delaying in active mode. Overridable only for testing.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal var activeDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
