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
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

/**
 * To keep the sample simple, we are only displaying the number of sensors. You could do
 * something much more complicated.
 */
@RequiresPermission(Manifest.permission.BODY_SENSORS)
fun Context.sensorSummary(): String {
    val sensorManager = getSystemService<SensorManager>()!!
    val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
    val numberOfSensorsOnDevice = sensorList.size
    return getString(R.string.sensor_summary, numberOfSensorsOnDevice)
}
