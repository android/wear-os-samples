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

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

// Watch Face Push requires API level 36 and above.
android {
    namespace = "com.google.samples.marketplace"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.samples.marketplace"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("release") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/release"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/release/res/"))
        }
        getByName("debug") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/debug"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/debug/res/"))
        }
    }
}


val mainAppNamespace = Attribute.of("wfp.app.namespace", String::class.java)
// Define configurations that allows this app to include the sample watch faces in their assets
configurations {
    create("debugWatchfaceOutput") {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                objects.named(LibraryElements::class.java, "debugWatchfaceOutput")
            )
        }
    }
    create("releaseWatchfaceOutput") {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                objects.named(LibraryElements::class.java, "releaseWatchfaceOutput")
            )
        }
    }
    create("validatorConfiguration") {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(mainAppNamespace, android.namespace!!)
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.wear.compose.ui.tooling)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.compose.navigation)
    implementation(libs.horologist.compose.layout)

    implementation(libs.watchface.push)

    "debugWatchfaceOutput"(project(":samples:defaultwf"))
    "debugWatchfaceOutput"(project(":samples:river"))
    "debugWatchfaceOutput"(project(":samples:firefly"))

    "releaseWatchfaceOutput"(project(":samples:defaultwf"))
    "releaseWatchfaceOutput"(project(":samples:river"))
    "releaseWatchfaceOutput"(project(":samples:firefly"))

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

// The watch face samples and their associated tokens are put in the
// watchfaceOutput configuration, and a dependency is added to these artifacts
// from the app module. This tasks copies the artifacts from the configuration
// into the build directory, so that we can include them as assets into the
// marketplace app. For more details, see the gradle configuration docs:
// https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.Configuration.html

// After the watch face apks and copied into the intermediates build directory,
// this tasks generate a string resource file, containing the validation token
// of the default watch face. This token is read from the marketplace manifest
// upon installation, so that the default watch face can be installed.

afterEvaluate {
    android.applicationVariants.all {
        val capsVariantName = name.replaceFirstChar(Char::titlecase)
        val variantName = name
        val copyTask = tasks.register("copyWatchFaceAssets$capsVariantName", Copy::class.java) {
            from(configurations.getByName("${variantName}WatchfaceOutput").incoming.artifactView {}.files)
            into(layout.buildDirectory.dir("intermediates/watchfaceAssets/$variantName"))
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        val tokenResTask =
            tasks.register<TokenResourceTask_gradle.TokenResourceTask>("tokenResTask$capsVariantName") {
                dependsOn(copyTask)
                buildVariant.set(variantName)
                outputDirectory.set(layout.buildDirectory.dir("generated/wfTokenRes/$variantName").get().asFile)
            }
        // make sure to run the two defined tasks before doing anything else.
        // tokenResTask will cause copyWatchFaceAssets to
        // run as well
        tasks.named("pre${capsVariantName}Build").configure {
            dependsOn(tokenResTask)
        }
    }
}
