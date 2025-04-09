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

import com.android.build.api.variant.impl.VariantOutputImpl

plugins {
    id("com.android.application")
}

android {
    namespace = "com.google.samples.marketplace.watchfacepush.defaultwf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.samples.marketplace.watchfacepush.defaultwf"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // just for convenience, re-use the debug certificate for the
            // release watch face. It is irrelevant anyway because, just for
            // the purpose of this sample, we're putting the debug watch face
            // into the assets, not the release one
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = false
        }
        debug {
            // this is required so that dex files are stripped from the watch
            // face, which is a necessary step for the DWF to be valid.
            isMinifyEnabled = true
        }
    }
    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                if (output is VariantOutputImpl && output.outputFileName.get().endsWith(".apk")) {
                    output.outputFileName = "default_watchface.apk"
                }
            }
        }
    }
}

