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

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptionsBuilder
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.android.wearable.composeadvanced.presentation.MainActivity
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Demonstration of a Compose Activity test.
 */
class NavActivityTest {
    @get:Rule
    var rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        createAndroidComposeRule()

    @Test
    fun testEvent() {
        val scenario = rule.activityRule.scenario

        assertEquals(Screen.Landing.route, rule.activity.route)

        rule.activity.navigateTo(Screen.WatchList.route)
        assertEquals(Screen.WatchList.route, rule.activity.route)

        rule.activity.navigateTo(Screen.Landing.route) {
            popUpTo(Screen.Landing.route)
        }
        assertEquals(Screen.Landing.route, rule.activity.route)

        scenario.moveToState(Lifecycle.State.STARTED)

        scenario.moveToState(Lifecycle.State.RESUMED)

        assertEquals(Screen.Landing.route, rule.activity.route)
        rule.activity.navigateTo(Screen.WatchList.route)

        assertEquals(Screen.WatchList.route, rule.activity.route)
        rule.activity.navigateTo(Screen.Landing.route) {
            popUpTo(Screen.Landing.route)
        }

        assertEquals(Screen.Landing.route, rule.activity.route)
    }

    private val MainActivity.route: String?
        get() {
            rule.waitForIdle()
            return navController.currentBackStackEntry?.destination?.route
        }

    private fun MainActivity.navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        rule.runOnUiThread {
            navController.navigate(route, builder = builder)
        }
        rule.waitForIdle()
    }
}
