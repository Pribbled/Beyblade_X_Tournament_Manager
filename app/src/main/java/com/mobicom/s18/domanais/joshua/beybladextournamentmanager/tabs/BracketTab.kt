package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

/**
 * Intelligent Bracket Tab.
 * Decides whether to show a Standings Table (Round Robin/Swiss) or a Bracket Tree (Elimination)
 * based on the tournament configuration and current stage.
 */
@Composable
fun BracketTab(
    tournament: Tournament? = null,
    matches: List<Match> = emptyList(),
    onViewMatchClick: (Match) -> Unit = {}
) {
    if (tournament == null) return

    // 1. Determine which Format is active based on the Stage
    val currentFormat = if (tournament.stageCount == 2 && tournament.currentStage == 2) {
        tournament.stage2Format
    } else {
        tournament.stage1Format
    }

    // 2. Decide View Type
    val isStandingsView = currentFormat.contains("Round Robin") ||
            currentFormat.contains("Swiss") ||
            currentFormat.contains("Snake Draft")

    Column(modifier = Modifier.fillMaxSize()) {
        // Optional: Stage Indicator
        if (tournament.stageCount > 1) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (tournament.currentStage == 1) "Stage 1: Group Stage ($currentFormat)" else "Stage 2: Finals ($currentFormat)",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (matches.isEmpty()) {
            EmptyBracketState()
        } else if (isStandingsView) {
            // Show Ranking Table
            StandingsView(matches)
        } else {
            // Show Bracket Tree
            BracketTreeView(matches, onViewMatchClick)
        }
    }
}

// ==========================================
// VIEW 1: STANDINGS TABLE (Round Robin / Swiss)
// ==========================================

data class PlayerStats(
    val playerId: String,
    val playerName: String,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val pointsScored: Int = 0,
    val pointsAllowed: Int = 0
) {
    val pointDiff: Int get() = pointsScored - pointsAllowed
    val winRate: Float get() = if (matchesPlayed > 0) wins.toFloat() / matchesPlayed else 0f
}

