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

class AnalogWatchFaceStyle {

    // Dimensions of watch face elements (in pixels).
    // Default values are based on a 280x280 Wear device, but will be overwritten when
    // onSurfaceChanged() returns the surface dimensions of the Wear OS device.
    // NOTE: The values are in float since the drawing methods require that type.
    var centerGapAndCircleRadius = 4

    // User's preference for if they want visual shown to indicate unread notifications.
    var unreadNotificationsPreference = false

    val complicationStyle = WatchFaceColorStyle(Color.RED, Color.WHITE, Color.BLACK)

    val backgroundColor = BackgroundComponent(
        WatchFaceColorStyle(Color.BLACK, Color.BLACK, Color.BLACK),
    )

    // Colors for all hands (hour, minute, seconds, ticks) and complications.
    // Can be set by user via config Activities.
    val hourHand = WatchFaceComponent(
        WatchFaceColorStyle(Color.WHITE, Color.WHITE, Color.BLACK),
        Size(5, 70)
    )

    val minuteHand = WatchFaceComponent(
        WatchFaceColorStyle(Color.WHITE, Color.WHITE, Color.BLACK),
        Size(3, 105)
    )

    val secondHand = WatchFaceComponent(
        WatchFaceColorStyle(Color.RED, Color.WHITE, Color.BLACK),
        Size(2, 122)
    )

    // TODO: Should this be different? You actually calculate this
    val ticks = WatchFaceComponent(
        WatchFaceColorStyle(Color.WHITE, Color.WHITE, Color.BLACK),
        Size(2, 2)
    )

    // The unread notification icon is split into two parts: an outer ring that is always
    // visible when any Wear OS notification is unread and a smaller circle inside the ring
    // when the watch is in active (non-ambient) mode. The values below represent pixels.
    // NOTE: We don't draw circle in ambient mode to avoid burn-in.
    var unreadNotificationIconOuterRingSize = 10
    var unreadNotificationIconInnerCircle = 4

    // Represents how far up from the bottom (Y-axis) the icon will be drawn (in pixels).
    var unreadNotificationIconOffsetY = 40

    fun highlightColor(highlightColor:Int) {
        secondHand.colorStyle.activeColor = highlightColor
        complicationStyle.activeColor = highlightColor
    }

    data class BackgroundComponent(
        val colorStyle: WatchFaceColorStyle
    )

    data class WatchFaceComponent(
        val colorStyle: WatchFaceColorStyle,
        var size: Size
    ) {
        // TODO: Make more efficient (only change on ambient change)
        val activePaint:Paint
            get() {
                return Paint().apply {
                    color = colorStyle.activeColor
                    strokeWidth = size.width.toFloat()
                    isAntiAlias = true
                    strokeCap = Paint.Cap.ROUND
                    // Do I need to separate these?
                    style = Paint.Style.STROKE
                    setShadowLayer(
                        SHADOW_RADIUS.toFloat(),
                        0f,
                        0f,
                        colorStyle.shadowColor

                    )
                }
            }

        val ambientPaint:Paint
            get() {
                return Paint().apply {
                    color = colorStyle.ambientColor
                    strokeWidth = size.width.toFloat()
                    isAntiAlias = false
                    strokeCap = Paint.Cap.ROUND
                }
            }
    }

    data class WatchFaceColorStyle(
        var activeColor:Int,
        var ambientColor:Int,
        var shadowColor:Int
    )
}