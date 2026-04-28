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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
}

@Suppress("DEPRECATION")
android {
    compileSdk = 37

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



}



dependencies {
    implementation(libs.androidx.core.ktx)
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
    implementation(libs.androidx.tiles.tooling)

    debugImplementation(libs.androidx.wear.tiles.renderer)

    debugImplementation(libs.androidx.wear.tooling.preview)
    debugImplementation(libs.androidx.wear.tiles.tooling)
    // The tile preview code is in the same file as the tiles themselves, so we need to make the
    // androidx.wear.tiles:tiles-tooling-preview dependency available to release builds, not
    // just debug builds.
    implementation(libs.androidx.wear.tiles.tooling.preview)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
