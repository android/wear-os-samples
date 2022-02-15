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
 * Displays a Slider, which allows users to make a selection from a range of values.
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/material/package-summary#InlineSlider(kotlin.Float,kotlin.Function1,kotlin.Int,androidx.compose.ui.Modifier,kotlin.Boolean,kotlin.ranges.ClosedFloatingPointRange,kotlin.Boolean,kotlin.Function0,kotlin.Function0,androidx.wear.compose.material.InlineSliderColors)
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
