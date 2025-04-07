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
