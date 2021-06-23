package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.content.ComponentName
import android.os.Parcelable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.parcelize.Parcelize

/**
 * The arguments for toggling a complication.
 */
@Parcelize
data class ComplicationToggleArgs(
    val providerComponent: ComponentName,
    val complicationId: Int
) : Parcelable

/**
 * Returns the key for the preference used to hold the current state of a given complication.
 */
fun ComplicationToggleArgs.getStatePreferenceKey(): Preferences.Key<Long> =
    longPreferencesKey("${providerComponent.className}$complicationId")
