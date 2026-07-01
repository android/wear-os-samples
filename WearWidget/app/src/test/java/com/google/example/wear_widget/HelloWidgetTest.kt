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

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.glance.wear.core.ContainerInfo
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.SquircleAllWidgetPreviewParams
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import kotlin.OptIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav")
class HelloWidgetTest(private val params: WearWidgetParams) {

    @get:Rule val composeRule = createComposeRule()

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun testHelloWidgetPreview() {
        val type =
            when (params.containerType) {
                ContainerInfo.CONTAINER_TYPE_SMALL -> "small"
                ContainerInfo.CONTAINER_TYPE_LARGE -> "large"
                else -> "unknown"
            }
        val sizeLabel = "${params.widthDp.toInt()}x${params.heightDp.toInt()}"

        composeRule.setContent { HelloWidgetPreview(params = params) }
        captureScreenRoboImage("src/test/screenshots/HelloWidgetPreview_${type}_$sizeLabel.png")
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun parameters(): List<WearWidgetParams> {
            return SquircleAllWidgetPreviewParams().values.toList()
        }
    }
}
