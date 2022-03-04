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
@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.android.wearable.oauth.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Simple implementation of a POST request. Normally you'd use a library to do these requests.
 */
suspend fun doPostRequest(
    url: String,
    params: Map<String, String>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JSONObject {
    return withContext(dispatcher) {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        val postData = StringBuilder()
        for ((key, value) in params) {
            if (postData.isNotEmpty()) postData.append('&')
            postData.append(URLEncoder.encode(key, "UTF-8"))
            postData.append('=')
            postData.append(URLEncoder.encode(value, "UTF-8"))
        }
        val postDataBytes = postData.toString().toByteArray(charset("UTF-8"))

        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Content-Length", postDataBytes.size.toString())
            doOutput = true
            outputStream.write(postDataBytes)
        }

        val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val response = inputReader.readText()

        Log.d("PostRequestUtil", "Response: $response")

        JSONObject(response)
    }
}

/**
 * Simple implementation of a GET request. Normally you'd use a library to do these requests.
 */
suspend fun doGetRequest(
    url: String,
    requestHeaders: Map<String, String>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JSONObject {
    return withContext(dispatcher) {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        requestHeaders.forEach { (key, value) ->
            conn.setRequestProperty(key, value)
        }
        val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val response = inputReader.readText()

        Log.d("RequestUtil", "Response: $response")

        JSONObject(response)
    }
}
