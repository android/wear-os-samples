package com.example.android.wearable.composeadvanced.presentation.ui.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.Text

/**
 * Displays a Stepper, which allows users to make a selection from a range of values.
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/material/package-summary#Stepper(kotlin.Int,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.ranges.IntProgression,kotlin.Function0,kotlin.Function0,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1)
 */
@Composable
fun StepperScreen(
    displayValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Stepper(
            value = displayValue,
            onValueChange = onValueChange,
            valueProgression = 1..10
        ) { Text("Value: $displayValue") }
    }
}
