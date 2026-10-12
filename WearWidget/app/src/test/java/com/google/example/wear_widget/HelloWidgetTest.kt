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
package com.google.example.wear_widget

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.glance.wear.tooling.preview.RectangularSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleLargeWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav")
class HelloWidgetTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun testHelloWidgetSquircleSmallPreview() {
        composeRule.setContent {
            HelloWidgetSquirclePreview(params = SquircleSmallWidgetPreviewParams().values.first())
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/HelloWidgetPreview.png")
    }

    @Test
    fun testHelloWidgetSquircleLarge240dpPreview() {
        composeRule.setContent {
            HelloWidgetSquirclePreview(params = SquircleLargeWidgetPreviewParams().values.last())
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/HelloWidgetLargePreview.png")
    }

    @Test
    fun testHelloWidgetRoundPreview() {
        composeRule.setContent {
            HelloWidgetRoundPreview(params = RoundSmallWidgetPreviewParams().values.first())
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/HelloWidgetRoundPreview.png")
    }

    @Test
    fun testHelloWidgetRectangularPreview() {
        composeRule.setContent {
            HelloWidgetRectangularPreview(
                params = RectangularSmallWidgetPreviewParams().values.first()
            )
        }
        composeRule
            .onRoot()
            .captureRoboImage("src/test/screenshots/HelloWidgetRectangularPreview.png")
    }

    @Test
    @Config(fontScale = 1.24f)
    fun testHelloWidgetFontScalePreview() {
        composeRule.setContent { HelloWidgetFontScalePreview() }
        composeRule
            .onRoot()
            .captureRoboImage("src/test/screenshots/HelloWidgetFontScalePreview.png")
    }
}
