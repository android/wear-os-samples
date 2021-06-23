package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.content.ComponentName
import android.content.Context
import android.os.Parcelable
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
 * Returns the key for the shared preference used to hold the current state of a given
 * complication.
 */
fun ComplicationToggleArgs.getPreferenceKey(): String =
    "${providerComponent.className}$complicationId"

/**
 * Returns the current state for a given complication.
 */
fun ComplicationToggleArgs.getState(context: Context): Int {
    val preferences = context.getSharedPreferences(ComplicationToggleReceiver.PREFERENCES_NAME, Context.MODE_PRIVATE)
    return preferences.getInt(getPreferenceKey(), 0)
}
