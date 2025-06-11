/*
 * Copyright 2021 The Android Open Source Project
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

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 35

    namespace = "com.example.wear.tiles"

    defaultConfig {
        applicationId = "com.example.wear.tiles"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.annotations.ExperimentalHorologistApi"
    }

    buildFeatures {
        compose = true
    }

}

dependencies {
    // Horologist provides helpful wrappers for Tiles development
    implementation(libs.horologist.tiles)

    // Coil for asynchronous image loading
    implementation(libs.coil)
    implementation(libs.coil.okhttp)

    // Java 8+ API desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Core Tile dependencies for creating the service and layouts
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.protolayout.material)
    implementation(libs.androidx.wear.protolayout.material3)

    // Tooling dependencies for previewing tiles in Android Studio.
    // Scoped as 'implementation' to allow previews in the 'main' source set.
    implementation(libs.androidx.wear.tiles.renderer)
    implementation(libs.androidx.wear.tiles.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.wear.tooling.preview)
}
