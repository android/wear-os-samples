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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.icu.util.Calendar
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasComplicationDrawable
import androidx.wear.watchface.ComplicationsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.LayerMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.Layer
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleRepository
import com.example.android.wearable.alpha.AnalogWatchFaceService.Companion.COLOR_STYLE_SETTING
import com.example.android.wearable.alpha.AnalogWatchFaceService.Companion.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.android.wearable.alpha.AnalogWatchFaceService.Companion.WATCH_HAND_LENGTH_STYLE_SETTING
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceAndStylesAndDimensions
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceEntity
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsEntity
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Renders watch face via data in Room database. Also, updates watch face state based on setting
 * changes by user via [userStyleRepository.addUserStyleListener()].
 */
class AnalogWatchCanvasRenderer(
    private val context: Context,
    private val analogWatchFaceViewModel: AnalogWatchFaceViewModel,
    private val complicationsManager: ComplicationsManager,
    surfaceHolder: SurfaceHolder,
    userStyleRepository: UserStyleRepository,
    private val watchState: WatchState,
    canvasType: Int,
    framePeriodMs: Long
) : Renderer.CanvasRenderer(
    surfaceHolder,
    userStyleRepository,
    watchState,
    canvasType,
    framePeriodMs
) {
    // Used to launch coroutines (non-blocking way to insert data). We need this to read and then
    // update multiple tables in the database for user style changes (colors, pip status, and
    // minute hand length).
    private val scope: CoroutineScope = MainScope()

    // Changed when setting changes cause a change in the minute hand arm (triggered by user in
    // updateUserStyle() via userStyleRepository.addUserStyleListener()).
    private var armLengthChangedRecalculateClockHands: Boolean = false

    // Default key assigned for this watch face.
    private val analogWatchFaceKeyId =
        com.example.android.wearable.alpha.data.db.analogWatchFaceKeyId

    // Contains all data to render this analog watch face (combines multiple tables).
    private var analogWatchFaceAndStylesAndDimensions: AnalogWatchFaceAndStylesAndDimensions? = null

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize: Rect = Rect(0, 0, 0, 0)

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

    init {
        // It's possible for the user to change the minute arm hand length via the settings, so
        // we need to observe any changes from that particular table [] and then render them.
        analogWatchFaceViewModel.getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceKeyId)
            .observeForever {
                val previousMinuteLength =
                analogWatchFaceAndStylesAndDimensions?.minuteHandDimensions?.lengthFraction ?: 0.0f

                val newMinuteLength = it.minuteHandDimensions.lengthFraction

                analogWatchFaceAndStylesAndDimensions = it

                if (previousMinuteLength != newMinuteLength) {
                    armLengthChangedRecalculateClockHands = true
                }
            }

        userStyleRepository.addUserStyleListener(
            object : UserStyleRepository.UserStyleListener {
                @SuppressLint("SyntheticAccessor")
                override fun onUserStyleChanged(userStyle: UserStyle) {
                    Log.d(TAG, "onUserStyleChanged(), userStyle: \n${userStyle.toMap()}")
                    scope.launch {
                        updateUserStylesInDatabase(userStyle)
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        scope.cancel("AnalogWatchCanvasRenderer.onDestroy()")
        super.onDestroy()
    }

    override fun render(canvas: Canvas, bounds: Rect, calendar: Calendar) {
        // If no data has returned yet from the database, skips this render of watch face.
        val watchFaceData: AnalogWatchFaceAndStylesAndDimensions =
            analogWatchFaceAndStylesAndDimensions ?: return

        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            watchFaceData.ambientColorStyle.backgroundColor
        } else {
            watchFaceData.activeColorStyle.backgroundColor
        }

        canvas.drawColor(backgroundColor)

        // CanvasComplicationDrawable already obeys rendererParameters.
        drawComplications(canvas, calendar)

        if (renderParameters.layerParameters[Layer.TOP_LAYER] != LayerMode.HIDE) {
            drawClockHands(canvas, bounds, calendar, watchFaceData)
        }

        if (renderParameters.drawMode != DrawMode.AMBIENT &&
            renderParameters.layerParameters[Layer.BASE_LAYER] != LayerMode.HIDE &&
            watchFaceData.watchFace.drawHourPips
        ) {
            drawNumberStyleOuterElement(
                canvas,
                bounds,
                watchFaceData.watchFace.numberRadiusFraction,
                watchFaceData.watchFace.numberStyleOuterCircleRadiusFraction,
                watchFaceData.activeColorStyle.outerElementColor,
                watchFaceData.watchFace.numberStyleOuterCircleRadiusFraction,
                watchFaceData.watchFace.gapBetweenOuterCircleAndBorderFraction
            )
        }
    }

    // All drawing functions:
    private fun drawComplications(canvas: Canvas, calendar: Calendar) {
        for ((_, complication) in complicationsManager.complications) {
            if (complication.enabled) {
                complication.render(canvas, calendar, renderParameters)
            }
        }
    }

    private fun drawClockHands(
        canvas: Canvas,
        bounds: Rect,
        calendar: Calendar,
        analogWatchFaceAndStylesAndDimensions: AnalogWatchFaceAndStylesAndDimensions
    ) {
        // Only recalculate bounds (watch face size/surface) has changed or the arm of one of the
        // clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time (when the size of the device is
        // initially broadcasted).
        if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
            armLengthChangedRecalculateClockHands = false
            currentWatchFaceSize = bounds
            recalculateClockHands(bounds, analogWatchFaceAndStylesAndDimensions)
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
            clockHandPaint.color =
                analogWatchFaceAndStylesAndDimensions.ambientColorStyle.primaryColor
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
            clockHandPaint.color =
                analogWatchFaceAndStylesAndDimensions.activeColorStyle.primaryColor
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
            clockHandPaint.color =
                analogWatchFaceAndStylesAndDimensions.activeColorStyle.secondaryColor

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
    private fun recalculateClockHands(
        bounds: Rect,
        analogWatchFaceAndStylesAndDimensions: AnalogWatchFaceAndStylesAndDimensions
    ) {
        Log.d(TAG, "recalculateClockHands()")
        hourHandBorder =
            createClockHand(
                bounds,
                analogWatchFaceAndStylesAndDimensions.hourHandDimensions.lengthFraction,
                analogWatchFaceAndStylesAndDimensions.hourHandDimensions.widthFraction,
                analogWatchFaceAndStylesAndDimensions.watchFace.gapBetweenHandAndCenterFraction,
                analogWatchFaceAndStylesAndDimensions.hourHandDimensions.xRadiusRoundedCorners,
                analogWatchFaceAndStylesAndDimensions.hourHandDimensions.yRadiusRoundedCorners
            )
        hourHandFill = hourHandBorder

        minuteHandBorder =
            createClockHand(
                bounds,
                analogWatchFaceAndStylesAndDimensions.minuteHandDimensions.lengthFraction,
                analogWatchFaceAndStylesAndDimensions.minuteHandDimensions.widthFraction,
                analogWatchFaceAndStylesAndDimensions.watchFace.gapBetweenHandAndCenterFraction,
                analogWatchFaceAndStylesAndDimensions.minuteHandDimensions.xRadiusRoundedCorners,
                analogWatchFaceAndStylesAndDimensions.minuteHandDimensions.yRadiusRoundedCorners
            )
        minuteHandFill = minuteHandBorder

        secondHand =
            createClockHand(
                bounds,
                analogWatchFaceAndStylesAndDimensions.secondHandDimensions.lengthFraction,
                analogWatchFaceAndStylesAndDimensions.secondHandDimensions.widthFraction,
                analogWatchFaceAndStylesAndDimensions.watchFace.gapBetweenHandAndCenterFraction,
                analogWatchFaceAndStylesAndDimensions.secondHandDimensions.xRadiusRoundedCorners,
                analogWatchFaceAndStylesAndDimensions.secondHandDimensions.yRadiusRoundedCorners
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

    // Saves user style changes to the database.
    suspend fun updateUserStylesInDatabase(userStyle: UserStyle) {
        val analogWatchFaceEntity: AnalogWatchFaceEntity =
            analogWatchFaceViewModel.getAnalogWatchFace(analogWatchFaceKeyId)

        updateColorStyleAndHourPips(
            userStyle,
            analogWatchFaceEntity
        )

        updateMinuteArmLength(
            userStyle,
            analogWatchFaceEntity.minuteHandDimensionsId
        )
    }

    // Updates the color style and hour hand pips (ticks on the outside of the watch) in the
    // associated [AnalogWatchFaceEntity] in the database.
    private suspend fun updateColorStyleAndHourPips(
        userStyle: UserStyle,
        analogWatchFaceEntity: AnalogWatchFaceEntity
    ) {
        var revisedAnalogWatchFaceEntity = analogWatchFaceEntity

        // 1. Updates color style associated with the watch face.
        val newColorStyleSetting: String =
            userStyle.toMap()[COLOR_STYLE_SETTING] ?: revisedAnalogWatchFaceEntity.activeColorStyleId

        if (revisedAnalogWatchFaceEntity.activeColorStyleId != newColorStyleSetting) {
            revisedAnalogWatchFaceEntity =
                revisedAnalogWatchFaceEntity.copy(activeColorStyleId = newColorStyleSetting)

            val complicationStyleDrawableId =
                analogWatchFaceViewModel.getWatchFaceColorStyles(newColorStyleSetting).complicationStyleDrawableId

            // Apply the color style to the complications. ComplicationDrawables for each of
            // the styles are defined in XML so we need to replace the complication's
            // drawables.
            for ((_, complication) in complicationsManager.complications) {
                complication.renderer = CanvasComplicationDrawable(
                    ComplicationDrawable.getDrawable(
                        context,
                        complicationStyleDrawableId)!!,
                    watchState
                )
            }
        }

        // 2. Update Hour Pips (dashes around the outside of the watch).
        val newHoursPipsSettingStringVersion: String =
            userStyle.toMap()[DRAW_HOUR_PIPS_STYLE_SETTING] ?: ""

        val newHoursPipsSetting =
            if (newHoursPipsSettingStringVersion.isEmpty()) {
                revisedAnalogWatchFaceEntity.drawHourPips
            } else {
                newHoursPipsSettingStringVersion.toBoolean()
            }

        if (revisedAnalogWatchFaceEntity.drawHourPips != newHoursPipsSetting) {
            revisedAnalogWatchFaceEntity =
                revisedAnalogWatchFaceEntity.copy(drawHourPips = newHoursPipsSetting)
        }

        analogWatchFaceViewModel.updateAnalogWatchFace(revisedAnalogWatchFaceEntity)
    }

    // Updates the length of the minute hand arm dimensions in the associated
    // [WatchFaceArmDimensionsEntity] in the database.
    private suspend fun updateMinuteArmLength(
        userStyle: UserStyle,
        minuteHandDimensionsId: String
    ) {
        val newHandLengthSettingStringVersion: String =
            userStyle.toMap()[WATCH_HAND_LENGTH_STYLE_SETTING] ?: ""

        if (newHandLengthSettingStringVersion.isNotEmpty()) {
            val newMinuteHandLength: Float =
                newHandLengthSettingStringVersion.toFloat()

            Log.d(TAG, "newMinuteHandLength: $newMinuteHandLength")

            val minuteWatchFaceArmDimensions: WatchFaceArmDimensionsEntity =
                analogWatchFaceViewModel.getWatchFaceArmDimensions(minuteHandDimensionsId)

            if (minuteWatchFaceArmDimensions.lengthFraction != newMinuteHandLength) {
                analogWatchFaceViewModel.updateWatchFaceArmDimensions(
                    minuteWatchFaceArmDimensions.copy(lengthFraction = newMinuteHandLength)
                )
            }
        }
    }

    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }
}
