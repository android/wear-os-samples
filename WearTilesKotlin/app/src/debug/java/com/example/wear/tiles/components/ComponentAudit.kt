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
package com.example.wear.tiles.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ButtonDefaults
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.Colors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.TitleChip
import com.example.wear.tiles.R
import com.example.wear.tiles.emptyClickable
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.google.android.horologist.compose.tools.LayoutElementPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

private const val ICON_CHECK = "check"
private const val IMAGE_AVATAR = "avatar"

private fun debugTheme(context: Context) = Colors(
    /* primary = */ android.graphics.Color.parseColor("#FFCF48"),
    /* onPrimary = */ android.graphics.Color.parseColor("#000000"),
    /* surface = */ android.graphics.Color.parseColor("#414A4C"),
    /* onSurface = */ android.graphics.Color.parseColor("#FFFFFF")
)

@WearSmallRoundDevicePreview
@Composable
fun ButtonDefaultPrimary() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setIconContent(ICON_CHECK)
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, drawableResToImageResource(R.drawable.ic_baseline_check_24))
    }
}

@WearSmallRoundDevicePreview
@Composable
fun ButtonLargeSecondary() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setIconContent(ICON_CHECK)
            .setSize(ButtonDefaults.LARGE_SIZE)
            // secondary colors from our debug theme
            .setButtonColors(ButtonColors.secondaryButtonColors(debugTheme(context)))
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, drawableResToImageResource(R.drawable.ic_baseline_check_24))
    }
}

@WearSmallRoundDevicePreview
@Composable
fun ButtonDefaultImage() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setImageContent(IMAGE_AVATAR)
            .build()
    ) {
        addIdToImageMapping(IMAGE_AVATAR, drawableResToImageResource(R.drawable.avatar))
    }
}

@WearSmallRoundDevicePreview
@Composable
fun TextButtonDefault() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setTextContent("AZ")
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun TextButtonExtraLarge() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setTextContent("AZ")
            // default secondary colors (not our theme)
            .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
            .setSize(ButtonDefaults.EXTRA_LARGE_SIZE)
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun ChipOneLine() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setPrimaryLabelContent("Primary label")
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun ChipTwoLineLabel() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setChipColors(ChipColors.secondaryChipColors(debugTheme(context)))
            .setPrimaryLabelContent("Primary label")
            .setSecondaryLabelContent("Secondary label")
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun ChipIconOneLine() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setIconContent(ICON_CHECK)
            .setPrimaryLabelContent("Primary label")
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, drawableResToImageResource(R.drawable.ic_baseline_check_24))
    }
}

@WearSmallRoundDevicePreview
@Composable
fun ChipTwoLineIcon() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setIconContent(ICON_CHECK)
            .setPrimaryLabelContent("Primary label")
            .setSecondaryLabelContent("Secondary label")
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, drawableResToImageResource(R.drawable.ic_baseline_check_24))
    }
}

@WearSmallRoundDevicePreview
@Composable
fun CompactChip() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        CompactChip.Builder(context, "Primary label", emptyClickable, deviceParameters)
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun CompactChipSecondaryColors() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        CompactChip.Builder(context, "Primary label", emptyClickable, deviceParameters)
            .setChipColors(ChipColors.secondaryChipColors(debugTheme(context)))
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun TitleChip() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        TitleChip.Builder(context, "Action", emptyClickable, deviceParameters)
            .setChipColors(ChipColors.primaryChipColors(debugTheme(context)))
            .build()
    )
}

@WearSmallRoundDevicePreview
@Composable
fun TitleChipSecondaryColors() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        TitleChip.Builder(context, "Action", emptyClickable, deviceParameters)
            .setChipColors(ChipColors.secondaryChipColors(debugTheme(context)))
            .build()
    )
}
