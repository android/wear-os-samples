/*
 * Copyright 2022 The Android Open Source Project
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

data class Contact(
    val id: Long,
    val initials: String,
    val name: String,
    val avatarSource: AvatarSource,
)

sealed interface AvatarSource {
    // Represents an image fetched from a network URL
    data class Network(val url: String) : AvatarSource

    // Represents an image loaded from Android drawable resources
    data class Resource(@DrawableRes val resourceId: Int) : AvatarSource

    // Represents the absence of a specific avatar
    object None : AvatarSource
}

fun Contact.imageResourceId() = "contact:$id"

fun getMockNetworkContacts() =
    listOf(
        Contact(
            id = 0,
            initials = "AC",
            name = "Ali C",
            avatarSource = AvatarSource.Network("$avatarPath/ali.png"),
        ),
        Contact(id = 1, initials = "JV", name = "Jyoti V", avatarSource = AvatarSource.None),
        Contact(
            id = 2,
            initials = "TB",
            name = "Taylor B",
            avatarSource = AvatarSource.Network("$avatarPath/taylor.jpg"),
        ),
        Contact(id = 3, initials = "FS", name = "Felipe S", avatarSource = AvatarSource.None),
        Contact(id = 4, initials = "JG", name = "Judith G", avatarSource = AvatarSource.None),
        Contact(id = 5, initials = "AO", name = "Andrew O", avatarSource = AvatarSource.None),
    )

fun getMockLocalContacts() =
    listOf(
        Contact(
            id = 0,
            initials = "AC",
            name = "Ali C",
            avatarSource = AvatarSource.Resource(R.drawable.ali),
        ),
        Contact(id = 1, initials = "JV", name = "Jyoti V", avatarSource = AvatarSource.None),
        Contact(
            id = 2,
            initials = "TB",
            name = "Taylor B",
            avatarSource = AvatarSource.Resource(R.drawable.taylor),
        ),
        Contact(id = 3, initials = "FS", name = "Felipe S", avatarSource = AvatarSource.None),
        Contact(id = 4, initials = "JG", name = "Judith G", avatarSource = AvatarSource.None),
        Contact(id = 5, initials = "AO", name = "Andrew O", avatarSource = AvatarSource.None),
    )

private const val avatarPath =
    "https://raw.githubusercontent.com" +
        "/android/wear-os-samples/main/WearTilesKotlin/app/src/main/res/drawable-nodpi"
