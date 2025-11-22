package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.cards.TournamentCard
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament

// Dummy data list



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        onProfileClick: () -> Unit = {},
        onCreateTournamentClick: () -> Unit = {},
        onJoinTournamentClick: () -> Unit = {},
        onTournamentClick: (String) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val pastTournaments = remember { mutableStateOf<List<Tournament>>(emptyList()) }

    //basically a coroutine for our composable so that when we use this component this runs
    LaunchedEffect(Unit) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return@LaunchedEffect
            val userDoc = db.collection("users")
                .document(auth.currentUser!!.uid)
                .get()
                .await()

            val pastTournamentIds = userDoc.get("pastTournaments") as? List<String> ?: emptyList()
            val fetchedTournaments = mutableListOf<Tournament>()
            val tournamentsCollection = db.collection("tournaments")
            for (tournamentId in pastTournamentIds) {
                val doc = tournamentsCollection.document(tournamentId).get().await()
                val tournament = doc.toObject(Tournament::class.java)
                if (tournament != null) fetchedTournaments.add(tournament)
            }

            pastTournaments.value = fetchedTournaments

        } catch (e: Exception) {
            Log.w("HomeScreen", "Error fetching tournaments", e)
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
                    Text("Create Tournament")
                }
                OutlinedButton(
                    onClick = onJoinTournamentClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Join Tournament")
                }
            }

            Text(
                text = "Your Tournaments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(pastTournaments.value) { tournament ->
                    TournamentCard(
                        tournament = tournament,
                        onClick = { onTournamentClick(tournament.uid) }
                    )
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

