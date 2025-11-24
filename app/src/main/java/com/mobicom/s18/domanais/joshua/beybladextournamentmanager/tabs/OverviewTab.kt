package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@Composable
fun OverviewTab(
    tournament: Tournament,
    participants: List<UserProfile> = emptyList()
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section (Status & Date)
        item {
            TournamentHeaderCard(tournament)
        }

        // Rules & Format Grid
        item {
            Text(
                "Tournament Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Format & Battle Type
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(
                        icon = Icons.Outlined.EmojiEvents,
                        label = "Format",
                        value = if (tournament.stageCount > 1) "Multi-Stage" else tournament.stage1Format.ifBlank { "Standard" },
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = Icons.Outlined.SportsMartialArts,
                        label = "Battle Type",
                        value = tournament.battleType.ifBlank { "3on3 Deck" },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Row 2: Scoring & Ranking
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(
                        icon = Icons.Outlined.Score,
                        label = "Scoring",
                        value = tournament.scoringSystem.ifBlank { "Standard" },
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = Icons.Outlined.Leaderboard,
                        label = "Ranking By",
                        value = tournament.rankingSystem.ifBlank { "Wins" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tie Breaker Rules
        if (tournament.tieBreaker1.isNotBlank()) {
            item {
                TieBreakerCard(tournament)
            }
        }

        // Participants List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Participants",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        "${participants.size} Registered", // Use real count
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        if (participants.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No participants loaded yet.", color = Color.Gray)
                }
            }
        } else {
            itemsIndexed(participants) { index, player ->
                ParticipantRow(index + 1, player)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TournamentHeaderCard(tournament: Tournament) {
    val statusColor = when (tournament.status) {
        "ongoing" -> Color(0xFF4CAF50) // Green
        "completed" -> Color.Gray
        else -> Color(0xFF2196F3) // Blue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = tournament.startDate.ifBlank { "Date TBD" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status Badge
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = tournament.status.uppercase(),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TieBreakerCard(tournament: Tournament) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tie-Breaker Priority", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            TieBreakerItem(1, tournament.tieBreaker1)
            if (tournament.tieBreaker2.isNotBlank()) TieBreakerItem(2, tournament.tieBreaker2)
            if (tournament.tieBreaker3.isNotBlank()) TieBreakerItem(3, tournament.tieBreaker3)
        }
    }
}

@Composable
fun TieBreakerItem(priority: Int, rule: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = priority.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = rule, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ParticipantRow(index: Int, player: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$index",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.width(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Use the real blader name
                Text(
                    text = player.bladerName.ifBlank { "Unnamed Blader" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Rank Badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                // Use the real rank
                Text(
                    text = player.rank.ifBlank { "Rookie" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewTabPreview() {
    BeybladeXTournamentManagerTheme {
        OverviewTab(
            tournament = Tournament(
                uid = "preview1",
                name = "Preview Tournament",
                startDate = "Oct 25, 2025",
                status = "upcoming",
                battleType = "3on3 Deck",
                stage1Format = "Round Robin",
                rankingSystem = "Match Wins",
                tieBreaker1 = "Points Difference",
                tieBreaker2 = "Head-to-Head",
                tournamentPlayers = listOf("uid1", "uid2")
            )
        )
    }
}