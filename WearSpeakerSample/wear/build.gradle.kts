/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.roborazzi)
}

android {
    compileSdk = 35

    namespace = "com.example.android.wearable.speaker"

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        minSdk = 26
        targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            isCoreLibraryDesugaringEnabled = true
        }

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.majorVersion
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }

        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion =
                libs.versions.compose.compiler
                    .get()
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    // General compose dependencies
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)

    // Compose for Wear OS Dependencies
    // NOTE: DO NOT INCLUDE a dependency on androidx.compose.material:material.
    // androidx.wear.compose:compose-material is designed as a replacement not an addition to
    // androidx.compose.material:material. If there are features from that you feel are missing from
    // androidx.wear.compose:compose-material please raise a bug to let us know:
    // https://issuetracker.google.com/issues/new?component=1077552&template=1598429&pli=1
    implementation(libs.wear.compose.material)

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation(libs.wear.compose.foundation)
    implementation(libs.compose.material.icons.extended)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.material.icons.core)

    // Horologist for correct Compose layout
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    // Horologist Media toolkit
    implementation(libs.horologist.media.ui)
    implementation(libs.horologist.media.ui.model)
    implementation(libs.horologist.audio.ui)
    implementation(libs.horologist.audio.ui.model)
    implementation(libs.horologist.media.data)
    implementation(libs.horologist.images.coil)

    implementation(libs.androidx.media3.exoplayerworkmanager)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)

    // Preview Tooling
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.media)

    // If you are using Compose Navigation, use the Wear OS version (NOT the
    // androidx.navigation:navigation-compose version), that is, uncomment the line below.
    implementation(libs.wear.compose.navigation)

    implementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.horologist.roboscreenshots)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(composeBom)
}
