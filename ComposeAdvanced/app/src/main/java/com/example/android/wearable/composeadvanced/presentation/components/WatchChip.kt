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

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.R

/**
 * Simple Chip for displaying the Watch models.
 */
@Composable
fun WatchAppChip(
    watchModelNumber: Int,
    watchName: String,
    watchIcon: Int,
    onClickWatch: (Int) -> Unit
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        enabled = true,
        icon = {
            Icon(
                painter = painterResource(id = watchIcon),
                contentDescription = stringResource(R.string.watch_icon_content_description),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onPrimary,
                text = watchName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryLabel = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onPrimary,
                text = "id: $watchModelNumber",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onClick = {
            onClickWatch(watchModelNumber)
        }
    )
}

@Preview(
    widthDp = 300,
    heightDp = 80,
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    backgroundColor = 0x000000,
    showBackground = true
)
@Composable
fun PreviewWatchAppChip() {
    WatchAppChip(
        watchModelNumber = 123456,
        watchName = "Watch 123456 Name",
        watchIcon = R.drawable.ic_watch,
        onClickWatch = { }
    )
}
