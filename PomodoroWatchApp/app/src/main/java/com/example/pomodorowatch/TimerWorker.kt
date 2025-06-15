package com.example.pomodorowatch

import android.content.Context
import android.content.ComponentName
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationDataSourceUpdateRequester
import com.example.pomodorowatch.watchface.ComplicationDataSourceService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ComplicationDataSourceUpdateRequester
            .create(applicationContext, ComponentName(applicationContext, ComplicationDataSourceService::class.java))
            .requestUpdateAll()
        return Result.success()
    }
}
