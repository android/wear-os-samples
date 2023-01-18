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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

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
 * Alarms can wake up the processor (what we need for ambient mode), but they are less efficient
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

    private val ambientCallbackState = AmbientCallbackState()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AmbientModeSupport.attach(this)

        setContent {
            AlwaysOnApp(
                ambientState = ambientCallbackState.ambientState,
                ambientUpdateTimestamp = ambientCallbackState.ambientUpdateTimestamp,
                clock = clock,
                activeDispatcher = activeDispatcher
            )
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = ambientCallbackState
}

private class AmbientCallbackState : AmbientModeSupport.AmbientCallback() {

    /**
     * A ticker state that increase whenever we get a call to `onUpdateAmbient`
     */
    var ambientUpdateTimestamp by mutableStateOf(Instant.now(clock))

    /**
     * The current [AmbientState].
     */
    var ambientState by mutableStateOf<AmbientState>(AmbientState.Interactive)

    /**
     * Prepares the UI for ambient mode.
     */
    override fun onEnterAmbient(ambientDetails: Bundle) {
        super.onEnterAmbient(ambientDetails)
        val isLowBitAmbient = ambientDetails.getBoolean(
            AmbientModeSupport.EXTRA_LOWBIT_AMBIENT,
            false
        )
        val doBurnInProtection = ambientDetails.getBoolean(
            AmbientModeSupport.EXTRA_BURN_IN_PROTECTION,
            false
        )

        ambientState = AmbientState.Ambient(
            isLowBitAmbient = isLowBitAmbient,
            doBurnInProtection = doBurnInProtection
        )
    }

    /**
     * Updates the display in ambient mode on the standard interval. Since we're using a custom
     * refresh cycle, this method does NOT update the data in the display. Rather, this method
     * simply updates the positioning of the data in the screen to avoid burn-in, if the display
     * requires it.
     */
    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
        ambientUpdateTimestamp = Instant.now(clock)
    }

    /**
     * Restores the UI to active (non-ambient) mode.
     */
    override fun onExitAmbient() {
        super.onExitAmbient()
        ambientState = AmbientState.Interactive
    }
}
