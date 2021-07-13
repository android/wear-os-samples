package com.example.android.wearable.alpha.editor

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.android.wearable.alpha.R

// TODO (codingjeremy): Add activity support to edit watch face (follow up PR).
class WatchFaceConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_face_config)
        Log.d(TAG, "onCreate()")
    }

    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}
