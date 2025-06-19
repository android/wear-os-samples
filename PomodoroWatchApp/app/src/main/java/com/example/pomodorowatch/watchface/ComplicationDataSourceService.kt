package com.example.pomodorowatch.watchface

import androidx.wear.complications.datasource.ComplicationDataSourceService
import androidx.wear.complications.datasource.ComplicationRequest
import androidx.wear.complications.datasource.ComplicationRequestListener
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.RangedValueComplicationData
import androidx.wear.complications.data.ComplicationText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.example.pomodorowatch.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComplicationDataSourceService : ComplicationDataSourceService() {
    @Inject lateinit var timerRepository: TimerRepository

    override fun onComplicationRequest(request: ComplicationRequest, listener: ComplicationRequestListener) {
        val data = when (request.complicationType) {
            ComplicationType.RANGED_VALUE -> createRangedValueData()
            else -> null
        }
        if (data != null) listener.onComplicationData(data) else listener.onComplicationData(null)
    }

    private fun createRangedValueData(): ComplicationData {
        val state = runBlocking { timerRepository.observeState().first() }
        return RangedValueComplicationData.Builder(
            value = state.remainingMillis / 1000f,
            min = 0f,
            max = 1500f,
            text = ComplicationText.plainText("${'$'}{state.remainingMillis / 1000}s")
        ).build()
    }
}
