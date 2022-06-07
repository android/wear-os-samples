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
package com.example.wear.tiles.messaging

import android.content.Context
import androidx.wear.tiles.TileService.getUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Updates(
    val application: Context,
    var repo: MessagingRepo,
    val scope: CoroutineScope
) {
    /**
     * Call to request data and trigger updates to tiles.
     */
    fun updateData() {
        scope.launch {
            repo.updateContacts(MessagingRepo.knownContacts)
            getUpdater(application).requestUpdate(MessagingTileService::class.java)
        }
    }
}
