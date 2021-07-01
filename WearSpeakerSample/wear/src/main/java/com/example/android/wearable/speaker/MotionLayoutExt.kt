/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.speaker

import androidx.annotation.IdRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.time.Duration
import kotlin.coroutines.resume

/**
 * A suspending function that will await transitioning to the given state in the [MotionLayout].
 *
 * If this doesn't occur within the specified [timeout] (a default of five seconds), this method will throw a
 * [TimeoutCancellationException].
 *
 * Based on Chris Banes's article "Suspending over Views":
 * [https://medium.com/androiddevelopers/suspending-over-views-example-260ce3dc9100]
 */
suspend fun MotionLayout.awaitState(
    @IdRes stateId: Int,
    timeout: Duration = Duration.ofMillis(5000),
) {
    // If we're already there, return immediately
    if (currentState == stateId) return

    var listener: MotionLayout.TransitionListener? = null

    try {
        withTimeout(timeout.toMillis()) {
            suspendCancellableCoroutine<Unit> { cont ->
                listener = object : TransitionAdapter() {
                    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                        if (currentId == stateId) {
                            cont.resume(Unit)
                        }
                    }
                }
                addTransitionListener(listener)
            }
        }
    } finally {
        removeTransitionListener(listener)
    }
}

/**
 * A version of [MotionLayout.transitionToState] that allows forcing an instant transition.
 *
 * If [transitionInstantly] is `true`, [MotionLayout.setProgress] will be called to directly update the position.
 */
fun MotionLayout.transitionToState(
    @IdRes id: Int,
    transitionInstantly: Boolean
) {
    transitionToState(id)
    // If we're in a defined transition, the state we are trying to transition to might be the start state or the end
    // state
    if (transitionInstantly) {
        progress = when {
            startState == id -> 0f
            endState == id -> 1f
            else -> throw AssertionError("Desired state was neither the start state nor the end state!")
        }
    }
}
