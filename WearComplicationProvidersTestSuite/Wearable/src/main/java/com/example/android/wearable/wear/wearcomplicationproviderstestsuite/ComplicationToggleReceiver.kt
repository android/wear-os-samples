/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.wearable.complications.ProviderUpdateRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives intents on tap and causes complication states to be toggled and updated.
 */
class ComplicationToggleReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getArgs()

        val result = goAsync()

        scope.launch {
            try {
                args.updateState(context)

                // Request an update for the complication that has just been toggled.
                ProviderUpdateRequester(context, args.providerComponent).requestUpdate(args.complicationId)
            } finally {
                // Always call finish, even if cancelled
                result.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ARGS = "arguments"

        /**
         * Returns a pending intent, suitable for use as a tap intent, that causes a complication to be
         * toggled and updated.
         */
        fun getComplicationToggleIntent(
            context: Context,
            args: ComplicationToggleArgs
        ): PendingIntent {
            val intent = Intent(context, ComplicationToggleReceiver::class.java).apply {
                putExtra(EXTRA_ARGS, args)
            }

            // Pass complicationId as the requestCode to ensure that different complications get
            // different intents.
            return PendingIntent.getBroadcast(context, args.complicationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns the [ComplicationToggleArgs] from the [Intent] sent to the [ComplicationToggleArgs].
         */
        private fun Intent.getArgs(): ComplicationToggleArgs = requireNotNull(extras?.getParcelable(EXTRA_ARGS))
    }
}
