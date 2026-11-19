/*
 * Copyright 2026 The Android Open Source Project
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
package com.example.android.wearable.datalayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityCountScheduleTest {
    @Test
    fun repeatedWakeupOvershoots_stayLockedToMonotonicGrid() {
        val startRealtimeMs = 10_000L
        var nextTriggerRealtimeMs = startRealtimeMs + INITIAL_COUNT_DELAY_MS
        assertEquals(11_000L, nextTriggerRealtimeMs)

        for (step in 0 until 100) {
            val overshootMs = 4L + (step % 16) // +4 ms..+19 ms wakeup overshoot per tick
            val wakeupRealtimeMs = nextTriggerRealtimeMs + overshootMs
            nextTriggerRealtimeMs =
                nextCountTriggerRealtimeMs(
                    previousTriggerRealtimeMs = nextTriggerRealtimeMs,
                    nowRealtimeMs = wakeupRealtimeMs
                )

            val expectedTriggerRealtimeMs =
                startRealtimeMs + INITIAL_COUNT_DELAY_MS + (step + 1L) * COUNT_INTERVAL_MS
            assertEquals(expectedTriggerRealtimeMs, nextTriggerRealtimeMs)
        }
    }

    @Test
    fun subIntervalSendLatency_preservesFiveSecondCadence() {
        val firstTriggerRealtimeMs = 1_000L

        // Typical 45 ms sendCount IPC after an 8 ms wakeup overshoot (completes at 1,053 ms):
        val typicalSendCompletionMs = 1_008L + 45L
        val nextAfterTypicalSend =
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = firstTriggerRealtimeMs,
                nowRealtimeMs = typicalSendCompletionMs
            )
        assertEquals(6_000L, nextAfterTypicalSend)
        assertEquals(4_947L, nextAfterTypicalSend - typicalSendCompletionMs)

        // Near-interval 4,999 ms sendCount IPC after exact wakeup (completes at 5,999 ms):
        val slowSubIntervalCompletionMs = firstTriggerRealtimeMs + 4_999L
        val nextAfterSlowSubIntervalSend =
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = firstTriggerRealtimeMs,
                nowRealtimeMs = slowSubIntervalCompletionMs
            )
        assertEquals(6_000L, nextAfterSlowSubIntervalSend)
        assertEquals(1L, nextAfterSlowSubIntervalSend - slowSubIntervalCompletionMs)
    }

    @Test
    fun multiIntervalStallAndExactBoundaries_skipMissedIntervalsWithoutBurst() {
        val previousTriggerRealtimeMs = 1_000L

        // 12.3 s stall skips the 6,000 ms and 11,000 ms slots and schedules 16,000 ms.
        val stalledNowMs = previousTriggerRealtimeMs + 12_300L
        val afterStall =
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = previousTriggerRealtimeMs,
                nowRealtimeMs = stalledNowMs
            )
        assertEquals(16_000L, afterStall)
        assertTrue(afterStall > stalledNowMs)

        // Exact 5,000 ms boundary (now = 6,000 ms) advances strictly into the future (11,000 ms).
        val exactSingleBoundaryNowMs = previousTriggerRealtimeMs + COUNT_INTERVAL_MS
        val afterExactSingleBoundary =
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = previousTriggerRealtimeMs,
                nowRealtimeMs = exactSingleBoundaryNowMs
            )
        assertEquals(11_000L, afterExactSingleBoundary)
        assertTrue(afterExactSingleBoundary > exactSingleBoundaryNowMs)

        // Early timer wakeup (now < previousTrigger) still advances by exactly one interval.
        val afterEarlyWakeup =
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = previousTriggerRealtimeMs,
                nowRealtimeMs = 998L
            )
        assertEquals(6_000L, afterEarlyWakeup)
    }

    @Test
    fun nonPositiveInterval_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = 1_000L,
                nowRealtimeMs = 1_000L,
                intervalMs = 0L
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            nextCountTriggerRealtimeMs(
                previousTriggerRealtimeMs = 1_000L,
                nowRealtimeMs = 1_000L,
                intervalMs = -5_000L
            )
        }
    }
}
