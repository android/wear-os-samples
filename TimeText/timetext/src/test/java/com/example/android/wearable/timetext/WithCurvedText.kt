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

import android.view.View
import androidx.test.espresso.matcher.BoundedDiagnosingMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.wear.widget.CurvedTextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`

/**
 * A variant of [ViewMatchers.withText] for [CurvedTextView].
 */
fun withCurvedText(text: String): Matcher<View> = CurvedWithTextMatcher(`is`(text))

private class CurvedWithTextMatcher(
    private val stringMatcher: Matcher<String>
) : BoundedDiagnosingMatcher<View, CurvedTextView>(CurvedTextView::class.java) {
    override fun describeMoreTo(description: Description) {
        description.appendText("view.getText() to match: ")
        stringMatcher.describeTo(description)
    }

    override fun matchesSafely(item: CurvedTextView, mismatchDescription: Description): Boolean =
        stringMatcher.matches(item.text)
}
