package com.example.wear.tiles

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ButtonDefaults
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CircularProgressIndicator
import androidx.wear.tiles.material.Colors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.ProgressIndicatorColors
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.TitleChip
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiSlotLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout
import androidx.wear.tiles.material.layouts.ProgressIndicatorLayout
import com.example.wear.tiles.tools.WearLargeRoundDevicePreview
import com.google.android.horologist.compose.tools.TilePreview
import kotlin.math.roundToInt

private const val ICON_CHECK = "check"
private const val IMAGE_AVATAR = "avatar"

private fun debugTheme(context: Context) = Colors(
    /* primary = */ ContextCompat.getColor(context, R.color.yellow),
    /* onPrimary = */ ContextCompat.getColor(context, R.color.black),
    /* surface = */ ContextCompat.getColor(context, R.color.dark_gray),
    /* onSurface = */ ContextCompat.getColor(context, R.color.white)
)

private fun imageResourceFrom(@DrawableRes resourceId: Int) = ImageResource.Builder()
    .setAndroidResourceByResId(
        ResourceBuilders.AndroidImageResourceByResId.Builder()
            .setResourceId(resourceId)
            .build()
    )
    .build()

private fun buildDeviceParameters(resources: Resources): DeviceParametersBuilders.DeviceParameters {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    val isScreenRound: Boolean = resources.configuration.isScreenRound
    return DeviceParametersBuilders.DeviceParameters.Builder()
        .setScreenWidthDp((displayMetrics.widthPixels / displayMetrics.density).roundToInt())
        .setScreenHeightDp((displayMetrics.heightPixels / displayMetrics.density).roundToInt())
        .setScreenDensity(displayMetrics.density).setScreenShape(
            if (isScreenRound) DeviceParametersBuilders.SCREEN_SHAPE_ROUND
            else DeviceParametersBuilders.SCREEN_SHAPE_RECT
        ).setDevicePlatform(DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS).build()
}

@Composable
private fun LayoutElementPreview(
    element: LayoutElement,
    @ColorInt windowBackgroundColor: Int = android.graphics.Color.BLACK,
    tileResourcesFn: ResourceBuilders.Resources.Builder.() -> Unit = {}
) {
    val root = LayoutElementBuilders.Box.Builder()
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder().setBackground(
                ModifiersBuilders.Background.Builder()
                    .setColor(ColorBuilders.argb(windowBackgroundColor))
                    .build()
            ).build()
        )
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .setHeight(DimensionBuilders.ExpandedDimensionProp.Builder().build())
        .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
        .addContent(element)
        .build()

    LayoutRootPreview(root = root, tileResourcesFn)
}

@Composable
private fun LayoutRootPreview(
    root: LayoutElement,
    tileResourcesFn: ResourceBuilders.Resources.Builder.() -> Unit = {}
) {
    val tile = TileBuilders.Tile.Builder()
        .setResourcesVersion("1")
        .setTimeline(singleEntryTimeline(root))
        .build()

    val tileResources = ResourceBuilders.Resources.Builder()
        .apply(tileResourcesFn)
        .build()

    TilePreview(tile, tileResources)
}

@WearLargeRoundDevicePreview
@Composable
fun ButtonDefaultPrimary() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setIconContent(ICON_CHECK)
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, imageResourceFrom(R.drawable.ic_baseline_check_24))
    }
}

@WearLargeRoundDevicePreview
@Composable
fun ButtonLargeSecondary() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setIconContent(ICON_CHECK)
            .setSize(ButtonDefaults.LARGE_BUTTON_SIZE)
            // secondary colors from our debug theme
            .setButtonColors(ButtonColors.secondaryButtonColors(debugTheme(context)))
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, imageResourceFrom(R.drawable.ic_baseline_check_24))
    }
}

@WearLargeRoundDevicePreview
@Composable
fun ButtonDefaultImage() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setImageContent(IMAGE_AVATAR)
            .build()
    ) {
        addIdToImageMapping(IMAGE_AVATAR, imageResourceFrom(R.drawable.avatar))
    }
}

