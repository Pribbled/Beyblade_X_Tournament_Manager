package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.dummyTournaments


// Dummy data for participants
data class Player(val name: String, val rank: Int)
val dummyParticipants = listOf(
    Player("Blader_ACE", 1), Player("SpeedKing", 2), Player("DraconicFury", 3),
    Player("VortexMaster", 4), Player("IronGrip", 5), Player("ShadowBlade", 6),
    Player("X-Treme", 7), Player("Leo a.k.a. Lion", 8)
)


@Composable
fun OverviewTab(tournament: Tournament) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tournament Details", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Date: ${tournament.date}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status: ${tournament.status}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Participants: ${tournament.participants}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                "Participants",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(dummyParticipants) { player ->
            ParticipantRow(player = player)
        }
    }
}

@Composable
fun ParticipantRow(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = player.name, fontWeight = FontWeight.Medium)
            Text(text = "Rank #${player.rank}", color = MaterialTheme.colorScheme.primary)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OverviewTabPreview() {
    BeybladeXTournamentManagerTheme {
        OverviewTab(tournament = dummyTournaments.first())
    }
}