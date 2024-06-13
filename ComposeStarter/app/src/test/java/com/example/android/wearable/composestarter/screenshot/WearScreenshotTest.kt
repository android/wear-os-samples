package com.example.android.wearable.composestarter.screenshot

import androidx.test.platform.app.InstrumentationRegistry
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.horologist.compose.layout.AppScaffold
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@Config(qualifiers = RobolectricDeviceQualifiers.WearOSLargeRound)
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
        println("snapshot " + preview.methodName)
        RobolectricPreviewInfosApplier.applyFor(preview)

        val context = InstrumentationRegistry.getInstrumentation().context
        val width = context.resources.configuration.screenWidthDp

        val fontScale =
            if (preview.previewInfo.fontScale != 1.0f) "_${preview.previewInfo.fontScale}" else ""
        val device = if (context.resources.configuration.isScreenRound) "${width}dp" else "square"
        captureRoboImage(
            filePath = "src/test/screenshots/${this.javaClass.simpleName}_${preview.methodName}_$device$fontScale.png",
            roborazziOptions = RoborazziOptionsMapper.createFor(preview),
        ) {
            AppScaffold {
                preview()
            }
        }
    }
}
