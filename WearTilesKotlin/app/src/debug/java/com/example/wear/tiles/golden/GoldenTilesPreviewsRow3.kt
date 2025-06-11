/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.wear.tiles.golden

import android.content.Context
import com.example.wear.tiles.tools.MultiRoundDevicesPreviews

/** b/238560022 misaligned because we can't add an offset, small preview is clipped */
@MultiRoundDevicesPreviews private fun weather(context: Context) = weatherPreview(context)

/** b/238556504 alignment doesn't match figma. */
@MultiRoundDevicesPreviews private fun news(context: Context) = newsPreview(context)

/** b/238571095 Alignment doesn't match Figma */
@MultiRoundDevicesPreviews private fun calendar(context: Context) = calendarPreview(context)

@MultiRoundDevicesPreviews private fun social(context: Context) = socialPreview(context)

@MultiRoundDevicesPreviews private fun media(context: Context) = mediaPreview(context)
