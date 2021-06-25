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
