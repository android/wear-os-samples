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

import android.graphics.Color
import android.graphics.Paint
import android.util.Size

const val SHADOW_RADIUS = 6

/**
 * Contains all style and dimension data to render the Analog watch face.
 *
 * Each watch face component contains three values color values (active, ambient, and shadow) and
 * dimensions (width x height) while the complication and background portions are the watch face
 * are just represented by color values.
 *
 * Component dimensions are calculated by a default 280x280 pixel device.
 */
class AnalogWatchFaceStyle {

    // Dimensions of watch face elements (in pixels).
    // Default values are based on a 280x280 Wear device, but will be overwritten when
    // onSurfaceChanged() returns the surface dimensions of the Wear OS device.
    var centerGapAndCircleRadius = 4

    // User's preference for if they want visual shown to indicate unread notifications.
    var unreadNotificationPref = false

    val complicationStyle = WatchFaceColorStyle(Color.RED, Color.WHITE, Color.BLACK)
    val backgroundColor = WatchFaceColorStyle(Color.BLACK, Color.BLACK, Color.BLACK)

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

    val notificationCircle = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(1, 1),
        // Sets alpha level of [Paint] lower than default for non-critical watch face components.
        muteModeAlpha = 80
    )

    // The unread notification indicator is split into two parts: an outer ring that is always
    // visible when any Wear OS notification is unread and a smaller circle inside the ring
    // when the watch is in active (non-ambient) mode. The values below represent pixels.
    // NOTE: We don't draw circle in ambient mode to avoid burn-in.
    var unreadNotificationIndicatorOuterRingSize = 10
    var unreadNotificationIndicatorInnerCircle = 4

    // Represents how far up from the bottom (Y-axis) the indicator will be drawn (in pixels).
    var unreadNotificationIndicatorOffsetY = 40

    // The highlight color covers both the second hand and all non-background complications.
    fun setHighlightColor(highlightColor:Int) {
        secondHand.colorStyle.activeColor = highlightColor
        complicationStyle.activeColor = highlightColor
    }

    fun setAmbientMode(ambient: Boolean) {
        hourHand.ambient = ambient
        minuteHand.ambient = ambient
        secondHand.ambient= ambient
        ticks.ambient = ambient
        notificationCircle.ambient = ambient
    }

    fun setMuteMode(muteMode: Boolean) {
        hourHand.muteMode = muteMode
        minuteHand.muteMode = muteMode
        secondHand.muteMode= muteMode
        ticks.muteMode = muteMode
        notificationCircle.muteMode = muteMode
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
                object : WatchFaceColorStyle.OnColorChangeEventListener {
                    override fun onColorChange() {
                        updatePaintColors()
                    }
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
                // Do I need to separate these?
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

        interface OnColorChangeEventListener {
            fun onColorChange()
        }
    }
}
