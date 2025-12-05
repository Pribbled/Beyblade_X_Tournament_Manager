package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NextPlan
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.NextPlan
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

/**
 * Composable that displays a list of matches for a tournament.
 * * NOTE: This component is stateless. It relies on the parent screen (TournamentDashboard)
 * to fetch the data via the Repository/ViewModel and pass the list here.
 */
@Composable
fun MatchesTab(
    tournament: Tournament,
    matches: List<Match>, // Accepts list directly from ViewModel
    isHost: Boolean = false, // Check if current user is owner
    onViewMatchClick: (Match) -> Unit = {},
    onGenerateMatches: () -> Unit = {}
) {
    // Separate matches by status
    val upcomingMatches = matches.filter {
        it.status == "scheduled" || it.status == "in_progress"
    }
    val completedMatches = matches.filter { it.status == "completed" }

    val hasMatches = matches.isNotEmpty()
    val currentRoundFinished = hasMatches && matches.all { it.status == "completed" }
    val currentRoundNumber = matches.maxOfOrNull { it.matchNumber.toString().substringAfter("Round ").toIntOrNull() ?: 0 } ?: 0
    val isTournamentComplete = false // Ideally check against tournament.roundsToPlay

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show message if no matches exist
        if (isHost) {
            if (!hasMatches) {
                // CASE 1: No matches yet -> Start Tournament
                item {
                    GeneratorCard(
                        title = "Ready to Start?",
                        subtitle = "Generate Round 1 to begin the tournament.",
                        buttonText = "Generate Round 1",
                        icon = Icons.Default.Casino,
                        onClick = onGenerateMatches
                    )
                }
            } else if (currentRoundFinished) {
                // CASE 2: Round Finished -> Generate Next Round
                // We show this button if we haven't played enough rounds yet
                // For Swiss, we play 'roundsToPlay' rounds.
                // We estimate current round by dividing matches by (Players/2)
                val players = tournament.tournamentPlayers.size
                val matchesPerRound = players / 2
                val calculatedCurrentRound = matches.size / matchesPerRound

                if (calculatedCurrentRound < tournament.roundsToPlay) {
                    item {
                        GeneratorCard(
                            title = "Round Complete",
                            subtitle = "Generate pairings for the next round based on current standings.",
                            buttonText = "Generate Round ${calculatedCurrentRound + 1}",
                            icon = Icons.Default.NextPlan,
                            onClick = onGenerateMatches
                        )
                    }
                } else if (tournament.stageCount == 2 && tournament.currentStage == 1) {
                    // Stage 1 Done -> Generate Stage 2
                    item {
                        GeneratorCard(
                            title = "Group Stage Complete",
                            subtitle = "Ready to generate the Final Stage bracket?",
                            buttonText = "Generate Finals",
                            icon = Icons.Default.NextPlan,
                            onClick = onGenerateMatches
                        )
                    }
                }
            }
        } else if (matches.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Waiting for host to start the tournament...", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        // --- MATCH LIST ---
        if (upcomingMatches.isNotEmpty()) {
            item { Text("Upcoming Matches", style = MaterialTheme.typography.titleLarge) }
            items(upcomingMatches) { match -> MatchCard(match, onViewMatchClick) }
        }

        if (completedMatches.isNotEmpty()) {
            item { Text("Completed", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp)) }
            items(completedMatches) { match -> MatchCard(match, onViewMatchClick) }
        }
    }
}

@Composable
fun GeneratorCard(
    title: String,
    subtitle: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    onViewMatchClick: (Match) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Round and Match Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = match.round,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Match #${match.matchNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player 1
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.player1Name.ifBlank { "TBD" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Player 2
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.player2Name.ifBlank { "TBD" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status and Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (match.status) {
                        "completed" -> MaterialTheme.colorScheme.tertiaryContainer
                        "in_progress" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = match.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Score or Action Button
                if (match.status == "completed") {
                    Text(
                        text = "${match.player1Score} - ${match.player2Score}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Button(
                        onClick = { onViewMatchClick(match) },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Match", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MatchesTabPreview() {
//    BeybladeXTournamentManagerTheme {
//        MatchesTab(
//            matches = emptyList(),
//            onViewMatchClick = {}
//        )
//    }
//}