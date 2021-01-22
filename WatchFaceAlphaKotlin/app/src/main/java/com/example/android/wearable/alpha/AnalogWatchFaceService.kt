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
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.complications.ComplicationBounds
import androidx.wear.complications.DefaultComplicationProviderPolicy
import androidx.wear.complications.SystemProviders
import androidx.wear.complications.data.ComplicationType
import androidx.wear.watchface.CanvasComplicationDrawable
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.Complication
import androidx.wear.watchface.ComplicationsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.Layer
import androidx.wear.watchface.style.UserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleEntity
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Handles much of the boilerplate needed to implement a watch face (minus rendering code; see
 * [AnalogWatchCanvasRenderer]) including the complications and settings (styles user can change on
 * the watch face).
 */
class AnalogWatchFaceService : WatchFaceService() {
    // Used to launch coroutines (non-blocking way to pull watch face color styles data [only
    // needed on the initial load of the user color style options]).
    private val scope: CoroutineScope = MainScope()

    private lateinit var analogWatchFaceViewModel: AnalogWatchFaceViewModel

    private var colorStylesList: List<WatchFaceColorStyleEntity>? = null
    private var complicationStyleDrawableId = R.drawable.complication_white_style

    override fun onCreate() {
        super.onCreate()

        analogWatchFaceViewModel =
            AnalogWatchFaceViewModel((application as MainApplication).repository)

        scope.launch {
            colorStylesList =
                analogWatchFaceViewModel.getAllWatchFaceColorStyles()
        }
    }

    override fun onDestroy() {
        analogWatchFaceViewModel.clear()
        // Cancels scope used for retrieving watch face styles.
        scope.cancel("AnalogWatchFaceService.onDestroy()")
        super.onDestroy()
    }

