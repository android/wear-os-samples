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
@file:OptIn(ExperimentalMetricApi::class, ExperimentalPerfettoTraceProcessorApi::class)
@file:Suppress("SEALED_INHERITOR_IN_DIFFERENT_MODULE", "SEALED_INHERITOR_IN_DIFFERENT_PACKAGE", "SEALED_SUPERTYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER", "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER")

package com.example.android.wearable.alpha.benchmark

import android.content.ComponentName
import android.content.Intent
import android.graphics.Point
import androidx.benchmark.DummyMetric
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.MemoryCountersMetric
import androidx.benchmark.macro.Metric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.benchmark.perfetto.PerfettoTraceProcessor
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@LargeTest
@RunWith(Parameterized::class)
class WatchFaceBenchmark(
    private val compilationMode: CompilationMode
) {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(MemoryCountersMetric(), DummyMetric()),
        compilationMode = compilationMode,
        iterations = 1,
        startupMode = StartupMode.WARM,
        setupBlock = {
            startWatchface(ComponentName(PACKAGE_NAME, "com.example.android.wearable.alpha.AnalogWatchFaceService"))
        }
    ) {
        Thread.sleep(10000)
//        val list = device.findObject(By.desc("ScalingLazyColumn"))
//
//        // Setting a gesture margin is important otherwise gesture nav is triggered.
//        list.setGestureMargin(device.displayWidth / 5)
//
//        repeat(5) {
//            list.drag(Point(list.visibleCenter.x, list.visibleCenter.y / 3))
//            device.waitForIdle()
//        }
    }

    companion object {
        @Parameterized.Parameters(name = "compilation={0}")
        @JvmStatic
        fun parameters() = listOf(CompilationMode.Partial())

        private const val PACKAGE_NAME = "com.example.android.wearable.alpha"
    }
}

private fun MacrobenchmarkScope.startWatchface(watchfaceName: ComponentName) {
    // From https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:deploy/deployer/src/main/java/com/android/tools/deployer/model/component/WatchFace.java
    val result = this.device.executeShellCommand("am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation set-watchface --ecn component ${watchfaceName.flattenToString()}")

    println(result)
}


