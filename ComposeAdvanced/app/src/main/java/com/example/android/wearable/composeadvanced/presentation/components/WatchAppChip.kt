/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.android.wearable.composeadvanced.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.util.WearComponentPreview
import com.google.android.horologist.compose.material.Chip

/**
 * Simple Chip for displaying the Watch models.
 */
@Composable
fun WatchAppChip(
    watchModelNumber: Int,
    watchName: String,
    watchIcon: Int,
    onClickWatch: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        icon = watchIcon,
        label = watchName,
        secondaryLabel = "id: $watchModelNumber",
        onClick = {
            onClickWatch(watchModelNumber)
        }
    )
}

@WearComponentPreview
@Composable
fun WatchAppChipPreview() {
    Box {
        WatchAppChip(
            watchModelNumber = 123456,
            watchName = "Watch 123456 Name",
            watchIcon = R.drawable.ic_watch,
            onClickWatch = { }
        )
    }
}
