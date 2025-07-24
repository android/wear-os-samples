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

import android.content.Context
import android.util.Log
import androidx.wear.protolayout.ResourceBuilders.ImageResource
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowRgb565
import coil3.toBitmap
import com.example.wear.tiles.tools.toImageResource

/**
 * Loads an avatar image for a given [Contact].
 *
 * This function attempts to load an avatar based on the [AvatarSource] associated with the
 * [Contact]. It supports loading avatars from:
 * - **Network URLs:** If the [AvatarSource] is [AvatarSource.Network], it fetches the image from
 *   the specified URL using the provided [ImageLoader]. It caches the result, handles potential
 *   network errors, and converts the result into an [ImageResource].
 * - **Local Resources:** If the [AvatarSource] is [AvatarSource.Resource], it directly converts the
 *   provided resource ID into an [ImageResource].
 * - **No Avatar:** If the [AvatarSource] is [AvatarSource.None], it returns `null`.
 *
 * @param context The application context, needed for building the [ImageRequest].
 * @param contact The [Contact] object containing information about the avatar source.
 * @return An [ImageResource] representing the loaded avatar, or `null` if no avatar is available or
 *   an error occurred during network loading.
 */
suspend fun ImageLoader.loadAvatar(context: Context, contact: Contact): ImageResource? {
    return when (val source = contact.avatarSource) {
        is AvatarSource.Network -> {
            val request =
                ImageRequest.Builder(context).data(source.url).size(300).allowRgb565(true).build()
            val response = execute(request)
            return when (response) {
                is SuccessResult -> {
                    response.image.toBitmap().toImageResource()
                }
                is ErrorResult -> {
                    Log.d("ImageLoader", "Error loading image $source: ${response.throwable}")
                    null
                }
            }
        }
        is AvatarSource.Resource -> source.resourceId.toImageResource()
        is AvatarSource.None -> null
    }
}
