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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.util.Size
import android.util.SparseArray

private const val SHADOW_RADIUS = 6

/**
 * Contains state, style, and dimension data for an analog watch face.
 *
 * TODO: Rewrite this
 *
 * Each watch face component contains three values color values (active, ambient, and shadow) and
 * dimensions (width x height) while the complication and background portions are the watch face
 * are just represented by color values.
 *
 * Component dimensions are calculated by a default 280x280 pixel device.
 */
class AnalogWatchFace (context: Context) {

    // TODO: Move to separate class?
    // Used to pull user's preferences for background color, highlight color, and visual
    // indicating there are unread notifications.
    private val loadSavedWatchFace = LoadSavedWatchFace(context)

    // The unread notification indicator is split into two parts: an outer ring that is always
    // visible when any Wear OS notification is unread and a smaller circle inside the ring
    // when the watch is in active (non-ambient) mode. The values below represent pixels.
    // NOTE: We don't draw circle in ambient mode to avoid burn-in.
    var unreadNotificationIndicatorOuterRingSize = 10
    var unreadNotificationIndicatorInnerCircle = 4

    // Represents how far up from the bottom (Y-axis) the indicator will be drawn (in pixels).
    var unreadNotificationIndicatorOffsetY = 40

    // Dimensions of watch face elements (in pixels).
    // Default values are based on a 280x280 Wear device, but will be overwritten when
    // onSurfaceChanged() returns the surface dimensions of the Wear OS device.
    var centerGapAndCircleRadius = 4

