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
import androidx.glance.wear.tooling.preview.RoundAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleAllWidgetPreviewParams
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.google.example.wear_widget.template.MediumAdaptiveCardRoundPreview
import com.google.example.wear_widget.template.MediumAdaptiveCardSquirclePreview
import com.google.example.wear_widget.template.SmallActionWidgetRoundPreview
import com.google.example.wear_widget.template.SmallActionWidgetSquirclePreview
import com.google.example.wear_widget.template.SmallHeroWidgetRoundPreview
import com.google.example.wear_widget.template.SmallHeroWidgetSquirclePreview
import kotlin.OptIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav")
class TemplateWidgetTest {

    @get:Rule val composeRule = createComposeRule()

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testSmallHeroWidgetSquirclePreview() {
        composeRule.setContent {
            SmallHeroWidgetSquirclePreview(params = SquircleAllWidgetPreviewParams().values.first())
        }
        captureScreenRoboImage("src/test/screenshots/SmallHeroWidgetSquirclePreview.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testSmallHeroWidgetRoundPreview() {
        composeRule.setContent {
            SmallHeroWidgetRoundPreview(params = RoundAllWidgetPreviewParams().values.first())
        }
        captureScreenRoboImage("src/test/screenshots/SmallHeroWidgetRoundPreview.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testSmallActionWidgetSquirclePreview() {
        composeRule.setContent {
            SmallActionWidgetSquirclePreview(
                params = SquircleAllWidgetPreviewParams().values.first()
            )
        }
        captureScreenRoboImage("src/test/screenshots/SmallActionWidgetSquirclePreview.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testSmallActionWidgetRoundPreview() {
        composeRule.setContent {
            SmallActionWidgetRoundPreview(params = RoundAllWidgetPreviewParams().values.first())
        }
        captureScreenRoboImage("src/test/screenshots/SmallActionWidgetRoundPreview.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testMediumAdaptiveCardSquirclePreview() {
        composeRule.setContent {
            MediumAdaptiveCardSquirclePreview(
                params = SquircleAllWidgetPreviewParams().values.first()
            )
        }
        captureScreenRoboImage("src/test/screenshots/MediumAdaptiveCardSquirclePreview.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testMediumAdaptiveCardRoundPreview() {
        composeRule.setContent {
            MediumAdaptiveCardRoundPreview(params = RoundAllWidgetPreviewParams().values.first())
        }
        captureScreenRoboImage("src/test/screenshots/MediumAdaptiveCardRoundPreview.png")
    }
}
