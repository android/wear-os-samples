package com.example.android.wearable.composeadvanced.presentation.ui.valuedisplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.R

/**
 * Displays a value by using a Stepper or a Slider
 */
@Composable
fun ValueDisplayScreen(
    value: Int,
    onClickStepper: () -> Unit,
    onClickSlider: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(1f),
            text = "$value",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(4.dp))

        CompactChip(
            onClick = onClickStepper,
            label = {
                Text(
                    stringResource(R.string.stepper_label),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        Spacer(modifier = Modifier.size(4.dp))

        CompactChip(
            onClick = onClickSlider,
            label = {
                Text(
                    stringResource(R.string.slider_label),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

    }
}
