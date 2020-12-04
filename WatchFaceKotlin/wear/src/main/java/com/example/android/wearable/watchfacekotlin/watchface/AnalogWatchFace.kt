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

/**
 * Contains state, style, and dimension data for an analog watch face.
 *
 * Each main watch face component (arms, ticks, etc.) contains three values color values (active,
 * ambient, and shadow), dimensions (width x height), state, and provide a [Paint] object based on
 * state to simplify the rendering process, that is, the renderer can just call for the [Paint]
 * object to use and not try to figure out what color to use based on state.
 *
 * While the complications and background related objects are somewhat simpler since they are
 * drawn differently (more information below).
 */
class AnalogWatchFace(context: Context) {

    // Allows loading of user's preferences for background color, highlight color, and visual
    // indicator for unread notifications.
    private val loadSavedWatchFace = WatchFacePrefLoader(context = context, analogWatchFace = this)

    // The unread notification indicator is split into two parts: an outer ring that is always
    // visible when any Wear OS notification is unread and a smaller circle inside the ring
    // when the watch is in active (non-ambient) mode. The values below represent pixels but is
    // calculated by the size of the device.
    // NOTE: We don't draw circle in ambient mode to avoid burn-in.
    var unreadNotificationIndicatorOuterRingSize = DEFAULT_OUTER_RING
    var unreadNotificationIndicatorInnerCircle = DEFAULT_INNER_RING

    // Represents how far up from the bottom (Y-axis) the indicator will be drawn (in pixels).
    var unreadNotificationIndicatorOffset = DEFAULT_NOTIFICATION_OFFSET

    // Represents the empty center (and circle) of the watch face. All watch hands are drawn away
    // from the center by this amount.
    // NOTE: You don't want watch faces drawing in the center continuously, as it can cause burn-in
    // which is why we don't draw anything in the center.
    var centerGapOffsetAndCircleRadius = DEFAULT_GAP_AND_CIRCLE_RADIUS

