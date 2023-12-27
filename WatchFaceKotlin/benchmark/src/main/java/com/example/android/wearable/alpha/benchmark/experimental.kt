package com.example.android.wearable.alpha.benchmark

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun Context.startWatchface(watchfaceName: ComponentName) {
    val intent = Intent("com.google.android.wearable.app.DEBUG_SURFACE").apply {
        putExtra("operation", "set-watchface")
        putExtra("component", watchfaceName)
    }
    submitBroadcast(intent) {
        println("BUT result")
        println(it)
        println(it.extras?.keySet()?.toList())
        println(it.extras?.get("operation"))
        println(it.dataString)
        println(it.flags)
    }
}
suspend fun Context.currentWatchface(): ComponentName {
    val intent = Intent("com.google.android.wearable.app.DEBUG_SURFACE").apply {
        putExtra("operation", "current-watchface")
    }
    return submitBroadcast(intent) {
        println("BUT result")
        println(it)
        println(it.extras?.keySet()?.toList())
        println(it.extras?.get("operation"))
        println(it.dataString)
        println(it.flags)
        ComponentName("", "")
    }
}

suspend fun <T> Context.submitBroadcast(intent: Intent, handleResult: (Intent) -> T): T {
    return suspendCancellableCoroutine { cont ->
        try {
            sendOrderedBroadcast(
                intent, null, object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        try {
                            cont.resume(handleResult(intent))
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }

                    }
                }, null, Activity.RESULT_OK, null, null
            )
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }
}
