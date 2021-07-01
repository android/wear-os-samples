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
    val name: String,
    @DrawableRes val avatarRes: Int?
)

object MessagingRepo {

    suspend fun getFavoriteContacts(): List<Contact> {
        delay(200)
        return listOf(
            Contact(
                id = 0,
                initials = "JV",
                name = "Jyoti V",
                avatarRes = null
            ),
            Contact(
                id = 1,
                initials = "AC",
                name = "Ali C",
                avatarRes = R.drawable.ali
            ),
            Contact(
                id = 2,
                initials = "FS",
                name = "Felipe S",
                avatarRes = null
            ),
            Contact(
                id = 3,
                initials = "TB",
                name = "Taylor B",
                avatarRes = R.drawable.taylor
            ),
            Contact(
                id = 4,
                initials = "JG",
                name = "Judith G",
                avatarRes = null
            ),
            Contact(
                id = 5,
                initials = "AO",
                name = "Andrew O",
                avatarRes = null
            ),
        )
    }
}
