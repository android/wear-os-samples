/*
* Copyright (C) 2020 The Android Open Source Project
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
package com.example.android.wearable.watchfacekotlin.watchface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.view.SurfaceHolder
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.InternalCoroutinesApi

/**
 * Demonstrates two simple complications plus a background complication in an analog watch face.
 *
 * While this class mostly just handles callbacks and triggering the onDraw() (via the invalidate()
 * method), the renderer [AnalogWatchFaceRenderer] does the heavy work of actually drawing on the
 * canvas.
 *
 * Also, the watch face style class is important as well, as it covers the look/feel of the watch
 * face via colors and dimensions.
 *
 * The watch face also links to a settings activity that previews the watch face and allows you to
 * adjust colors, complication data, etc.
 */
class AnalogComplicationWatchFaceService : CanvasWatchFaceService() {
    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine internal constructor() :
        CanvasWatchFaceService.Engine(true) {

        // Calendar and time zone receiver are used to listen for any time zone changes in the
        // settings, etc., so the time is updated and stays accurate.
        private var calendar = Calendar.getInstance()
        private var registeredTimeZoneReceiver = false

        // Renders watch face (background, hands, ticks, complications, etc.).
        // NOTE: The context is required by [AnalogWatchFace] to initiate [ComplicationDrawables]
        // and for loading user preferences for the watch face.
        private val analogWatchFaceRender = AnalogWatchFaceRenderer(
            AnalogWatchFace(applicationContext),
            AnalogWatchFaceRenderer.WatchFaceRendererListener {
                // Draw request triggered from the renderer (usually because it requires an
                // animation that can't wait for the next automated call to onDraw()). A good
                // example is the second hand animation while the watch face is in its active mode.
                // Calling invalidate() triggers onDraw() from the system.
                invalidate()
            }
        )

        // Required in case the time zone is changed outside the watch face to keep accurate time.
        private val timeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            Log.d(TAG, "onCreate")
            super.onCreate(holder)

            // This is the system watch face style which only covers a couple basic settings
            // (accepts tap events, hiding/showing indicators, etc.). The custom class in this
            // project, [AnalogWatchFace], handles all detailed visual styling (color
            // styles, dimensions, etc.) and state.
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@AnalogComplicationWatchFaceService)
                    .setAcceptsTapEvents(true)
                    // Hidden because we draw our own.
                    .setHideNotificationIndicator(true)
                    .build()
            )

            // System requires we set all active complication ids. These are unique numbers we
            // define for each complication location on our watch face. They are defined in
            // [AnalogWatchFace] and used by [AnalogWatchFaceRenderer] to render complications.
            setActiveComplications(*AnalogWatchFace.complicationIds)
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy()")
            analogWatchFaceRender.tearDown()
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            Log.d(TAG, "onPropertiesChanged: properties = $properties")
            super.onPropertiesChanged(properties)

            // Only need to determine low bit mode and burn in protection to determine the color
            // of the background.
            analogWatchFaceRender.setLowBitAndBurnInProtection(properties)
        }

        /*
         * Called when there is updated data for a complication id.
         */
        override fun onComplicationDataUpdate(
            complicationId: Int,
            complicationData: ComplicationData
        ) {
            Log.d(TAG, "onComplicationDataUpdate() id: $complicationId")

            analogWatchFaceRender.updateWatchFaceComplication(
                complicationId,
                complicationData
            )
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            Log.d(TAG, "OnTapCommand()")

            analogWatchFaceRender.checkTapLocation(tapType, x, y, eventTime)
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == INTERRUPTION_FILTER_NONE

            analogWatchFaceRender.toggleDimMode(inMuteMode)
            invalidate()
        }

        @InternalCoroutinesApi
        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            Log.d(TAG, "onAmbientModeChanged: $inAmbientMode")
            analogWatchFaceRender.ambient = inAmbientMode
        }

        /*
         * This is one of the more important callbacks. It is called when we get the dimensions of
         * the watch face, that is, the dimensions of the Wear OS device.
         * We use this to set the dimensions for all [AnalogWatchFace] elements, as we don't want
         * to compute this every time we draw the watch face.
         */
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            analogWatchFaceRender.calculateWatchFaceDimensions(holder, format, width, height)
        }

        /**
         * Triggered by the system or by invalidate() calls.
         */
        override fun onDraw(canvas: Canvas, bounds: Rect) {
            analogWatchFaceRender.render(canvas, bounds, calendar)
        }

        @InternalCoroutinesApi
        override fun onVisibilityChanged(visible: Boolean) {
            Log.d(TAG, "onVisibilityChanged(): $visible")
            super.onVisibilityChanged(visible)

            // visible = loads user styles, preps complications and animations.
            // not visible = shuts down animations.
            analogWatchFaceRender.visible = visible

            if (visible) {
                // Update time zone in case it changed while we weren't visible.
                calendar.timeZone = TimeZone.getDefault()

                // Register for any time zone changes while the watch face is active, so
                // the time stays accurate despite the user's location.
                registerTimeZoneChangedReceiver()

                // Since the watch face is visible, we need to draw it again.
                invalidate()
            } else {
                unregisterTimeZoneChangedReceiver()
            }
        }

        override fun onUnreadCountChanged(count: Int) {
            Log.d(TAG, "onUnreadCountChanged(): $count")

            // Renderer will trigger draw if the user prefers a visual notification indicator.
            analogWatchFaceRender.numberOfUnreadNotifications = count
        }

        private fun registerTimeZoneChangedReceiver() {
            if (registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            registerReceiver(timeZoneReceiver, filter)
        }

        private fun unregisterTimeZoneChangedReceiver() {
            if (!registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = false
            unregisterReceiver(timeZoneReceiver)
        }
    }

    companion object {
        private const val TAG = "AnalogWatchFaceService"
    }
}
