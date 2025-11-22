package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

/**
 * Composable that displays a list of matches for a tournament.
 * Shows upcoming matches (scheduled/in_progress) and completed matches separately.
 *
 * @param matches List of Match objects from Firestore
 * @param isLoading Whether the data is currently loading
 * @param onViewMatchClick Callback when a match is clicked
 */
@Composable
fun MatchesTab(
    matches: List<Match> = emptyList(),
    isLoading: Boolean = false,
    onViewMatchClick: (Match) -> Unit = {}
) {
    // Separate matches by status
    val upcomingMatches = matches.filter {
        it.status == "scheduled" || it.status == "in_progress"
    }
    val completedMatches = matches.filter { it.status == "completed" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Show message if no matches exist
        if (!isLoading && matches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No matches generated yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Generate a bracket to create matches",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Upcoming Matches Section
        if (upcomingMatches.isNotEmpty()) {
            item {
                Text(
                    "Upcoming Matches (${upcomingMatches.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(upcomingMatches) { match ->
                MatchCard(match = match, onViewMatchClick = onViewMatchClick)
            }
        }

        // Completed Matches Section
        if (completedMatches.isNotEmpty()) {
            item {
                Text(
                    "Completed Matches (${completedMatches.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(completedMatches) { match ->
                MatchCard(match = match, onViewMatchClick = onViewMatchClick)
            }
        }
    }
}

/**
 * Card component displaying a single match with player names, status, and scores.
 *
 * @param match The Match object from Firestore
 * @param onViewMatchClick Callback when the card or button is clicked
 */
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
                        text = match.player1Name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${match.player1Id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = match.player2Name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${match.player2Id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(showBackground = true)
@Composable
fun MatchesTabPreview() {
    BeybladeXTournamentManagerTheme {
        MatchesTab(
            matches = emptyList(),
            isLoading = false,
            onViewMatchClick = {}
        )
    }
}