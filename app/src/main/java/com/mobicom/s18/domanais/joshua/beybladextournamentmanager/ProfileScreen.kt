package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
// Import Firebase and our UserProfile data class
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import kotlinx.coroutines.tasks.await

// Dummy data structure for past tournaments
// We'll keep this for now to show the UI, but the profile data will be real.
data class PastTournament(
    val name: String,
    val date: String,
    val placement: String
)

@Composable
fun PlacementBadge(text: String, color: Color) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    // --- Backend State ---
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db

    // --- Load User Data ---
    LaunchedEffect(key1 = Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "You must be logged in."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val document = db.collection("users").document(userId).get().await()
            val profile = document.toObject(UserProfile::class.java)
            if (profile != null) {
                userProfile = profile
            } else {
                errorMessage = "Could not find user profile."
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load profile."
            Log.w("ProfileScreen", "loadProfile:failure", e)
        }
        isLoading = false
    }

    // --- Dummy data for "Past Tournaments" ---
    // We will replace this later with a real query based on userProfile.pastTournaments
    val pastTournaments = listOf(
        PastTournament("Beyblade National Championship", "June 15, 2023", "1st Place"),
        PastTournament("Regional Qualifier", "May 3, 2023", "2nd Place"),
        PastTournament("Local Tournament", "April 12, 2023", "1st Place")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            // Show a full-screen loading spinner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            // Show an error message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (userProfile != null) {
            // Data is loaded, show the profile
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    // We can add userProfile.photoUrl here in the future
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- REAL DATA ---
                            Text(
                                text = userProfile!!.bladerName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- REAL DATA ---
                            Text(
                                text = userProfile!!.rank,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "XP Progress",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // --- REAL DATA ---
                            // Simple calculation for a progress bar
                            val xpProgress = (userProfile!!.xp % 1000) / 1000f

                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onEditProfileClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Edit Profile")
                            }
                        }
                    }
                }


                item {
                    Text(
                        text = "Past Tournaments",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // --- DUMMY LIST (for now) ---
                items(pastTournaments) { tournament ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = tournament.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = tournament.date,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val placementColor = when {
                                tournament.placement.contains("1st") -> Color(0xFFFFD700) // Gold
                                tournament.placement.contains("2nd") -> Color(0xFFC0C0C0) // Silver
                                tournament.placement.contains("3rd") -> Color(0xFFCD7F32) // Bronze
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            PlacementBadge(text = tournament.placement, color = placementColor)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Preview will show loading state by default
    BeybladeXTournamentManagerTheme {
        ProfileScreen()
    }
}