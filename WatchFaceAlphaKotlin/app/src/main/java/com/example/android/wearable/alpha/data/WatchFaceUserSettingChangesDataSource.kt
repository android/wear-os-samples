/*
 * Copyright (C) 2021 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.alpha.data

import android.util.Log
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * Data Source for all changes by the user in settings related to the watch face styles. Coverts
 * the watch face api callback to a flow.
 */
class WatchFaceUserSettingChangesDataSource(
    private val currentUserStyleRepository: CurrentUserStyleRepository
) {
    @ExperimentalCoroutinesApi
    fun getUserWatchFaceChanges(): Flow<UserStyle> = callbackFlow<UserStyle> {
        val userStyleChangeListener = object : CurrentUserStyleRepository.UserStyleChangeListener {
            override fun onUserStyleChanged(userStyle: UserStyle) {
                Log.d(TAG, "onUserStyleChanged(): userStyle: $userStyle")
                trySend(userStyle)
            }
        }

        currentUserStyleRepository.addUserStyleChangeListener(userStyleChangeListener)

        // The callback inside awaitClose will be executed when the flow is either closed or
        // cancelled. In this case, remove the CurrentUserStyleRepository.UserStyleChangeListener()
        // callback.
        awaitClose {
            Log.d(TAG, "awaitClose{ }")
            currentUserStyleRepository.removeUserStyleChangeListener(userStyleChangeListener)
        }
    }.buffer(Channel.CONFLATED)

    companion object {
        const val TAG = "UserConfigurableWatchFaceDataSource"
    }
}
