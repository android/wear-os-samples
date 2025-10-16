/*
 * Copyright 2025 The Android Open Source Project
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

import android.util.Log
import com.example.android.wearable.datalayer.MainActivity.Companion.COUNT_PATH
import com.example.android.wearable.datalayer.MainActivity.Companion.IMAGE_PATH
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataItemBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * Class to migrate data items to the new phone when the user gets a new device.
 */
class MigrationDataLayerService : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    /**
     * Receives the archive of data items from the old phone.
     *
     * @param nodeId The node ID of the old device.
     * @param archive The archive of data items.
     */
    override fun onNodeMigrated(nodeId: String, archive: DataItemBuffer) {
        super.onNodeMigrated(nodeId, archive)

        Log.i(TAG, "Migrating ${archive.count} items")
        Log.i(TAG, "From: $nodeId")

        val dataItemsToHandle = archive.use { buffer ->
            buffer.map { dataItem ->
                Log.i(TAG, "Preparing ${dataItem.uri.path}")
                dataItem.freeze()
            }
        }

        runBlocking {
            Log.i(TAG, "Migrating items")
            Log.i(TAG, "To: ${getLocalNode().displayName} - ${getLocalNode().id}")

            dataItemsToHandle.forEach { archiveItem ->
                migrateDataItem(archiveItem)
            }
            Log.i(TAG, "Migration complete")
        }
    }

    private suspend fun migrateDataItem(dataItem: DataItem) {
        val data = dataItem.data ?: return
        val path = dataItem.uri.path ?: return

        Log.i(TAG, "Restoring $path to new device.")
        when (path) {
            IMAGE_PATH -> {
                val srcDataMapItem = DataMapItem.fromDataItem(dataItem)
                dataClient
                    .putDataItem(
                        PutDataMapRequest
                            .create(path)
                            .apply {
                                dataMap.putAll(srcDataMapItem.dataMap)
                            }.asPutDataRequest()
                    ).await()
            }
            COUNT_PATH -> {
                dataClient
                    .putDataItem(
                        PutDataRequest.create(path).setData(data)
                    ).await()
            }
        }
    }

    private suspend fun getLocalNode(): Node = nodeClient.localNode.await()

    companion object {
        private const val TAG = "MigrationDataLayerService"
    }
}
