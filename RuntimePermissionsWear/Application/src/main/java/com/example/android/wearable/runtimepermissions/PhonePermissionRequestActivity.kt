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
package com.example.android.wearable.runtimepermissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.databinding.ActivityPhonePermissionRequestBinding

/**
 * This is a simple splash screen (activity) for giving more details on why the user should approve
 * phone permissions for phone information. If they choose to move forward, the permission screen
 * is brought up. Either way (approve or disapprove), this will exit to the MainPhoneActivity after
 * they are finished with their final decision.
 */
class PhonePermissionRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhonePermissionRequestBinding

    /**
     * The [ActivityResultLauncher] for requesting permissions locally.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Close activity regardless of user's decision (decision picked up in main activity).
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhonePermissionRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.approvePermissionRequest.setOnClickListener {
            requestPermissionLauncher.launch(phoneSummaryPermission)
        }

        binding.denyPermissionRequest.setOnClickListener {
            finish()
        }

        // If permissions granted, we start the main activity (shut this activity down).
        if (ActivityCompat.checkSelfPermission(this, phoneSummaryPermission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            finish()
        }
    }

    companion object {
        private const val TAG = "PhoneRationale"

        /**
         * An [ActivityResultContract] for checking that the user wants to allow for phone information.
         */
        object RequestPermission : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit): Intent =
                Intent(context, PhonePermissionRequestActivity::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?): Unit = Unit
        }
    }
}
