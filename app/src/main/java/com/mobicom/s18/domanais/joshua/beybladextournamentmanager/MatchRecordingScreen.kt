package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchDetailsViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchRecordingViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchUiState
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.VideoUploadState
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@Composable
fun MatchRecordingScreen(
    tournamentId: String = "",
    matchId: String = "",
    onBackClick: () -> Unit = {},
    viewModel: MatchDetailsViewModel = viewModel(),
    recordingViewModel: MatchRecordingViewModel = viewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(tournamentId, matchId) {
        viewModel.listenToMatch(tournamentId, matchId)
    }

    val matchState by viewModel.matchState.collectAsState()
    val uploadState by recordingViewModel.uploadState.collectAsState()
    val tournamentState by viewModel.tournamentState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: false
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    var playerAScore by remember { mutableStateOf(0) }
    var playerBScore by remember { mutableStateOf(0) }

    LaunchedEffect(matchState) {
        if (matchState is MatchUiState.Success) {
            val match = (matchState as MatchUiState.Success).match
            playerAScore = match.player1Score
            playerBScore = match.player2Score
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }

    val player1Name = (matchState as? MatchUiState.Success)?.match?.player1Name ?: "Loading..."
    val player2Name = (matchState as? MatchUiState.Success)?.match?.player2Name ?: "Loading..."

    LaunchedEffect(uploadState) {
        if (uploadState is VideoUploadState.Success) {
            isProcessing = false
            showCompleteDialog = true
            recordingViewModel.resetUploadState()
        } else if (uploadState is VideoUploadState.Error) {
            isProcessing = false
            Toast.makeText(context, (uploadState as VideoUploadState.Error).message, Toast.LENGTH_LONG).show()
            recordingViewModel.resetUploadState()
        }
    }

    // Complete Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Match Saved!") },
            text = { Text("Scores updated and video uploaded.") },
            confirmButton = {
                Button(onClick = {
                    showCompleteDialog = false
                    onBackClick()
                }) { Text("OK") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        if (hasCameraPermission && hasAudioPermission && !LocalInspectionMode.current) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onVideoCaptureReady = { videoCapture = it }
            )
        }

        // Loading Overlay (Uploading)
        if (isProcessing || uploadState is VideoUploadState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Uploading Video...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Player A Controls ---
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterStart),
            playerName = player1Name,
            playerScore = playerAScore,
            scoringValues = tournamentState,
            onScoreUpdate = { points -> playerAScore += points },
            enabled = isRecording && !isProcessing
        )

        // --- Player B Controls ---
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterEnd),
            playerName = player2Name,
            playerScore = playerBScore,
            scoringValues = tournamentState,
            onScoreUpdate = { points -> playerBScore += points },
            enabled = isRecording && !isProcessing
        )

        // --- Record Button ---
        IconButton(
            onClick = {
                if (!isRecording) {
                    // Start Recording
                    val videoFile = createVideoFile(context)
                    val outputOptions = FileOutputOptions.Builder(videoFile).build()

                    recording = videoCapture?.output
                        ?.prepareRecording(context, outputOptions)
                        ?.withAudioEnabled()
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            if (event is VideoRecordEvent.Finalize) {
                                if (!event.hasError()) {
                                    scope.launch {
                                        isProcessing = true
                                        viewModel.submitMatchResult(tournamentId, matchId, playerAScore, playerBScore)
                                        recordingViewModel.uploadVideo(context, tournamentId, matchId, Uri.fromFile(videoFile))
                                    }
                                } else {
                                    isProcessing = false
                                    Toast.makeText(context, "Recording Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    isRecording = true
                } else {
                    // Stop Recording
                    recording?.stop()
                    recording = null
                    isRecording = false
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).size(80.dp),
            enabled = !isProcessing && hasCameraPermission
        ) {
            Icon(
                imageVector = if (isRecording) ImageVector.vectorResource(R.drawable.baseline_stop_24) else Icons.Default.PlayArrow,
                contentDescription = "Record",
                tint = if (isRecording) Color.Red else Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    playerName: String,
    playerScore: Int,
    scoringValues: Tournament?,
    onScoreUpdate: (Int) -> Unit,
    enabled: Boolean
) {
    val extremePoints = scoringValues?.scoringValueExtreme ?: 3
    val burstPoints = scoringValues?.scoringValueBurst ?: 2
    val overPoints = scoringValues?.scoringValueOver ?: 1
    val spinPoints = scoringValues?.scoringValueSpin ?: 1

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = playerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = "$playerScore", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)

        val btnMod = Modifier.width(150.dp)
        Button(onClick = { onScoreUpdate(extremePoints) }, modifier = btnMod, enabled = enabled) { Text("Extreme ($extremePoints)") }
        Button(onClick = { onScoreUpdate(burstPoints) }, modifier = btnMod, enabled = enabled) { Text("Burst ($burstPoints)") }
        Button(onClick = { onScoreUpdate(overPoints) }, modifier = btnMod, enabled = enabled) { Text("Over ($overPoints)") }
        Button(onClick = { onScoreUpdate(spinPoints) }, modifier = btnMod, enabled = enabled) { Text("Spin ($spinPoints)") }
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    if (!LocalInspectionMode.current) {
        DisposableEffect(Unit) {
            val activity = context as? Activity ?: return@DisposableEffect onDispose {}
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onVideoCaptureReady: (VideoCapture<Recorder>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build()
                val videoCapture = VideoCapture.withOutput(recorder)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
                    onVideoCaptureReady(videoCapture)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

fun createVideoFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "MATCH_$timeStamp.mp4"
    return File(context.cacheDir, fileName)
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Composable
fun MatchRecordingScreenPreview() {
    BeybladeXTournamentManagerTheme {
        MatchRecordingScreen()
    }
}