    override fun createWatchFace(surfaceHolder: SurfaceHolder, watchState: WatchState): WatchFace {
        // 1. Creates user styles. User styles are in memory storage for user style choices which
        // allows listeners to be registered to observe style changes.
        // In our case, we have a list of color styles (populated by the database), toggling the
        // hour ticks around the watch face on/off, and adjusting the length of the minute hand.
        val userStyleRepository = createUserStyles(
            context = this,
            colorStylesList
        )

        // 2. Initializes your complication slots (left and right).
        val complications = createComplications(
            applicationContext,
            complicationStyleDrawableId,
            watchState
        )

        // 3. Creates the [ComplicationsManager] (manages all watch faces).
        // Note: The [ComplicationsManager] also adds our complications as user styles to the user
        // style repository, so the user can edit them in the watch face settings.
        val complicationsManager = ComplicationsManager(
            complications,
            userStyleRepository
        )

        // 4. Create class that renders the watch face.
        val renderer = AnalogWatchCanvasRenderer(
            context = this,
            analogWatchFaceViewModel = analogWatchFaceViewModel,
            complicationsManager = complicationsManager,
            surfaceHolder = surfaceHolder,
            userStyleRepository = userStyleRepository,
            watchState = watchState,
            canvasType = CanvasType.HARDWARE,
            framePeriodMs = FRAME_PERIOD_MS
        )

        // 5. Create the watch face.
        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            userStyleRepository = userStyleRepository,
            complicationsManager = complicationsManager,
            renderer = renderer
        )
    }

    // All helper functions to generate the content above.
    private fun createComplications(
        context: Context,
        drawableId: Int,
        watchState: WatchState
    ): List<Complication> {
        // Create left Complication:
        // If not a valid drawable (XML complication color style), return empty list.
        val leftComplicationDrawable: ComplicationDrawable =
            ComplicationDrawable.getDrawable(context, drawableId) ?: return emptyList()

        val leftCanvasComplicationDrawable = CanvasComplicationDrawable(
            leftComplicationDrawable,
            watchState
        )

        val leftComplication = Complication.createRoundRectComplicationBuilder(
            id = ComplicationConfig.Left.id,
            renderer = leftCanvasComplicationDrawable,
            supportedTypes = ComplicationConfig.Left.supportedTypes,
            defaultProviderPolicy = DefaultComplicationProviderPolicy(SystemProviders.DAY_OF_WEEK),
            complicationBounds = ComplicationBounds(
                RectF(
                    LEFT_COMPLICATION_LEFT_BOUND,
                    LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND,
                    LEFT_COMPLICATION_RIGHT_BOUND,
                    LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND
                )
            )
        ).setDefaultProviderType(ComplicationType.SHORT_TEXT)
            .build()

        // Create right Complication:
        // If not a valid drawable (XML complication color style), return empty list (want both or
        // none).
        val rightComplicationDrawable: ComplicationDrawable =
            ComplicationDrawable.getDrawable(context, drawableId) ?: return emptyList()

        val rightCanvasComplicationDrawable = CanvasComplicationDrawable(
            rightComplicationDrawable,
            watchState
        )
        val rightComplication = Complication.createRoundRectComplicationBuilder(
            id = ComplicationConfig.Right.id,
            renderer = rightCanvasComplicationDrawable,
            supportedTypes = ComplicationConfig.Right.supportedTypes,
            defaultProviderPolicy = DefaultComplicationProviderPolicy(SystemProviders.STEP_COUNT),
            complicationBounds = ComplicationBounds(
                RectF(
                    RIGHT_COMPLICATION_LEFT_BOUND,
                    LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND,
                    RIGHT_COMPLICATION_RIGHT_BOUND,
                    LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND
                )
            )
        ).setDefaultProviderType(ComplicationType.SHORT_TEXT)
            .build()

        return listOf(leftComplication, rightComplication)
    }

    /*
     * Creates user styles in the settings activity associated with the watch face, so users can
     * edit different parts of the watch face. In the renderer (after something has changed), the
     * watch face is updated and the data is saved to the database.
     */
    private fun createUserStyles(
        context: Context,
        styles: List<WatchFaceColorStyleEntity>?
    ): UserStyleRepository {
        Log.d(TAG, "createUserStyles: $styles")

        // 1. Allows user to change the color styles of the watch face (if any are available).
        var colorStyleSetting: UserStyleSetting.ListUserStyleSetting? = null
        val options: MutableList<UserStyleSetting.ListUserStyleSetting.ListOption> =
            mutableListOf()

        // If color styles are available, add them as options in the settings.
        if (styles is List<WatchFaceColorStyleEntity>) {
            for (style in styles) {
                options.add(
                    UserStyleSetting.ListUserStyleSetting.ListOption(
                        id = style.id,
                        displayName = style.name,
                        icon = Icon.createWithResource(context, style.iconDrawableId)
                    )
                )
            }

            if (options.size > 0) {
                colorStyleSetting = UserStyleSetting.ListUserStyleSetting(
                    id = COLOR_STYLE_SETTING,
                    displayName = context.getString(R.string.colors_style_setting),
                    description = context.getString(R.string.colors_style_setting_description),
                    icon = null,
                    options = options,
                    affectsLayers = listOf(Layer.BASE_LAYER, Layer.COMPLICATIONS, Layer.TOP_LAYER)
                )
            }
        }

        // 2. Allows user to toggle on/off the hour pips (dashes around the outer edge of the watch
        // face).
        val drawHourPipsStyleSetting = UserStyleSetting.BooleanUserStyleSetting(
            id = DRAW_HOUR_PIPS_STYLE_SETTING,
            displayName = context.getString(R.string.watchface_pips_setting),
            description = context.getString(R.string.watchface_pips_setting_description),
            icon = null,
            defaultValue = true,
            affectsLayers = listOf(Layer.BASE_LAYER)
        )

        // 3. Allows user to change the length of the minute hand.
        val watchHandLengthStyleSetting = UserStyleSetting.DoubleRangeUserStyleSetting(
            id = WATCH_HAND_LENGTH_STYLE_SETTING,
            displayName = context.getString(R.string.watchface_hand_length_setting),
            description = context.getString(R.string.watchface_hand_length_setting_description),
            icon = null,
            minimumValue = 0.10000,
            defaultValue = 0.37383,
            maximumValue = 0.40000,
            affectsLayers = listOf(Layer.TOP_LAYER)
        )

        // Create style settings to hold all options.
        val userStyleSettings: MutableList<UserStyleSetting> = mutableListOf()

        if (options.isNotEmpty()) {
            userStyleSettings.add(colorStyleSetting!!)
        }

        userStyleSettings.add(drawHourPipsStyleSetting)
        userStyleSettings.add(watchHandLengthStyleSetting)

        return UserStyleRepository(
            UserStyleSchema(userStyleSettings.toList())
        )
    }

    companion object {
        private const val TAG = "AnalogWatchFaceService"

        /** How long each frame is displayed at expected frame rate.  */
        private const val FRAME_PERIOD_MS: Long = 16L

        // Keys to matched content in the  the user style settings. We listen for changes to these
        // values in the renderer and if new, we will update the database and update the watch face
        // being rendered.
        const val COLOR_STYLE_SETTING = "color_style_setting"
        const val DRAW_HOUR_PIPS_STYLE_SETTING = "draw_hour_pips_style_setting"
        const val WATCH_HAND_LENGTH_STYLE_SETTING = "watch_hand_length_style_setting"

        // Information needed for complications.
        // Creates bounds for the locations of both right and left complications. (This is the
        // location from 0.0 - 1.0.)
        // Both left and right complications use the same top and bottom bounds.
        private const val LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND = 0.4f
        private const val LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND = 0.6f

        private const val LEFT_COMPLICATION_LEFT_BOUND = 0.2f
        private const val LEFT_COMPLICATION_RIGHT_BOUND = 0.4f

        private const val RIGHT_COMPLICATION_LEFT_BOUND = 0.6f
        private const val RIGHT_COMPLICATION_RIGHT_BOUND = 0.8f

        // Unique IDs for each complication. The settings activity that supports allowing users
        // to select their complication data provider requires numbers to be >= 0.
        internal const val LEFT_COMPLICATION_ID = 100
        internal const val RIGHT_COMPLICATION_ID = 101

        /**
         * Represents the unique id associated with a complication and the complication types it
         * supports.
         */
        sealed class ComplicationConfig(val id: Int, val supportedTypes: List<ComplicationType>) {
            object Left : ComplicationConfig(
                LEFT_COMPLICATION_ID,
                listOf(
                    ComplicationType.RANGED_VALUE,
                    ComplicationType.MONOCHROMATIC_IMAGE,
                    ComplicationType.SHORT_TEXT,
                    ComplicationType.SMALL_IMAGE
                )
            )
            object Right : ComplicationConfig(
                RIGHT_COMPLICATION_ID,
                listOf(
                    ComplicationType.RANGED_VALUE,
                    ComplicationType.MONOCHROMATIC_IMAGE,
                    ComplicationType.SHORT_TEXT,
                    ComplicationType.SMALL_IMAGE
                )
            )
        }
    }
}
