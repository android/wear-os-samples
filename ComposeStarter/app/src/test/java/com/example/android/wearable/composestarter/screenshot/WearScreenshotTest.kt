package com.example.android.wearable.composestarter.screenshot

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewParameterizedTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("com.example.android.wearable.composestarter.presentation")
                .filterPreviews { true }
                .includeAnnotationInfoForAllOf(RoborazziConfig::class.java)
                .getPreviews()
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [33])
    @Test
    fun snapshot() {
        RobolectricPreviewInfosApplier.applyFor(preview)

        captureRoboImage(
            roborazziOptions = RoborazziOptionsMapper.createFor(preview)
        ) {
            preview()
        }
    }
}
