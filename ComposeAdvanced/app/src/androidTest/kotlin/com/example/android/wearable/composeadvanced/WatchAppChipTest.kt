/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.android.wearable.composeadvanced

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.android.wearable.composeadvanced.presentation.components.WatchAppChip
import org.junit.Rule
import org.junit.Test

/**
 * Demonstration of a composable test.
 */
class WatchAppChipTest {
    @get:Rule
    var rule = createComposeRule()

    @Test
    fun testEvent() {
        rule.setContent {
            WatchAppChip(
                watchModelNumber = 1,
                watchName = "PixelWatch",
                watchIcon = R.drawable.ic_watch,
                onClickWatch = {}
            )
        }

        rule.onNodeWithText("PixelWatch").assertHasClickAction()
    }
}
