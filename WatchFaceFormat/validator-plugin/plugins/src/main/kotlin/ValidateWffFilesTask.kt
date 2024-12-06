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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import javax.inject.Inject

/**
 * Runs the validator against WFF XML files.
 */
@CacheableTask
abstract class ValidateWffFilesTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFiles
    @get:Incremental
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wffFiles: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val validatorJarPath: RegularFileProperty

    @get:OutputFile
    abstract val validatorOutputFile: RegularFileProperty

    @get:Input
    abstract val wffVersion: Property<Int>

    @TaskAction
    fun validate(inputs: InputChanges) {
        val changedFiles = inputs.getFileChanges(wffFiles)
        changedFiles.forEach { change ->
            if (change.changeType != ChangeType.REMOVED) {
                val result = execOperations.javaexec {
                    it.classpath = project.files(validatorJarPath)
                    // Stop-on-fail ensures that the Gradle Task throws an exception when a WFF file fails
                    // to validate.
                    it.args(wffVersion.get().toString(), "--stop-on-fail", change.file.absolutePath)
                }
                validatorOutputFile.get().asFile.writeText(result.toString())
            }
        }
    }
}

