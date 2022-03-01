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

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService

/**
 * Returns the [Manifest.permission] needed to fetch the user's phone number.
 *
 * On API 26 and above, this is [Manifest.permission.READ_PHONE_NUMBERS], whereas on API 25 and
 * below this is [Manifest.permission.READ_PHONE_STATE].
 */
val phoneSummaryPermission: String
    get() =
        if (Build.VERSION.SDK_INT >= 26) {
            Manifest.permission.READ_PHONE_NUMBERS
        } else {
            Manifest.permission.READ_PHONE_STATE
        }

/**
 * Returns the phone summary.
 *
 * To call this, the [phoneSummaryPermission] must be granted.
 *
 * We are only using a hardware id for demonstration purposes, to try toshow the user their
 * phone number.
 */
@Suppress("MissingPermission", "HardwareIds")
fun Context.getPhoneSummary(): String =
    getSystemService<TelephonyManager>()?.line1Number
        ?: getString(R.string.phone_number_not_available)
