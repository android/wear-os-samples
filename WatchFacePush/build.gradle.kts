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

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.compose.compiler) apply false
}

subprojects {
    if (!path.startsWith(":samples:")) {
        return@subprojects
    }
    project.plugins.withType(AppPlugin::class.java) {
        val androidComponents =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            val variantName = variant.name.replaceFirstChar(Char::titlecase)
            configurations {
                create("${variant.name}WatchfaceOutput") {
                    isCanBeConsumed = true
                    isCanBeResolved = false
                    attributes {
                        attribute(
                            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                            objects.named(
                                LibraryElements::class.java,
                                "${variant.name}WatchfaceOutput"
                            )
                        )
                    }
                }
            }

            afterEvaluate {
                val assembleTask = project.tasks.find {
                    it.name.startsWith("assemble") && it.name.lowercase()
                        .endsWith(variantName.lowercase())
                }
                val tokenTask =
                    tasks.register<TokenGenerationTask_gradle.TokenGenerationTask>("assembleToken$variantName") {
                        dependsOn(assembleTask)
                        packageName.set(variant.namespace)
                        cliToolClasspath.set(rootProject.project("app").configurations.getByName("cliToolConfiguration"))
                        artifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                        apkLocation.set(variant.artifacts.get(SingleArtifact.APK))

                        val tokenDir = variant.artifacts.get(SingleArtifact.APK)
                            .get().asFile.parentFile.parentFile.resolve("token/${variant.name}")
                        tokenDirectory.set(tokenDir)
                    }

                val watchFaceOutput =
                    project.configurations.getByName("${variant.name}WatchfaceOutput")

                tokenTask.get().outputs.files.forEach { file ->
                    watchFaceOutput.outgoing.artifact(file) {
                        builtBy(tokenTask)
                    }
                }
                watchFaceOutput.outgoing.artifact(variant.artifacts.get(SingleArtifact.APK))
            }
        }
    }
}
