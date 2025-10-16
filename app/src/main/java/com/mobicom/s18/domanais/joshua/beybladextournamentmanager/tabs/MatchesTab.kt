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
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

// Dummy data for matches
data class Match(
    val id: String,
    val player1: String,
    val player2: String,
    val round: String,
    val isCompleted: Boolean,
    val score: String? = null
)

val dummyMatches = listOf(
    Match("m1", "Blader_ACE", "X-Treme", "Round 1", true, "3-1"),
    Match("m2", "SpeedKing", "Leo a.k.a. Lion", "Round 1", true, "3-0"),
    Match("m3", "DraconicFury", "ShadowBlade", "Round 1", false),
    Match("m4", "VortexMaster", "IronGrip", "Round 1", false),
    Match("m5", "Blader_ACE", "SpeedKing", "Round 2", false)
)

@Composable
fun MatchesTab() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Upcoming Matches", style = MaterialTheme.typography.titleLarge)
        }
        items(dummyMatches.filter { !it.isCompleted }) { match ->
            MatchCard(match = match)
        }

        item {
            Text(
                "Completed Matches",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        items(dummyMatches.filter { it.isCompleted }) { match ->
            MatchCard(match = match)
        }
    }
}

@Composable
fun MatchCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(match.round, style = MaterialTheme.typography.labelSmall)
                Text("${match.player1} vs ${match.player2}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (match.isCompleted) {
                Text(match.score ?: "N/A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            } else {
                Button(onClick = { /* TODO: to match details*/ }) {
                    Text("View")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchesTabPreview() {
    BeybladeXTournamentManagerTheme {
        MatchesTab()
    }
}