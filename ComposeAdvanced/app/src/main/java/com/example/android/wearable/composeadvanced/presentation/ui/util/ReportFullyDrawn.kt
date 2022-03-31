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
package com.example.android.wearable.composeadvanced.presentation.ui.util

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.doOnPreDraw

// From https://github.com/androidx/androidx/blob/42d58ade87b9338c563ee8f182057a7da93f5c78/compose/integration-tests/macrobenchmark-target/src/main/java/androidx/compose/integration/macrobenchmark/target/FullyDrawnStartupActivity.kt
@Composable
fun ReportFullyDrawn() {
    val localView: View = LocalView.current
    SideEffect {
        val activity = localView.context as? Activity
        if (activity != null) {
            localView.doOnPreDraw {
                activity.reportFullyDrawn()
            }
        }
    }
}
