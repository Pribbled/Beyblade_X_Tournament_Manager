package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.BracketTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MatchesTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MetricsTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.OverviewTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.TournamentDashboardViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    tournamentId: String = "preview1",
    onBackClick: () -> Unit = {} ,
    onViewMatchClick: (Match) -> Unit = {},
    viewModel: TournamentDashboardViewModel = viewModel()
) {
    val db = FirebaseFirestore.getInstance()
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Matches", "Bracket", "Metrics")

    LaunchedEffect(tournamentId) {
        if (tournamentId == "preview1") {
            // Handle preview mode
            isLoading = false
            return@LaunchedEffect
        }

        // Fetch real data
        db.collection("tournaments").document(tournamentId).get()
            .addOnSuccessListener { document ->
                tournament = document.toObject(Tournament::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament?.name ?: "Loading...",
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tournament != null) {
                // Only render content when tournament data exists
                when (selectedTabIndex) {
                    0 -> OverviewTab(tournament!!)
                    1 -> MatchesTab(onViewMatchClick = onViewMatchClick)
                    2 -> BracketTab(onViewMatchClick = onViewMatchClick)
                    3 -> MetricsTab()
                }
            } else {
                // Handle error case where tournament wasn't found
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tournament not found")
                }
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