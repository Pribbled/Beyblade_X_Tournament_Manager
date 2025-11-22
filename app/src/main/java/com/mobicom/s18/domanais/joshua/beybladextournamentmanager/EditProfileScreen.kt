package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
// Import our FirebaseModule and UserProfile data class
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {}
) {
    // --- UI State ---
    // Initialize fields as empty, they will be loaded
    var bladerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    // --- Backend State ---
    var isLoadingData by remember { mutableStateOf(true) } // For initial load
    var isSaving by remember { mutableStateOf(false) } // For save button
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db

    // --- Load User Data ---
    // This runs once when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "You must be logged in to edit."
            isLoadingData = false
            return@LaunchedEffect
        }

        try {
            val userDocRef = db.collection("users").document(userId)
            val document = userDocRef.get().await() // Get the user's document
            val userProfile = document.toObject(UserProfile::class.java) // Convert it to our data class

            if (userProfile != null) {
                // Populate the state variables with real data
                bladerName = userProfile.bladerName
                email = userProfile.email
                contactInfo = userProfile.contactInfo
                birthday = userProfile.birthday
            } else {
                errorMessage = "Could not find user profile."
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load profile."
            Log.w("EditProfile", "loadProfile:failure", e)
        }
        isLoadingData = false // Stop loading
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
        if (isLoadingData) {
            // Show a loading spinner while we fetch data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Data is loaded, show the form
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = bladerName,
                    onValueChange = { bladerName = it },
                    label = { Text("Blader Name/Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true // Email is usually not edited here
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Contact Info") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("Birthday (MM/DD/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            errorMessage = null

                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                errorMessage = "Not logged in."
                                isSaving = false
                                return@launch
                            }

                            // Create a map of the fields to update
                            val updatedData = mapOf(
                                "bladerName" to bladerName.trim(),
                                "contactInfo" to contactInfo.trim(),
                                "birthday" to birthday.trim()
                            )

                            // --- Save to Firestore ---
                            try {
                                db.collection("users")
                                    .document(userId)
                                    .update(updatedData) // Update the fields
                                    .await()

                                // Success!
                                isSaving = false
                                onBackClick() // Navigate back

                            } catch (e: Exception) {
                                isSaving = false
                                errorMessage = e.localizedMessage ?: "Failed to save profile."
                                Log.w("EditProfile", "saveProfile:failure", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isSaving // Disable button while saving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    BeybladeXTournamentManagerTheme {
        EditProfileScreen()
    }
}