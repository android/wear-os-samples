/*
 * Copyright 2024 The Android Open Source Project
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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@Config(
    sdk = [33],
    qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav"
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class WearScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    abstract val device: WearDevice

    @OptIn(ExperimentalRoborazziApi::class)
    fun runTest(content: @Composable () -> Unit) {
        RuntimeEnvironment.setQualifiers("+w${device.dp}dp-h${device.dp}dp")
        composeRule.setContent {
            content()
        }

        composeRule.onRoot().captureRoboImage(
            filePath ="src/test/screenshots/${this.javaClass.simpleName}_${device.id}.png",
            roborazziOptions = RoborazziOptions(
                recordOptions = RoborazziOptions.RecordOptions(
                    applyDeviceCrop = true
                )
            )
        )
    }
}
