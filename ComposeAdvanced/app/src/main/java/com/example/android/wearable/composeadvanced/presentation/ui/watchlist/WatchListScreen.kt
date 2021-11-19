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
package com.example.android.wearable.composeadvanced.presentation.ui.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.items
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.data.WatchModel
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.components.WatchAppChip

/**
 * Displays a list of watches plus a [ToggleChip] at the top to display/hide the Vignette around
 * the screen. The list is powered using a [ScalingLazyColumn].
 */
@Composable
@OptIn(ExperimentalWearMaterialApi::class)
fun WatchListScreen(
    scalingLazyListState: ScalingLazyListState,
    showVignette: Boolean,
    onClickVignetteToggle: (Boolean) -> Unit,
    onClickWatch: (Int) -> Unit,
    watchRepository: WatchRepository,
    viewModel: WatchListViewModel = viewModel(
        factory = WatchListViewModelFactory(
            watchRepository = watchRepository
        )
    )
) {
    val watches: List<WatchModel> by viewModel.watches

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 28.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.Center,
        state = scalingLazyListState
    ) {

        item {
            ToggleChip(
                modifier = Modifier
                    .height(32.dp)
                    .padding(
                        start = if (LocalConfiguration.current.isScreenRound) {
                            20.dp
                        } else {
                            10.dp
                        },
                        end = if (LocalConfiguration.current.isScreenRound) {
                            20.dp
                        } else {
                            10.dp
                        },
                    ),
                checked = showVignette,
                onCheckedChange = onClickVignetteToggle,
                label = {
                    Text(
                        text = stringResource(R.string.vignette_toggle_chip_label),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }

        // Displays all watches.
        items(watches) { watch ->
            WatchAppChip(
                watchModelNumber = watch.modelId,
                watchName = watch.name,
                watchIcon = watch.icon,
                onClickWatch = onClickWatch
            )
        }
    }
}
