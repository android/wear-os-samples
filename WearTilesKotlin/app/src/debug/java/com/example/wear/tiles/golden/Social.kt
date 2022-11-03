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
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout

object Social {

    const val AVATAR_ID_1 = "social avatar id 1"
    const val AVATAR_ID_2 = "social avatar id 2"

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        contact1: Contact,
        contact2: Contact,
        contact3: Contact,
        contact4: Contact
    ) = PrimaryLayout.Builder(deviceParameters)
        .setContent(
            MultiButtonLayout.Builder()
                .addButtonContent(button(context, contact1))
                .addButtonContent(button(context, contact2))
                .addButtonContent(button(context, contact3))
                .addButtonContent(button(context, contact4))
                .build()
        )
        .build()

    private fun button(
        context: Context,
        contact: Contact
    ) = Button.Builder(context, contact.clickable)
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
                /* contentColor = */ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
            )
        )
        .build()

    data class Contact(
        val initials: String,
        @ColorInt val color: Int = GoldenTilesColors.LightBlue,
        val clickable: Clickable,
        val avatarId: String?
    )
}
