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

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.nio.file.Path


/**
 * Runs the validator against WFF XML files found in the project structure, matching any files:
 *
 *    res\raw*\*.xml
 */
internal fun validateWffFiles(project: Project, version: Int) {
    val resourcePath = Path.of(project.projectDir.absolutePath, "src", "main", "res")
    val rawDirs = resourcePath.toFile().listFiles()
        ?.filter { it.isDirectory && it.name.startsWith("raw") }
    val wffFiles = rawDirs?.flatMap { it.listFiles()?.toList() ?: listOf() }
        ?.filter { it.name.endsWith(".xml") }
        ?: throw GradleException("No Watch Face Format XML files found")

    val validatorJarPath = getValidatorPath(project)

    wffFiles.forEach { wffFile ->
        project.javaexec {
            it.classpath = project.files(validatorJarPath)
            // Stop-on-fail ensures that the Gradle Task throws an exception when a WFF file fails
            // to validate.
            it.args(version, "--stop-on-fail", wffFile.absolutePath)
        }
    }
}