    // Default colors for all watch face components (hands [hour, minute, seconds, ticks],
    // complications.
    // Highlight and background colors are saved and loaded from XYZ and can be configured in the
    // [AnalogComplicationConfigActivity].
    // TODO: above.
    val hourHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(5, 70)
    )

    val minuteHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(3, 105)
    )

    val secondHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.RED,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(2, 122),
        // Sets alpha level of [Paint] lower than default for non-critical watch face components.
        muteModeAlpha = 80
    )

    val ticks = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(2, 2),
        // Sets alpha level of [Paint] lower than default for non-critical watch face components.
        muteModeAlpha = 80
    )

    // User's preference for if they want visual shown to indicate unread notifications.
    var unreadNotificationPref = false
    val unreadNotificationCircle = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(1, 1),
        // Sets alpha level of [Paint] lower than default for non-critical watch face components.
        muteModeAlpha = 80
    )

    // Used to paint the background of the watch face.
    val backgroundComponent = WatchFaceBackground(
        colorStyle = WatchFaceColorStyle(Color.BLACK, Color.BLACK, Color.BLACK)
    )

    private val complicationColors = WatchFaceColorStyle(Color.RED, Color.WHITE, Color.BLACK)

    /* Maps complication ids (unique for each location) to corresponding [ComplicationDrawable].
     * The [ComplicationDrawable] is a system API that renders complication data on the watch face.
     */
    private var complicationDrawableSparseArray: SparseArray<ComplicationDrawable>


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
            complicationColors.activeColor,
            complicationColors.ambientColor
        )
    }

    // TODO: Fix this
    fun loadColorPreferences() {
        // User's style preferences may have changed since the last time the
        // watch face was visible, so we load the latest.
        loadSavedWatchFace.load(this)

        // With the rest of the watch face, we update the paint colors based on
        // ambient/active mode callbacks, but because the ComplicationDrawable handles
        // the active/ambient colors, we only need to update the complications' colors when
        // the user actually makes a change to the highlight color, not when the watch goes
        // in and out of ambient mode.
        setComplicationsActiveAndAmbientColors(
            complicationColors.activeColor,
            complicationColors.ambientColor
        )

    }

    /**
     * Used by renderer to get full list of complications (that have all proper colors set) to
     * render with the rest of the watch face.
     */
    fun getComplications(): SparseArray<ComplicationDrawable> {
        return complicationDrawableSparseArray
    }

    /**
     * The highlight color covers both the second hand and all non-background complications.
     */
    fun setHighlightColor(highlightColor:Int) {
        secondHand.colorStyle.activeColor = highlightColor
        complicationColors.activeColor = highlightColor
    }

    fun setAmbientMode(ambient: Boolean) {
        hourHand.ambient = ambient
        minuteHand.ambient = ambient
        secondHand.ambient= ambient
        ticks.ambient = ambient
        unreadNotificationCircle.ambient = ambient
        backgroundComponent.ambient = ambient

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, so we just
        // have to set the current ambient mode and it does the rest.
        for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
            // Sets ComplicationDrawable ambient mode
            complicationDrawableSparseArray[complicationId].setInAmbientMode(ambient)
        }
    }

    fun setMuteMode(muteMode: Boolean) {
        hourHand.muteMode = muteMode
        minuteHand.muteMode = muteMode
        secondHand.muteMode= muteMode
        ticks.muteMode = muteMode
        unreadNotificationCircle.muteMode = muteMode
    }

    fun updateWatchFaceComplication(complicationId: Int, complicationData: ComplicationData) {

        if (complicationId == AnalogComplicationWatchFaceService.BACKGROUND_COMPLICATION_ID) {
            // If background image isn't the correct type, it means either there was no data,
            // a permission problem, or the data was empty, i.e., user deselected it.
            backgroundComponent.backgroundComplicationActive =
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

    fun setLowBitAndBurnInProtection(lowBitAmbient:Boolean, burnInProtection:Boolean) {
        backgroundComponent.lowBitAmbient = lowBitAmbient
        backgroundComponent.burnInProtection = burnInProtection

        // Updates complications to properly render in ambient mode based on the screen's
        // capabilities.
        for (complicationId in AnalogComplicationWatchFaceService.complicationIds) {
            complicationDrawableSparseArray[complicationId].apply {
                setLowBitAmbient(lowBitAmbient)
                setBurnInProtection(burnInProtection)
            }
        }
    }

    fun calculateWatchFaceDimensions(width: Int, height: Int) {
        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
        val halfWidth = width / 2f
        val halfHeight = height / 2f

        /*
         * Calculate lengths of different hands based on half the watch screen size (centerX).
         */
        val hourHeight = (halfWidth * 0.5).toInt()
        val minuteHeight = (halfWidth * 0.75).toInt()
        val secondHeight = (halfWidth * 0.875).toInt()

        // Calculates stroke width and radius by total screen size.
        val hourWidth = (width / 56)
        val minuteWidth = (width / 93)

        val secondWidth = (width / 140)

        hourHand.dimensions = Size(hourWidth, hourHeight)
        minuteHand.dimensions = Size(minuteWidth, minuteHeight)
        secondHand.dimensions = Size(secondWidth, secondHeight)

        // TODO: Add notes
        ticks.dimensions = Size(secondWidth, secondWidth)


        centerGapAndCircleRadius = (width / 70)

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

    /* Sets active/ambient mode colors for all complications.
     *
     * With the rest of the watch face, we update the paint colors based on ambient/active mode
     * callbacks, but because the ComplicationDrawable handles the active/ambient colors itself,
     * we only set the colors twice. Once at initialization and again if the user changes the
     * highlight color via AnalogComplicationConfigActivity.
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

    class WatchFaceBackground (
        val colorStyle: WatchFaceColorStyle,
        var ambient: Boolean = false,
        var backgroundComplicationActive:Boolean = false,
        var lowBitAmbient: Boolean = false,
        var burnInProtection: Boolean = false
    ) {
        val currentColor: Int
            get() {
                // Background should always be black when in ambient, low bit and burn in, or
                // if a background complication is active, i.e., a background image is set, we also
                // want the background to be black.
                return when {
                    ambient || (lowBitAmbient && burnInProtection) || backgroundComplicationActive ->
                        colorStyle.ambientColor

                    else ->
                        colorStyle.activeColor
                }
            }
    }

    /**
     * Represents components on a watch face such as a watch face hour arm, a minute arm, ticks
     * around the watch face, etc.
     *
     * The class also contains a [Paint] object to be used to render the component on a [Canvas].
     * Because the onDraw() call is executed quite often, it's more efficient to store a single
     * [Paint] object than recreate it every time. Also, the [Paint] object changes based on whether
     * or not the watch face is in ambient mode.
     */
    class WatchFaceComponent (
        val colorStyle: WatchFaceColorStyle,
        var dimensions: Size,
        var muteModeAlpha: Int = 100
    ) {
        init {
            // If any of the colors are changed in this component, we will update the [Paint]
            // object (which is used to draw the component).
            colorStyle.setOnColorChangeEventListener(
                WatchFaceColorStyle.OnColorChangeEventListener {
                    updatePaintColors()
                }
            )
        }

        private var _paint:Paint = createActivePaint()

        private var _muteMode = false
        var muteMode: Boolean
            get() = _muteMode
            set(value) {
                _muteMode = value
                updatePaintColors()
            }

        val paint: Paint
            get() = _paint

        private var _ambient = false
        var ambient:Boolean
            get() = _ambient
            set(ambient) {
                _ambient = ambient
                updatePaintColors()
            }

        private fun updatePaintColors() {
            _paint = if (_ambient) {
                createAmbientPaint()
            } else {
                createActivePaint()
            }
        }

        private fun createActivePaint(): Paint {
            return Paint().apply {
                color = colorStyle.activeColor
                strokeWidth = dimensions.width.toFloat()
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                alpha = if (_muteMode) muteModeAlpha else 255
                setShadowLayer(
                    SHADOW_RADIUS.toFloat(),
                    0f,
                    0f,
                    colorStyle.shadowColor
                )
            }
        }

        private fun createAmbientPaint(): Paint {
            return Paint().apply {
                color = colorStyle.ambientColor
                strokeWidth = dimensions.width.toFloat()
                isAntiAlias = false
                strokeCap = Paint.Cap.ROUND
            }
        }
    }

    /**
     * Represents all possible colors for a watch face color style. For each color style, three
     * colors are needed:
     * * activeColor - Device is in active mode (full color)
     * * ambientColor - Device is in ambient mode (limited colors, functionality, etc.)
     * * shadowColor - Color used beneath all edges of component.
     *
     * The class all contains an optional listener for color changes.
     */
    class WatchFaceColorStyle(
        activeColorArgument:Int,
        ambientColorArgument:Int,
        shadowColorArgument:Int
    ) {
        private var _activeColor = activeColorArgument
        var activeColor
            get() = _activeColor
            set(value) {
                _activeColor = value
                colorChangeListener?.onColorChange()
            }

        private var _ambientColor = ambientColorArgument
        var ambientColor
            get() = _ambientColor
            set(value) {
                _ambientColor = value
                colorChangeListener?.onColorChange()
            }

        private var _shadowColor = shadowColorArgument
        var shadowColor
            get() = _shadowColor
            set(value) {
                _shadowColor = value
                colorChangeListener?.onColorChange()
            }

        private var colorChangeListener:OnColorChangeEventListener? = null

        fun setOnColorChangeEventListener(listener:OnColorChangeEventListener) {
            colorChangeListener = listener
        }

        class OnColorChangeEventListener (val colorListener: () -> Unit) {
            fun onColorChange() = colorListener()
        }
    }

    // TODO: Move to separate class?
    class LoadSavedWatchFace (context: Context) {

        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
            analog_complication_preference_file_key,
            CanvasWatchFaceService.MODE_PRIVATE
        )

        // Pulls all user's preferences for watch face appearance.
        fun load(analogWatchFace:AnalogWatchFace) {

            analogWatchFace.backgroundComponent.colorStyle.activeColor =
                sharedPreferences.getInt(saved_background_color_pref, Color.BLACK)

            val highlightColor = sharedPreferences.getInt(saved_highlight_color_pref, Color.RED)

            analogWatchFace.setHighlightColor(highlightColor)

            analogWatchFace.unreadNotificationPref =
                sharedPreferences.getBoolean(saved_unread_notifications_pref, true)

        }
    }
    companion object {
        const val analog_complication_preference_file_key =
            "com.example.android.wearable.watchfacekotlin.ANALOG_WATCH_FACE_PREFERENCE_FILE_KEY"
        const val saved_highlight_color_pref = "saved_markers_color"
        const val saved_background_color_pref = "saved_background_color"
        const val saved_unread_notifications_pref = "saved_unread_notifications"
    }
}