@WearLargeRoundDevicePreview
@Composable
fun TextButtonDefault() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setTextContent("AZ")
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun TextButtonExtraLarge() {
    val context = LocalContext.current
    LayoutElementPreview(
        Button.Builder(context, emptyClickable)
            .setTextContent("AZ")
            // default secondary colors (not our theme)
            .setButtonColors(ButtonDefaults.SECONDARY_BUTTON_COLORS)
            .setSize(ButtonDefaults.EXTRA_LARGE_BUTTON_SIZE)
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun ChipOneLine() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setPrimaryTextContent("Primary label")
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun ChipTwoLineLabel() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setChipColors(ChipColors.secondaryChipColors(debugTheme(context)))
            .setPrimaryTextLabelContent("Primary label", "Secondary label")
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun ChipIconOneLine() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setPrimaryTextIconContent("Primary label", ICON_CHECK)
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, imageResourceFrom(R.drawable.ic_baseline_check_24))
    }
}

@WearLargeRoundDevicePreview
@Composable
fun ChipTwoLineIcon() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        Chip.Builder(context, emptyClickable, deviceParameters)
            .setPrimaryTextLabelIconContent("Primary label", "Secondary label", ICON_CHECK)
            .build()
    ) {
        addIdToImageMapping(ICON_CHECK, imageResourceFrom(R.drawable.ic_baseline_check_24))
    }
}

@WearLargeRoundDevicePreview
@Composable
fun CompactChipPrimary() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutElementPreview(
        CompactChip.Builder(context, "Primary label", emptyClickable, deviceParameters)
            .build()
    )
}

@WearLargeRoundDevicePreview
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

@WearLargeRoundDevicePreview
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

@WearLargeRoundDevicePreview
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

private fun colorBuildersColorPropFrom(
    context: Context,
    @ColorRes colorRes: Int
): ColorBuilders.ColorProp {
    return ColorBuilders.argb(
        ContextCompat.getColor(
            context,
            colorRes
        )
    )
}

@WearLargeRoundDevicePreview
@Composable
fun ProgressIndicatorLayout() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutRootPreview(
        ProgressIndicatorLayout.Builder(deviceParameters)
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Steps")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(
                        colorBuildersColorPropFrom(context, R.color.primary)
                    )
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, "/ 8000")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(
                        ColorBuilders.argb(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                    )
                    .build()
            )
            .setProgressIndicatorContent(
                CircularProgressIndicator.Builder()
                    .setProgress(5168f / 8000)
                    .setCircularProgressIndicatorColors(
                        ProgressIndicatorColors(
                            /* indicatorColor = */ ColorBuilders.argb(
                                ContextCompat.getColor(
                                    context,
                                    R.color.primary
                                )
                            ),
                            /* trackColor = */ ColorBuilders.argb(Color(1f, 1f, 1f, 0.1f).toArgb())
                        )
                    )
                    .build()
            )
            .setContent(
                Text.Builder(context, "5168")
                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                    .setColor(
                        ColorBuilders.argb(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                    )
                    .build()
            )
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun PrimaryLayout() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutRootPreview(
        PrimaryLayout.Builder(deviceParameters)
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Primary label")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(
                        ColorBuilders.argb(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                    )
                    .build()
            )
            .setContent(
                TitleChip.Builder(context, "Start", emptyClickable, deviceParameters)
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, "Secondary label")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(
                        ColorBuilders.argb(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                    )
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "Action", emptyClickable, deviceParameters)
                    .build()
            )
            .build()
    )
}

@WearLargeRoundDevicePreview
@Composable
fun MultiSlotLayout() {
    val context = LocalContext.current
    val deviceParameters = buildDeviceParameters(context.resources)
    LayoutRootPreview(
        LayoutElementBuilders.Box.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setHeight(DimensionBuilders.ExpandedDimensionProp.Builder().build())
            .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
            .addContent(
                MultiSlotLayout.Builder()
                    .addSlotContent(
                        skiThing(context, "Max Spd", "46.5", "mph")
                    )
                    .addSlotContent(
                        skiThing(context, "Distance", "21.8", "miles")
                    )
                    .build()
            )
            .build()
    )
}

private fun skiThing(
    context: Context,
    label: String,
    value: String,
    unit: String
) = Column.Builder()
    .addContent(
        Text.Builder(context, label)
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .setColor(argb(debugTheme(context).primary))
            .build()
    )
    .addContent(
        Text.Builder(context, value)
            .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
            .setColor(argb(Color.White.toArgb()))
            .build()
    )
    .addContent(
        Text.Builder(context, unit)
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .setColor(argb(Color.White.toArgb()))
            .build()
    )
    .build()
