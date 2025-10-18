package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
@Composable
fun PlayerDetailsColumn(
    playerName: String,
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
@Composable
fun MatchDetailsComponent(
    onRecord: () -> Unit = {},
    onUpdateScore: () -> Unit = {}
) {
    var elapsed by remember { mutableStateOf(0L) } // seconds
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (running) {
            while (running) {
                delay(1000L)
                elapsed += 1L
            }
        }
    }

    fun formatTime(totalSeconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds)
        val seconds = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Timer",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatTime(elapsed),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { running = true },
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp)
            ) {
                Text("Start")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { running = false },
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp)
            ) {
                Text("Stop")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = { onRecord() },
                modifier = Modifier
                    .width(160.dp)
                    .height(44.dp)
            ) {
                Text("Record Match")
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = { onUpdateScore() },
                modifier = Modifier
                    .width(160.dp)
                    .height(44.dp)
            ) {
                Text("Update Score")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    onBackClick: () -> Unit = {}
) {
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
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Text(
                    text = "Qualifier: Match 1",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Text(
                    text = "Format: First to Four",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Text(
                    text = "Round : 4!",
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
                        playerName = "Blader_ACE",
                        playerScore = 3,
                        playerWins = 2,
                        playerLosses = 1,
                        modifier = Modifier
                    )

                    PlayerDetailsColumn(
                        playerName = "X-Treme",
                        playerScore = 1,
                        playerWins = 1,
                        playerLosses = 2,
                        modifier = Modifier
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { MatchDetailsComponent {  } }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { RoundDetail(1, "Blader_ACE","Video_Link_1") }
            item { RoundDetail(2, "X-Treme","Video_Link_2") }
            item { RoundDetail(3, "Blader_ACE","Video_Link_3") }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}


@Preview
@Composable
fun MatchDetailsScreenPreview() {
    MatchDetailsScreen()
}
