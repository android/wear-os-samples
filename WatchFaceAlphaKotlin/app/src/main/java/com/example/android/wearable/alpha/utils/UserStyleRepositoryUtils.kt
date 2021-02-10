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
package com.example.android.wearable.alpha.utils

import android.util.Log
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel

private const val TAG = "UserStyleRepositoryUtils"

// Creates UserStyleRepository and adds a callback for any changes to the style to
// trigger updates to data layer via ViewModel for the specified key.
fun createUserStyleRepositoryWithCallback(
    userStyleSchema: UserStyleSchema,
    analogWatchFaceKeyId: Int,
    analogWatchFaceViewModel: AnalogWatchFaceViewModel
): UserStyleRepository {
    // Create repository
    val userStyleRepository = UserStyleRepository(userStyleSchema)

    // Add callback to ViewModel.
    userStyleRepository.addUserStyleListener(
        object : UserStyleRepository.UserStyleListener {
            override fun onUserStyleChanged(userStyle: UserStyle) {
                Log.d(TAG, "onUserStyleChanged(), userStyle: \n${userStyle.toMap()}")

                analogWatchFaceViewModel.updateUserStylesInDatabase(
                    analogWatchFaceKeyId,
                    userStyle
                )
            }
        }
    )
    return userStyleRepository
}
