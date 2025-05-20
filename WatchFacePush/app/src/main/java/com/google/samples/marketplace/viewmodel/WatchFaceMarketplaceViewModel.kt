/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.marketplace.viewmodel

import android.content.pm.PackageInfo
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.watchface.push.WatchFacePushManager
import com.google.samples.marketplace.MarketplaceApplication
import com.google.samples.marketplace.data.WatchFaceData
import com.google.samples.marketplace.data.WatchFacePackageRepository
import com.google.samples.marketplace.data.WatchFaceSlotInfo
import com.google.samples.marketplace.data.WatchFaceSlots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path

/** View model that implements the business logic for the watch face marketplace app. */
class WatchFaceMarketplaceViewModel(
    val watchFacePushManager: WatchFacePushManager,
    val watchFacePackageRepository: WatchFacePackageRepository
) : ViewModel() {
    /** Backing mutable state for controlling the loading state of the view model. */
    private val _loading = MutableStateFlow<Boolean>(true)

    /** Backing state to hold status messages to be displayed in a dialog. */
    val dialogMessage = mutableStateOf("")

    /**
     * Mutable state that contains the currently active watch face slot. Updating this triggers
     * re-computing the watch faces list, to update the state of whether a watch face is installed
     * or not.
     */
    private val _watchFaceSlots = MutableStateFlow<WatchFaceSlots?>(null)

    /**
     * Mutable state that contains the list of watch face packages available in the marketplace. It
     * is loaded upon initialisation of the view model and re-used whenever the watch face slot
     * changes to re-compute the state of active watch faces and whether any of them is installed or
     * not.
     */
    private val _watchFacePackages =
        MutableStateFlow<List<Pair<Path, PackageInfo>>>(placeholderPackages())

    /** Backing mutable state for the selected watch face details. */
    private val _selectedWatchFace = MutableStateFlow<String?>(null)

    /** Observable state that contains the watch face state to be displayed in the UI. */
    val watchFaceState: StateFlow<WatchFaceState> =
        combine(
            _watchFaceSlots,
            _watchFacePackages,
            _selectedWatchFace,
            _loading,
            ::computeWatchFaceUiData
        )
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                WatchFaceState(emptyList(), null, 0, true)
            )

    init {
        viewModelScope.launch {
            _watchFaceSlots.value = loadWatchFaces()
            _watchFacePackages.value = watchFacePackageRepository.loadPackages()
            _loading.value = false
        }
    }

    /**
     * Selects a watch face for which the details should be loaded.
     *
     * @param packageName representing the watch face to be selected.
     */
    fun selectWatchFace(packageName: String) {
        _selectedWatchFace.value = packageName
    }

    fun resetDialogMessage() {
        dialogMessage.value = ""
    }

    fun setDialogMessage(message: String) {
        dialogMessage.value = message
    }

    /**
     * Installs an embedded watch face.
     *
     * Note: In a production app, the status codes should not be leaked here, but mapped to some
     * domain object. However, this is a sample app, so the code is kept as simple as possible.
     *
     * @param watchFaceData The watch face data representing an embedded watch face.
     */
    fun installWatchFace(watchFaceData: WatchFaceData) {
        viewModelScope.launch {
            try {
                watchFacePackageRepository.pipeWatchFace(viewModelScope, watchFaceData).use {
                    val token = watchFacePackageRepository.getValidationToken(watchFaceData.name)
                    if (token != null) {
                        watchFacePushManager.addWatchFace(it.readFd, token)
                        _watchFaceSlots.value = loadWatchFaces()
                        logWithDialog("Watch face installed")
                    } else {
                        logWithDialog("No token found for watch face")
                    }
                }
            } catch (exception: WatchFacePushManager.AddWatchFaceException) {
                logWithDialog("Failed to install watch face - unhandled exception", exception)
            }
        }
    }

    /**
     * Uninstalls the currently installed watch face..
     *
     * Note: In a production app, the status codes should not be leaked here, but mapped to some
     * domain object. However, this is a sample app, so the code is kept as simple as possible.
     */
    fun uninstallWatchFace() {
        viewModelScope.launch {
            val slotId = _watchFaceSlots.value?.installedWatchFaces?.firstOrNull()?.slotId ?: run {
                logWithDialog("No watch face slot is defined")
                return@launch
            }
            try {
                watchFacePushManager.removeWatchFace(slotId)
                _watchFaceSlots.value = loadWatchFaces()
                logWithDialog("Watch face uninstalled")
            } catch (exception: WatchFacePushManager.RemoveWatchFaceException) {
                logWithDialog("Failed to uninstall watch face - unhandled exception", exception)
            }
        }
    }

    /**
     * Sets the watch face for the marketplace slot as active.
     *
     * Note: In a production app, the status codes should not be leaked here, but mapped to some
     * domain object. However, this is a sample app, so the code is kept as simple as possible.
     */
    fun setActiveWatchFace() {
        viewModelScope.launch {
            val slotId = _watchFaceSlots.value?.installedWatchFaces?.firstOrNull()?.slotId ?: run {
                logWithDialog("No watch face slot is defined")
                return@launch
            }
            try {
                watchFacePushManager.setWatchFaceAsActive(slotId)
                _watchFaceSlots.value = loadWatchFaces()
                logWithDialog("Watch face set as active")
            } catch (exception: Exception) {
                logWithDialog("Failed to set watch face as active", exception)
            }
        }
    }

    fun updateWatchFace(watchFaceData: WatchFaceData) {
        viewModelScope.launch {
            val slotId = _watchFaceSlots.value?.installedWatchFaces?.firstOrNull()?.slotId ?: run {
                logWithDialog("No watch face slot is defined")
                return@launch
            }
            try {
                watchFacePackageRepository.pipeWatchFace(viewModelScope, watchFaceData).use {
                    val token = watchFacePackageRepository.getValidationToken(watchFaceData.name)
                    if (token != null) {
                        watchFacePushManager.updateWatchFace(slotId, it.readFd, token)
                        _watchFaceSlots.value = loadWatchFaces()
                        logWithDialog("Watch face updated")
                    } else {
                        logWithDialog("No token found for watch face")
                    }
                }
            } catch (updateException: WatchFacePushManager.UpdateWatchFaceException) {
                logWithDialog("Failed to update watch face - unhandled exception", updateException)
            }
        }
    }

    /**
     * Computes the list of [WatchFaceState] from the given slot and list of watch face packages. It is
     * invoked any time the watch face slot changes as a result of install / uninstalling a watch
     * face.
     */
    private fun computeWatchFaceUiData(
        slots: WatchFaceSlots?,
        packages: List<Pair<Path, PackageInfo>>,
        selectedWatchFace: String?,
        isLoading: Boolean
    ): WatchFaceState {
        val watchFaces = packages.map { (packagePath, assetPackage) ->
            WatchFaceData(
                name =
                    packagePath.fileName?.toString()?.replace("marketplace_", "")
                        ?.replace(".apk", "") ?: "",
                assetPath = packagePath.toString(),
                packageName = assetPackage.packageName,
                versionCode = assetPackage.longVersionCode,
                slotInfo = slots?.installedWatchFaces?.firstOrNull { it.packageName == assetPackage.packageName },
            )
        }
        return WatchFaceState(
            watchFaces = watchFaces,
            selectedWatchFace = selectedWatchFace,
            remainingUnusedSlots = slots?.unusedSlots ?: 0,
            isLoading
        )
    }

    /**
     * Obtains the watch face details from the Watch Face Push Manager, or returns null if no watch
     * face is currently installed.
     */
    private suspend fun loadWatchFaces() = withContext(Dispatchers.IO) {
        try {
            val watchFaces = watchFacePushManager.listWatchFaces()
            val slots = mutableListOf<WatchFaceSlotInfo>()
            watchFaces.installedWatchFaceDetails.forEach { face ->
                val isActive = watchFacePushManager.isWatchFaceActive(face.packageName)
                val metaDataValue =
                    face.getMetaData("com.google.samples.marketplace.watchfacepush.river.custom_version")
                        .invoke().firstOrNull()
                val info = WatchFaceSlotInfo(
                    packageName = face.packageName,
                    slotId = face.slotId,
                    versionCode = face.versionCode,
                    isActive = isActive,
                    customVersion = metaDataValue
                )
                slots.add(info)
            }
            return@withContext WatchFaceSlots(slots, watchFaces.remainingSlotCount)
        } catch (exception: WatchFacePushManager.ListWatchFacesException) {
            logWithDialog("Failed to list watch faces", exception)
            null
        }
    }

    private fun logWithDialog(message: String, exception: Exception? = null) {
        if (exception != null) {
            Log.e(TAG, message, exception)
        } else {
            Log.i(TAG, message)
        }
        dialogMessage.value = message
    }

    private fun placeholderPackages() = List(3) { idx ->
        val packageInfo = PackageInfo().apply {
            packageName = "com.example.watchface1$idx"
            longVersionCode = 1
        }
        Pair(Path.of("marketplace_watchface1$idx.apk"), packageInfo)
    }

    companion object {
        private const val TAG = "WFMarketplaceViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as MarketplaceApplication)
                WatchFaceMarketplaceViewModel(
                    watchFacePushManager = app.watchFacePushManager,
                    watchFacePackageRepository = app.watchFacePackageRepository
                )
            }
        }
    }
}

/**
 * Represents the state of the watch faces for the purposes of the UI, including installed watch
 * face, those packages that could be installed, the currently selected watch face and the
 * remaining slots that could be used.
 */
data class WatchFaceState(
    val watchFaces: List<WatchFaceData>,
    val selectedWatchFace: String?,
    val remainingUnusedSlots: Int,
    val isLoading: Boolean
)
