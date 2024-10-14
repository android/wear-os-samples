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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import java.nio.file.Files
import java.nio.file.Path

private const val VALIDATOR_URL =
    "https://github.com/google/watchface/releases/download/release/dwf-format-2-validator-1.0.jar"
private const val VALIDATOR_FILE_NAME = "validator.jar"

/**
 * Downloads the WFF validator for use in the build process.
 */
internal fun downloadFileToPath(filePath: Path, url: String) {
    val client = HttpClient()
    val file = filePath.toFile()

    runBlocking {
        client.prepareGet(url).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    file.appendBytes(bytes)
                }
            }
        }
    }
}

internal fun ensureValidatorDownloaded(project: Project) {
    prepareValidatorDirectory(project)
    val validatorJarPath = getValidatorPath(project)

    // Avoid downloading multiple times
    if (!validatorJarPath.toFile().exists()) {
        downloadFileToPath(validatorJarPath, VALIDATOR_URL)
    }
}

internal fun prepareValidatorDirectory(project: Project): Path {
    val path = Path.of(project.projectDir.absolutePath, "build", "validator")
    Files.createDirectories(path)
    return path
}

internal fun getValidatorPath(project: Project) =
    Path.of(project.projectDir.absolutePath, "build", "validator", VALIDATOR_FILE_NAME)
