/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * A simple subclass of [ComplicationProviderService] that controls a [CoroutineScope] so that
 * [onComplicationUpdateImpl] can be suspending. This allows the complication update to be asynchronous, so that
 * suspending functions can be called to drive the update.
 */
abstract class SuspendingComplicationProviderService : ComplicationProviderService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    final override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        scope.launch {
            onComplicationUpdateImpl(complicationId, type, manager)
        }
    }

    /**
     * @see ComplicationProviderService.onComplicationUpdate
     */
    abstract suspend fun onComplicationUpdateImpl(complicationId: Int, type: Int, manager: ComplicationManager)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
