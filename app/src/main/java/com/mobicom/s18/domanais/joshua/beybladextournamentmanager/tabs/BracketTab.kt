package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme



val dummyRound1Matches = dummyMatches.filter { it.round == "Round 1" }
val dummyRound2Matches = dummyMatches.filter { it.round == "Round 2" }

val dummyChampion = dummyMatches.last()

@Composable
fun PlayerCard(name: String, width: Dp = 150.dp, height: Dp = 50.dp) {
    Card(
        modifier = Modifier
            .width(width)
            .height(height),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF645DD7)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {
            Text(text = name, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MatchBracketCard(
    match: Match,
    onViewMatchClick: (Match) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PlayerCard(match.player1)


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

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
            IconButton(
                onClick = { onViewMatchClick(match) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility, // eye icon
                    contentDescription = "View Match",
                    tint = Color.White
                )
            }
        }

        PlayerCard(match.player2)
    }
}


@Composable
fun BracketTab(onViewMatchClick: (Match) -> Unit) {
    val round1Matches = dummyRound1Matches
    val round2Matches = dummyRound2Matches

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())// scroll horizontally if too wide
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Round 1
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Text("Qualifiers", style = MaterialTheme.typography.titleLarge)
            round1Matches.forEach { match ->
                MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
            }
        }

        // Round 2
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(120.dp)
        ) {
            Text("Semi Finals", style = MaterialTheme.typography.titleLarge)
            round2Matches.forEach { match ->
                MatchBracketCard(match, onViewMatchClick = onViewMatchClick)
            }
        }

        // Champion column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(220.dp)
        ) {
            Text("Finals", style = MaterialTheme.typography.titleLarge)
            MatchBracketCard(dummyChampion, onViewMatchClick = onViewMatchClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BracketTabPreview() {
    BeybladeXTournamentManagerTheme {
        val onViewMatchClick = null
        BracketTab({ onViewMatchClick })
    }
}
