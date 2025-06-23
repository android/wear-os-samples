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


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.android.build.api.variant.BuiltArtifactsLoader
import com.google.android.wearable.watchface.validator.client.DwfValidatorFactory
import kotlin.collections.single
import kotlin.io.path.name
import kotlin.io.resolve
import kotlin.io.writeText
import kotlin.text.removeSuffix

/**
 * Task to generate a validation token for a given APK and write it to the specified location.
 */
abstract class TokenGenerationTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkLocation: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Internal
    abstract val artifactsLoader: Property<BuiltArtifactsLoader>

    @get:OutputDirectory
    abstract val tokenDirectory: RegularFileProperty

    @TaskAction
    fun performAction() {
        val artifacts =
            artifactsLoader.get().load(apkLocation.get())
                ?: throw GradleException("Cannot load APKs")
        if (artifacts.elements.size != 1)
            throw GradleException("Expected only one APK!")
        val apk = File(artifacts.elements.single().outputFile)
        val appPackageName = packageName.get()

        val validator = DwfValidatorFactory.create()
        val result = validator.validate(apk, appPackageName)

        val failures = result.failures()
        if (failures.isNotEmpty()) {
            val validationException = GradleException("Watch face validation failed with ${failures.size} failures")
            failures.forEach { failure ->
                validationException.addSuppressed(SecurityException(failure.toString()))
            }
            throw validationException
        }

        val tokenFileName = apk.toPath().name.removeSuffix(".apk") + "_token.txt"
        val tokenOutFile = tokenDirectory.get().asFile.resolve(tokenFileName)
        if (result.validationToken().isNotEmpty()) {
            tokenOutFile.writeText(result.validationToken())
        } else {
            throw TaskExecutionException(
                this@TokenGenerationTask,
                GradleException("No token generated for $tokenFileName")
            )
        }
    }
}
