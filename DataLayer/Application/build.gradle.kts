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
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 35

    namespace = "com.example.android.wearable.datalayer"

    defaultConfig {
        // NOTE: This must be the same in the phone app and the wear app for the capabilities API
        applicationId = "com.example.android.wearable.datalayer"
        versionCode = 1
        versionName = "1.0"
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.compose.compiler
                .get()
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(composeBom)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.playservices.wearable)
    implementation(libs.androidx.fragment.ktx)
}
