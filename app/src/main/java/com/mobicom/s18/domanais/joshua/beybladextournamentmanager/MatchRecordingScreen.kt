package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

/**
 * Match Recording Screen - Integrated with Firestore
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

import android.app.Activity
import android.content.pm.ActivityInfo
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchDetailsViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchUiState
import kotlinx.coroutines.launch

@Composable
fun MatchRecordingScreen(
    tournamentId: String = "",
    matchId: String = "",
    onBackClick: () -> Unit = {},
    viewModel: MatchDetailsViewModel = viewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    // Observe match state from ViewModel to get real player names
    val matchState by viewModel.matchState.collectAsState()

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
    var recordingFinished by remember { mutableStateOf(false) }

    // Dialog states
    var showPlayerAEditDialog by remember { mutableStateOf(false) }
    var showPlayerBEditDialog by remember { mutableStateOf(false) }

    // Submission state
    var isSubmitting by remember { mutableStateOf(false) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Left screen - Player A
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterStart),
            playerName = playerAName,
            playerScore = playerAScore,
            onScoreChange = { playerAScore = it },
            onEditClick = { showPlayerAEditDialog = true }
        )

        // Right screen - Player B
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterEnd),
            playerName = playerBName,
            playerScore = playerBScore,
            onScoreChange = { playerBScore = it },
            onEditClick = { showPlayerBEditDialog = true }
        )

        // Bottom control buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Submit Score Button (Checkmark)
            IconButton(
                onClick = {
                    if (tournamentId.isNotEmpty() && matchId.isNotEmpty()) {
                        coroutineScope.launch {
                            isSubmitting = true

                            // Submit the final scores to Firestore
                            val result = viewModel.submitMatchResult(
                                tournamentId = tournamentId,
                                matchId = matchId,
                                player1Score = playerAScore,
                                player2Score = playerBScore
                            )

                            result.onSuccess {
                                // Navigate back to Match Details on success
                                onBackClick()
                            }.onFailure {
                                // TODO: Show error message if needed
                                isSubmitting = false
                            }
                        }
                    } else {
                        // If no IDs provided (preview mode), just go back
                        onBackClick()
                    }
                },
                modifier = Modifier.size(56.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit Score",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Record/Stop Button
            IconButton(
                onClick = {
                    isRecording = !isRecording
                    if (!isRecording) recordingFinished = true
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = if (isRecording)
                        ImageVector.vectorResource(R.drawable.baseline_stop_24)
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = "Record",
                    tint = if (isRecording) Color.Red else Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Slow Motion Replay Button (appears after recording)
            AnimatedVisibility(visible = recordingFinished) {
                IconButton(
                    onClick = { /* TODO: Play replay in slo-mo */ },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.outline_slow_motion_video_24),
                        contentDescription = "Slow Motion Replay",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    playerName: String,
    playerScore: Int,
    onScoreChange: (Int) -> Unit,
    onEditClick: () -> Unit
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
                .clickable(onClick = onEditClick)
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
            Button(onClick = { onScoreChange(playerScore + 3) }, modifier = buttonModifier) {
                Text("Extreme Finish")
            }
            Button(onClick = { onScoreChange(playerScore + 2) }, modifier = buttonModifier) {
                Text("Burst Finish")
            }
            Button(onClick = { onScoreChange(playerScore + 1) }, modifier = buttonModifier) {
                Text("Over Finish")
            }
            Button(onClick = { onScoreChange(playerScore + 1) }, modifier = buttonModifier) {
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

