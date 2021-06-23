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
 * [onComplicationUpdateImpl] can be suspending.
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
