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

import androidx.annotation.DrawableRes
import com.example.wear.tiles.R
import kotlinx.coroutines.delay

data class Contact(
    val id: Long,
    val initials: String,
    @DrawableRes val avatarRes: Int?
)

object MessagingRepo {

    suspend fun getFavoriteContacts(): List<Contact> {
        delay(200)
        return listOf(
            Contact(0, "JV", null),
            Contact(1, "AC", R.drawable.ali),
            Contact(2, "FS", null),
            Contact(3, "TB", R.drawable.taylor),
            Contact(3, "JG", null),
            Contact(3, "AO", null),
        )
    }
}
