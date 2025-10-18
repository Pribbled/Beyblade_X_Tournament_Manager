package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@Composable
fun TournamentCard(
    tournament: Tournament,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tournament.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tournament.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "${tournament.participants} participants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                text = tournament.status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when (tournament.status) {
                    "Ongoing" -> MaterialTheme.colorScheme.primary
                    "Upcoming" -> MaterialTheme.colorScheme.secondary
                    "Finished" -> Color.Gray
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Preview
@Composable
fun TournamentCardPreview() {
    BeybladeXTournamentManagerTheme {
        TournamentCard(
            tournament = Tournament("p1", "Preview Tournament", "Oct 25, 2025", "Ongoing", 16),
            onClick = {}
        )
    }
}