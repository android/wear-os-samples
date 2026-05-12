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