@Composable
fun StandingsView(matches: List<Match>) {
    // 1. Calculate Stats on the fly
    val statsMap = remember(matches) { mutableMapOf<String, PlayerStats>() }

    matches.forEach { match ->
        if (match.status == "completed") {
            // Player 1
            val p1 = statsMap.getOrDefault(match.player1Id, PlayerStats(match.player1Id, match.player1Name))
            statsMap[match.player1Id] = p1.copy(
                matchesPlayed = p1.matchesPlayed + 1,
                wins = p1.wins + (if (match.winnerId == match.player1Id) 1 else 0),
                losses = p1.losses + (if (match.winnerId != match.player1Id) 1 else 0),
                pointsScored = p1.pointsScored + match.player1Score,
                pointsAllowed = p1.pointsAllowed + match.player2Score
            )

            // Player 2
            val p2 = statsMap.getOrDefault(match.player2Id, PlayerStats(match.player2Id, match.player2Name))
            statsMap[match.player2Id] = p2.copy(
                matchesPlayed = p2.matchesPlayed + 1,
                wins = p2.wins + (if (match.winnerId == match.player2Id) 1 else 0),
                losses = p2.losses + (if (match.winnerId != match.player2Id) 1 else 0),
                pointsScored = p2.pointsScored + match.player2Score,
                pointsAllowed = p2.pointsAllowed + match.player1Score
            )
        } else {
            // Initialize players even if matches aren't done so they show up in table
            if (!statsMap.containsKey(match.player1Id)) statsMap[match.player1Id] = PlayerStats(match.player1Id, match.player1Name)
            if (!statsMap.containsKey(match.player2Id)) statsMap[match.player2Id] = PlayerStats(match.player2Id, match.player2Name)
        }
    }

    // 2. Sort Logic (Wins -> Point Diff -> Points Scored)
    // Note: Head-to-head is complex to auto-calculate here, relying on these primary metrics for MVP
    val sortedStats = statsMap.values.sortedWith(
        compareByDescending<PlayerStats> { it.wins }
            .thenByDescending { it.pointDiff }
            .thenByDescending { it.pointsScored }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Live Standings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("#", Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                    Text("Blader", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("W-L", Modifier.width(50.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text("Diff", Modifier.width(40.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text("Pts", Modifier.width(40.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }

                // Rows
                sortedStats.forEachIndexed { index, player ->
                    StandingRow(index + 1, player)
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun StandingRow(rank: Int, player: PlayerStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .width(30.dp)
                .padding(end = 8.dp)
        ) {
            if (rank <= 3) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (rank == 1) Color(0xFFFFD700) else if (rank == 2) Color(0xFFC0C0C0) else Color(0xFFCD7F32),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = rank.toString(), fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        Text(
            text = player.playerName.ifBlank { "TBD" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        // W-L
        Text(
            text = "${player.wins}-${player.losses}",
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        // Point Diff
        val diffColor = if (player.pointDiff > 0) Color(0xFF4CAF50) else if (player.pointDiff < 0) Color.Red else Color.Gray
        Text(
            text = if (player.pointDiff > 0) "+${player.pointDiff}" else "${player.pointDiff}",
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center,
            color = diffColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )

        // Total Points Scored
        Text(
            text = "${player.pointsScored}",
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ==========================================
// VIEW 2: BRACKET TREE (Elimination)
// ==========================================

@Composable
fun BracketTreeView(matches: List<Match>, onViewMatchClick: (Match) -> Unit) {
    // Dynamic Round Detection
    val roundsMap = matches.groupBy { it.round }
    // Sort rounds (assuming strings like "Round 1", "Round 2", "Finals")
    // Needs robust sorting logic. For MCO, we iterate known keys.
    val sortedRounds = roundsMap.keys.sortedWith(compareBy {
        when {
            it.contains("1") -> 1
            it.contains("2") -> 2
            it.contains("3") -> 3
            it.contains("Semi") -> 8
            it.contains("Final") -> 9
            else -> 10
        }
    })

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(40.dp) // Space between columns
    ) {
        sortedRounds.forEachIndexed { index, roundName ->
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp), // Space between matches in a column
                modifier = Modifier.padding(top = (index * 40).dp) // Stagger start to center tree
            ) {
                Text(
                    text = roundName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val roundMatches = roundsMap[roundName] ?: emptyList()
                roundMatches.forEach { match ->
                    MatchBracketCard(match, onViewMatchClick)
                }
            }
        }
    }
}

@Composable
fun MatchBracketCard(
    match: Match,
    onViewMatchClick: (Match) -> Unit = {}
) {
    Card(
        modifier = Modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Player 1
            BracketPlayerRow(
                name = match.player1Name,
                score = if (match.status == "completed") match.player1Score.toString() else "-",
                isWinner = match.status == "completed" && match.winnerId == match.player1Id
            )
            Divider(color = Color.LightGray, thickness = 1.dp)
            // Player 2
            BracketPlayerRow(
                name = match.player2Name,
                score = if (match.status == "completed") match.player2Score.toString() else "-",
                isWinner = match.status == "completed" && match.winnerId == match.player2Id
            )

            // View Button Overlay (Optional, simple icon)
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
                IconButton(
                    onClick = { onViewMatchClick(match) },
                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BracketPlayerRow(name: String, score: String, isWinner: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isWinner) Color(0xFFE8F5E9) else Color.Transparent) // Light green background for winner
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.ifBlank { "TBD" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = score,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isWinner) Color(0xFF2E7D32) else Color.Black
        )
    }
}

@Composable
fun EmptyBracketState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bracket not generated", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text("Wait for the host to start the stage.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BracketTabPreview() {
    BeybladeXTournamentManagerTheme {
        BracketTab()
    }
}