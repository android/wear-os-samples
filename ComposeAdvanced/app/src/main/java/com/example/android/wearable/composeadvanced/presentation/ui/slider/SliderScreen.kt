package com.example.android.wearable.composeadvanced.presentation.ui.slider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.InlineSlider

/**
 * Displays a Slider
 */
@Composable
fun SliderScreen(
    displayValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        InlineSlider(
            value = displayValue,
            onValueChange = onValueChange,
            valueProgression = 0..10,
            segmented = true
        )
    }
}
