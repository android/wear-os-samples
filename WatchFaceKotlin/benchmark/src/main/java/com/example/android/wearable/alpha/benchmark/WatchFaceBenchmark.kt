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
@file:OptIn(ExperimentalMetricApi::class)

package com.example.android.wearable.alpha.benchmark

import android.app.Instrumentation
import android.app.UiAutomation
import android.content.ComponentName
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.PowerMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Open Web UI.
 */
@LargeTest
@RunWith(Parameterized::class)
class WatchFaceBenchmark(
    private val ambient: Boolean
) {
    private lateinit var instrumentation: Instrumentation
    private lateinit var uiAutomation: UiAutomation
    private lateinit var device: UiDevice

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun setup() {
        instrumentation = InstrumentationRegistry.getInstrumentation()
        uiAutomation = instrumentation.uiAutomation
        device = UiDevice.getInstance(instrumentation)
    }

    @Before
    fun after() {
        device.wakeUp()
        device.startWatchface(DefaultWatchFace)
    }

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(PowerMetric(PowerMetric.Battery())),
        compilationMode = CompilationMode.Partial(),
        iterations = 10,
        startupMode = StartupMode.WARM,
        setupBlock = {
            device.startWatchface(
                ComponentName(
                    PACKAGE_NAME,
                    "com.example.android.wearable.alpha.AnalogWatchFaceService"
                )
            )
            repeat(5) {
                println("Sleep in setupBlock $it")
                Thread.sleep(1000)
            }
            if (ambient) {
                println("Ambient Mode")
                device.sleep()
            } else {
                println("Waking Up")
                device.wakeUp()
            }
        },
    ) {
        repeat(10) {
            println("Sleep in test $it")
            Thread.sleep(1000)
        }
    }

    companion object {
        @Parameterized.Parameters(name = "ambient={0}")
        @JvmStatic
        fun parameters() = listOf(true)

        const val PACKAGE_NAME = "com.example.android.wearable.alpha"
    }
}


