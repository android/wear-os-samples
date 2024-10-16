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
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

/**
 * Downloads the WFF validator for use in the build process.
 */
@CacheableTask
abstract class ValidatorDownloadTask : DefaultTask() {
    @get:OutputFile
    abstract val validatorJarPath: RegularFileProperty

    @get:Input
    abstract val validatorUrl: Property<String>

    @TaskAction
    fun install() {
        downloadFileToPath(validatorJarPath.get().asFile.toPath(), validatorUrl.get())
    }

    private fun downloadFileToPath(filePath: Path, url: String) {
        val client = HttpClient { expectSuccess = true }
        val file = filePath.toFile()

        // The validator generally won't exist already -- but there is the potential for the input
        // URL to change, in which case the existing validator should be removed.
        if (file.exists()) {
            file.delete()
        }

        runBlocking {
            client.get(url).bodyAsChannel().copyAndClose(filePath.toFile().writeChannel())
        }
    }
}
