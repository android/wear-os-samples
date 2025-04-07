package com.google.samples.marketplace

import android.app.Application
import androidx.wear.watchface.push.WatchFacePushManager
import com.google.samples.marketplace.data.WatchFacePackageRepository

class MarketplaceApplication : Application() {
    val watchFacePushManager by lazy { WatchFacePushManager(this) }
    val watchFacePackageRepository by lazy { WatchFacePackageRepository(this) }
}
