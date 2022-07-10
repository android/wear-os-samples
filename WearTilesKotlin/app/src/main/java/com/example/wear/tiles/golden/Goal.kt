package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.material.CircularProgressIndicator
import androidx.wear.tiles.material.ProgressIndicatorColors
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.EdgeContentLayout

object Goal {
    fun layout(context: Context, deviceParameters: DeviceParameters, steps: Int, goal: Int) =
        EdgeContentLayout.Builder(deviceParameters)
            .setEdgeContent(
                CircularProgressIndicator.Builder()
                    .setProgress(steps.toFloat() / goal)
                    .setCircularProgressIndicatorColors(blueOnTranslucentWhite())
                    .build()
            )
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Steps")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Blue))
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, "/ $goal")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .setContent(
                Text.Builder(context, "$steps")
                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .build()
}

private fun blueOnTranslucentWhite() = ProgressIndicatorColors(
    /* indicatorColor = */ ColorBuilders.argb(GoldenTilesColors.Blue),
    /* trackColor = */ ColorBuilders.argb(GoldenTilesColors.White10Pc)
)
