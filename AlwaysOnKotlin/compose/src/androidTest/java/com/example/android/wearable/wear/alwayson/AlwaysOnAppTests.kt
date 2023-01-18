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
package com.example.android.wearable.wear.alwayson

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AlwaysOnAppTests {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    private val testScope = TestScope()

    /**
     * A timestamp in the relatively far, far future (year 2200).
     *
     * This ensures the real alarm manager won't actually trigger.
     */
    private var instant = YEAR_2200_INSTANT

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    private lateinit var uiDevice: UiDevice

    @Before
    fun setup() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Ensure we are starting in active mode
        pressKeyCodeWithWait(KeyEvent.KEYCODE_WAKEUP)

        // Override the active dispatcher with a test one that we can control the time for
        activeDispatcher = StandardTestDispatcher(testScope.testScheduler)

        // Override the clock to always delegate to a fixed clock for the current instant
        val delegateClock = { Clock.fixed(instant, ZoneId.of("UTC")) }
        clock = object : Clock() {
            override fun getZone(): ZoneId = delegateClock().zone
            override fun withZone(zone: ZoneId?): Clock = delegateClock().withZone(zone)
            override fun instant(): Instant = delegateClock().instant()
        }
        scenario = launchActivity()
    }

    @After
    fun teardown() {
        scenario.close()
    }

    @Test
    fun initialTextIsCorrect(): Unit = testScope.runTest {
        scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.onNodeWithTag("time").assertTextEquals(ZERO_SEC_DISPLAY)
        composeTestRule.onNodeWithTag("timestamp")
            .assertTextEquals(
                context.getString(
                    R.string.timestamp_label,
                    YEAR_2200_INSTANT.toEpochMilli()
                )
            )
        composeTestRule.onNodeWithTag("mode")
            .assertTextEquals(context.getString(R.string.mode_active_label))
        composeTestRule.onNodeWithTag("drawCount")
            .assertTextEquals(context.getString(R.string.draw_count_label, 1))

        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun textIsCorrectAfterFiveSeconds(): Unit = testScope.runTest {
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        // Advance 5 seconds, one at a time
        repeat(5) {
            advanceTime(Duration.ofSeconds(1))
        }

        composeTestRule.onNodeWithTag("time").assertTextEquals(FIVE_SEC_DISPLAY)
        composeTestRule.onNodeWithTag("timestamp")
            .assertTextEquals(
                context.getString(
                    R.string.timestamp_label,
                    YEAR_2200_INSTANT.plusSeconds(5).toEpochMilli()
                )
            )
        composeTestRule.onNodeWithTag("mode")
            .assertTextEquals(context.getString(R.string.mode_active_label))
        composeTestRule.onNodeWithTag("drawCount")
            .assertTextEquals(context.getString(R.string.draw_count_label, 6))

        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun textIsCorrectAfterGoingIntoAmbientMode(): Unit = testScope.runTest {
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        // Advance 5 seconds, one at a time
        repeat(5) {
            advanceTime(Duration.ofSeconds(1))
        }

        pressKeyCodeWithWait(KeyEvent.KEYCODE_SLEEP)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("time").assertTextEquals(FIVE_SEC_DISPLAY)
        composeTestRule.onNodeWithTag("timestamp")
            .assertTextEquals(
                context.getString(
                    R.string.timestamp_label,
                    YEAR_2200_INSTANT.plusSeconds(5).toEpochMilli()
                )
            )
        composeTestRule.onNodeWithTag("mode")
            .assertTextEquals(context.getString(R.string.mode_ambient_label))
        composeTestRule.onNodeWithTag("drawCount")
            .assertTextEquals(context.getString(R.string.draw_count_label, 7))

        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun textIsCorrectAfterGoingIntoAmbientModeAndReceivingIntent(): Unit = testScope.runTest {
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        // Advance 5 seconds, one at a time
        repeat(5) {
            advanceTime(Duration.ofSeconds(1))
        }

        advanceTime(Duration.ofMillis(500))

        pressKeyCodeWithWait(KeyEvent.KEYCODE_SLEEP)
        composeTestRule.waitForIdle()

        // Simulate a sent broadcast
        advanceTime(Duration.ofSeconds(5))
        scenario.onActivity {
            PendingIntent.getBroadcast(
                it,
                0,
                Intent(AMBIENT_UPDATE_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT
            ).send()
        }

        Thread.sleep(1000) // Ugly sleep, without it sometimes the broadcast won't be received
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("time").assertTextEquals(TEN_SEC_DISPLAY)
        composeTestRule.onNodeWithTag("timestamp")
            .assertTextEquals(
                context.getString(
                    R.string.timestamp_label,
                    YEAR_2200_INSTANT.plusSeconds(10).plusMillis(500).toEpochMilli()
                )
            )
        composeTestRule.onNodeWithTag("mode")
            .assertTextEquals(context.getString(R.string.mode_ambient_label))
        composeTestRule.onNodeWithTag("drawCount")
            .assertTextEquals(context.getString(R.string.draw_count_label, 8))

        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun textIsCorrectAfterReturningToActiveMode(): Unit = testScope.runTest {
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        // Advance 5 seconds, one at a time
        repeat(5) {
            advanceTime(Duration.ofSeconds(1))
        }

        advanceTime(Duration.ofMillis(500))

        // Enter ambient mode
        pressKeyCodeWithWait(KeyEvent.KEYCODE_SLEEP)
        composeTestRule.waitForIdle()

        // Simulate a sent broadcast
        advanceTime(Duration.ofSeconds(5))
        scenario.onActivity {
            PendingIntent.getBroadcast(
                it,
                0,
                Intent(AMBIENT_UPDATE_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT
            ).send()
        }

        Thread.sleep(1000) // Ugly sleep, without it sometimes the broadcast won't be received
        composeTestRule.waitForIdle()

        advanceTime(Duration.ofSeconds(2))

        // Exit ambient mode
        pressKeyCodeWithWait(KeyEvent.KEYCODE_WAKEUP)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("time").assertTextEquals(TWELVE_SEC_DISPLAY)
        composeTestRule.onNodeWithTag("timestamp")
            .assertTextEquals(
                context.getString(
                    R.string.timestamp_label,
                    YEAR_2200_INSTANT.plusSeconds(12).plusMillis(500).toEpochMilli()
                )
            )
        composeTestRule.onNodeWithTag("mode")
            .assertTextEquals(context.getString(R.string.mode_active_label))
        composeTestRule.onNodeWithTag("drawCount")
            .assertTextEquals(context.getString(R.string.draw_count_label, 9))

        scenario.moveToState(Lifecycle.State.STARTED)
    }

    /**
     * Advances the simulated time by the given [duration], updating the [TestCoroutineScheduler]
     * and running any updates due to those changes.
     */
    private fun advanceTime(duration: Duration) {
        instant += duration
        testScope.testScheduler.advanceTimeBy(duration.toMillis())
        testScope.testScheduler.runCurrent()
        composeTestRule.waitForIdle()
    }

    /**
     * Presses the given key with an ugly sleep, without it sometimes ambient mode won't be entered
     * or exited.
     */
    private fun pressKeyCodeWithWait(keyCode: Int) {
        uiDevice.pressKeyCode(keyCode)
        Thread.sleep(1000)
    }
}

private val YEAR_2200_INSTANT = Instant.ofEpochMilli(7258118400000L)

private const val ZERO_SEC_DISPLAY = "00:00:00"
private const val FIVE_SEC_DISPLAY = "00:00:05"
private const val TEN_SEC_DISPLAY = "00:00:10"
private const val TWELVE_SEC_DISPLAY = "00:00:12"
