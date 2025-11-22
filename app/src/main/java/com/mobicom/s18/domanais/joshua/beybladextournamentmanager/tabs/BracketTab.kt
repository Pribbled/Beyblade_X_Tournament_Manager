package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

/**
 * BracketTab displays tournament bracket visualization.
 * Shows matches organized by round in a tree structure.
 *
 * @param matches List of Match objects from Firestore
 * @param onViewMatchClick Callback when a match is clicked
 */
@Composable
fun BracketTab(
    matches: List<Match> = emptyList(),
    onViewMatchClick: (Match) -> Unit = {}
) {
    if (matches.isEmpty()) {
        // Show placeholder when no matches exist
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No Bracket Generated Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Generate a bracket in the Overview tab to see the tournament bracket visualization here.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        // Display bracket visualization
        val round1Matches = matches.filter { it.round == "Round 1" }
        val round2Matches = matches.filter { it.round == "Round 2" }
        val round3Matches = matches.filter { it.round == "Round 3" }
        val finalsMatches = matches.filter { it.round.contains("Final", ignoreCase = true) }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Round 1
            if (round1Matches.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    Text("Round 1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    round1Matches.forEach { match ->
                        MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
                    }
                }
            }

            // Round 2
            if (round2Matches.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(120.dp)
                ) {
                    Text("Round 2", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    round2Matches.forEach { match ->
                        MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
                    }
                }
            }

            // Round 3
            if (round3Matches.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(220.dp)
                ) {
                    Text("Round 3", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    round3Matches.forEach { match ->
                        MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
                    }
                }
            }

            // Finals
            if (finalsMatches.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(220.dp)
                ) {
                    Text("Finals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    finalsMatches.forEach { match ->
                        MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a player's name in the bracket.
 *
 * @param name Player's name
 * @param width Card width
 * @param height Card height
 * @param isWinner Whether this player won the match
 */
@Composable
fun PlayerCard(
    name: String,
    width: Dp = 150.dp,
    height: Dp = 50.dp,
    isWinner: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(width)
            .height(height),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) {
                Color(0xFF4CAF50) // Green for winner
            } else {
                Color(0xFF645DD7) // Purple for regular
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

/**
 * Card displaying a match in the bracket with two players.
 *
 * @param match Match object from Firestore
 * @param onViewMatchClick Callback when view button is clicked
 */
@Composable
fun MatchBracketCard(
    match: Match,
    onViewMatchClick: (Match) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Player 1
        PlayerCard(
            name = match.player1Name,
            isWinner = match.winnerId == match.player1Id && match.status == "completed"
        )

        // Connector and View Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Vertical line connector
            Canvas(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
            ) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.6f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 4f
                )
            }

            // View Match Button
            IconButton(
                onClick = { onViewMatchClick(match) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View Match",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Player 2
        PlayerCard(
            name = match.player2Name,
            isWinner = match.winnerId == match.player2Id && match.status == "completed"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BracketTabPreview() {
    BeybladeXTournamentManagerTheme {
        BracketTab(
            matches = emptyList(),
            onViewMatchClick = {}
        )
    }
}
