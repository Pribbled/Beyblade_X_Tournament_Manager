package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.TournamentDashboardViewModel
import kotlinx.coroutines.launch

/**
 * A simple composable button to test bracket generation.
 * This can be added to the Tournament Dashboard UI for testing purposes.
 *
 * Usage:
 * Add this to your TournamentDashboardScreen:
 * ```
 * GenerateBracketButton(tournamentId = tournament.uid)
 * ```
 */
@Composable
fun GenerateBracketButton(
    tournamentId: String,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    isGenerating = true
                    message = null

                    try {
                        val repository = MatchRepository()
                        val result = repository.generateBracket(
                            tournamentId = tournamentId,
                            tournamentFormat = "First to Four"
                        )

                        result.onSuccess { matchCount ->
                            message = "✅ Generated $matchCount matches!"
                            onSuccess()
                        }.onFailure { error ->
                            message = "❌ Error: ${error.message}"
                        }
                    } catch (e: Exception) {
                        message = "❌ Error: ${e.message}"
                    } finally {
                        isGenerating = false
                    }
                }
            },
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isGenerating) "Generating..." else "Generate Bracket (Mock Data)")
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = if (it.startsWith("✅"))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Complete testing panel that can be added to the Overview tab.
 * Shows current match count and allows bracket generation.
 */
@Composable
fun BracketTestingPanel(
    tournamentId: String,
    viewModel: TournamentDashboardViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bracket Testing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Current matches: ${matches.size}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            GenerateBracketButton(tournamentId = tournamentId)
        }
    }
}

