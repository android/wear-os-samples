/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.alpha

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.icu.util.Calendar
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.android.wearable.alpha.data.watchface.ColorStyleIdAndResourceIds
import com.example.android.wearable.alpha.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import com.example.android.wearable.alpha.data.watchface.WatchFaceData
import com.example.android.wearable.alpha.utils.COLOR_STYLE_SETTING
import com.example.android.wearable.alpha.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.android.wearable.alpha.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

/**
 * Renders watch face via data in Room database. Also, updates watch face state based on setting
 * changes by user via [userStyleRepository.addUserStyleListener()].
 */
class AnalogWatchCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    private val analogWatchFaceViewModel: AnalogWatchFaceViewModel,
    canvasType: Int
) : Renderer.CanvasRenderer(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT
) {
    private val scope: CoroutineScope = MainScope()

    // Represents all data needed to render the watch face. All value defaults are constants. Only
    // three values are changeable by the user (color scheme, ticks being rendered, and length of
    // the minute arm). Those dynamic values are saved in the watch face APIs and we update those
    // here (in the renderer) through a Kotlin Flow.
    private var watchFaceData: WatchFaceData = WatchFaceData()

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = convertToWatchFaceColorPalette(
        context,
        watchFaceData.activeColorStyle,
        watchFaceData.ambientColorStyle
    )

    // Initializes paint object for painting the clock hands with default values.
    private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

    private val outerElementPaint = Paint().apply {
        isAntiAlias = true
    }

    // Used to paint the main hour hand text with the hour pips, i.e., 3, 6, 9, and 12 o'clock.
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.hour_mark_size).toFloat()
    }

    private lateinit var hourHandFill: Path
    private lateinit var hourHandBorder: Path
    private lateinit var minuteHandFill: Path
    private lateinit var minuteHandBorder: Path
    private lateinit var secondHand: Path

    // Changed when setting changes cause a change in the minute hand arm (triggered by user in
    // updateUserStyle() via userStyleRepository.addUserStyleListener()).
    private var armLengthChangedRecalculateClockHands: Boolean = false

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

    init {
        scope.launch {
            analogWatchFaceViewModel.uiState.collect { uiState ->
                when (uiState) {
                    is AnalogWatchFaceViewModel.UserChangesUiState.Loading -> {
                        Log.d(TAG, "StateFlow Loading: ${uiState.message}")
                    }
                    is AnalogWatchFaceViewModel.UserChangesUiState.Success -> {
                        Log.d(TAG, "StateFlow Success.")
                        updateWatchFaceData(uiState.userStyle)
                    }
                    is AnalogWatchFaceViewModel.UserChangesUiState.Error -> {
                        Log.e(TAG, "Flow error: ${uiState.exception}")
                    }
                }
            }
        }
    }

    /*
     * Triggered when the user makes changes to the watch face through the settings activity. The
     * function is called by a flow.
     */
    private fun updateWatchFaceData(userStyle: UserStyle) {
        Log.d(TAG, "updateWatchFace(): $userStyle")

        var newWatchFaceData: WatchFaceData = watchFaceData

        // Loops through user style and applies new values to watchFaceData.
        for (options in userStyle.selectedOptions) {
            when (options.key.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    val listOption = options.value as
                            UserStyleSetting.ListUserStyleSetting.ListOption

                    newWatchFaceData = newWatchFaceData.copy(
                        activeColorStyle = ColorStyleIdAndResourceIds.getColorStyleConfig(
                            listOption.id.toString()
                        )
                    )
                }
                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    val booleanValue = options.value as
                            UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                    newWatchFaceData = newWatchFaceData.copy(
                        drawHourPips = booleanValue.value
                    )
                }
                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                    val doubleValue = options.value as
                            UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

                    // Updates length of minute hand based on edits from user.
                    val newMinuteHandDimensions = newWatchFaceData.minuteHandDimensions.copy(
                        lengthFraction = doubleValue.value.toFloat()
                    )

                    newWatchFaceData = newWatchFaceData.copy(
                        minuteHandDimensions = newMinuteHandDimensions
                    )
                }
                // TODO (codingjeremy): Add complication change support if settings activity
                // PR doesn't cover it. Otherwise, remove comment.
            }
        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            // Recreates Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToWatchFaceColorPalette(
                context,
                watchFaceData.activeColorStyle,
                watchFaceData.ambientColorStyle
            )

            // Applies the user chosen complication color scheme changes. ComplicationDrawables for
            // each of the styles are defined in XML so we need to replace the complication's
            // drawables.
            for ((_, complication) in complicationSlotsManager.complicationSlots) {
                (complication.renderer as CanvasComplicationDrawable).drawable =
                    watchFaceColors.complicationStyleDrawable
            }
        }
    }

    override fun onDestroy() {
        scope.cancel("AnalogWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, calendar: Calendar) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)

        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.renderHighlightLayer(canvas, calendar, renderParameters)
            }
        }
    }

    override fun render(canvas: Canvas, bounds: Rect, calendar: Calendar) {
        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            watchFaceColors.ambientBackgroundColor
        } else {
            watchFaceColors.activeBackgroundColor
        }

        canvas.drawColor(backgroundColor)

        // CanvasComplicationDrawable already obeys rendererParameters.
        drawComplications(canvas, calendar)

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            drawClockHands(canvas, bounds, calendar)
        }

        if (renderParameters.drawMode == DrawMode.INTERACTIVE &&
            renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE) &&
            watchFaceData.drawHourPips
        ) {
            drawNumberStyleOuterElement(
                canvas,
                bounds,
                watchFaceData.numberRadiusFraction,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceColors.activeOuterElementColor,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceData.gapBetweenOuterCircleAndBorderFraction
            )
        }
    }

    // ----- All drawing functions -----
    private fun drawComplications(canvas: Canvas, calendar: Calendar) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, calendar, renderParameters)
            }
        }
    }

    private fun drawClockHands(
        canvas: Canvas,
        bounds: Rect,
        calendar: Calendar
    ) {
        // Only recalculate bounds (watch face size/surface) has changed or the arm of one of the
        // clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time (when the size of the device is
        // initially broadcasted).
        if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
            armLengthChangedRecalculateClockHands = false
            currentWatchFaceSize = bounds
            recalculateClockHands(bounds)
        }

        // Retrieve current time to calculate location/rotation of watch arms.
        val hours = calendar.get(Calendar.HOUR).toFloat()
        val minutes = calendar.get(Calendar.MINUTE).toFloat()
        val seconds = calendar.get(Calendar.SECOND).toFloat() +
                (calendar.get(Calendar.MILLISECOND).toFloat() / 1000f)

        // Determines the rotation based on 360 degrees.
        val hourRotation = (hours + minutes / 60.0f + seconds / 3600.0f) / 12.0f * 360.0f
        val minuteRotation = (minutes + seconds / 60.0f) / 60.0f * 360.0f

        canvas.save()

        if (renderParameters.drawMode == DrawMode.AMBIENT) {
            clockHandPaint.style = Paint.Style.STROKE
            clockHandPaint.color = watchFaceColors.ambientPrimaryColor

            canvas.scale(
                WATCH_HAND_SCALE,
                WATCH_HAND_SCALE,
                bounds.exactCenterX(),
                bounds.exactCenterY()
            )

            // Rotate hour hand, draw, and rotate back.
            canvas.rotate(hourRotation, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(hourHandBorder, clockHandPaint)
            canvas.rotate(-hourRotation, bounds.exactCenterX(), bounds.exactCenterY())

            // Rotate minute hand, draw, and rotate back.
            canvas.rotate(minuteRotation, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(minuteHandBorder, clockHandPaint)
            canvas.rotate(-minuteRotation, bounds.exactCenterX(), bounds.exactCenterY())
        } else {
            clockHandPaint.style = Paint.Style.FILL
            clockHandPaint.color = watchFaceColors.activePrimaryColor
            canvas.scale(
                WATCH_HAND_SCALE,
                WATCH_HAND_SCALE,
                bounds.exactCenterX(),
                bounds.exactCenterY()
            )

            // Rotate hour hand, draw, and rotate back.
            canvas.rotate(hourRotation, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(hourHandFill, clockHandPaint)
            canvas.rotate(-hourRotation, bounds.exactCenterX(), bounds.exactCenterY())

            // Rotate minute hand, draw, and rotate back.
            canvas.rotate(minuteRotation, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(minuteHandFill, clockHandPaint)
            canvas.rotate(-minuteRotation, bounds.exactCenterX(), bounds.exactCenterY())

            // Second hand has a different color style (secondary color) and is only drawn in
            // active mode, so we calculate it here (not above with others).
            val secondsRotation = seconds / 60.0f * 360.0f
            clockHandPaint.color = watchFaceColors.activeSecondaryColor

            // Rotate second hand, draw, and rotate back.
            canvas.rotate(secondsRotation, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(secondHand, clockHandPaint)
            canvas.rotate(-secondsRotation, bounds.exactCenterX(), bounds.exactCenterY())
        }
        canvas.restore()
    }

    /*
     * Rarely called (only when watch face surface changes; usually only once) from the
     * drawClockHands() method.
     */
    private fun recalculateClockHands(bounds: Rect) {
        Log.d(TAG, "recalculateClockHands()")
        hourHandBorder =
            createClockHand(
                bounds,
                watchFaceData.hourHandDimensions.lengthFraction,
                watchFaceData.hourHandDimensions.widthFraction,
                watchFaceData.gapBetweenHandAndCenterFraction,
                watchFaceData.hourHandDimensions.xRadiusRoundedCorners,
                watchFaceData.hourHandDimensions.yRadiusRoundedCorners
            )
        hourHandFill = hourHandBorder

        minuteHandBorder =
            createClockHand(
                bounds,
                watchFaceData.minuteHandDimensions.lengthFraction,
                watchFaceData.minuteHandDimensions.widthFraction,
                watchFaceData.gapBetweenHandAndCenterFraction,
                watchFaceData.minuteHandDimensions.xRadiusRoundedCorners,
                watchFaceData.minuteHandDimensions.yRadiusRoundedCorners
            )
        minuteHandFill = minuteHandBorder

        secondHand =
            createClockHand(
                bounds,
                watchFaceData.secondHandDimensions.lengthFraction,
                watchFaceData.secondHandDimensions.widthFraction,
                watchFaceData.gapBetweenHandAndCenterFraction,
                watchFaceData.secondHandDimensions.xRadiusRoundedCorners,
                watchFaceData.secondHandDimensions.yRadiusRoundedCorners
            )
    }

    /**
     * Returns a round rect clock hand if {@code rx} and {@code ry} equals to 0, otherwise return a
     * rect clock hand.
     *
     * @param bounds The bounds use to determine the coordinate of the clock hand.
     * @param length Clock hand's length, in fraction of {@code bounds.width()}.
     * @param thickness Clock hand's thickness, in fraction of {@code bounds.width()}.
     * @param gapBetweenHandAndCenter Gap between inner side of arm and center.
     * @param roundedCornerXRadius The x-radius of the rounded corners on the round-rectangle.
     * @param roundedCornerYRadius The y-radius of the rounded corners on the round-rectangle.
     */
    private fun createClockHand(
        bounds: Rect,
        length: Float,
        thickness: Float,
        gapBetweenHandAndCenter: Float,
        roundedCornerXRadius: Float,
        roundedCornerYRadius: Float
    ): Path {
        val width = bounds.width()
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val left = centerX - thickness / 2 * width
        val top = centerY - (gapBetweenHandAndCenter + length) * width
        val right = centerX + thickness / 2 * width
        val bottom = centerY - gapBetweenHandAndCenter * width
        val path = Path()

        if (roundedCornerXRadius != 0.0f || roundedCornerYRadius != 0.0f) {
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                roundedCornerXRadius,
                roundedCornerYRadius,
                Path.Direction.CW
            )
        } else {
            path.addRect(
                left,
                top,
                right,
                bottom,
                Path.Direction.CW
            )
        }
        return path
    }

    private fun drawNumberStyleOuterElement(
        canvas: Canvas,
        bounds: Rect,
        numberRadiusFraction: Float,
        outerCircleStokeWidthFraction: Float,
        outerElementColor: Int,
        numberStyleOuterCircleRadiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {

        // Draws text hour indicators (12, 3, 6, and 9).
        val textBounds = Rect()
        textPaint.color = outerElementColor
        for (i in 0 until 4) {
            val rotation = 0.5f * (i + 1).toFloat() * Math.PI
            val dx = sin(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            val dy = -cos(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            textPaint.getTextBounds(HOUR_MARKS[i], 0, HOUR_MARKS[i].length, textBounds)
            canvas.drawText(
                HOUR_MARKS[i],
                bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
                bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
                textPaint
            )
        }

        // Draws dots for the remain hour indicators between the numbers above.
        outerElementPaint.strokeWidth = outerCircleStokeWidthFraction * bounds.width()
        outerElementPaint.color = outerElementColor
        canvas.save()
        for (i in 0 until 12) {
            if (i % 3 != 0) {
                drawTopMiddleCircle(
                    canvas,
                    bounds,
                    numberStyleOuterCircleRadiusFraction,
                    gapBetweenOuterCircleAndBorderFraction
                )
            }
            canvas.rotate(360.0f / 12.0f, bounds.exactCenterX(), bounds.exactCenterY())
        }
        canvas.restore()
    }

    /** Draws the outer circle on the top middle of the given bounds. */
    private fun drawTopMiddleCircle(
        canvas: Canvas,
        bounds: Rect,
        radiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        outerElementPaint.style = Paint.Style.FILL_AND_STROKE

        // X and Y coordinates of the center of the circle.
        val centerX = 0.5f * bounds.width().toFloat()
        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)

        canvas.drawCircle(
            centerX,
            centerY,
            radiusFraction * bounds.width(),
            outerElementPaint
        )
    }

    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }
}
