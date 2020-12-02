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

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.util.Size
import android.util.SparseArray
import android.view.SurfaceHolder
import com.example.android.wearable.watchfacekotlin.R

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handles all rendering/drawing of the watch face.
 *
 * Uses [AnalogWatchFaceStyle] to style colors, dimensions, etc. of the watch face.
 */
class AnalogWatchFaceRenderer (
    private val context: Context,
    private val analogWatchFaceStyle: AnalogWatchFaceStyle,
    private val watchFaceRendererListener:WatchFaceRendererListener) {

    // TODO: Move to separate class?
    // Used to pull user's preferences for background color, highlight color, and visual
    // indicating there are unread notifications.
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.analog_complication_preference_file_key),
        CanvasWatchFaceService.MODE_PRIVATE
    )

    // Update time for animations in interactive mode (second hand animation, etc.).
    private val interactiveUpdateRateMillis = TimeUnit.SECONDS.toMillis(1)

    // Used to trigger the animation every second.
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private var _visible = false

    @InternalCoroutinesApi
    var visible:Boolean
        get() = _visible
        set(isVisible) {
            _visible = isVisible

            if (visible) {

                // User's style preferences may have changed since the last time the
                // watch face was visible, so we load the latest.
                loadWatchFaceStylePreferences(analogWatchFaceStyle)

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                setComplicationsActiveAndAmbientColors(
                    analogWatchFaceStyle.complicationStyle.activeColor,
                    analogWatchFaceStyle.complicationStyle.ambientColor
                )

                startSecondHandAnimation()

            } else {
                stopSecondHandAnimation()
            }
        }

    // Represents half the width and height of the screen and used for animation calculations.
    // (Half both the width and height puts you in the center of the screen.)
    private var centerX = 0f
    private var centerY = 0f

    /* Maps complication ids (unique for each location) to corresponding [ComplicationDrawable].
     * The [ComplicationDrawable] is a system API that renders complication data on the watch face.
     */
    private var complicationDrawableSparseArray: SparseArray<ComplicationDrawable>

    // If image is active as the background complication, this is used to set the background color
    // to black to not conflict with image.
    private var backgroundComplicationActive = false

    private var lowBitAmbient = false
    private var burnInProtection = false

    // Ambient is the battery saving mode of the watch face. We limit the color palette and
    // animations in the mode.
    private var _ambient = false

    @InternalCoroutinesApi
    var ambient: Boolean
        get() = _ambient
        set(inAmbientMode) {
            _ambient = inAmbientMode

            // Update drawable complications' ambient state.
            // Note: ComplicationDrawable handles switching between active/ambient colors, we just
            // have to inform it to enter ambient mode.
            for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
                // Sets ComplicationDrawable ambient mode
                complicationDrawableSparseArray[complicationId].setInAmbientMode(ambient)
            }

            analogWatchFaceStyle.setAmbientMode(_ambient)

            if (ambient) {
                stopSecondHandAnimation()
            } else {
                startSecondHandAnimation()
            }
        }

    private var _numberOfUnreadNotifications = 0

    var numberOfUnreadNotifications:Int
        get() = _numberOfUnreadNotifications
        set(count) {
            if (analogWatchFaceStyle.unreadNotificationPref &&
                _numberOfUnreadNotifications != count &&
                count > 0) {
                    // Tell [CanvasWatchFaceService], that is, [AnalogComplicationWatchFaceService] to
                    // trigger onDraw() from system.
                    watchFaceRendererListener.onDrawRequest()
            }

            _numberOfUnreadNotifications = count
        }

    /*
     * Coroutine/flow Job that animates the second hand every second when the watch face
     * is active.
     */
    private var secondHandAnimationJob: Job? = null

    init {
        // Creates a ComplicationDrawable for each location where the user can render a
        // complication on the watch face. In this watch face, we create one for left, right,
        // and background, but you could add many more.
        val leftComplicationDrawable = ComplicationDrawable(context)
        val rightComplicationDrawable = ComplicationDrawable(context)
        val backgroundComplicationDrawable = ComplicationDrawable(context)

        // Adds new complications to a SparseArray to simplify setting styles and ambient
        // properties for all complications, i.e., iterate over them all.
        complicationDrawableSparseArray =
            SparseArray(AnalogComplicationWatchFaceService.complicationIds.size)
        complicationDrawableSparseArray.put(
            AnalogComplicationWatchFaceService.LEFT_COMPLICATION_ID,
            leftComplicationDrawable
        )
        complicationDrawableSparseArray.put(
            AnalogComplicationWatchFaceService.RIGHT_COMPLICATION_ID,
            rightComplicationDrawable
        )
        complicationDrawableSparseArray.put(
            AnalogComplicationWatchFaceService.BACKGROUND_COMPLICATION_ID,
            backgroundComplicationDrawable
        )

        setComplicationsActiveAndAmbientColors(
            analogWatchFaceStyle.complicationStyle.activeColor,
            analogWatchFaceStyle.complicationStyle.ambientColor
        )
    }

    fun render(canvas: Canvas, bounds: Rect, calendar: Calendar) {
        val now = System.currentTimeMillis()
        calendar.timeInMillis = now

        drawBackground(canvas)
        drawComplications(canvas, now)
        drawUnreadNotificationIndicator(canvas)
        drawWatchFace(canvas, calendar)

    }

    private fun drawBackground(canvas: Canvas) {
        val backgroundColor = when {
            // Background should always be black when in ambient, low bit and burn in, or if a
            // background complication is active, i.e., a background image is set, we also want the
            // background to be black.
            _ambient || (lowBitAmbient && burnInProtection) || backgroundComplicationActive ->
                analogWatchFaceStyle.backgroundColor.ambientColor

            else ->
                analogWatchFaceStyle.backgroundColor.activeColor
        }

        canvas.drawColor(backgroundColor)
    }

    private fun drawUnreadNotificationIndicator(canvas: Canvas) {

        if (analogWatchFaceStyle.unreadNotificationPref && _numberOfUnreadNotifications > 0) {

            // Calculates how far up from the bottom the icon needs to be painted (Y axis).
            val localOffsetY =
                canvas.width - analogWatchFaceStyle.unreadNotificationIndicatorOffsetY

            canvas.drawCircle(
                centerX,
                localOffsetY.toFloat(),
                analogWatchFaceStyle.unreadNotificationIndicatorOuterRingSize.toFloat(),
                analogWatchFaceStyle.notificationCircle.paint
            )

            /*
             * Ensure center highlight circle is only drawn in interactive mode. This ensures
             * we don't burn the screen with a solid circle in ambient mode.
             */
            if (!_ambient) {
                canvas.drawCircle(
                    centerX,
                    localOffsetY.toFloat(),
                    analogWatchFaceStyle.unreadNotificationIndicatorInnerCircle.toFloat(),
                    analogWatchFaceStyle.secondHand.paint
                )
            }
        }
    }

    private fun drawComplications(canvas: Canvas, currentTimeMillis: Long) {
        var complicationDrawable: ComplicationDrawable

        for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
            complicationDrawable = complicationDrawableSparseArray[complicationId]
            complicationDrawable.draw(canvas, currentTimeMillis)
        }
    }

    private fun drawWatchFace(canvas: Canvas, calendar:Calendar) {

        /*
         * Draw ticks. Usually you will want to bake this directly into the photo, but in
         * cases where you want to allow users to select their own photos, this dynamically
         * creates them on top of the photo.
         */
        val innerTickRadius = centerX - 10
        val outerTickRadius = centerX
        for (tickIndex in 0 until 12) {
            val tickRot = (tickIndex * Math.PI * 2 / 12)
            val innerX = sin(tickRot).toFloat() * innerTickRadius
            val innerY = (-cos(tickRot)).toFloat() * innerTickRadius
            val outerX = sin(tickRot).toFloat() * outerTickRadius
            val outerY = (-cos(tickRot)).toFloat() * outerTickRadius
            canvas.drawLine(
                centerX + innerX,
                centerY + innerY,
                centerX + outerX,
                centerY + outerY,
                analogWatchFaceStyle.ticks.paint
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
            centerY - analogWatchFaceStyle.centerGapAndCircleRadius,
            centerX,
            centerY - analogWatchFaceStyle.hourHand.dimensions.height,
            analogWatchFaceStyle.hourHand.paint
        )

        canvas.rotate(minutesRotation - hoursRotation, centerX, centerY)
        canvas.drawLine(
            centerX,
            centerY - analogWatchFaceStyle.centerGapAndCircleRadius,
            centerX,
            centerY - analogWatchFaceStyle.minuteHand.dimensions.height,
            analogWatchFaceStyle.minuteHand.paint
        )

        /*
         * Ensure the "seconds" hand is drawn only when we are in interactive mode.
         * Otherwise, we only update the watch face once a minute.
         */
        if (!_ambient) {
            canvas.rotate(secondsRotation - minutesRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY - analogWatchFaceStyle.centerGapAndCircleRadius,
                centerX,
                centerY - analogWatchFaceStyle.secondHand.dimensions.height,
                analogWatchFaceStyle.secondHand.paint
            )
        }

        canvas.drawCircle(
            centerX,
            centerY,
            analogWatchFaceStyle.centerGapAndCircleRadius.toFloat(),
            analogWatchFaceStyle.ticks.paint
        )

        /* Restore the canvas' original orientation. */
        canvas.restore()
    }


    /**
     * Starts the coroutine/flow "timer" to animate the second hand every second when the
     * watch face is active.
     */
    @InternalCoroutinesApi
    private fun startSecondHandAnimation() {
        if (!secondHandAnimationActive()) {
            secondHandAnimationJob = coroutineScope.launch(Dispatchers.Main) {
                secondHandAnimationFlow().collect {
                    // Redraws second hand one second apart (triggered by flow)
                    // Tell CanvasWatchFaceService to trigger onDraw() from system.
                    watchFaceRendererListener.onDrawRequest()
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
        return _visible && !_ambient
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
            val delayMs = interactiveUpdateRateMillis - (timeMs % interactiveUpdateRateMillis)
            delay(delayMs)
            // Emit next value
            emit(Unit)
        }
    }

    /**
     * Dims display in mute mode.
     */
    fun toggleDimMode(muteMode: Boolean) {
        analogWatchFaceStyle.setMuteMode(muteMode)
    }

    /*
     * Called when there is updated data for a complication id.
     */
    fun updateWatchFaceComplication(
        complicationId: Int,
        complicationData: ComplicationData
    ) {

        if (complicationId == AnalogComplicationWatchFaceService.BACKGROUND_COMPLICATION_ID) {
            // If background image isn't the correct type, it means either there was no data,
            // a permission problem, or the data was empty, i.e., user deselected it.
            backgroundComplicationActive =
                complicationData.type == ComplicationData.TYPE_LARGE_IMAGE
        }

        // Updates correct ComplicationDrawable with updated data.
        val complicationDrawable = complicationDrawableSparseArray[complicationId]
        complicationDrawable.setComplicationData(complicationData)
    }

    fun checkTapLocation(tapType: Int, x: Int, y: Int, eventTime: Long) {
        when (tapType) {
            CanvasWatchFaceService.TAP_TYPE_TAP ->
                // If your background complication is the first item in your array, you need
                // to walk backward through the array to make sure the tap isn't for a
                // complication above the background complication.
            {
                var index = AnalogComplicationWatchFaceService.complicationIds.size - 1
                while (index >= 0) {
                    val complicationId = AnalogComplicationWatchFaceService.complicationIds[index]
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

    fun tearDown() {
        stopSecondHandAnimation()
    }

    fun setLowBitAndBurnInProtection(properties: Bundle) {
        lowBitAmbient =
            properties.getBoolean(CanvasWatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false)
        burnInProtection =
            properties.getBoolean(CanvasWatchFaceService.PROPERTY_BURN_IN_PROTECTION, false)


        // Updates complications to properly render in ambient mode based on the screen's
        // capabilities.
        for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
            complicationDrawableSparseArray[complicationId].apply {
                setLowBitAmbient(lowBitAmbient)
                setBurnInProtection(burnInProtection)
            }
        }
    }

    /* Sets active/ambient mode colors for all complications.
     *
     * Note: With the rest of the watch face, we update the paint colors based on ambient/active
     * mode callbacks, but because the ComplicationDrawable handles the active/ambient colors
     * itself, we only set the colors twice. Once at initialization and again if the user changes
     * the highlight color via AnalogComplicationConfigActivity.
     */
    private fun setComplicationsActiveAndAmbientColors(activeColor: Int, ambientColor:Int) {
        var complicationDrawable: ComplicationDrawable

        for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
            complicationDrawable = complicationDrawableSparseArray[complicationId]
            if (complicationId == AnalogComplicationWatchFaceService.BACKGROUND_COMPLICATION_ID) {
                // It helps for the background color to be black in case the image used for the
                // watch face's background takes some time to load.
                complicationDrawable.setBackgroundColorActive(Color.BLACK)

            } else {
                complicationDrawable.apply {
                    // Active mode colors.
                    setBorderColorActive(activeColor)
                    setRangedValuePrimaryColorActive(activeColor)

                    // Ambient mode colors.
                    setBorderColorAmbient(ambientColor)
                    setRangedValuePrimaryColorAmbient(ambientColor)
                }
            }
        }
    }

    fun calculateWatchFaceDimensions(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
        centerX = width / 2f
        centerY = height / 2f

        /*
         * Calculate lengths of different hands based on half the watch screen size (centerX).
         */
        val hourHeight = (centerX * 0.5).toInt()
        val minuteHeight = (centerX * 0.75).toInt()
        val secondHeight = (centerX * 0.875).toInt()

        // Calculates stroke width and radius by total screen size.
        val hourWidth = (width / 56)
        val minuteWidth = (width / 93)

        val secondWidth = (width / 140)

        analogWatchFaceStyle.hourHand.dimensions = Size(hourWidth, hourHeight)
        analogWatchFaceStyle.minuteHand.dimensions = Size(minuteWidth, minuteHeight)
        analogWatchFaceStyle.secondHand.dimensions = Size(secondWidth, secondHeight)

        // TODO: Add notes
        analogWatchFaceStyle.ticks.dimensions = Size(secondWidth, secondWidth)


        analogWatchFaceStyle.centerGapAndCircleRadius = (width / 70)

        // TODO: Assign shadow radius for all paint objects
        val shadowRadius = (width / 46).toFloat()

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
        val leftComplicationDrawable =
            complicationDrawableSparseArray[AnalogComplicationWatchFaceService.LEFT_COMPLICATION_ID]
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
            complicationDrawableSparseArray[AnalogComplicationWatchFaceService.RIGHT_COMPLICATION_ID]
        rightComplicationDrawable.bounds = rightBounds

        val screenForBackgroundBound =
            // Left, Top, Right, Bottom
            Rect(0, 0, width, height)

        val backgroundComplicationDrawable =
            complicationDrawableSparseArray[AnalogComplicationWatchFaceService.BACKGROUND_COMPLICATION_ID]
        backgroundComplicationDrawable.bounds = screenForBackgroundBound
    }

    // TODO: Move to separate class?
    // Pulls all user's preferences for watch face appearance.
    private fun loadWatchFaceStylePreferences(analogWatchFaceStyle:AnalogWatchFaceStyle) {
        val backgroundColorResourceName =
            context.getString(R.string.saved_background_color_pref)

        analogWatchFaceStyle.backgroundColor.activeColor =
            sharedPreferences.getInt(backgroundColorResourceName, Color.BLACK)

        val markerColorResourceName =
            context.getString(R.string.saved_marker_color_pref)

        val highlightColor =
            sharedPreferences.getInt(markerColorResourceName, Color.RED)

        analogWatchFaceStyle.setHighlightColor(highlightColor)

        val unreadNotificationPreferenceResourceName =
            context.getString(R.string.saved_unread_notifications_pref)
        analogWatchFaceStyle.unreadNotificationPref =
            sharedPreferences.getBoolean(unreadNotificationPreferenceResourceName, true)
    }

    class WatchFaceRendererListener(val drawListener: () -> Unit) {
        fun onDrawRequest() = drawListener()
    }
}