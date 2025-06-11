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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.ColorInt
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Social {

    const val AVATAR_ID_1 = "social avatar id 1"
    const val AVATAR_ID_2 = "social avatar id 2"
    const val AVATAR_ID_3 = "social avatar id 3"
    const val AVATAR_ID_4 = "social avatar id 4"

    fun layout(context: Context, deviceParameters: DeviceParameters, contacts: List<Contact>) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiButtonLayout.Builder()
                    .apply {
                        contacts.take(if (deviceParameters.screenWidthDp > 225) 6 else 4).forEach {
                            contact ->
                            addButtonContent(button(context, contact))
                        }
                    }
                    .build()
            )
            .build()

    private fun button(context: Context, contact: Contact) =
        Button.Builder(context, contact.clickable)
            .apply {
                if (contact.avatarId != null) {
                    setImageContent(contact.avatarId)
                } else {
                    setTextContent(contact.initials, Typography.TYPOGRAPHY_TITLE3)
                }
            }
            .setButtonColors(
                ButtonColors(
                    /* backgroundColor = */ ColorBuilders.argb(contact.color),
                    /* contentColor = */ ColorBuilders.argb(GoldenTilesColors.DarkerGray),
                )
            )
            .build()

    data class Contact(
        val initials: String,
        @ColorInt val color: Int = GoldenTilesColors.LightBlue,
        val clickable: Clickable,
        val avatarId: String?,
    )
}

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview(context: Context) =
    TilePreviewData(
        resources {
            addIdToImageMapping(Social.AVATAR_ID_1, drawableResToImageResource(R.drawable.avatar1))
            addIdToImageMapping(Social.AVATAR_ID_2, drawableResToImageResource(R.drawable.avatar2))
            addIdToImageMapping(Social.AVATAR_ID_3, drawableResToImageResource(R.drawable.avatar3))
            addIdToImageMapping(Social.AVATAR_ID_4, drawableResToImageResource(R.drawable.avatar4))
        }
    ) {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
                Social.layout(
                    context,
                    it.deviceConfiguration,
                    listOf(
                        Social.Contact(
                            initials = "AC",
                            clickable = emptyClickable,
                            avatarId = Social.AVATAR_ID_1,
                        ),
                        Social.Contact(
                            initials = "AD",
                            clickable = emptyClickable,
                            avatarId = null,
                        ),
                        Social.Contact(
                            initials = "BD",
                            color = GoldenTilesColors.Purple,
                            clickable = emptyClickable,
                            avatarId = null,
                        ),
                        Social.Contact(
                            initials = "DC",
                            clickable = emptyClickable,
                            avatarId = Social.AVATAR_ID_2,
                        ),
                        Social.Contact(
                            initials = "DA",
                            clickable = emptyClickable,
                            avatarId = Social.AVATAR_ID_3,
                        ),
                        Social.Contact(
                            initials = "DB",
                            clickable = emptyClickable,
                            avatarId = Social.AVATAR_ID_4,
                        ),
                    ),
                )
            )
            .build()
    }
