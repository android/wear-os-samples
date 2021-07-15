/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.datasource.ComplicationDataSourceService
import androidx.wear.complications.datasource.ComplicationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * A simple subclass of [ComplicationDataSourceService] that controls a [CoroutineScope] so that
 * [onComplicationRequest] can be suspending. This allows the complication update to be asynchronous, so that
 * suspending functions can be called to drive the update.
 */
abstract class SuspendingComplicationDataSourceService : ComplicationDataSourceService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    final override fun onComplicationRequest(request: ComplicationRequest, listener: ComplicationRequestListener) {
        scope.launch {
            var result: ComplicationData? = null
            try {
                result = onComplicationRequest(request)
            } finally {
                listener.onComplicationData(result)
            }
        }
    }

    /**
     * Computes the [ComplicationData] for the given [request].
     *
     * The [ComplicationData] returned from this method will be passed to the
     * [ComplicationDataSourceService.ComplicationRequestListener] provided to [onComplicationRequest].
     * Return `null` to indicate that the previous complication data shouldn't be overwritten.
     *
     * @see ComplicationDataSourceService.onComplicationRequest
     * @see ComplicationDataSourceService.ComplicationRequestListener.onComplicationData
     */
    abstract suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData?

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
