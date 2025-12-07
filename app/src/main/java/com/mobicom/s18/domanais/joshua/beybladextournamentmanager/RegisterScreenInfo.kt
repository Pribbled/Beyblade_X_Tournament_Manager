package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenInfo(
    onRegistrationComplete: () -> Unit = {}
) {
    // --- UI State ---
    var bladerName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    // --- Backend State ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Just a few more details...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // --- Blader Name Field ---
            OutlinedTextField(
                value = bladerName,
                onValueChange = { bladerName = it },
                label = { Text("Blader Name / Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Birthday Field ---
            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("Birthday (MM/DD/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // --- Contact Info Field ---
            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Contact Info") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Error Message Display ---
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // --- Complete Registration Button ---
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        // --- 1. Get Current User ---
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            errorMessage = "Error: No user is logged in. Please go back."
                            isLoading = false
                            return@launch
                        }

                        // --- 2. Validation ---
                        if (bladerName.isBlank()) {
                            errorMessage = "Blader Name cannot be blank."
                            isLoading = false
                            return@launch
                        }

                        // --- 3. Create User Profile Object ---
                        val userProfile = UserProfile(
                            uid = currentUser.uid,
                            email = currentUser.email ?: "", // Get email from auth
                            bladerName = bladerName.trim(),
                            birthday = birthday.trim(),
                            contactInfo = contactInfo.trim()
                            // Other fields (rank, xp) will use default values
                        )

                        // --- 4. Save to Firestore ---
                        try {
                            db.collection("users")
                                .document(currentUser.uid) // Set document ID to be the user's UID
                                .set(userProfile) // Save the user profile object
                                .await() // Wait for the operation to complete

                            // Success!
                            isLoading = false
                            onRegistrationComplete() // Navigate to home screen

                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Failed to save profile."
                            Log.w("RegisterScreenInfo", "saveProfile:failure", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Complete Registration")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenInfoPreview() {
    BeybladeXTournamentManagerTheme {
        RegisterScreenInfo()
    }
}