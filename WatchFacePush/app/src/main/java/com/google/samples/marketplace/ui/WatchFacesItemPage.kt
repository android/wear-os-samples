package com.google.samples.marketplace.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.google.samples.marketplace.R
import com.google.samples.marketplace.viewmodel.WatchFaceMarketplaceViewModel

/** Renders the details and the controls for a watch face. */
@Composable
fun WatchFaceItemPage(viewModel: WatchFaceMarketplaceViewModel) {
    val context = LocalContext.current
    val changeActiveWatchFacePermissionLauncher =
        rememberLauncherForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.setDialogMessage(context.getString(R.string.permission_granted))
                viewModel.setActiveWatchFace()
            } else {
                viewModel.setDialogMessage(context.getString(R.string.permission_denied))
            }
        }

    val onSetActiveButtonClicked = {
        if (PermissionHelper.hasChangeActiveWatchFacePermission(context as Activity)) {
            viewModel.setDialogMessage(context.getString(R.string.permission_granted))
            viewModel.setActiveWatchFace()
        } else {
            if (PermissionHelper.shouldShowRequestPermissionRationaleForChangeActiveWatchFace(
                    context
                )
            ) {
                PermissionHelper.launchPermissionSettings(context)
            } else {
                PermissionHelper.requestChangeActiveWatchFacePermission(
                    changeActiveWatchFacePermissionLauncher
                )
            }
        }
    }

    val scrollState = rememberTransformingLazyColumnState()
    val watchFaceState by viewModel.watchFaceState.collectAsStateWithLifecycle()
    val selectedWatchFace = watchFaceState.selectedWatchFace!!
    val watchFaceData = watchFaceState.watchFaces.find { it.packageName == selectedWatchFace }!!
    val updatable = watchFaceState.watchFaces.any {
        (it != watchFaceData && it.slotInfo != null) ||
            (it == watchFaceData && it.slotInfo != null && it.slotInfo.versionCode < watchFaceData.versionCode)
    }
    val statusUpdate by viewModel.dialogMessage

    ScreenScaffold(
        scrollState = scrollState
    ) { contentPadding ->
        TransformingLazyColumn(
            state = scrollState,
            contentPadding = contentPadding
        ) {
            watchFaceItem(
                name = watchFaceData.name,
                watchFaceData = watchFaceData,
                isUpdateable = updatable,
                isUnusedSlotAvailable = watchFaceState.remainingUnusedSlots > 0,
                onSetActiveButtonClicked = onSetActiveButtonClicked,
                onInstallClick = { viewModel.installWatchFace(watchFaceData) },
                onUpdateClick = { viewModel.updateWatchFace(watchFaceData) },
                onUninstallClick = { viewModel.uninstallWatchFace() }
            )
        }
        ConfirmationDialog(
            visible = statusUpdate.isNotEmpty(),
            onDismissRequest = { viewModel.resetDialogMessage() },
            curvedText = {},
            durationMillis = 1000L
        ) {
            Text(textAlign = TextAlign.Center, text = statusUpdate)
        }
    }
}
