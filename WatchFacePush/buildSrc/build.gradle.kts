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
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    google()
    // Note: Jitpack is required for dependencies used by the Watch Face Push Validator.
    maven { url = uri("https://jitpack.io") }
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle-api:8.10.1")
    implementation("com.google.android.wearable.watchface.validator:validator-push:1.0.0-alpha03")
}
