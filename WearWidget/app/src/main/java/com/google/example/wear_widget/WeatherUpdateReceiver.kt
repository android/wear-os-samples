/*
 * Copyright 2026 The Android Open Source Project
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
package com.google.example.wear_widget

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.wear.GlanceWearWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// This BroadcastReceiver is used to simulate external updates for the sample.
class WeatherUpdateReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_UPDATE_WEATHER = "com.google.example.wear_widget.UPDATE_WEATHER"
        const val EXTRA_TEMP = "temp"
        const val EXTRA_CONDITION = "condition"

        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    // Suppressed because triggerUpdate is restricted to LIBRARY_GROUP.
    @SuppressLint("RestrictedApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WEATHER) {
            val temp = intent.getIntExtra(EXTRA_TEMP, 72)
            val condition = intent.getStringExtra(EXTRA_CONDITION) ?: "☀️"

            val goAsync = goAsync()
            // In production apps, use an application-scoped coroutine scope injected via DI.
            // Using a shared scope in the companion object here as a compromise for this sample
            // to avoid per-broadcast allocations without adding DI complexity.
            scope.launch {
                try {
                    context.setWeatherState(
                        WeatherState(temp, WeatherCondition.fromEmoji(condition))
                    )
                    val manager = GlanceWearWidgetManager(context)
                    val widget = WeatherWidget()
                    val activeWidgets = manager.fetchActiveWidgets(widget::class)
                    activeWidgets.forEach { handle ->
                        widget.triggerUpdate(context.applicationContext, handle.instanceId)
                    }
                    Log.d("WeatherReceiver", "Pushed weather update: $temp, $condition")
                } catch (e: Exception) {
                    Log.e("WeatherReceiver", "Error updating weather", e)
                } finally {
                    goAsync.finish()
                }
            }
        }
    }
}