    // Watch face components (hands [hour, minute, seconds, ticks]).
    val hourHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(DEFAULT_HOUR_HAND_WIDTH, DEFAULT_HOUR_HAND_HEIGHT)
    )

    val minuteHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(DEFAULT_MINUTE_HAND_WIDTH, DEFAULT_MINUTE_HAND_HEIGHT)
    )

    val secondHand = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.RED,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(DEFAULT_SECOND_HAND_WIDTH, DEFAULT_SECOND_HAND_HEIGHT),
        muteModeAlpha = ALPHA_EXTRA_DIM
    )

    val ticks = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(DEFAULT_TICKS_WIDTH, DEFAULT_TICKS_HEIGHT),
        muteModeAlpha = ALPHA_EXTRA_DIM
    )

    // User's preference for if they want visual shown to indicate unread notifications.
    var unreadNotificationPref = false
    val unreadNotificationCircle = WatchFaceComponent(
        colorStyle = WatchFaceColorStyle(
            Color.WHITE,
            Color.WHITE,
            Color.BLACK
        ),
        dimensions = Size(DEFAULT_UNREAD_WIDTH, DEFAULT_UNREAD_HEIGHT),
        muteModeAlpha = ALPHA_EXTRA_DIM
    )

    // Background color for the watch face.
    val backgroundComponent = WatchFaceBackground(
        colorStyle = WatchFaceColorStyle(Color.BLACK, Color.BLACK, Color.BLACK)
    )

    // Colors for all non-background complications.
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
            SparseArray(complicationIds.size)
        complicationDrawableSparseArray.put(
            LEFT_COMPLICATION_ID,
            leftComplicationDrawable
        )
        complicationDrawableSparseArray.put(
            RIGHT_COMPLICATION_ID,
            rightComplicationDrawable
        )
        complicationDrawableSparseArray.put(
            BACKGROUND_COMPLICATION_ID,
            backgroundComplicationDrawable
        )

        setComplicationsActiveAndAmbientColors(
            complicationColors.activeColor,
            complicationColors.ambientColor
        )
    }

    // Used to trigger loading the user preferences because the watch face has become visible.
    fun loadColorPreferences() {
        // User's style preferences may have changed since the last time the
        // watch face was visible, so we load the latest preferences.
        loadSavedWatchFace.loadPreferences()

        // With the rest of the watch face, we update the paint colors based on
        // ambient/active mode callbacks, but because the [ComplicationDrawable] handles
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
    fun setHighlightColor(highlightColor: Int) {
        secondHand.colorStyle.activeColor = highlightColor
        complicationColors.activeColor = highlightColor
    }

    fun setAmbientMode(ambient: Boolean) {
        hourHand.ambient = ambient
        minuteHand.ambient = ambient
        secondHand.ambient = ambient
        ticks.ambient = ambient
        unreadNotificationCircle.ambient = ambient
        backgroundComponent.ambient = ambient

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, so we just
        // have to set the current ambient mode and it does the rest.
        for (complicationId in complicationIds) {
            // Sets ComplicationDrawable ambient mode
            complicationDrawableSparseArray[complicationId].setInAmbientMode(ambient)
        }
    }

    fun setMuteMode(muteMode: Boolean) {
        hourHand.muteMode = muteMode
        minuteHand.muteMode = muteMode
        secondHand.muteMode = muteMode
        ticks.muteMode = muteMode
        unreadNotificationCircle.muteMode = muteMode
    }

    fun updateWatchFaceComplication(complicationId: Int, complicationData: ComplicationData) {
        if (complicationId == BACKGROUND_COMPLICATION_ID) {
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

    fun setLowBitAndBurnInProtection(lowBitAmbient: Boolean, burnInProtection: Boolean) {
        backgroundComponent.lowBitAmbient = lowBitAmbient
        backgroundComponent.burnInProtection = burnInProtection

        // Updates complications to properly render in ambient mode based on the screen's
        // capabilities.
        for (complicationId in complicationIds) {
            complicationDrawableSparseArray[complicationId].apply {
                setLowBitAmbient(lowBitAmbient)
                setBurnInProtection(burnInProtection)
            }
        }
    }

    /**
     * Calculate lengths of different watch face components based on device screen size.
     */
    fun calculateWatchFaceDimensions(width: Int, height: Int) {

        val hourHeight = (HOUR_HAND_HEIGHT_RATIO * width).toInt()
        val minuteHeight = (MINUTE_HAND_HEIGHT_RATIO * width).toInt()
        val secondHeight = (SECOND_HAND_HEIGHT_RATIO * width).toInt()
        val ticksHeight = (TICKS_HEIGHT_RATIO * width).toInt()

        val hourWidth = (HOUR_HAND_WIDTH_RATIO * width).toInt()
        val minuteWidth = (MINUTE_HAND_WIDTH_RATIO * width).toInt()
        val secondWidth = (SECOND_HAND_WIDTH_RATIO * width).toInt()
        val ticksWidth = (TICKS_WIDTH_RATIO * width).toInt()

        hourHand.dimensions = Size(hourWidth, hourHeight)
        minuteHand.dimensions = Size(minuteWidth, minuteHeight)
        secondHand.dimensions = Size(secondWidth, secondHeight)
        ticks.dimensions = Size(ticksWidth, ticksHeight)

        unreadNotificationIndicatorOuterRingSize = UNREAD_NOTIFICATION_OUTER_RING_RATIO * width
        unreadNotificationIndicatorInnerCircle = UNREAD_NOTIFICATION_INNER_RING_RATIO * width
        unreadNotificationIndicatorOffset = UNREAD_NOTIFICATION_OFFSET_RATIO * height

        centerGapOffsetAndCircleRadius = (GAP_AND_CIRCLE_RATIO * width).toInt()

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
            complicationDrawableSparseArray[LEFT_COMPLICATION_ID]
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

    /* Sets active/ambient mode colors for all complications.
     *
     * With the rest of the watch face, we update the paint colors based on ambient/active mode
     * callbacks, but because the ComplicationDrawable handles the active/ambient colors itself,
     * we only set the colors twice. Once at initialization and again if the user changes the
     * highlight color via AnalogComplicationConfigActivity.
     */
    private fun setComplicationsActiveAndAmbientColors(activeColor: Int, ambientColor: Int) {
        var complicationDrawable: ComplicationDrawable

        for (complicationId in complicationIds) {
            complicationDrawable = complicationDrawableSparseArray[complicationId]
            if (complicationId == BACKGROUND_COMPLICATION_ID) {
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

    /**
     * Represents the background color of the watch face. This will be black if the device is in
     * ambient mode, low bit mode with burn in protection enabled, or if a background complication
     * is active (meaning there is a image being draw for the background).
     *
     * Otherwise, it will use the active color (chosen by the user).
     */
    class WatchFaceBackground(
        val colorStyle: WatchFaceColorStyle,
        var ambient: Boolean = false,
        var backgroundComplicationActive: Boolean = false,
        var lowBitAmbient: Boolean = false,
        var burnInProtection: Boolean = false
    ) {
        val currentColor: Int
            get() {
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
    class WatchFaceComponent(
        val colorStyle: WatchFaceColorStyle,
        var dimensions: Size,
        var muteModeAlpha: Int = ALPHA_DIM_DEFAULT
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

        private var _muteMode = false
        var muteMode: Boolean
            get() = _muteMode
            set(value) {
                _muteMode = value
                updatePaintColors()
            }

        private var _ambient = false
        var ambient: Boolean
            get() = _ambient
            set(ambient) {
                _ambient = ambient
                updatePaintColors()
            }

        private var _paint: Paint = createActivePaint()
        val paint: Paint
            get() = _paint

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
                alpha = if (_muteMode) muteModeAlpha else ALPHA_FULLY_OPAQUE
                setShadowLayer(
                    SHADOW_RADIUS,
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
                style = Paint.Style.STROKE
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
     * The class contains an optional listener for color changes.
     */
    class WatchFaceColorStyle(
        activeColorArgument: Int,
        ambientColorArgument: Int,
        shadowColorArgument: Int
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

        private var colorChangeListener: OnColorChangeEventListener? = null

        fun setOnColorChangeEventListener(listener: OnColorChangeEventListener) {
            colorChangeListener = listener
        }

        class OnColorChangeEventListener(val colorListener: () -> Unit) {
            fun onColorChange() = colorListener()
        }
    }

    /**
     * Loads user preferences for highlight color, background color, and unread notification
     * indicator.
     */
    class WatchFacePrefLoader(context: Context, private val analogWatchFace: AnalogWatchFace) {

        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
            analog_complication_preference_file_key,
            CanvasWatchFaceService.MODE_PRIVATE
        )

        // Pulls all user's preferences for watch face appearance.
        fun loadPreferences() {
            analogWatchFace.backgroundComponent.colorStyle.activeColor =
                sharedPreferences.getInt(saved_background_color_pref, Color.BLACK)

            val highlightColor = sharedPreferences.getInt(saved_highlight_color_pref, Color.RED)

            analogWatchFace.setHighlightColor(highlightColor)

            analogWatchFace.unreadNotificationPref =
                sharedPreferences.getBoolean(saved_unread_notifications_pref, true)
        }
    }
    companion object {

        // Unique IDs for each complication. The settings activity that supports allowing users
        // to select their complication data provider requires numbers to be >= 0.
        internal const val BACKGROUND_COMPLICATION_ID = 0
        internal const val LEFT_COMPLICATION_ID = 100
        internal const val RIGHT_COMPLICATION_ID = 101

        sealed class ComplicationConfig(val id: Int, val supportedTypes: IntArray) {
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

        // Used by {@link AnalogComplicationConfigActivity} and this class to retrieve all
        // complication ids. Background, Left, and right complication IDs as array for
        // Complication API.
        val complicationIds = intArrayOf(
            ComplicationConfig.Background.id,
            ComplicationConfig.Left.id,
            ComplicationConfig.Right.id
        )

        // [SharedPreference] strings to save user preferences for the appearance of the watch
        // face.
        const val analog_complication_preference_file_key =
            "com.example.android.wearable.watchfacekotlin.ANALOG_WATCH_FACE_PREFERENCE_FILE_KEY"

        const val saved_highlight_color_pref = "saved_markers_color"
        const val saved_background_color_pref = "saved_background_color"
        const val saved_unread_notifications_pref = "saved_unread_notifications"

        // Ratios for how large each component of the watch face is based on the overall size of
        // the watch face.
        private const val HOUR_HAND_WIDTH_RATIO = 0.018
        private const val HOUR_HAND_HEIGHT_RATIO = 0.3

        private const val MINUTE_HAND_WIDTH_RATIO = 0.011
        private const val MINUTE_HAND_HEIGHT_RATIO = 0.4

        private const val SECOND_HAND_WIDTH_RATIO = 0.007
        private const val SECOND_HAND_HEIGHT_RATIO = 0.46

        private const val TICKS_WIDTH_RATIO = 0.007
        private const val TICKS_HEIGHT_RATIO = 0.007

        private const val GAP_AND_CIRCLE_RATIO = 0.014

        private const val UNREAD_NOTIFICATION_OFFSET_RATIO = 0.143

        private const val UNREAD_NOTIFICATION_OUTER_RING_RATIO = 0.036
        private const val UNREAD_NOTIFICATION_INNER_RING_RATIO = 0.014

        private const val SHADOW_RADIUS = 6f

        // Alpha options for [Paint] objects. They are used to dim the watch face when in do not
        // disturb mode.
        private const val ALPHA_FULLY_OPAQUE = 255
        private const val ALPHA_DIM_DEFAULT = 100
        private const val ALPHA_EXTRA_DIM = 80

        // Default dimensions of watch face elements (in pixels) based on a 280x280 Wear device.
        // They will be overwritten when the device dimensions of the Wear OS device is passed into
        // to the watch face.
        private const val DEFAULT_OUTER_RING = 10.0
        private const val DEFAULT_INNER_RING = 4.0
        private const val DEFAULT_NOTIFICATION_OFFSET = 40.0

        private const val DEFAULT_GAP_AND_CIRCLE_RADIUS = 4

        private const val DEFAULT_HOUR_HAND_WIDTH = 5
        private const val DEFAULT_HOUR_HAND_HEIGHT = 70

        private const val DEFAULT_MINUTE_HAND_WIDTH = 3
        private const val DEFAULT_MINUTE_HAND_HEIGHT = 105

        private const val DEFAULT_SECOND_HAND_WIDTH = 2
        private const val DEFAULT_SECOND_HAND_HEIGHT = 122

        private const val DEFAULT_TICKS_WIDTH = 2
        private const val DEFAULT_TICKS_HEIGHT = 2

        private const val DEFAULT_UNREAD_WIDTH = 1
        private const val DEFAULT_UNREAD_HEIGHT = 1
    }
}
