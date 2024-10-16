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

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

const val ASSEMBLE_DEBUG_TASK = "assembleDebug"
const val BUNDLE_DEBUG_TASK = "bundleDebug"
const val VALIDATE_TASK = "validateWff"
const val DOWNLOAD_VALIDATOR_TASK = "downloadWffValidator"
const val INSTALL_TASK = "validateWffAndInstall"

// TODO move from here
private const val VALIDATOR_URL =
    "https://github.com/google/watchface/releases/download/release/dwf-format-2-validator-1.0.jar"
private const val VALIDATOR_PATH = "validator/validator.jar"

class WffValidatorPlugin : Plugin<Project> {
    private lateinit var manifestPath: String

    override fun apply(project: Project) {
        val downloadTask = project.tasks.register<ValidatorDownloadTask>(DOWNLOAD_VALIDATOR_TASK) {
            val validatorPath = project.layout.buildDirectory.file(VALIDATOR_PATH)
            this.validatorUrl.set(VALIDATOR_URL)
            this.validatorJarPath.set(validatorPath)
        }

        project.tasks.register<ValidateWffFilesTask>(VALIDATE_TASK) {
            val wffFileCollection = getWffFileCollection(project)
            if (wffFileCollection.isEmpty) {
                throw GradleException("No WFF XML files found in project!")
            }
            validatorJarPath.set(downloadTask.get().validatorJarPath)
            wffFiles.setFrom(wffFileCollection)
            wffVersion.set(getWffVersion(manifestPath))
        }

        val androidComponents =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        lateinit var apkDirectoryProvider: Provider<Directory>
        lateinit var loader: BuiltArtifactsLoader

        androidComponents.onVariants(androidComponents.selector().withName("debug")) { variant ->
            manifestPath =
                variant.sources.manifests.all.get().firstOrNull { it.asFile.exists() }?.toString()
                    ?: throw GradleException("No AndroidManifest.xml found!")

            apkDirectoryProvider = variant.artifacts.get(SingleArtifact.APK)
            loader = variant.artifacts.getBuiltArtifactsLoader()
        }

        project.afterEvaluate { proj ->
            // Ensure that validation is run as part of the debug build for APKs and for bundles.
            proj.tasks[ASSEMBLE_DEBUG_TASK].dependsOn(VALIDATE_TASK)
            proj.tasks[BUNDLE_DEBUG_TASK].dependsOn(VALIDATE_TASK)

            // Register additional task that allows for installing and setting of watch face.
            proj.tasks.register<AdbInstallTask>(INSTALL_TASK) {
                apkLocation = apkDirectoryProvider
                artifactLoader = loader
                dependsOn(ASSEMBLE_DEBUG_TASK)
            }
        }
    }

    private fun getWffFileCollection(project: Project): FileCollection {
        return project.layout.files("src/main/res/").asFileTree
            .filter { it.isFile }
            .filter { it.name.endsWith(".xml") }
            .filter { it.parentFile.name.startsWith("raw") }
    }
}


