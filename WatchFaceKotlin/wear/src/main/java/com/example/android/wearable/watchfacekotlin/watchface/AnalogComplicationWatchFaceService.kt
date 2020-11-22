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

import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigRecyclerViewAdapter
import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation

import kotlinx.coroutines.InternalCoroutinesApi
import java.util.Calendar
import java.util.TimeZone


/**
 * Demonstrates two simple complications plus a background complication in an analog watch face.
 *
 * While this class mostly just handles callbacks and triggering the onDraw() (via the invalidate()
 * method), the renderer (AnalogWatchFaceRenderer) does the heavy work of actually drawing on the
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
        CanvasWatchFaceService.Engine(true),
        AnalogWatchFaceRenderer.DrawWatchFaceCallback {

        private var calendar = Calendar.getInstance()
        private var registeredTimeZoneReceiver = false

        // Renders watch face (background, hands, ticks, complications, etc.).
        // NOTE: The context is needed for the ComplicationDrawables.
        private val analogWatchFaceRender = AnalogWatchFaceRenderer(
            applicationContext,
            AnalogWatchFaceStyle(),
            this
        )

        private val timeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            Log.d(TAG, "onCreate")
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@AnalogComplicationWatchFaceService)
                    .setAcceptsTapEvents(true)
                    .setHideNotificationIndicator(true)
                    .build()
            )

            setActiveComplications(*complicationIds)
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy()")
            analogWatchFaceRender.tearDown()
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            Log.d(TAG, "onPropertiesChanged: properties = $properties")
            super.onPropertiesChanged(properties)

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

        @InternalCoroutinesApi
        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            Log.d(TAG, "onAmbientModeChanged: $inAmbientMode")
            analogWatchFaceRender.ambient = inAmbientMode
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == INTERRUPTION_FILTER_NONE

            analogWatchFaceRender.toggleDimMode(inMuteMode)
            invalidate()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            analogWatchFaceRender.calculateWatchFaceDimensions(holder, format, width, height)
        }

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

        // Draw request triggered from the renderer (usually because it requires for the
        // second hand animation.
        override fun onRendererDrawRequest() {
            invalidate()
        }
    }

    companion object {
        private const val TAG = "AnalogWatchFace"

        // Unique IDs for each complication. The settings activity that supports allowing users
        // to select their complication data provider requires numbers to be >= 0.
        internal const val BACKGROUND_COMPLICATION_ID = 0
        internal const val LEFT_COMPLICATION_ID = 100
        internal const val RIGHT_COMPLICATION_ID = 101

        sealed class ComplicationConfig(val id:Int, val supportedTypes:IntArray) {
            object Left : ComplicationConfig(
                LEFT_COMPLICATION_ID,
                intArrayOf(
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
                )
            )
            object Right : ComplicationConfig(
                RIGHT_COMPLICATION_ID,
                intArrayOf(
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
                )
            )
            object Background : ComplicationConfig(
                BACKGROUND_COMPLICATION_ID,
                intArrayOf(ComplicationData.TYPE_LARGE_IMAGE)
            )
        }

        // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to retrieve all complication
        // ids. Background, Left, and right complication IDs as array for Complication API.
        val complicationIds = intArrayOf(
            ComplicationConfig.Background.id,
            ComplicationConfig.Left.id,
            ComplicationConfig.Right.id
        )
        // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to check if complication
        // location is supported in settings config activity.
        fun getComplicationId(
            complicationLocation: AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation
        ): Int {
            // Add any other supported locations here.
            return when (complicationLocation) {
                ComplicationLocation.BACKGROUND -> ComplicationConfig.Background.id
                ComplicationLocation.LEFT -> ComplicationConfig.Left.id
                ComplicationLocation.RIGHT -> ComplicationConfig.Right.id
                else -> -1
            }
        }

        // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to see which complication
        // types are supported in the settings config activity.
        fun getSupportedComplicationTypes(
            complicationLocation: AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation
        ): IntArray {
            // Add any other supported locations here.
            return when (complicationLocation) {
                ComplicationLocation.BACKGROUND -> ComplicationConfig.Background.supportedTypes
                ComplicationLocation.LEFT -> ComplicationConfig.Left.supportedTypes
                ComplicationLocation.RIGHT -> ComplicationConfig.Right.supportedTypes
                else -> intArrayOf()
            }
        }
    }
}
