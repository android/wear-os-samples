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
package com.example.android.wearable.composeadvanced.benchmark

import android.content.Intent
import android.graphics.Point
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalBaselineProfilesApi
@RunWith(AndroidJUnit4::class)
class BaselineProfile {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun profile() {
        baselineRule.collectBaselineProfile(
            packageName = PACKAGE_NAME,
            profileBlock = {
                val intent = Intent()
                intent.action = ACTION
                startActivityAndWait(intent)

                findAndClickText("List of Watches")
                scrollDown()
                findAndClickText("Watch 2K")
                backWhenIdle()
                backWhenIdle()
                findAndClickText("Add to Time")
                findAndClickText("User Input Components")
                findAndClickText("Stepper")
                findAndClickDesc("Increase")
                backWhenIdle()
                findAndClickText("Slider")
                findAndClickDesc("Decrease")
                backWhenIdle()
                backWhenIdle()
            }
        )
    }

    private fun scrollDown() {
        // Scroll down to view remaining UI elements
        // Setting a gesture margin is important otherwise gesture nav is triggered.
        device.waitForIdle()
        val list = device.findObject(By.scrollable(true))
        list.setGestureMargin(device.displayWidth / 5)
        list.drag(Point(list.visibleCenter.x, list.visibleCenter.y / 2))
        device.waitForIdle()
    }

    private fun findAndClickText(text: String) {
        device.wait(Until.findObject(By.text(text)), 3000)
        device.findObject(By.text(text)).click()
    }

    private fun findAndClickDesc(desc: String) {
        device.wait(Until.findObject(By.desc(desc)), 3000)
        device.findObject(By.desc(desc)).click()
    }

    private fun backWhenIdle() {
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }

    companion object {
        private const val PACKAGE_NAME = "com.example.android.wearable.composeadvanced"
        private const val ACTION = "com.example.android.wearable.composeadvanced.MAIN"
    }
}
