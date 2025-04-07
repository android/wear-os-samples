package com.google.samples.marketplace.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// TODO: Consider using Accompanist Permissions library
/** Utility class for Android permissions. */
object PermissionHelper {
    private const val CHANGE_ACTIVE_WATCH_FACE_PERMISSION =
        "com.google.wear.permission.SET_PUSHED_WATCH_FACE_AS_ACTIVE"

    fun hasChangeActiveWatchFacePermission(activity: Activity): Boolean =
        ContextCompat.checkSelfPermission(activity, CHANGE_ACTIVE_WATCH_FACE_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED

    fun shouldShowRequestPermissionRationaleForChangeActiveWatchFace(activity: Activity): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            CHANGE_ACTIVE_WATCH_FACE_PERMISSION,
        )

    fun launchPermissionSettings(activity: Activity) {
        val intent =
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", activity.packageName, null)
            }
        activity.startActivity(intent)
    }

    fun requestChangeActiveWatchFacePermission(
        launcher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        launcher.launch(CHANGE_ACTIVE_WATCH_FACE_PERMISSION)
    }
}
