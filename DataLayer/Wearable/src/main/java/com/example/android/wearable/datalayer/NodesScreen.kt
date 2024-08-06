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
package com.example.android.wearable.datalayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ResponsiveListHeader

@Composable
@OptIn(ExperimentalHorologistApi::class)
fun NodesScreen(
    nodes: Set<NodeUiModel>,
    modifier: Modifier = Modifier
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier
        ) {
            item {
                ResponsiveListHeader {
                    Text(stringResource(id = R.string.nodes))
                }
            }
            items(nodes.size) { index ->
                Chip(
                    label = nodes.elementAt(index).displayName,
                    onClick = { },
                    secondaryLabel = nodes.elementAt(index).id
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun NodesScreenPreview() {
    NodesScreen(setOf(NodeUiModel("aaaaaa", displayName = "Pixel Watch", isNearby = true)))
}
