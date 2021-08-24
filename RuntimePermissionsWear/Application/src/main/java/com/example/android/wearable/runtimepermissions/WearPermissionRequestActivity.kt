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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.example.android.wearable.runtimepermissions.databinding.ActivityWearPermissionRequestBinding

/**
 * This is a simple splash screen (activity) for giving more details on why the user should approve
 * wear permissions for sensors. This will exit to the [MainPhoneActivity] with the user's choice.
 */
class WearPermissionRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityWearPermissionRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.approvePermissionRequest.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.denyPermissionRequest.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    companion object {

        /**
         * An [ActivityResultContract] for checking that the user wants to request permission on
         * their watch.
         */
        object RequestPermissionOnWear : ActivityResultContract<Unit, Boolean>() {
            override fun createIntent(context: Context, input: Unit): Intent =
                Intent(context, WearPermissionRequestActivity::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
                resultCode == Activity.RESULT_OK
        }
    }
}
