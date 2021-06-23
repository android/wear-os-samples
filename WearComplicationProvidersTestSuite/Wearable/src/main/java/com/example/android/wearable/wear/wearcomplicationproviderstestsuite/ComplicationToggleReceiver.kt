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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.wearable.complications.ProviderUpdateRequester

/** Receives intents on tap and causes complication states to be toggled and updated.  */
class ComplicationToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val provider = extras!!.getParcelable<ComponentName>(EXTRA_PROVIDER_COMPONENT)
        val complicationId = extras.getInt(EXTRA_COMPLICATION_ID)
        val preferenceKey = getPreferenceKey(provider, complicationId)
        val pref = context.getSharedPreferences(PREFERENCES_NAME, 0)
        val value = pref.getInt(preferenceKey, 0)
        val editor = pref.edit()
        editor.putInt(preferenceKey, value + 1) // Increase value by 1
        editor.apply()

        // Request an update for the complication that has just been toggled.
        val requester = ProviderUpdateRequester(context, provider)
        requester.requestUpdate(complicationId)
    }

    companion object {
        private const val EXTRA_PROVIDER_COMPONENT = "providerComponent"
        private const val EXTRA_COMPLICATION_ID = "complicationId"
        const val PREFERENCES_NAME = "ComplicationTestSuite"

        /**
         * Returns a pending intent, suitable for use as a tap intent, that causes a complication to be
         * toggled and updated.
         */
        fun getToggleIntent(
            context: Context?, provider: ComponentName?, complicationId: Int
        ): PendingIntent {
            val intent = Intent(context, ComplicationToggleReceiver::class.java)
            intent.putExtra(EXTRA_PROVIDER_COMPONENT, provider)
            intent.putExtra(EXTRA_COMPLICATION_ID, complicationId)

            // Pass complicationId as the requestCode to ensure that different complications get
            // different intents.
            return PendingIntent.getBroadcast(
                context, complicationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns the key for the shared preference used to hold the current state of a given
         * complication.
         */
        fun getPreferenceKey(provider: ComponentName?, complicationId: Int): String {
            return provider!!.className + complicationId
        }
    }
}