@file:OptIn(ExperimentalRoborazziApi::class)

package com.example.android.wearable.composestarter.screenshot

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.ThresholdValidator
import org.robolectric.RuntimeEnvironment
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation

object RoborazziOptionsMapper {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): RoborazziOptions {
        val roborazziConfig = preview.getAnnotation<RoborazziConfig>() ?: RoborazziConfig(0.02f)

        return RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(
                resultValidator = ThresholdValidator(
                    roborazziConfig.comparisonThreshold
                ),
            ),
            recordOptions = RoborazziOptions.RecordOptions(
                applyDeviceCrop = true,
            ),
        )
    }
}

object RobolectricPreviewInfosApplier {
    fun applyFor(preview: ComposablePreview<AndroidPreviewInfo>) {
        val device = when (preview.previewInfo.device) {
            "id:wearos_large_round" -> RobolectricDeviceQualifiers.WearOSLargeRound
            "id:wearos_small_round" -> RobolectricDeviceQualifiers.WearOSSmallRound
            "id:wearos_square" -> RobolectricDeviceQualifiers.WearOSSquare
            else -> null
        }
        if (device != null) {
            RuntimeEnvironment.setQualifiers(device)
        }
        RuntimeEnvironment.setFontScale(preview.previewInfo.fontScale)
    }
}
