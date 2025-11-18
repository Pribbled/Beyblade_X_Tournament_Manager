package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

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
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@Composable
fun MatchRecordingScreen(
    onBackClick: () -> Unit = {}
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    var playerAScore by remember { mutableStateOf(0) }
    var playerBScore by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingFinished by remember { mutableStateOf(false) }

    var showPlayerAEditDialog by remember { mutableStateOf(false) }
    var showPlayerBEditDialog by remember { mutableStateOf(false) }

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
        // left screen
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterStart),
            playerName = "Blader A",
            playerScore = playerAScore,
            onScoreChange = { playerAScore = it },
            onEditClick = { showPlayerAEditDialog = true }
        )

        // right screen
        PlayerControls(
            modifier = Modifier.align(Alignment.CenterEnd),
            playerName = "Blader B",
            playerScore = playerBScore,
            onScoreChange = { playerBScore = it },
            onEditClick = { showPlayerBEditDialog = true }
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = { /* TODO: Submit Score Logic */ },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit Score",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = {
                    isRecording = !isRecording
                    if (!isRecording) recordingFinished = true
                },
                modifier = Modifier
                    .size(80.dp)
            ) {
                Icon(
                    imageVector = if (isRecording) ImageVector.vectorResource(R.drawable.baseline_stop_24) else Icons.Default.PlayArrow,
                    contentDescription = "Record",
                    tint = if(isRecording) Color.Red else Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

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
        // Score Display
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
            Button(onClick = { onScoreChange(playerScore + 3) }, modifier = buttonModifier) { Text("Extreme Finish") }
            Button(onClick = { onScoreChange(playerScore + 2) }, modifier = buttonModifier) { Text("Burst Finish") }
            Button(onClick = { onScoreChange(playerScore + 1) }, modifier = buttonModifier) { Text("Over Finish") }
            Button(onClick = { onScoreChange(playerScore + 1) }, modifier = buttonModifier) { Text("Spin Finish") }
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


@Preview(device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=240")
@Composable
fun MatchRecordingScreenPreview() {
    BeybladeXTournamentManagerTheme {
        MatchRecordingScreen()
    }
}