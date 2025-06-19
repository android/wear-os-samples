package com.example.pomodorowatch.watchface

import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.compose.runtime.Composable
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotBounds
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.ComplicationType
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import com.example.pomodorowatch.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroWatchFaceService : WatchFaceService() {

    @Inject lateinit var timerRepository: TimerRepository

    override fun createUserStyleSchema() = UserStyleSchema(listOf())

    override fun createComplicationSlotsManager(currentUserStyleRepository: CurrentUserStyleRepository): ComplicationSlotsManager {
        val slot = androidx.wear.watchface.ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 1,
            canvasComplicationFactory = { _, _ -> null },
            supportedTypes = listOf(ComplicationType.RANGED_VALUE),
            defaultDataSourcePolicy = null,
            bounds = ComplicationSlotBounds(RectF(0.3f, 0.3f, 0.7f, 0.7f)),
            screenReaderName = ComplicationText.plainText("Timer")
        ).build()
        return ComplicationSlotsManager(listOf(slot), currentUserStyleRepository)
    }

    override fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = object : Renderer.CanvasRenderer(surfaceHolder, currentUserStyleRepository, watchState, CanvasType.HARDWARE) {
            override fun render(canvas: android.graphics.Canvas, bounds: Rect, zonedDateTime: java.time.ZonedDateTime) {
                // draw nothing for now
            }
        }
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}
