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

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.view.SurfaceHolder
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Handles all rendering/drawing of the watch face (includes second hand animation that
 * triggers drawing faster than the normal draw request [once a second]).
 *
 * Uses [AnalogWatchFace] to for watch face state, colors, dimensions, etc.
 */
class AnalogWatchFaceRenderer(
    private val analogWatchFace: AnalogWatchFace,
    private val watchFaceRendererListener: WatchFaceRendererListener
) {

    // Update time for animations in interactive mode (second hand animation, etc.).
    private val interactiveUpdateRateMillis = TimeUnit.SECONDS.toMillis(1)

    // Used to trigger the animation every second.
    private val coroutineScope = CoroutineScope(SupervisorJob())

    // Represents half the width and height of the screen and used for animation calculations.
    // (Half both the width and height puts you in the center of the screen.)
    private var centerX = 0f
    private var centerY = 0f

    // visible = loads user styles, preps complications and animations.
    // not visible = shuts down animations.
    private var _visible = false
    var visible: Boolean
        get() = _visible
        set(isVisible) {
            _visible = isVisible

            if (visible) {
                // Loads user color preferences in case they have changed since the last
                // the watch was visible.
                analogWatchFace.loadColorPreferences()
                startSecondHandAnimation()
            } else {
                stopSecondHandAnimation()
            }
        }

    // Ambient is the battery saving mode of the watch face. We limit the color palette and
    // animations in the mode.
    private var _ambient = false
    var ambient: Boolean
        get() = _ambient
        set(inAmbientMode) {
            _ambient = inAmbientMode

            analogWatchFace.setAmbientMode(_ambient)

            if (ambient) {
                stopSecondHandAnimation()
            } else {
                startSecondHandAnimation()
            }
        }

    private var _numberOfUnreadNotifications = 0
    var numberOfUnreadNotifications: Int
        get() = _numberOfUnreadNotifications
        set(count) {
            if (analogWatchFace.unreadNotificationPref &&
                _numberOfUnreadNotifications != count &&
                count > 0) {
                    // Tells [CanvasWatchFaceService] to trigger onDraw() from system.
                    watchFaceRendererListener.onDrawRequest()
            }

            _numberOfUnreadNotifications = count
        }

    /*
     * Coroutine/flow Job that animates the second hand every second when the watch face
     * is active.
     */
    private var secondHandAnimationJob: Job? = null

    fun calculateWatchFaceDimensions(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
        centerX = width / 2f
        centerY = height / 2f

        analogWatchFace.calculateWatchFaceDimensions(width, height)
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
        canvas.drawColor(analogWatchFace.backgroundComponent.currentColor)
    }

    private fun drawUnreadNotificationIndicator(canvas: Canvas) {

        if (analogWatchFace.unreadNotificationPref && _numberOfUnreadNotifications > 0) {

            // Calculates how far up from the bottom the icon needs to be painted (Y axis).
            val localOffsetY =
                canvas.width - analogWatchFace.unreadNotificationIndicatorOffset

            canvas.drawCircle(
                centerX,
                localOffsetY.toFloat(),
                analogWatchFace.unreadNotificationIndicatorOuterRingSize.toFloat(),
                analogWatchFace.unreadNotificationCircle.paint
            )

            /*
             * Ensure center highlight circle is only drawn in interactive mode. This ensures
             * we don't burn the screen with a solid circle in ambient mode.
             */
            if (!_ambient) {
                canvas.drawCircle(
                    centerX,
                    localOffsetY.toFloat(),
                    analogWatchFace.unreadNotificationIndicatorInnerCircle.toFloat(),
                    analogWatchFace.secondHand.paint
                )
            }
        }
    }

    private fun drawComplications(canvas: Canvas, currentTimeMillis: Long) {
        var complicationDrawable: ComplicationDrawable

        val complicationDrawableSparseArray = analogWatchFace.getComplications()

        for (complicationId in AnalogWatchFace.complicationIds) {
            complicationDrawable = complicationDrawableSparseArray[complicationId]
            complicationDrawable.draw(canvas, currentTimeMillis)
        }
    }

    /**
     * Calculates watch face element locations and paints them on canvas.
     */
    private fun drawWatchFace(canvas: Canvas, calendar: Calendar) {
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
                analogWatchFace.ticks.paint
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
            centerY - analogWatchFace.centerGapOffsetAndCircleRadius,
            centerX,
            centerY - analogWatchFace.hourHand.dimensions.height,
            analogWatchFace.hourHand.paint
        )

        canvas.rotate(minutesRotation - hoursRotation, centerX, centerY)
        canvas.drawLine(
            centerX,
            centerY - analogWatchFace.centerGapOffsetAndCircleRadius,
            centerX,
            centerY - analogWatchFace.minuteHand.dimensions.height,
            analogWatchFace.minuteHand.paint
        )

        /*
         * Ensure the "seconds" hand is drawn only when we are in interactive mode.
         * Otherwise, we only update the watch face once a minute.
         */
        if (!_ambient) {
            canvas.rotate(secondsRotation - minutesRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY - analogWatchFace.centerGapOffsetAndCircleRadius,
                centerX,
                centerY - analogWatchFace.secondHand.dimensions.height,
                analogWatchFace.secondHand.paint
            )
        }

        canvas.drawCircle(
            centerX,
            centerY,
            analogWatchFace.centerGapOffsetAndCircleRadius.toFloat(),
            analogWatchFace.ticks.paint
        )

        /* Restore the canvas' original orientation. */
        canvas.restore()
    }

    fun updateWatchFaceComplication(complicationId: Int, complicationData: ComplicationData) {
        analogWatchFace.updateWatchFaceComplication(complicationId, complicationData)
    }

    fun checkTapLocation(tapType: Int, x: Int, y: Int, eventTime: Long) {
        analogWatchFace.checkTapLocation(tapType, x, y, eventTime)
    }

    fun setLowBitAndBurnInProtection(properties: Bundle) {
        val lowBitAmbient =
            properties.getBoolean(CanvasWatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false)
        val burnInProtection =
            properties.getBoolean(CanvasWatchFaceService.PROPERTY_BURN_IN_PROTECTION, false)

        analogWatchFace.setLowBitAndBurnInProtection(lowBitAmbient, burnInProtection)
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
     * Dims display in mute mode.
     */
    fun toggleDimMode(muteMode: Boolean) {
        analogWatchFace.setMuteMode(muteMode)
    }

    /**
     * Starts the coroutine/flow "timer" to animate the second hand every second when the
     * watch face is active.
     */
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

    fun tearDown() {
        stopSecondHandAnimation()
    }

    /**
     * Allows the renderer class to trigger the system onDraw() sooner for animations (example, the
     * second hand animation). The [CanvasWatchFaceService] containing the renderer simply needs to
     * specify a lambda.
     */
    class WatchFaceRendererListener(val drawListener: () -> Unit) {
        fun onDrawRequest() = drawListener()
    }
}
