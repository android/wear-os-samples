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
    alias(libs.plugins.compose.compiler)
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

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.horologist.tiles)

    implementation(libs.coil3.coil)
    implementation(libs.coil3.network.okhttp)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.renderer)
    implementation(libs.androidx.protolayout.material3)
    implementation(libs.androidx.protolayout.material)

    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.androidx.tiles.tooling)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.wear.tooling.preview)

    debugImplementation(libs.androidx.tiles.tooling.preview)
    debugImplementation(libs.androidx.tiles.tooling)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.wear.tooling.preview)
}
