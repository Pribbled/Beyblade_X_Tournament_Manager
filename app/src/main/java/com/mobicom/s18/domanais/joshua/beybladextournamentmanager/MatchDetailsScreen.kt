package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.RoundDetail
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchDetailsViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.MatchUiState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
@Composable
fun PlayerDetailsColumn(
    playerName: String,
    playerScore: Int,
    playerWins: Int,
    playerLosses: Int,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        playerWins > playerLosses -> Color.Green
        playerWins < playerLosses -> Color.Red
        else -> Color.Gray
    }
    Card(
        modifier = modifier
            .width(150.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            Text(
                text = playerName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = scoreColor
            )
            Text(
                text = "Current Score",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$playerWins",
                    color = scoreColor,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun RoundDetail(
    roundNumber: Int,
    winnerName: String,
    videoLink: String ,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Round $roundNumber",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Winner: $winnerName",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = "Video Link: $videoLink",
            fontSize = 16.sp,
            color = Color.Gray
        )

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    tournamentId: String,
    matchId: String,
    onBackClick: () -> Unit = {},
    onRecord: () -> Unit,
    viewModel: MatchDetailsViewModel = viewModel()
) {
    // Collect UI state from ViewModel
    val matchState by viewModel.matchState.collectAsState()
    val isHostOrJudge by viewModel.isHostOrJudge.collectAsState()
    var showScoreDialog by rememberSaveable { mutableStateOf(false) }
    var player1ScoreInput by rememberSaveable { mutableStateOf("") }
    var player2ScoreInput by rememberSaveable { mutableStateOf("") }

    // Listen to real-time match updates
    LaunchedEffect(tournamentId, matchId) {
        viewModel.listenToMatch(tournamentId, matchId)
    }
    val openScoreDialog: (Match) -> Unit = { match ->
        player1ScoreInput = match.player1Score.toString()
        player2ScoreInput = match.player2Score.toString()
        showScoreDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        when (val state = matchState) {
            is MatchUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MatchUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is MatchUiState.Success -> {
                MatchDetailsContent(
                    match = state.match,
                    canManageMatch = isHostOrJudge,
                    onEditScore = { openScoreDialog(state.match) },
                    onRecord = onRecord,
                    modifier = Modifier.padding(paddingValues)
                )

                if (showScoreDialog) {
                    EditScoreDialog(
                        player1Name = state.match.player1Name,
                        player2Name = state.match.player2Name,
                        player1Score = player1ScoreInput,
                        player2Score = player2ScoreInput,
                        onPlayer1ScoreChange = { player1ScoreInput = it.filter { ch -> ch.isDigit() } },
                        onPlayer2ScoreChange = { player2ScoreInput = it.filter { ch -> ch.isDigit() } },
                        onDismiss = { showScoreDialog = false },
                        onConfirm = {
                            val p1 = player1ScoreInput.toIntOrNull()
                            val p2 = player2ScoreInput.toIntOrNull()
                            if (p1 != null && p2 != null) {
                                viewModel.overrideScore(tournamentId, matchId, p1, p2)
                                showScoreDialog = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditScoreDialog(
    player1Name: String,
    player2Name: String,
    player1Score: String,
    player2Score: String,
    onPlayer1ScoreChange: (String) -> Unit,
    onPlayer2ScoreChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Scores") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = player1Score,
                    onValueChange = onPlayer1ScoreChange,
                    label = { Text(player1Name) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = player2Score,
                    onValueChange = onPlayer2ScoreChange,
                    label = { Text(player2Name) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MatchDetailsContent(
    match: Match,
    canManageMatch: Boolean,
    onEditScore: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = "${match.round}: Match ${match.matchNumber}",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Text(
                text = "Format: ${match.format}",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text(
                text = "Round: ${match.currentRound}",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerDetailsColumn(
                    playerName = match.player1Name,
                    playerScore = match.player1Score,
                    playerWins = match.player1Wins,
                    playerLosses = match.player1Losses,
                    modifier = Modifier
                )

                PlayerDetailsColumn(
                    playerName = match.player2Name,
                    playerScore = match.player2Score,
                    playerWins = match.player2Wins,
                    playerLosses = match.player2Losses,
                    modifier = Modifier
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (canManageMatch) {
                    Button(
                        onClick = onRecord,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Start Match", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onEditScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Edit Score")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Display round history
        items(match.rounds.size) { index ->
            val round = match.rounds[index]
            RoundDetail(
                roundNumber = round.roundNumber,
                winnerName = round.winnerName,
                videoLink = round.videoLink
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}


@Preview
@Composable
fun MatchDetailsScreenPreview() {
    // Preview with sample data
    val sampleMatch = Match(
        matchId = "match1",
        tournamentId = "tournament1",
        matchNumber = 1,
        round = "Qualifier",
        format = "First to Four",
        player1Id = "player1",
        player1Name = "Blader_ACE",
        player1Score = 3,
        player1Wins = 2,
        player1Losses = 1,
        player2Id = "player2",
        player2Name = "X-Treme",
        player2Score = 1,
        player2Wins = 1,
        player2Losses = 2,
        status = "in_progress",
        currentRound = 4,
        rounds = listOf(
            RoundDetail(1, "player1", "Blader_ACE", "Video_Link_1"),
            RoundDetail(2, "player2", "X-Treme", "Video_Link_2"),
            RoundDetail(3, "player1", "Blader_ACE", "Video_Link_3")
        )
    )

    var previewScoresVisible by rememberSaveable { mutableStateOf(false) }
    Column {
        MatchDetailsContent(
            match = sampleMatch,
            canManageMatch = true,
            onEditScore = { previewScoresVisible = true },
            onRecord = {}
        )
        if (previewScoresVisible) {
            EditScoreDialog(
                player1Name = sampleMatch.player1Name,
                player2Name = sampleMatch.player2Name,
                player1Score = sampleMatch.player1Score.toString(),
                player2Score = sampleMatch.player2Score.toString(),
                onPlayer1ScoreChange = {},
                onPlayer2ScoreChange = {},
                onDismiss = { previewScoresVisible = false },
                onConfirm = { previewScoresVisible = false }
            )
        }
    }
}
