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
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder

import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigRecyclerViewAdapter
import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Demonstrates two simple complications in a watch face. The watch face also links to a settings
 * activity that previews the watch face and allows you to adjust colors, complication data, etc.
 */
class AnalogComplicationWatchFaceService : CanvasWatchFaceService() {
    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine internal constructor() : CanvasWatchFaceService.Engine(true) {
        private val watchfaceCoroutineScope = CoroutineScope(SupervisorJob())

        private lateinit var calendar: Calendar
        private var registeredTimeZoneReceiver = false

        private var muteMode = false
        private var centerX = 0f
        private var centerY = 0f
        private var secondHandLength = 0f
        private var minuteHandLength = 0f
        private var hourHandLength = 0f

        // Colors for all hands (hour, minute, seconds, ticks) based on photo loaded.
        private var watchHandAndComplicationsColor = 0
        private var watchHandHighlightColor = 0
        private var watchHandShadowColor = 0
        private var backgroundColor = 0


        private lateinit var hourPaint: Paint
        private lateinit var minutePaint: Paint
        private lateinit var secondAndHighlightPaint: Paint
        private lateinit var tickAndCirclePaint: Paint
        private lateinit var backgroundPaint: Paint

        /* Maps complication ids to corresponding ComplicationDrawable that renders the
         * the complication data on the watch face.
         */
        private lateinit var complicationDrawableSparseArray: SparseArray<ComplicationDrawable>

        // If active, this is used to set the background color to black.
        private var backgroundComplicationActive = false


        private var ambient = false
        private var lowBitAmbient = false
        private var burnInProtection = false

        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        private lateinit var sharedPreferences: SharedPreferences

        // User's preference for if they want visual shown to indicate unread notifications.
        private var unreadNotificationsPreference = false
        private var numberOfUnreadNotifications = 0
        private val timeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        /**
         * Coroutine/flow Job that animates the second hand every second when the watch face
         * is active.
         */
        private var secondHandAnimationJob: Job? = null

        override fun onCreate(holder: SurfaceHolder) {
            Log.d(TAG, "onCreate")
            super.onCreate(holder)

            // Used throughout watch face to pull user's preferences.
            val context = applicationContext
            sharedPreferences = context.getSharedPreferences(
                getString(R.string.analog_complication_preference_file_key),
                MODE_PRIVATE
            )
            calendar = Calendar.getInstance()
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@AnalogComplicationWatchFaceService)
                    .setAcceptsTapEvents(true)
                    .setHideNotificationIndicator(true)
                    .build()
            )
            loadSavedPreferences()
            initializeComplicationsAndBackground()
            initializeWatchFace()
        }

        // Pulls all user's preferences for watch face appearance.
        private fun loadSavedPreferences() {
            val backgroundColorResourceName =
                applicationContext.getString(R.string.saved_background_color)
            backgroundColor = sharedPreferences.getInt(backgroundColorResourceName, Color.BLACK)
            val markerColorResourceName = applicationContext.getString(R.string.saved_marker_color)

            // Set defaults for colors
            watchHandHighlightColor = sharedPreferences.getInt(markerColorResourceName, Color.RED)
            if (backgroundColor == Color.WHITE) {
                watchHandAndComplicationsColor = Color.BLACK
                watchHandShadowColor = Color.WHITE
            } else {
                watchHandAndComplicationsColor = Color.WHITE
                watchHandShadowColor = Color.BLACK
            }
            val unreadNotificationPreferenceResourceName =
                applicationContext.getString(R.string.saved_unread_notifications_pref)
            unreadNotificationsPreference =
                sharedPreferences.getBoolean(unreadNotificationPreferenceResourceName, true)
        }

        private fun initializeComplicationsAndBackground() {
            Log.d(TAG, "initializeComplications()")

            // Initialize background color (in case background complication is inactive).
            backgroundPaint = Paint()
            backgroundPaint.color = backgroundColor

            // Creates a ComplicationDrawable for each location where the user can render a
            // complication on the watch face. In this watch face, we create one for left, right,
            // and background, but you could add many more.
            val leftComplicationDrawable = ComplicationDrawable(applicationContext)
            val rightComplicationDrawable = ComplicationDrawable(applicationContext)
            val backgroundComplicationDrawable = ComplicationDrawable(applicationContext)

            // Adds new complications to a SparseArray to simplify setting styles and ambient
            // properties for all complications, i.e., iterate over them all.
            complicationDrawableSparseArray = SparseArray(complicationIds.size)
            complicationDrawableSparseArray.put(LEFT_COMPLICATION_ID, leftComplicationDrawable)
            complicationDrawableSparseArray.put(RIGHT_COMPLICATION_ID, rightComplicationDrawable)
            complicationDrawableSparseArray.put(
                BACKGROUND_COMPLICATION_ID,
                backgroundComplicationDrawable
            )
            setComplicationsActiveAndAmbientColors(watchHandHighlightColor)
            setActiveComplications(*complicationIds)
        }

        private fun initializeWatchFace() {
            hourPaint = Paint()
            hourPaint.color = watchHandAndComplicationsColor
            hourPaint.strokeWidth = HOUR_STROKE_WIDTH
            hourPaint.isAntiAlias = true
            hourPaint.strokeCap = Paint.Cap.ROUND
            hourPaint.setShadowLayer(
                SHADOW_RADIUS.toFloat(),
                0f,
                0f,
                watchHandShadowColor
            )

            minutePaint = Paint()
            minutePaint.color = watchHandAndComplicationsColor
            minutePaint.strokeWidth = MINUTE_STROKE_WIDTH
            minutePaint.isAntiAlias = true
            minutePaint.strokeCap = Paint.Cap.ROUND
            minutePaint.setShadowLayer(
                SHADOW_RADIUS.toFloat(),
                0f,
                0f,
                watchHandShadowColor
            )

            secondAndHighlightPaint = Paint()
            secondAndHighlightPaint.color = watchHandHighlightColor
            secondAndHighlightPaint.strokeWidth =
                SECOND_TICK_STROKE_WIDTH
            secondAndHighlightPaint.isAntiAlias = true
            secondAndHighlightPaint.strokeCap = Paint.Cap.ROUND
            secondAndHighlightPaint.setShadowLayer(
                SHADOW_RADIUS.toFloat(),
                0f,
                0f,
                watchHandShadowColor
            )

            tickAndCirclePaint = Paint()
            tickAndCirclePaint.color = watchHandAndComplicationsColor
            tickAndCirclePaint.strokeWidth =
                SECOND_TICK_STROKE_WIDTH
            tickAndCirclePaint.isAntiAlias = true
            tickAndCirclePaint.style = Paint.Style.STROKE
            tickAndCirclePaint.setShadowLayer(
                SHADOW_RADIUS.toFloat(),
                0f,
                0f,
                watchHandShadowColor
            )
        }

        /* Sets active/ambient mode colors for all complications.
         *
         * Note: With the rest of the watch face, we update the paint colors based on
         * ambient/active mode callbacks, but because the ComplicationDrawable handles
         * the active/ambient colors, we only set the colors twice. Once at initialization and
         * again if the user changes the highlight color via AnalogComplicationConfigActivity.
         */
        private fun setComplicationsActiveAndAmbientColors(primaryComplicationColor: Int) {
            var complicationId: Int
            var complicationDrawable: ComplicationDrawable

            for (index in complicationIds.indices) {
                complicationId = complicationIds[index]
                complicationDrawable = complicationDrawableSparseArray[complicationId]
                if (complicationId == BACKGROUND_COMPLICATION_ID) {
                    // It helps for the background color to be black in case the image used for the
                    // watch face's background takes some time to load.
                    complicationDrawable.setBackgroundColorActive(Color.BLACK)
                } else {
                    // Active mode colors.
                    complicationDrawable.setBorderColorActive(primaryComplicationColor)
                    complicationDrawable.setRangedValuePrimaryColorActive(primaryComplicationColor)

                    // Ambient mode colors.
                    complicationDrawable.setBorderColorAmbient(Color.WHITE)
                    complicationDrawable.setRangedValuePrimaryColorAmbient(Color.WHITE)
                }
            }
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy()")
            stopSecondHandAnimation()
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            Log.d(TAG, "onPropertiesChanged: low-bit ambient = $lowBitAmbient")

            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false)
            burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false)

            // Updates complications to properly render in ambient mode based on the
            // screen's capabilities.
            var complicationDrawable: ComplicationDrawable
            for (index in complicationIds.indices) {
                complicationDrawable = complicationDrawableSparseArray[complicationIds[index]]
                complicationDrawable.setLowBitAmbient(lowBitAmbient)
                complicationDrawable.setBurnInProtection(burnInProtection)
            }
        }

        /*
         * Called when there is updated data for a complication id.
         */
        override fun onComplicationDataUpdate(
            complicationId: Int,
            complicationData: ComplicationData
        ) {
            Log.d(TAG, "onComplicationDataUpdate() id: $complicationId")

            if (complicationId == BACKGROUND_COMPLICATION_ID) {
                // If background image isn't the correct type, it means either there was no data,
                // a permission problem, or the data was empty, i.e., user deselected it.
                backgroundComplicationActive =
                    complicationData.type == ComplicationData.TYPE_LARGE_IMAGE
            }

            // Updates correct ComplicationDrawable with updated data.
            val complicationDrawable = complicationDrawableSparseArray[complicationId]
            complicationDrawable.setComplicationData(complicationData)
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            Log.d(TAG, "OnTapCommand()")
            when (tapType) {
                TAP_TYPE_TAP ->
                    // If your background complication is the first item in your array, you need
                    // to walk backward through the array to make sure the tap isn't for a
                    // complication above the background complication.
                {
                    var index = complicationIds.size - 1
                    while (index >= 0) {
                        val complicationId = complicationIds[index]
                        val complicationDrawable =
                            complicationDrawableSparseArray[complicationId]
                        val successfulTap = complicationDrawable.onTap(x, y)
                        if (successfulTap) {
                            return
                        }
                        index--
                    }
                }
            }
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        @InternalCoroutinesApi
        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            Log.d(TAG, "onAmbientModeChanged: $inAmbientMode")
            ambient = inAmbientMode
            updateWatchPaintStyles()

            // Update drawable complications' ambient state.
            // Note: ComplicationDrawable handles switching between active/ambient colors, we just
            // have to inform it to enter ambient mode.
            var complicationDrawable: ComplicationDrawable
            for (index in complicationIds.indices) {
                complicationDrawable = complicationDrawableSparseArray[complicationIds[index]]
                complicationDrawable.setInAmbientMode(ambient)
            }

            if (ambient) {
               stopSecondHandAnimation()
            } else {
                startSecondHandAnimation()
            }
        }

        private fun updateWatchPaintStyles() {
            if (ambient) {
                backgroundPaint.color = Color.BLACK
                hourPaint.color = Color.WHITE
                minutePaint.color = Color.WHITE
                secondAndHighlightPaint.color = Color.WHITE
                tickAndCirclePaint.color = Color.WHITE
                hourPaint.isAntiAlias = false
                minutePaint.isAntiAlias = false
                secondAndHighlightPaint.isAntiAlias = false
                tickAndCirclePaint.isAntiAlias = false
                hourPaint.clearShadowLayer()
                minutePaint.clearShadowLayer()
                secondAndHighlightPaint.clearShadowLayer()
                tickAndCirclePaint.clearShadowLayer()

            } else {
                backgroundPaint.color = backgroundColor
                hourPaint.color = watchHandAndComplicationsColor
                minutePaint.color = watchHandAndComplicationsColor
                tickAndCirclePaint.color = watchHandAndComplicationsColor
                secondAndHighlightPaint.color = watchHandHighlightColor
                hourPaint.isAntiAlias = true
                minutePaint.isAntiAlias = true
                secondAndHighlightPaint.isAntiAlias = true
                tickAndCirclePaint.isAntiAlias = true
                hourPaint.setShadowLayer(
                    SHADOW_RADIUS.toFloat(),
                    0f,
                    0f,
                    watchHandShadowColor
                )
                minutePaint.setShadowLayer(
                    SHADOW_RADIUS.toFloat(),
                    0f,
                    0f,
                    watchHandShadowColor
                )
                secondAndHighlightPaint.setShadowLayer(
                    SHADOW_RADIUS.toFloat(),
                    0f,
                    0f,
                    watchHandShadowColor
                )
                tickAndCirclePaint.setShadowLayer(
                    SHADOW_RADIUS.toFloat(),
                    0f,
                    0f,
                    watchHandShadowColor
                )
            }
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == INTERRUPTION_FILTER_NONE

            // Dim display in mute mode.
            if (muteMode != inMuteMode) {
                muteMode = inMuteMode
                hourPaint.alpha = if (inMuteMode) 100 else 255
                minutePaint.alpha = if (inMuteMode) 100 else 255
                secondAndHighlightPaint.alpha = if (inMuteMode) 80 else 255
                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            centerX = width / 2f
            centerY = height / 2f

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            secondHandLength = (centerX * 0.875).toFloat()
            minuteHandLength = (centerX * 0.75).toFloat()
            hourHandLength = (centerX * 0.5).toFloat()

            /*
             * Calculates location bounds for right and left circular complications. Please note,
             * we are not demonstrating a long text complication in this watch face.
             *
             * We suggest using at least 1/4 of the screen width for circular (or squared)
             * complications and 2/3 of the screen width for wide rectangular complications for
             * better readability.
             */

            // For most Wear devices, width and height are the same, so we just chose one (width).
            val sizeOfComplication = width / 4
            val midpointOfScreen = width / 2
            val horizontalOffset = (midpointOfScreen - sizeOfComplication) / 2
            val verticalOffset = midpointOfScreen - sizeOfComplication / 2

            val leftBounds =
                // Left, Top, Right, Bottom
                Rect(
                    horizontalOffset,
                    verticalOffset,
                    horizontalOffset + sizeOfComplication,
                    verticalOffset + sizeOfComplication
                )
            val leftComplicationDrawable = complicationDrawableSparseArray[LEFT_COMPLICATION_ID]
            leftComplicationDrawable.bounds = leftBounds

            val rightBounds =
                // Left, Top, Right, Bottom
                Rect(
                    midpointOfScreen + horizontalOffset,
                    verticalOffset,
                    midpointOfScreen + horizontalOffset + sizeOfComplication,
                    verticalOffset + sizeOfComplication
                )
            val rightComplicationDrawable =
                complicationDrawableSparseArray[RIGHT_COMPLICATION_ID]
            rightComplicationDrawable.bounds = rightBounds

            val screenForBackgroundBound =
                // Left, Top, Right, Bottom
                Rect(0, 0, width, height)

            val backgroundComplicationDrawable =
                complicationDrawableSparseArray[BACKGROUND_COMPLICATION_ID]
            backgroundComplicationDrawable.bounds = screenForBackgroundBound
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()
            calendar.timeInMillis = now

            drawBackground(canvas)
            drawComplications(canvas, now)
            drawUnreadNotificationIcon(canvas)
            drawWatchFace(canvas)
        }

        private fun drawUnreadNotificationIcon(canvas: Canvas) {

            if (unreadNotificationsPreference && numberOfUnreadNotifications > 0) {

                val width = canvas.width
                val height = canvas.height

                canvas.drawCircle(
                    width / 2.toFloat(),
                    height - 40.toFloat(),
                    10f,
                    tickAndCirclePaint
                )

                /*
                 * Ensure center highlight circle is only drawn in interactive mode. This ensures
                 * we don't burn the screen with a solid circle in ambient mode.
                 */
                if (!ambient) {
                    canvas.drawCircle(
                        width / 2.toFloat(),
                        height - 40.toFloat(),
                        4f,
                        secondAndHighlightPaint
                    )
                }
            }
        }

        private fun drawBackground(canvas: Canvas) {
            if (ambient && (lowBitAmbient || burnInProtection)) {
                canvas.drawColor(Color.BLACK)
            } else if (backgroundComplicationActive) {
                // Overrides any background color to match background complication
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawColor(backgroundColor)
            }
        }

        private fun drawComplications(canvas: Canvas, currentTimeMillis: Long) {
            var complicationId: Int
            var complicationDrawable: ComplicationDrawable

            for (index in complicationIds.indices) {
                complicationId = complicationIds[index]
                complicationDrawable = complicationDrawableSparseArray[complicationId]
                complicationDrawable.draw(canvas, currentTimeMillis)
            }
        }

        private fun drawWatchFace(canvas: Canvas) {
            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            val innerTickRadius = centerX - 10
            val outerTickRadius = centerX
            for (tickIndex in 0..11) {
                val tickRot = (tickIndex * Math.PI * 2 / 12).toFloat()
                val innerX = Math.sin(tickRot.toDouble()).toFloat() * innerTickRadius
                val innerY = (-Math.cos(tickRot.toDouble())).toFloat() * innerTickRadius
                val outerX = Math.sin(tickRot.toDouble()).toFloat() * outerTickRadius
                val outerY = (-Math.cos(tickRot.toDouble())).toFloat() * outerTickRadius
                canvas.drawLine(
                    centerX + innerX,
                    centerY + innerY,
                    centerX + outerX,
                    centerY + outerY,
                    tickAndCirclePaint
                )
            }

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            val seconds = calendar[Calendar.SECOND] + calendar[Calendar.MILLISECOND] / 1000f
            val secondsRotation = seconds * 6f
            val minutesRotation = calendar[Calendar.MINUTE] * 6f
            val hourHandOffset = calendar[Calendar.MINUTE] / 2f
            val hoursRotation = calendar[Calendar.HOUR] * 30 + hourHandOffset

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save()

            canvas.rotate(hoursRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY - CENTER_GAP_AND_CIRCLE_RADIUS,
                centerX,
                centerY - hourHandLength,
                hourPaint
            )

            canvas.rotate(minutesRotation - hoursRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY - CENTER_GAP_AND_CIRCLE_RADIUS,
                centerX,
                centerY - minuteHandLength,
                minutePaint
            )

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            if (!ambient) {
                canvas.rotate(secondsRotation - minutesRotation, centerX, centerY)
                canvas.drawLine(
                    centerX,
                    centerY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    centerX,
                    centerY - secondHandLength,
                    secondAndHighlightPaint
                )
            }

            canvas.drawCircle(
                centerX,
                centerY,
                CENTER_GAP_AND_CIRCLE_RADIUS,
                tickAndCirclePaint
            )

            /* Restore the canvas' original orientation. */
            canvas.restore()
        }

        @InternalCoroutinesApi
        override fun onVisibilityChanged(visible: Boolean) {
            Log.d(TAG, "onVisibilityChanged(): $visible")
            super.onVisibilityChanged(visible)
            if (visible) {

                // Preferences might have changed since last time watch face was visible.
                loadSavedPreferences()

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                setComplicationsActiveAndAmbientColors(watchHandHighlightColor)
                updateWatchPaintStyles()

                registerReceiver()
                // Update time zone in case it changed while we weren't visible.
                calendar.timeZone = TimeZone.getDefault()

                invalidate()
                startSecondHandAnimation()

            } else {
                stopSecondHandAnimation()
                unregisterReceiver()
            }


        }

        override fun onUnreadCountChanged(count: Int) {
            Log.d(TAG, "onUnreadCountChanged(): $count")

            if (unreadNotificationsPreference) {
                if (numberOfUnreadNotifications != count) {
                    numberOfUnreadNotifications = count
                    invalidate()
                }
            }
        }

        private fun registerReceiver() {
            if (registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            registerReceiver(timeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = false
            unregisterReceiver(timeZoneReceiver)
        }

        /**
         * Starts the coroutine/flow "timer" to animate the second hand every second when the
         * watch face is active.
         */
        @InternalCoroutinesApi
        private fun startSecondHandAnimation() {
            if (!secondHandAnimationActive()) {
                secondHandAnimationJob = watchfaceCoroutineScope.launch(Dispatchers.Main) {
                    secondHandAnimationFlow().collect {
                        // Redraws second hand one second apart (triggered by flow)
                        invalidate()
                    }
                }
            }
        }

        /**
         * Stops the coroutine/flow "timer" when the watch face isn't visible (or is in ambient
         * mode).
         */
        private fun stopSecondHandAnimation() {
            if (secondHandAnimationJob?.isActive == true) {
                secondHandAnimationJob?.cancel()
            }
        }

        /**
         * Second hand should only be visible in active mode.
         */
        private fun secondHandVisible(): Boolean {
            return isVisible && !ambient
        }

        private fun secondHandAnimationActive(): Boolean {
            return secondHandAnimationJob?.isActive ?: false
        }

        /**
         * Creates a flow that triggers one second apart. It's used to animate the second hand when
         * the watch face is in active mode.
         */
        private fun secondHandAnimationFlow(): Flow<Unit> = flow {
            while (secondHandVisible()) {
                val timeMs = System.currentTimeMillis()
                Log.d(TAG, "timeMs: $timeMs")
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS)
                Log.d(TAG, "delayMs: $delayMs")
                delay(delayMs)
                // Emit next value
                emit(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "AnalogWatchFace"

        /*
         * Update rate in milliseconds for interactive mode. We update once a second to advance the
         * second hand.
         */
        private val INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1)

        private const val HOUR_STROKE_WIDTH = 5f
        private const val MINUTE_STROKE_WIDTH = 3f
        private const val SECOND_TICK_STROKE_WIDTH = 2f
        private const val CENTER_GAP_AND_CIRCLE_RADIUS = 4f
        private const val SHADOW_RADIUS = 6


        // Unique IDs for each complication. The settings activity that supports allowing users
        // to select their complication data provider requires numbers to be >= 0.
        private const val BACKGROUND_COMPLICATION_ID = 0
        private const val LEFT_COMPLICATION_ID = 100
        private const val RIGHT_COMPLICATION_ID = 101

        // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to retrieve all complication
        // ids. Background, Left and right complication IDs as array for Complication API.
        val complicationIds = intArrayOf(
            BACKGROUND_COMPLICATION_ID, LEFT_COMPLICATION_ID, RIGHT_COMPLICATION_ID
        )

        // Left and right dial supported types.
        private val COMPLICATION_SUPPORTED_TYPES = arrayOf(
            intArrayOf(ComplicationData.TYPE_LARGE_IMAGE),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            )
        )

        // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to check if complication
        // location is supported in settings config activity.
        fun getComplicationId(
            complicationLocation: AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation
        ): Int {
            // Add any other supported locations here.
            return when (complicationLocation) {
                ComplicationLocation.BACKGROUND -> BACKGROUND_COMPLICATION_ID
                ComplicationLocation.LEFT -> LEFT_COMPLICATION_ID
                ComplicationLocation.RIGHT -> RIGHT_COMPLICATION_ID
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
                ComplicationLocation.BACKGROUND -> COMPLICATION_SUPPORTED_TYPES[0]
                ComplicationLocation.LEFT -> COMPLICATION_SUPPORTED_TYPES[1]
                ComplicationLocation.RIGHT -> COMPLICATION_SUPPORTED_TYPES[2]
                else -> intArrayOf()
            }
        }
    }
}
