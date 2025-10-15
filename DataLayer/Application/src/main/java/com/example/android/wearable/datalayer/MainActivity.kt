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
package com.example.android.wearable.datalayer

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Manages Wearable clients to showcase the [DataClient], [MessageClient], [CapabilityClient].
 *
 * While resumed, this activity periodically sends a count through the [DataClient], and offers
 * the ability for the user to take and send a photo over the [DataClient].
 *
 * This activity also allows the user to launch the companion wear activity via the [MessageClient].
 *
 * While resumed, this activity also logs all interactions across the clients, which includes events
 * sent from this activity and from the watch(es).
 */
@SuppressLint("VisibleForTests")
class MainActivity : ComponentActivity() {
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val isCameraSupported by lazy {
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    private val takePhotoLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            clientDataViewModel.onPictureTaken(bitmap = bitmap)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var count = 0

        lifecycleScope.launch {
            if (isAvailable(capabilityClient)) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    // Set the initial trigger such that the first count will happen in one second.
                    var lastTriggerTime = Instant.now() - (countInterval - Duration.ofSeconds(1))
                    while (isActive) {
                        // Figure out how much time we still have to wait until our next desired trigger
                        // point. This could be less than the count interval if sending the count took
                        // some time.
                        delay(
                            Duration
                                .between(Instant.now(), lastTriggerTime + countInterval)
                                .toMillis()
                        )
                        // Update when we are triggering sending the count
                        lastTriggerTime = Instant.now()
                        sendCount(count)

                        // Increment the count to send next time
                        // This count is local to the specific instance of this activity and may reset
                        // when a new instance is recreated. For a more complex example where the counter
                        // is stored in a DataStore and modeled as a proto, see thre Horologist DataLayer sample in
                        // https://google.github.io/horologist/datalayer/
                        count++
                    }
                }
            }
        }

        // Loads any previous photo that was stored, to show in the app when it is launched.
        lifecycleScope.launch {
            if (isAvailable(dataClient)) {
                val initialImage = loadInitialImage()
                initialImage?.let {
                    clientDataViewModel.onPictureTaken(it)
                }
            }
        }

        setContent {
            MaterialTheme {
                var apiAvailable by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    apiAvailable = isAvailable(capabilityClient)
                }
                MainApp(
                    events = clientDataViewModel.events,
                    image = clientDataViewModel.image,
                    isCameraSupported = isCameraSupported,
                    apiAvailable = apiAvailable,
                    onTakePhotoClick = ::takePhoto,
                    onSendPhotoClick = ::sendPhoto,
                    onStartWearableActivityClick = ::startWearableActivity
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )

        if (isCameraSupported) {
            lifecycleScope.launch {
                try {
                    capabilityClient.addLocalCapability(CAMERA_CAPABILITY).await()
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (exception: Exception) {
                    Log.e(TAG, "Could not add capability: $exception")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)

        lifecycleScope.launch {
            // This is a judicious use of NonCancellable.
            // This is asynchronous clean-up, since the capability is no longer available.
            // If we allow this to be cancelled, we may leave the capability in-place for other
            // nodes to see.
            withContext(NonCancellable) {
                try {
                    capabilityClient.removeLocalCapability(CAMERA_CAPABILITY).await()
                } catch (exception: Exception) {
                    Log.e(TAG, "Could not remove capability: $exception")
                }
            }
        }
    }

    // This method starts the Wearable app on the connected Wear device.
    // Alternative to this implementation, Horologist offers a DataHelper API which allows to
    // start the main activity or a different activity of your choice from the Wearable app
    // see https://google.github.io/horologist/datalayer-helpers-guide/#launching-a-specific-activity-on-the-other-device
    // for details
    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                val nodes =
                    capabilityClient
                        .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                        .await()
                        .nodes

                // Send a message to all nodes in parallel
                // If you need an acknowledge for the start activity use case, you can alternatively use
                // [MessageClient.sendRequest](https://developers.google.com/android/reference/com/google/android/gms/wearable/MessageClient#sendRequest(java.lang.String,%20java.lang.String,%20byte[]))
                // See an implementation in Horologist DataHelper https://github.com/google/horologist/blob/release-0.5.x/datalayer/core/src/main/java/com/google/android/horologist/data/apphelper/DataLayerAppHelper.kt#L210
                nodes
                    .map { node ->
                        async {
                            messageClient
                                .sendMessage(node.id, START_ACTIVITY_PATH, byteArrayOf())
                                .await()
                        }
                    }.awaitAll()

                Log.d(TAG, "Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
    }

    private suspend fun loadInitialImage(): Bitmap? {
        try {
            val uri = buildUri(IMAGE_PATH) ?: return null

            val dataItem = dataClient.getDataItem(uri).await()
            dataItem.data?.let {
                val dataMapItem = DataMapItem.fromDataItem(dataItem)
                val asset = dataMapItem.dataMap.getAsset(IMAGE_KEY)
                asset?.let {
                    return dataClient.getFdForAsset(asset).await().let { fd ->
                        BitmapFactory.decodeFileDescriptor(fd.fdForAsset.fileDescriptor)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "loadInitialImage failed", e)
        }
        return null
    }

    private suspend fun buildUri(dataPath: String): Uri? {
        val node = nodeClient.localNode.await()
        return node?.let { it ->
            Uri
                .Builder()
                .scheme(WEAR_SCHEME)
                .authority(it.id)
                .path(dataPath)
                .build()
        }
    }

    private fun takePhoto() {
        if (!isCameraSupported) return
        takePhotoLauncher.launch(null)
    }

    private fun sendPhoto() {
        lifecycleScope.launch {
            try {
                val image = clientDataViewModel.image ?: return@launch
                val imageAsset = image.toAsset()
                val request =
                    PutDataMapRequest
                        .create(IMAGE_PATH)
                        .apply {
                            dataMap.putAsset(IMAGE_KEY, imageAsset)
                            dataMap.putLong(TIME_KEY, Instant.now().epochSecond)
                        }.asPutDataRequest()
                        .setUrgent()

                val result = dataClient.putDataItem(request).await()

                Log.d(TAG, "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Saving DataItem failed: $exception")
            }
        }
    }

    private suspend fun sendCount(count: Int) {
        try {
            val request =
                PutDataMapRequest
                    .create(COUNT_PATH)
                    .apply {
                        dataMap.putInt(COUNT_KEY, count)
                    }.asPutDataRequest()
                    .setUrgent()

            val result = dataClient.putDataItem(request).await()

            Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    /**
     * Converts the [Bitmap] to an asset, compress it to a png image in a background thread.
     */
    private suspend fun Bitmap.toAsset(): Asset = withContext(Dispatchers.Default) {
        ByteArrayOutputStream().use { byteStream ->
            compress(Bitmap.CompressFormat.PNG, 100, byteStream)
            Asset.createFromBytes(byteStream.toByteArray())
        }
    }

    // This function checks that the Wearable API is available on the mobile device.
    // If you are using the Horologist DataHelpers, this method is available in
    // https://google.github.io/horologist/datalayer-helpers-guide/#check-api-availability

    private suspend fun isAvailable(api: GoogleApi<*>): Boolean = try {
        GoogleApiAvailability
            .getInstance()
            .checkApiAvailability(api)
            .await()

        true
    } catch (e: AvailabilityException) {
        Log.d(
            TAG,
            "${api.javaClass.simpleName} API is not available in this device."
        )
        false
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val START_ACTIVITY_PATH = "/start-activity"
        const val COUNT_PATH = "/count"
        const val IMAGE_PATH = "/image"
        const val IMAGE_KEY = "photo"
        const val TIME_KEY = "time"
        const val COUNT_KEY = "count"
        private const val CAMERA_CAPABILITY = "camera"
        private const val WEAR_CAPABILITY = "wear"
        private const val WEAR_SCHEME = "wear"

        private val countInterval = Duration.ofSeconds(5)
    }
}
