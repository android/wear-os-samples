package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.TitleChip
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout

object Workout {
    const val BUTTON_1_ICON_ID = "1"
    const val BUTTON_2_ICON_ID = "2"
    const val BUTTON_3_ICON_ID = "3"

    fun buttonsLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        weekSummary: String,
        button1Clickable: Clickable,
        button2Clickable: Clickable,
        button3Clickable: Clickable,
        chipClickable: Clickable
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setPrimaryLabelTextContent(
                Text.Builder(context, weekSummary)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Blue))
                    .build()
            )
            .setContent(
                MultiButtonLayout.Builder()
                    .addButtonContent(
                        Button.Builder(context, button1Clickable).setIconContent(BUTTON_1_ICON_ID)
                            .build()
                    )
                    .addButtonContent(
                        Button.Builder(context, button2Clickable).setIconContent(BUTTON_2_ICON_ID)
                            .build()
                    )
                    .addButtonContent(
                        Button.Builder(context, button3Clickable).setIconContent(BUTTON_3_ICON_ID)
                            .build()
                    )
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "More", chipClickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.BlueGray),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White)
                        )
                    )
                    .build()
            )
            .build()

    fun largeChipLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        clickable: Clickable,
        title: String,
        chipText: String,
        lastWorkoutSummary: String
    ) = PrimaryLayout.Builder(deviceParameters)
            .setPrimaryLabelTextContent(
                Text.Builder(context, title)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Yellow))
                    .build()
            )
            .setContent(
                TitleChip.Builder(context, chipText, clickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.Black)
                        )
                    )
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, lastWorkoutSummary)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .build()
}
