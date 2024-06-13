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
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): RoborazziOptions =
        preview.getAnnotation<RoborazziConfig>()?.let { config ->
            RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(
                    resultValidator = ThresholdValidator(
                        config.comparisonThreshold
                    )
                )
            )
        } ?: RoborazziOptions()
}

object RobolectricPreviewInfosApplier {
    fun applyFor(preview: ComposablePreview<AndroidPreviewInfo>) {
        RuntimeEnvironment.setQualifiers(RobolectricDeviceQualifiers.WearOSLargeRound)
    }
}
