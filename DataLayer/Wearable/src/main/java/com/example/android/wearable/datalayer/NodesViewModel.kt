/*
 * Copyright 2024 The Android Open Source Project
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android.wearable.datalayer.MainActivity.Companion.CAMERA_CAPABILITY
import com.example.android.wearable.datalayer.MainActivity.Companion.MOBILE_CAPABILITY
import com.example.android.wearable.datalayer.MainActivity.Companion.WEAR_CAPABILITY
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class NodesViewModel(
    private val capabilityClient: CapabilityClient
) : ViewModel() {

    private val nodes: SharedFlow<Set<Node>> = flow {
        emit(
            getCapabilitiesForReachableNodes()
                .filterValues { MOBILE_CAPABILITY in it || WEAR_CAPABILITY in it }.keys
        )
    }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )

    private val cameraNodes: SharedFlow<Set<Node>> = flow {
        emit(
            getCapabilitiesForReachableNodes()
                .filterValues { MOBILE_CAPABILITY in it && CAMERA_CAPABILITY in it }.keys
        )
    }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )

    /**
     * Collects the capabilities for all nodes that are reachable using the [CapabilityClient].
     *
     * [CapabilityClient.getAllCapabilities] returns this information as a [Map] from capabilities
     * to nodes, while this function inverts the map so we have a map of [Node]s to capabilities.
     *
     * This form is easier to work with when trying to operate upon all [Node]s.
     */
    private suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> =
        capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
            .await()
            // Pair the list of all reachable nodes with their capabilities
            .flatMap { (capability, capabilityInfo) ->
                capabilityInfo.nodes.map { it to capability }
            }
            // Group the pairs by the nodes
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            // Transform the capability list for each node into a set
            .mapValues { it.value.toSet() }

    val state =
        combine(
            nodes,
            cameraNodes
        ) { nodes, cameraNodes ->
            UIState(
                nodes.mapTo(mutableSetOf()) { it.toNodesUI() },
                cameraNodes.mapTo(mutableSetOf()) { it.toNodesUI() }
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UIState()
            )

    data class UIState(
        val nodes: Set<NodeUiModel> = setOf(),
        val cameraNodes: Set<NodeUiModel> = setOf()
    )

    companion object {
        private const val TAG = "NodesViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!

                val capabilityClient = (application as SampleApplication).capabilityClient
                NodesViewModel(
                    capabilityClient
                )
            }
        }
    }
}

data class NodeUiModel(
    val id: String,
    val displayName: String,
    val isNearby: Boolean
)

fun Node.toNodesUI(): NodeUiModel = NodeUiModel(
    id = this.id,
    displayName = this.displayName,
    isNearby = isNearby
)
