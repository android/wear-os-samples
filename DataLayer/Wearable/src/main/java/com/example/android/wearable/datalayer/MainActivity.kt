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

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp(mainViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(mainViewModel)
        messageClient.addListener(mainViewModel)
        capabilityClient.addListener(
            mainViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(mainViewModel)
        messageClient.removeListener(mainViewModel)
        capabilityClient.removeListener(mainViewModel)
    }

    companion object {
        private const val TAG = "MainActivity"

        const val CAMERA_CAPABILITY = "camera"
        const val WEAR_CAPABILITY = "wear"
        const val MOBILE_CAPABILITY = "mobile"
    }
}
