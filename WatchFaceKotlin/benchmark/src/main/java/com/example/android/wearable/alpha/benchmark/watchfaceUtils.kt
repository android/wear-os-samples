package com.example.android.wearable.alpha.benchmark

import android.app.UiAutomation
import android.content.ComponentName
import android.view.KeyEvent
import androidx.test.uiautomator.UiDevice

fun UiDevice.startWatchface(watchfaceName: ComponentName) {
    // From https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:deploy/deployer/src/main/java/com/android/tools/deployer/model/component/WatchFace.java
    val result =
        executeShellCommand("am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation set-watchface --ecn component ${watchfaceName.flattenToString()}")

    // TODO error checking
}

fun UiDevice.currentWatchface(): String {
    return executeShellCommand("am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation current-watchface")
}

val DefaultWatchFace = ComponentName(
    "com.google.android.wearable.sysui",
    "com.google.android.clockwork.sysui.experiences.defaultwatchface.DefaultWatchFace"
)

inline fun UiAutomation.withShellPermission(block: () -> Unit) {
    adoptShellPermissionIdentity()

    try {
        block()
    } finally {
        dropShellPermissionIdentity()
    }
}

fun UiDevice.pressSleep() {
    pressKeyCode(KeyEvent.KEYCODE_SLEEP)
}

fun UiDevice.pressWakeup() {
    pressKeyCode(KeyEvent.KEYCODE_WAKEUP)
}
