package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.cards.TournamentCard
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onCreateTournamentClick: () -> Unit = {},
    onJoinTournamentClick: () -> Unit = {},
    onTournamentClick: (String) -> Unit = {}
) {
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db

    var rawTournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userId = FirebaseModule.auth.currentUser?.uid
    var pastTournaments by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun refreshVisible() {
        tournaments = rawTournaments.filter { tour ->
            pastTournaments.isEmpty() || tour.uid in pastTournaments
        }.sortedWith(
            compareBy<Tournament>(
                { it.status == "completed" },
                { it.status == "upcoming" }
            )
        )
    }

    DisposableEffect(userId) {
        if (userId == null) {
            rawTournaments = emptyList()
            pastTournaments = emptySet()
            refreshVisible()
            isLoading = false
            errorMessage = "Please log in to view tournaments."
            return@DisposableEffect onDispose {}
        }

        val profileRegistration = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeScreen", "Profile listen failed", error)
                    errorMessage = "Error loading profile: ${error.message}"
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                pastTournaments = profile?.pastTournaments?.toSet().orEmpty()
                refreshVisible()
            }

        val tournamentRegistration = db.collection("tournaments")
            .where(
                Filter.or(
                    Filter.equalTo("tournamentOwner", userId),
                    Filter.arrayContains("tournamentPlayers", userId),
                    Filter.arrayContains("tournamentJudges", userId)
                )
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeScreen", "Listen failed", error)
                    errorMessage = "Error loading tournaments: ${error.message}"
                    isLoading = false
                    return@addSnapshotListener
                }

                rawTournaments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Tournament::class.java)?.copy(uid = doc.id)
                }.orEmpty()
                refreshVisible()
                isLoading = false
                errorMessage = null
            }

        onDispose {
            profileRegistration.remove()
            tournamentRegistration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome back, Blader!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCreateTournamentClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Create")
                }
                OutlinedButton(
                    onClick = onJoinTournamentClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Join")
                }
            }

            Text(
                text = "Your Tournaments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Loading & Empty States
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (tournaments.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("You haven't joined any tournaments yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(tournaments) { tournament ->
                        TournamentCard(
                            tournament = tournament,
                            // Use 'uid' as defined in your Tournament.kt data class
                            onClick = { onTournamentClick(tournament.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BeybladeXTournamentManagerTheme {
        HomeScreen()
    }
}