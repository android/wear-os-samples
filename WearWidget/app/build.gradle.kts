plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
    // alias(libs.plugins.dependency.analysis)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.google.example.wear_widget"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.google.example.wear_widget"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                if (System.getProperty("robo.screenshot.target") != null) {
                    test.systemProperty("robo.screenshot.target", System.getProperty("robo.screenshot.target"))
                }
            }
        }
    }
}

dependencies {
    // WORKAROUND: Pin core remote compose libraries to alpha08.
    // The latest available version of remote-material3 (1.0.0-alpha02) is currently
    // incompatible with core library versions >= 1.0.0-alpha09 due to an ABI break
    // (relocated classes and changed method signatures in the clickable modifier).
    // This block should be removed once remote-material3 is updated to support alpha09+.
    constraints {
        val coreVersion = "1.0.0-alpha08"
        implementation("androidx.compose.remote:remote-creation-compose") {
            version { strictly(coreVersion) }
        }
        implementation("androidx.compose.remote:remote-creation") {
            version { strictly(coreVersion) }
        }
        implementation("androidx.compose.remote:remote-creation-core") {
            version { strictly(coreVersion) }
        }
        implementation("androidx.compose.remote:remote-core") {
            version { strictly(coreVersion) }
        }
        implementation("androidx.compose.remote:remote-tooling-preview") {
            version { strictly(coreVersion) }
        }
    }
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    
    // Transitive dependencies declared directly
    implementation(libs.runtime)

    debugImplementation(libs.ui.tooling)

    // Java 8+ API desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Remote Compose Dependencies
    implementation(libs.remote.creation.compose)
    implementation(libs.remote.material3)
    implementation(libs.remote.core)

    implementation(libs.wear.core)
    implementation(libs.wear)

    // Tooling dependencies for previewing tiles in Android Studio.
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.ui.tooling.preview)
    implementation(libs.wear.compose.ui.tooling)
    implementation(libs.wear.tooling.preview)
    implementation(libs.remote.tooling.preview)

    implementation(libs.remote.creation.core)
    implementation(libs.remote.creation)

    // Tooling dependencies for previewing tiles in Android Studio.
    implementation(libs.wear.tiles.tooling)
    debugImplementation(libs.wear.tiles.renderer)
    // ui-tooling is already added above via libs.ui.tooling (debugImplementation) and libs.androidx.ui.tooling
    // debugImplementation("androidx.compose.ui:ui-tooling:1.9.4") 
    // wear-tooling-preview is already added above via libs.wear.tooling.preview
    // debugImplementation("androidx.wear:wear-tooling-preview:1.0.0")
    // Tiles tooling repeated in original file, removing duplicate.
    
    // The tile preview code is in the same file as the tiles themselves, so we need to make the
    // androidx.wear.tiles:tiles-tooling-preview dependency available to release builds, not
    // just debug builds.
    implementation(libs.wear.tiles.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.foundation)
    implementation(libs.datastore.preferences)

    testImplementation(enforcedPlatform(libs.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.remote.creation.compose.ExperimentalRemoteCreationComposeApi")
    }
}
