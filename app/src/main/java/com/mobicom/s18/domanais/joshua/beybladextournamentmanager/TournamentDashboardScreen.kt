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
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.BracketTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MatchesTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MetricsTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.OverviewTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    tournamentId: String = "preview1",
    onBackClick: () -> Unit = {} ,
    onViewMatchClick: (Match) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Matches", "Bracket", "Metrics")

    // Snapshot listener for real-time tournament updates
    DisposableEffect(tournamentId) {
        val listenerRegistration = db.collection("tournaments")
            .document(tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("TournamentDashboard", "Listen failed.", error)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    tournament = snapshot.toObject(Tournament::class.java)
                    isLoading = false
                } else {
                    Log.d("TournamentDashboard", "Tournament not found")
                    isLoading = false
                }
            }

        // Cleanup listener when composable leaves composition
        onDispose {
            listenerRegistration.remove()
        }
    }

    if (isLoading) {
        // Show loading indicator
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (tournament == null) {
        // Show error state
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Tournament not found")
            Button(onClick = onBackClick) {
                Text("Go Back")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament!!.name,
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
                0 -> OverviewTab(tournament!!)
                1 -> MatchesTab(onViewMatchClick = onViewMatchClick)
                2 -> BracketTab(onViewMatchClick = onViewMatchClick)
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