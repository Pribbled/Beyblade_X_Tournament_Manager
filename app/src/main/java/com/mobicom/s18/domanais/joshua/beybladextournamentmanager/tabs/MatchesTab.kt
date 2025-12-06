package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NextPlan
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.BeybladeBuild
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

/**
 * Composable that displays a list of matches for a tournament.
 * * NOTE: This component is stateless. It relies on the parent screen (TournamentDashboard)
 * to fetch the data via the Repository/ViewModel and pass the list here.
 */
@Composable
fun MatchesTab(
    tournament: Tournament,
    matches: List<Match>,
    isHost: Boolean = false,
    onViewMatchClick: (Match) -> Unit = {},
    onGenerateMatches: () -> Unit = {},
    currentUserId: String? = FirebaseModule.auth.currentUser?.uid,
    isJudge: Boolean = tournament.tournamentJudges.contains(currentUserId),
    participants: List<UserProfile> = emptyList(),
    finalBuilds: List<BeybladeBuild> = emptyList(),
    onSubmitFinalBuild: (UserProfile) -> Unit = {},
    judgesAlsoPlay: Boolean = tournament.tournamentJudgesAlsoPlay
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

    val players = tournament.tournamentPlayers.size
    val matchesPerRound = (players + 1) / 2
    val calculatedCurrentRound = if (matchesPerRound > 0) matches.size / matchesPerRound else 0

    // Check if all required final builds are submitted for the qualifiers
    val qualifiers = participants.sortedBy { it.rank }.take(tournament.topXQualifiers)
    val allFinalBuildsSubmitted = qualifiers.isNotEmpty() && qualifiers.all { qualifier ->
        finalBuilds.any { build -> build.playerId == qualifier.uid }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show message if no matches exist
        if (isHost) {
            if (!hasMatches) {
                // ... (Generate Round 1) ...
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
                // CHECK: Are we in Stage 1 of a 2-Stage tournament?
                val isStage1Done = tournament.stageCount == 2 && tournament.currentStage == 1
                // For Swiss/RR, check if we played enough rounds
                val areRoundsDone = calculatedCurrentRound >= tournament.roundsToPlay

                if (isStage1Done && (areRoundsDone || tournament.stage1Format == "Single Elimination")) {
                    val subtitleText = if (qualifiers.isEmpty()) {
                        "Waiting for standings..."
                    } else if (!allFinalBuildsSubmitted) {
                        "Collect final builds from qualifiers before starting finals."
                    } else {
                        "Calculate standings and generate the Final Bracket?"
                    }
                    // CONDITION: Stage 1 is totally finished. Time for Stage 2.
                    item {
                        GeneratorCard(
                            title = "Group Stage Complete",
                            subtitle = subtitleText,
                            buttonText = "Start Finals",
                            icon = Icons.Default.EmojiEvents,
                            onClick = onGenerateMatches,
                            enabled = qualifiers.isNotEmpty() && allFinalBuildsSubmitted
                        )
                    }

                    if ((isHost || isJudge) && qualifiers.isNotEmpty() && !allFinalBuildsSubmitted) {
                        item {
                            QualifiedFinalistList(
                                qualifiers = qualifiers,
                                finalBuilds = finalBuilds,
                                callToAction = "Submit Final Build",
                                onSubmit = onSubmitFinalBuild
                            )
                        }
                    }
                } else if (calculatedCurrentRound < tournament.roundsToPlay && (tournament.stage1Format == "Swiss System" || tournament.stage1Format == "Round Robin")) {
                    // CONDITION: Generate Next Round (Swiss/RR)
                    item {
                        GeneratorCard(
                            title = "Round $calculatedCurrentRound Complete",
                            subtitle = "Generate pairings for the next round.",
                            buttonText = "Generate Round ${calculatedCurrentRound + 1}",
                            icon = Icons.Default.NextPlan,
                            onClick = onGenerateMatches
                        )
                    }
                }
            }
        }

        // --- MATCH LIST ---
        if (upcomingMatches.isNotEmpty()) {
            item { Text("Upcoming Matches", style = MaterialTheme.typography.titleLarge) }
            items(upcomingMatches) { match ->
                MatchCard(
                    match = match,
                    canManage = isHost || isJudge,
                    onViewMatchClick = onViewMatchClick,
                    judgesAlsoPlay = judgesAlsoPlay
                )
            }
        }

        if (completedMatches.isNotEmpty()) {
            item { Text("Completed", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp)) }
            items(completedMatches) { match ->
                MatchCard(
                    match = match,
                    canManage = isHost || isJudge,
                    onViewMatchClick = onViewMatchClick,
                    judgesAlsoPlay = judgesAlsoPlay
                )
            }
        }
    }
}

@Composable
fun GeneratorCard(
    title: String,
    subtitle: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
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
            Button(onClick = onClick, enabled = enabled) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    canManage: Boolean,
    onViewMatchClick: (Match) -> Unit = {},
    judgesAlsoPlay: Boolean = false
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
                    if (canManage) {
                        Button(onClick = { onViewMatchClick(match) }, modifier = Modifier.height(36.dp)) {
                            Text("Edit Score", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Text(
                            text = "${match.player1Score} - ${match.player2Score}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (canManage) {
                    Button(
                        onClick = { onViewMatchClick(match) },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Match", style = MaterialTheme.typography.labelMedium)
                    }
                } else if (judgesAlsoPlay && match.status != "completed") {
                    Text(
                        text = "Awaiting host/judge",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                } else {
                    Text("Awaiting host", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun QualifiedFinalistList(
    qualifiers: List<UserProfile>,
    finalBuilds: List<BeybladeBuild>,
    callToAction: String,
    onSubmit: (UserProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Finalists Pending Builds",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        qualifiers.forEach { qualifier ->
            val submitted = finalBuilds.any { it.playerId == qualifier.uid }
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(qualifier.bladerName.ifBlank { qualifier.uid }, fontWeight = FontWeight.Bold)
                        Text(if (submitted) "Build submitted" else "Waiting for submission", color = if (submitted) Color(0xFF2E7D32) else Color.Gray)
                    }
                    if (!submitted) {
                        TextButton(onClick = { onSubmit(qualifier) }) {
                            Text(callToAction)
                        }
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