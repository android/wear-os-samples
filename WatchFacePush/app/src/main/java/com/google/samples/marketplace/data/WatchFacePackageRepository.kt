/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.marketplace.data

import android.content.Context
import android.content.pm.PackageInfo
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Class that loads the watch face packages and validation tokens from the assets folder.
 */
class WatchFacePackageRepository(val context: Context) {
    /**
     * Creates a pipe to transfer the watch face apk to the Watch Face Push service.
     *
     * @param watchFaceData The watch face data object representing the watch face to be
     *     transferred.
     * @return The pipe, containing the read and write ends of the pipe. It is the responsibility of
     *     the caller to close the pipe.
     */
    fun pipeWatchFace(scope: CoroutineScope, watchFaceData: WatchFaceData): FdPipe {
        val (readFd, writeFd) = ParcelFileDescriptor.createPipe()
        scope.launch(Dispatchers.IO) {
            writeFd.use {
                context.assets.open(watchFaceData.assetPath).use { inStream ->
                    FileOutputStream(writeFd.fileDescriptor).use { outStream ->
                        Log.d(TAG, "before transfer")
                        try {
                            val count = inStream.transferTo(outStream)
                            Log.d(TAG, "Transferred $count bytes")
                        } catch (e: IOException) {
                            Log.i(TAG, "Force closed the pipe")
                            // In case Watch Face Push rejects the APK (based on the returned error
                            // code), the pipe must be closed prematurely, to not hang the thread.
                            // However, that causes an IOException here, which needs to be caught.
                            // There is nothing to do about it, since it is the expected behaviour
                            // and a result not having a better API for non-blocking IO.
                        }
                        Log.d(TAG, "after transfer")
                    }
                }
            }
        }
        return FdPipe(readFd, writeFd)
    }

    suspend fun loadPackages(): List<Pair<Path, PackageInfo>> {
        val loadedPackages = mutableListOf<Pair<Path, PackageInfo>>()
        val packages = context.assets
            .list("")!!
            .asSequence()
            .filter { it.endsWith(".apk") }
            .toList()
        packages.forEach {
            val loadedPackage = parseWatchFacePackage(it)
            loadedPackage?.let { loadedPackages.add(it) }
        }
        return loadedPackages
    }

    /**
     * Gets the validation token for the given watch face name.
     *
     * @param name The name of the watch face.
     * @return The validation token, or an empty string if no token is found.
     */
    fun getValidationToken(name: String): String? {
        val tokenName = name.split('.').first() + "_token.txt"
        return try {
            context.assets.open(tokenName).readAllBytes().let { String(it, Charsets.UTF_8) }
        } catch (e: IOException) {
            null
        }
    }

    private suspend fun parseWatchFacePackage(assetPath: String) = withContext(Dispatchers.IO) {
        val apkFile = copyApkFileIntoTemporaryFile(assetPath)
        val packageInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        if (packageInfo == null) {
            Log.w(TAG, "Package $assetPath could not be parsed.")
            null
        } else {
            Paths.get(assetPath) to packageInfo
        }
    }


    private suspend fun copyApkFileIntoTemporaryFile(apkAssetPath: String) =
        withContext(Dispatchers.IO) {
            val copiedFile = File.createTempFile(apkAssetPath, null, context.cacheDir)
            copiedFile.deleteOnExit()
            context.assets.open(apkAssetPath).use { inputStream ->
                FileOutputStream(copiedFile).use { outputStream -> inputStream.copyTo(outputStream) }
            }
            copiedFile
        }

    companion object {
        private const val TAG = "WatchFacePackageRepository"
    }
}

data class FdPipe(
    val readFd: ParcelFileDescriptor,
    private val writeFd: ParcelFileDescriptor,
) : AutoCloseable {
    override fun close() {
        Log.d(TAG, "Closing pipe")
        readFd.close()
        // The close method is idempotent on an already closed file descriptor. If the apk is
        // consumed by Watch Face Push, closing writeFd is redundant here, but it causes no problem.
        // On the other hand, if the Watch Face Push rejects the APK, the pipe must be closed
        // explicitly.
        writeFd.close()
    }

    companion object {
        private const val TAG = "FdPipe"
    }
}
