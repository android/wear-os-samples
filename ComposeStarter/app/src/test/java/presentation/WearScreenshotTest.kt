/*
 * Copyright 2026 The Android Open Source Project
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
package presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.wear.compose.material3.TimeSource
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
abstract class WearScreenshotTest {
    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    abstract val device: WearDevice
    open val tolerance: Float = 0.02f

    protected val fixedTimeSource =
        object : TimeSource {
            @Composable
            override fun currentTime(): String = "10:10"
        }

    fun runTest(
        suffix: String = "",
        content: @Composable () -> Unit
    ) {
        RuntimeEnvironment.setQualifiers(device.qualifier)

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides
                    Density(
                        density = device.density,
                        fontScale = device.fontScale
                    )
            ) {
                content()
            }
        }
        captureScreenshot(suffix)
    }

    @OptIn(ExperimentalRoborazziApi::class)
    fun captureScreenshot(suffix: String) {
        val fileName = "${this::class.simpleName}_${device.id}$suffix.png"
        captureScreenRoboImage(
            filePath = "src/test/screenshots/$fileName",
            roborazziOptions =
                RoborazziOptions(
                    recordOptions = RoborazziOptions.RecordOptions(applyDeviceCrop = true),
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = tolerance)
                )
        )
    }
}
