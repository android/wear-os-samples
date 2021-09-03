package com.example.android.wearable.speaker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Duration
import kotlin.coroutines.resume

/**
 * The logic for the speaker sample.
 *
 * The stateful logic is kept by a [MainStateHolder].
 */
@Composable
fun SpeakerApp() {
    MaterialTheme {
        lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

        val context = LocalContext.current
        val activity = context.findActivity()

        val soundRecorder = remember(context) { SoundRecorder(context, "audiorecord.pcm") }

        val stateHolder = remember(activity) {
            MainStateHolder(
                activity = activity,
                requestPermission = {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                showPermissionRationale = {
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.rationale_for_microphone_permission)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .create()
                        .show()
                },
                showSpeakerNotSupported = {
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.no_speaker_supported)
                        .setPositiveButton(R.string.ok) { _, _ -> }
                        .create()
                        .show()
                }
            )
        }

        requestPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) {
            // We ignore the direct result here, since we're going to check anyway.
            stateHolder.onAction(MainStateHolder.AppAction.PermissionResultReturned)
        }

        val lifecycle = LocalLifecycleOwner.current

        // Notify the state holder whenever we become started to reset the state
        LaunchedEffect(stateHolder) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateHolder.onAction(MainStateHolder.AppAction.Started)
            }
        }

        val appState by stateHolder.appState.collectAsState()
        val isPermissionDenied by stateHolder.isPermissionDenied.collectAsState()

        var recordingProgress by remember(appState) { mutableStateOf(0f) }

        // The primary actor within the presence of state.
        // Based on the app state, this will play music, record, and play back the recording while the app is
        // visible
        LaunchedEffect(appState) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (appState) {
                    is MainStateHolder.AppState.Ready -> Unit
                    MainStateHolder.AppState.PlayingVoice -> {
                        soundRecorder.play()
                        stateHolder.onAction(MainStateHolder.AppAction.PlayRecordingFinished)
                    }
                    MainStateHolder.AppState.PlayingMusic -> {
                        playMusic(activity)
                        stateHolder.onAction(MainStateHolder.AppAction.PlayMusicFinished)
                    }
                    MainStateHolder.AppState.Recording -> {
                        // This condition is guaranteed via the logic in the state holder
                        check(
                            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED
                        )

                        coroutineScope {
                            // Kick off a parallel job to record
                            val recordingJob = launch { soundRecorder.record() }

                            val delayPerTickMs = MAX_RECORDING_DURATION.toMillis() / NUMBER_TICKS
                            val startTime = System.currentTimeMillis()

                            repeat(NUMBER_TICKS) { index ->
                                recordingProgress = index.toFloat() / NUMBER_TICKS
                                delay(startTime + delayPerTickMs * (index + 1) - System.currentTimeMillis())
                            }
                            // Update the progress to be complete
                            recordingProgress = 1f

                            // Stop recording
                            recordingJob.cancel()
                        }

                        stateHolder.onAction(MainStateHolder.AppAction.RecordingFinished)
                    }
                }
            }
        }

        SpeakerScreen(
            appState = appState,
            isPermissionDenied = isPermissionDenied,
            recordingProgress = recordingProgress,
            onMicClicked = { stateHolder.onAction(MainStateHolder.AppAction.MicClicked) },
            onPlayClicked = { stateHolder.onAction(MainStateHolder.AppAction.PlayClicked) },
            onMusicClicked = { stateHolder.onAction(MainStateHolder.AppAction.MusicClicked) }
        )
    }
}

/**
 * Find the closest Activity in a given Context.
 */
tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("Permissions should be called in the context of an Activity")
    }

/**
 * Plays the MP3 file embedded in the application.
 */
private suspend fun playMusic(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.sound)

    try {
        suspendCancellableCoroutine<Unit> { cont ->
            mediaPlayer.setOnCompletionListener {
                cont.resume(Unit)
            }
            mediaPlayer.start()
        }
    } finally {
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}

private val MAX_RECORDING_DURATION = Duration.ofSeconds(10)
private const val NUMBER_TICKS = 10

