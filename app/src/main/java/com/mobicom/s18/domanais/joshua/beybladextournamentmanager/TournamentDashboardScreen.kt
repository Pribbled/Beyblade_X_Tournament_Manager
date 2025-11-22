package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.BracketTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MatchesTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MetricsTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.OverviewTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.TournamentDashboardViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    tournament: Tournament = dummyTournaments.first(), // Use a dummy tournament for preview
    onBackClick: () -> Unit = {} ,
    onViewMatchClick: (Match) -> Unit = {},
    viewModel: TournamentDashboardViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Matches", "Bracket", "Metrics")

    // Collect matches from ViewModel
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load matches when the composable is first displayed
    LaunchedEffect(tournament.id) {
        viewModel.loadMatches(tournament.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content for each tab
            when (selectedTabIndex) {
                0 -> OverviewTab(tournament = tournament, viewModel = viewModel)
                1 -> MatchesTab(
                    matches = matches,
                    isLoading = isLoading,
                    onViewMatchClick = onViewMatchClick
                )
                2 -> BracketTab(
                    matches = matches,
                    onViewMatchClick = onViewMatchClick
                )
                3 -> MetricsTab()
            }
        }
    }
}

@Composable
fun PlaceholderTabContent(screenName: String) {
    // This is a placeholder for screens assigned to other group members.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "$screenName content will be displayed here.")
        Text(text = "Assigned to another team member.")
    }
}


@Preview(showBackground = true)
@Composable
fun TournamentDashboardScreenPreview() {
    BeybladeXTournamentManagerTheme {
        TournamentDashboardScreen()
    }
}