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
package com.example.android.wearable.alpha.benchmark

import android.app.Instrumentation
import android.app.UiAutomation
import android.content.ComponentName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Demonstration of a composable test.
 */
@RunWith(AndroidJUnit4::class)
class BroadcastUtilsTest {
    private lateinit var instrumentation: Instrumentation
    private lateinit var uiAutomation: UiAutomation
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        instrumentation = InstrumentationRegistry.getInstrumentation()
        uiAutomation = instrumentation.uiAutomation
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun testSettingAndGettingCurrentWatchface() = runTest {
        uiAutomation.withShellPermission {
            println(instrumentation.context.currentWatchface())
            println(
                instrumentation.context.startWatchface(
                    ComponentName(
                        WatchFaceBenchmark.PACKAGE_NAME,
                        "com.example.android.wearable.alpha.AnalogWatchFaceService"
                    )
                )
            )
            println(instrumentation.context.currentWatchface())
            println(
                instrumentation.context.startWatchface(
                    DefaultWatchFace
                )
            )
        }
    }

    @Test
    fun testSettingAndGettingCurrentWatchfaceWithShell() = runTest {
        uiAutomation.withShellPermission {
            println(device.currentWatchface())
            println(
                device.startWatchface(
                    ComponentName(
                        WatchFaceBenchmark.PACKAGE_NAME,
                        "com.example.android.wearable.alpha.AnalogWatchFaceService"
                    )
                )
            )
            println(device.currentWatchface())
        }
    }

    @Test
    fun testAmbientMode() = runTest {
        device.pressSleep()
    }

    @Test
    fun testInteractiveMode() = runTest {
        device.pressWakeup()
        device.wakeUp()
    }
}
