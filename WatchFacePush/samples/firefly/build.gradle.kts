plugins {
    id("com.android.application")
}

android {
    namespace = "com.google.samples.marketplace.watchfacepush.firefly"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.samples.marketplace.watchfacepush.firefly"
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
}

