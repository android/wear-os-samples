@file:OptIn(ExperimentalBaselineProfilesApi::class)

package com.example.baselineprofile

import android.graphics.Point
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the Generate Baseline Profile run configuration,
 * or directly with `generateBaselineProfile` Gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun generate() {
        rule.collectBaselineProfile("com.example.android.wearable.composeadvanced") {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll
            // through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            findAndClickText("List of Watches")
            scrollDown()
            findAndClickText("Watch 2K")
            backWhenIdle()
            backWhenIdle()
            scrollDown()
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
        device.findObject(By.text(text))?.click()
    }

    private fun findAndClickDesc(desc: String) {
        device.wait(Until.findObject(By.desc(desc)), 3000)
        device.findObject(By.desc(desc))?.click()
    }

    private fun backWhenIdle() {
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }
}
