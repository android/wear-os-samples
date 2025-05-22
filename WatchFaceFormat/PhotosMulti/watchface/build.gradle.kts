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
plugins {
    id("com.android.application")
    // Use the locally-defined validator to demonstrate validation on-build.
    id("com.google.wff.validatorplugin")
}

android {
    namespace = "com.example.photosmulti"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.photosmulti"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
        }
        release {
            // TODO:Add your signingConfig here to build release builds
            isMinifyEnabled = true
            // Ensure shrink resources is false, to avoid potential for them
            // being removed.
            isShrinkResources = false

            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
