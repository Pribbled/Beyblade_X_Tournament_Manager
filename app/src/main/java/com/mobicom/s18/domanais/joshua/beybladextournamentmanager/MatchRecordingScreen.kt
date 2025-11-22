package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

/**
 * Match Recording Screen - CameraX Live Recording with Auto-Submit
 *
 * HOW TO USE IN APPNAVHOST:
 *
 * composable(
 *     route = "match_recording/{tournamentId}/{matchId}",
 *     arguments = listOf(
 *         navArgument("tournamentId") { type = NavType.StringType },
 *         navArgument("matchId") { type = NavType.StringType }
 *     )
 * ) { backStackEntry ->
 *     val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
 *     val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
 *
 *     MatchRecordingScreen(
 *         tournamentId = tournamentId,
 *         matchId = matchId,
 *         onBackClick = { navController.popBackStack() }
 *     )
 * }
 */

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchDetailsViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchRecordingViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchUiState
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.VideoUploadState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

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
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission launcher
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (!hasCameraPermission || !hasAudioPermission) {
            Toast.makeText(context, "Camera and Audio permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    // Request permissions on launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    // Observe match state from ViewModel to get real player names
    val matchState by viewModel.matchState.collectAsState()

    // Observe video upload state
    val uploadState by recordingViewModel.uploadState.collectAsState()

    // Load match details on first composition
    LaunchedEffect(tournamentId, matchId) {
        if (tournamentId.isNotEmpty() && matchId.isNotEmpty()) {
            viewModel.listenToMatch(tournamentId, matchId)
        }
    }

    // Local score state
    var playerAScore by remember { mutableStateOf(0) }
    var playerBScore by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Dialog states
    var showPlayerAEditDialog by remember { mutableStateOf(false) }
    var showPlayerBEditDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Get player names from match data or use defaults
    val playerAName = when (val state = matchState) {
        is MatchUiState.Success -> state.match.player1Name
        else -> "Blader A"
    }
    val playerBName = when (val state = matchState) {
        is MatchUiState.Success -> state.match.player2Name
        else -> "Blader B"
    }

    // CameraX state
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var savedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Handle upload completion
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is VideoUploadState.Success -> {
                isProcessing = false
                showCompleteDialog = true
                recordingViewModel.resetUploadState()
            }
            is VideoUploadState.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    "Upload failed: ${(uploadState as VideoUploadState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                recordingViewModel.resetUploadState()
            }
            else -> { /* Do nothing for Idle and Loading */ }
        }
    }

    // Show edit dialogs
    if (showPlayerAEditDialog) {
        ManualScoreEditDialog(
            currentScore = playerAScore,
            onDismiss = { showPlayerAEditDialog = false },
            onConfirm = { newScore ->
                playerAScore = newScore
                showPlayerAEditDialog = false
            }
        )
    }

    if (showPlayerBEditDialog) {
        ManualScoreEditDialog(
            currentScore = playerBScore,
            onDismiss = { showPlayerBEditDialog = false },
            onConfirm = { newScore ->
                playerBScore = newScore
                showPlayerBEditDialog = false
            }
        )
    }

    // Complete dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Match Complete!") },
            text = { Text("Scores saved and video uploaded successfully.") },
            confirmButton = {
                Button(onClick = {
                    showCompleteDialog = false
                    onBackClick()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Preview (Bottom Layer)
        if (hasCameraPermission && hasAudioPermission && !LocalInspectionMode.current) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onVideoCaptureReady = { capture ->
                    videoCapture = capture
                }
            )
        } else {
            // Fallback black background for preview mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Processing overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color.White
                    )
                    Text(
                        text = "Uploading & Saving...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Left screen - Player A (Score UI Overlay)
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterStart),
            playerName = playerAName,
            playerScore = playerAScore,
            onScoreChange = { playerAScore = it },
            onEditClick = { showPlayerAEditDialog = true },
            enabled = isRecording && !isProcessing
        )

        // Right screen - Player B (Score UI Overlay)
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterEnd),
            playerName = playerBName,
            playerScore = playerBScore,
            onScoreChange = { playerBScore = it },
            onEditClick = { showPlayerBEditDialog = true },
            enabled = isRecording && !isProcessing
        )

        // Bottom control buttons (Overlay)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Record/Stop Button
            IconButton(
                onClick = {
                    if (!isRecording) {
                        // Start recording
                        val videoFile = createVideoFile(context)
                        val outputOptions = FileOutputOptions.Builder(videoFile).build()

                        recording = videoCapture?.output
                            ?.prepareRecording(context, outputOptions)
                            ?.withAudioEnabled()
                            ?.start(ContextCompat.getMainExecutor(context)) { event ->
                                when (event) {
                                    is VideoRecordEvent.Finalize -> {
                                        if (event.hasError()) {
                                            Toast.makeText(
                                                context,
                                                "Recording error: ${event.error}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isProcessing = false
                                        } else {
                                            savedVideoUri = Uri.fromFile(videoFile)

                                            // AUTO-SUBMIT: Save scores and upload video
                                            coroutineScope.launch {
                                                isProcessing = true

                                                // Step 1: Submit match scores to Firestore
                                                val scoreResult = viewModel.submitMatchResult(
                                                    tournamentId = tournamentId,
                                                    matchId = matchId,
                                                    player1Score = playerAScore,
                                                    player2Score = playerBScore
                                                )

                                                scoreResult.onSuccess {
                                                    // Step 2: Upload video to Supabase Storage
                                                    savedVideoUri?.let { uri ->
                                                        recordingViewModel.uploadVideo(
                                                            context = context,
                                                            tournamentId = tournamentId,
                                                            matchId = matchId,
                                                            videoUri = uri
                                                        )
                                                    } ?: run {
                                                        isProcessing = false
                                                        Toast.makeText(
                                                            context,
                                                            "Video file not found",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }.onFailure { error ->
                                                    isProcessing = false
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to save scores: ${error.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        isRecording = true
                        Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
                    } else {
                        // Stop recording (triggers auto-submit in Finalize event)
                        recording?.stop()
                        recording = null
                        isRecording = false
                    }
                },
                modifier = Modifier.size(80.dp),
                enabled = !isProcessing && hasCameraPermission && hasAudioPermission
            ) {
                Icon(
                    imageVector = if (isRecording)
                        ImageVector.vectorResource(R.drawable.baseline_stop_24)
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) Color.Red else Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * CameraX Preview Composable
 */
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

                // Preview use case
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // VideoCapture use case
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                val videoCapture = VideoCapture.withOutput(recorder)

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )

                    // Notify parent that VideoCapture is ready
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

/**
 * Create a temporary video file in cache directory
 */
fun createVideoFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "MATCH_$timeStamp.mp4"
    return File(context.cacheDir, fileName)
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    playerName: String,
    playerScore: Int,
    onScoreChange: (Int) -> Unit,
    onEditClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Score Display (clickable to edit manually)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(enabled = enabled, onClick = onEditClick)
        ) {
            Text(
                text = playerName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$playerScore",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Scoring Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val buttonModifier = Modifier.width(150.dp)
            Button(
                onClick = { onScoreChange(playerScore + 3) },
                modifier = buttonModifier,
                enabled = enabled
            ) {
                Text("Extreme Finish")
            }
            Button(
                onClick = { onScoreChange(playerScore + 2) },
                modifier = buttonModifier,
                enabled = enabled
            ) {
                Text("Burst Finish")
            }
            Button(
                onClick = { onScoreChange(playerScore + 1) },
                modifier = buttonModifier,
                enabled = enabled
            ) {
                Text("Over Finish")
            }
            Button(
                onClick = { onScoreChange(playerScore + 1) },
                modifier = buttonModifier,
                enabled = enabled
            ) {
                Text("Spin Finish")
            }
        }
    }
}

@Composable
fun ManualScoreEditDialog(
    currentScore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manually Edit Score") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { char -> char.isDigit() } },
                label = { Text("New Score") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val newScore = text.toIntOrNull() ?: currentScore
                    onConfirm(newScore)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current

    // This check prevents the screen orientation code from running in Preview mode
    if (!LocalInspectionMode.current) {
        DisposableEffect(Unit) {
            val activity = context as? Activity ?: return@DisposableEffect onDispose {}
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                // Reset the orientation when the composable is disposed
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Composable
fun MatchRecordingScreenPreview() {
    BeybladeXTournamentManagerTheme {
        MatchRecordingScreen()
    }
}

