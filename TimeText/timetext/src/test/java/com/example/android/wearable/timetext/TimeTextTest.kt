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
package com.example.android.wearable.timetext

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.widget.CurvedTextView
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.TimeZone

@Config(
    instrumentedPackages = [
        // https://github.com/robolectric/robolectric/issues/6593
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
@RunWith(AndroidJUnit4::class)
class TimeTextTest {

    private val application = RuntimeEnvironment.getApplication()

    /**
     * An arbitrary instant when this code was written.
     */
    private var currentInstant = 1_629_501_755_137L

    private val fakeClock = TimeText.Clock { currentInstant }

    // region straight time text tests

    @Test
    fun `straight title is hidden by default`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(activity).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextDivider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isNull()
    }

    @Test
    fun `straight title is shown when specified in attributes`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(
                context = activity,
                attrs = Robolectric.buildAttributeSet()
                    .addAttribute(R.attr.titleText, "Title")
                    .build()
            ).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(allOf(isDisplayed(), withText("Title"))))
        onView(withId(R.id.timeTextDivider)).check(matches(allOf(isDisplayed(), withText("·"))))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
    }

    @Test
    fun `straight title and color is shown when specified in attributes`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(
                context = activity,
                attrs = Robolectric.buildAttributeSet()
                    .addAttribute(R.attr.titleText, "Title")
                    .addAttribute(android.R.attr.titleTextColor, "#ffff00")
                    .build()
            ).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(allOf(isDisplayed(), withText("Title"))))
        onView(withId(R.id.timeTextDivider)).check(matches(allOf(isDisplayed(), withText("·"))))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
        assertThat(timeText.titleTextColor).isEqualTo(Color.YELLOW)
    }

    @Test
    fun `straight title is shown when updated post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(activity).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextDivider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        timeText.title = "Title"

        onView(withId(R.id.timeTextTitle)).check(matches(allOf(isDisplayed(), withText("Title"))))
        onView(withId(R.id.timeTextDivider)).check(matches(allOf(isDisplayed(), withText("·"))))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
    }

    @Test
    fun `straight 12 hour time shows`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("422")))
    }

    @Test
    fun `straight 24 hour time shows`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("1622")))
    }

    @Test
    fun `straight time updates format when changed post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("1622")))

        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        onView(withId(R.id.timeTextClock)).check(matches(withText("422")))
    }

    @Test
    fun `straight time updates timezone when changed post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("1622")))

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
        application.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED))

        onView(withId(R.id.timeTextClock)).check(matches(withText("022")))
    }

    @Test
    fun `straight time updates time on tick`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("1622")))

        currentInstant += 60_000
        application.sendBroadcast(Intent(Intent.ACTION_TIME_TICK))

        onView(withId(R.id.timeTextClock)).check(matches(withText("1623")))
    }

    @Test
    fun `straight time updates time on time changed`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withText("1622")))

        currentInstant += 3_600_000
        application.sendBroadcast(Intent(Intent.ACTION_TIME_CHANGED))

        onView(withId(R.id.timeTextClock)).check(matches(withText("1722")))
    }

    @SuppressLint("CheckResult")
    @Test
    fun `straight time ellipsizes title`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")

        val timeText = TimeText(application).apply {
            clock = fakeClock
        }.apply {
            title = "This is a really long title"
        }

        timeText.measure(
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
        )

        val timeTextTitle = timeText.findViewById<TextView>(R.id.timeTextTitle)
        assertThat(timeTextTitle.layout.getEllipsisCount(timeTextTitle.lineCount - 1))
    }

    // endregion straight time text tests

    // region curved time text tests

    @Test
    @Config(qualifiers = "round")
    fun `curved title is hidden by default`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(activity).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextDivider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isNull()
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved title is shown when specified in attributes`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(
                context = activity,
                attrs = Robolectric.buildAttributeSet()
                    .addAttribute(R.attr.titleText, "Title")
                    .build()
            ).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("Title")
                )
            )
        )
        onView(withId(R.id.timeTextDivider)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("·")
                )
            )
        )
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
        assertThat(timeText.titleTextColor).isEqualTo(Color.WHITE)
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved title and color is shown when specified in attributes`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(
                context = activity,
                attrs = Robolectric.buildAttributeSet()
                    .addAttribute(R.attr.titleText, "Title")
                    .addAttribute(android.R.attr.titleTextColor, "#ffff00")
                    .build()
            ).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("Title")
                )
            )
        )
        onView(withId(R.id.timeTextDivider)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("·")
                )
            )
        )
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
        assertThat(timeText.titleTextColor).isEqualTo(Color.YELLOW)
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved title is shown when updated post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        lateinit var timeText: TimeText

        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            timeText = TimeText(activity).apply {
                clock = fakeClock
            }

            activity.setContentView(timeText)
        }

        onView(withId(R.id.timeTextTitle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextDivider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        timeText.title = "Title"

        onView(withId(R.id.timeTextTitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("Title")
                )
            )
        )
        onView(withId(R.id.timeTextDivider)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withCurvedText("·")
                )
            )
        )
        onView(withId(R.id.timeTextClock)).check(matches(isDisplayed()))

        assertThat(timeText.title).isEqualTo("Title")
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved 12 hour time shows`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("422")))
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved 24 hour time shows`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("1622")))
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved time updates format when changed post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("1622")))

        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "12")

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("422")))
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved time updates timezone when changed post creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("1622")))

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
        application.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED))

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("022")))
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved time updates time on tick`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            activity.setContentView(
                TimeText(activity).apply {
                    clock = fakeClock
                }
            )
        }

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("1622")))

        currentInstant += 60_000
        application.sendBroadcast(Intent(Intent.ACTION_TIME_TICK))

        onView(withId(R.id.timeTextClock)).check(matches(withCurvedText("1623")))
    }

    @Test
    @Config(qualifiers = "round")
    fun `curved time ellipsizes title`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        Settings.System.putString(application.contentResolver, Settings.System.TIME_12_24, "24")

        val timeText = TimeText(application).apply {
            clock = fakeClock
        }.apply {
            title = "This is a really long title"
        }

        timeText.measure(
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
        )

        val timeTextTitle = timeText.findViewById<CurvedTextView>(R.id.timeTextTitle)
        val timeTextDivider = timeText.findViewById<CurvedTextView>(R.id.timeTextDivider)
        val timeTextClock = timeText.findViewById<CurvedTextView>(R.id.timeTextClock)

        // Evil reflection hack to check what is going to be drawn.
        val textToDrawField = CurvedTextView::class.java.getDeclaredField("mTextToDraw")
        textToDrawField.isAccessible = true
        val textToDraw = textToDrawField.get(timeTextTitle)
        assertThat(textToDraw).isNotEqualTo("This is a really long title")

        assertThat(
            timeTextTitle.sweepAngleDegrees + timeTextDivider.sweepAngleDegrees +
                timeTextClock.sweepAngleDegrees
        ).isAtMost(90f)
    }

    // endregion curved time text tests
}
