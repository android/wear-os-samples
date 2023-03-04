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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.MaterialTheme
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Duration between updates while in active mode.
 */
val ACTIVE_INTERVAL: Duration = Duration.ofSeconds(1)

/**
 * Duration between updates while in ambient mode.
 */
val AMBIENT_INTERVAL: Duration = Duration.ofSeconds(10)

const val AMBIENT_UPDATE_ACTION = "com.example.android.wearable.wear.alwayson.action.AMBIENT_UPDATE"

/**
 * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
 * on an interval which we've define.
 */
private val ambientUpdateIntent = Intent(AMBIENT_UPDATE_ACTION)

@Composable
fun AlwaysOnApp(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant,
    clock: Clock,
    activeDispatcher: CoroutineDispatcher
) {
    val ambientUpdateAlarmManager = rememberAlarmManager()

    val context = LocalContext.current

    /**
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
    val ambientUpdatePendingIntent = remember(context) {
        PendingIntent.getBroadcast(
            context,
            0,
            ambientUpdateIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * A ping used to set up a loopback side-effect loop, to continuously update the time.
     */
    var updateDataTrigger by remember { mutableStateOf(0L) }

    /**
     * The current instant to display
     */
    var currentInstant by remember { mutableStateOf(Instant.now(clock)) }

    /**
     * The current time to display
     */
    var currentTime by remember { mutableStateOf(LocalTime.now(clock)) }

    /**
     * The number of times the current time and instant have been updated
     */
    var drawCount by remember { mutableStateOf(0) }

    fun updateData() {
        updateDataTrigger++
        currentInstant = Instant.now(clock)
        currentTime = LocalTime.now(clock)
        drawCount++
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    /**
     * Construct a boolean indicating if we are resumed.
     */
    val isResumed by produceState(initialValue = false) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = true
            try {
                awaitCancellation()
            } finally {
                value = false
            }
        }
    }

    if (isResumed) {
        when (ambientState) {
            is AmbientState.Ambient -> {
                // When we are resumed and ambient, setup the broadcast receiver
                SystemBroadcastReceiver(systemAction = AMBIENT_UPDATE_ACTION) {
                    updateData()
                }
                DisposableEffect(ambientUpdateAlarmManager, ambientUpdatePendingIntent) {
                    onDispose {
                        // Upon leaving resumption or ambient, cancel any ongoing pending intents
                        ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent)
                    }
                }
            }
            AmbientState.Interactive -> Unit
        }

        // Whenever we change ambient state (and initially) update the data.
        LaunchedEffect(ambientState) {
            updateData()
        }

        // Then, setup a ping to refresh data again: either via the alarm manager, or simply
        // after a delay
        LaunchedEffect(updateDataTrigger, ambientState) {
            when (ambientState) {
                is AmbientState.Ambient -> {
                    val triggerTime = currentInstant.getNextInstantWithInterval(
                        AMBIENT_INTERVAL
                    )
                    ambientUpdateAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime.toEpochMilli(),
                        ambientUpdatePendingIntent
                    )
                }
                AmbientState.Interactive -> {
                    val delay = currentInstant.getDelayToNextInstantWithInterval(
                        ACTIVE_INTERVAL
                    )
                    withContext(activeDispatcher) {
                        // Delay on the active dispatcher for testability
                        delay(delay.toMillis())
                    }
                    updateData()
                }
            }
        }
    }

    MaterialTheme {
        AlwaysOnScreen(
            ambientState = ambientState,
            ambientUpdateTimestamp = ambientUpdateTimestamp,
            drawCount = drawCount,
            currentInstant = currentInstant,
            currentTime = currentTime
        )
    }
}

/**
 * Returns the delay from this [Instant] to the next one that is aligned with the given [interval].
 */
private fun Instant.getDelayToNextInstantWithInterval(interval: Duration): Duration =
    Duration.ofMillis(interval.toMillis() - toEpochMilli() % interval.toMillis())

/**
 * Returns the next [Instant] that is aligned with the given [interval].
 */
private fun Instant.getNextInstantWithInterval(interval: Duration): Instant =
    plus(getDelayToNextInstantWithInterval(interval))

@Composable
fun rememberAlarmManager(): AlarmManager {
    val context = LocalContext.current
    return remember(context) {
        context.getSystemService()!!
    }
}

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    // Grab the current context in this part of the UI tree
    val context = LocalContext.current

    // Safely use the latest onSystemEvent lambda passed to the function
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        registerReceiver(context, broadcast, intentFilter, RECEIVER_NOT_EXPORTED)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}
