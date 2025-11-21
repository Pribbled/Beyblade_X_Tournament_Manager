package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.text.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    onBackClick: () -> Unit = {},
    onCreateTournamentClick: () -> Unit = {}
) {
    var tournamentName by remember { mutableStateOf("") }
    var tournamentFormat by remember { mutableStateOf("") }
    var scoringSystem by remember { mutableStateOf("") }
    var tieBreakRules by remember { mutableStateOf("") }

    // Dropdown options and selections
    val formatOptions = listOf("Single Elimination", "Double Elimination", "Round Robin", "Swiss System")
    var expandedFormatDropdown by remember { mutableStateOf(false) }
    var selectedFormatOption by remember { mutableStateOf(formatOptions[0]) }

    val scoringOptions = listOf("Standard (1-2-3)", "Custom", "Win-Loss Only")
    var expandedScoringDropdown by remember { mutableStateOf(false) }
    var selectedScoringOption by remember { mutableStateOf(scoringOptions[0]) }

    // Toggle states
    var allowSelfRegistration by remember { mutableStateOf(true) }
    var publicVisibility by remember { mutableStateOf(true) }


    // --- Backend State ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope() // For launching backend tasks
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Tournament") },
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tournament Name
            OutlinedTextField(
                value = tournamentName,
                onValueChange = { tournamentName = it },
                label = { Text("Tournament Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tournament Format Dropdown
            Text(
                text = "Tournament Format",
                style = MaterialTheme.typography.bodyMedium
            )
            ExposedDropdownMenuBox(
                expanded = expandedFormatDropdown,
                onExpandedChange = { expandedFormatDropdown = !expandedFormatDropdown }
            ) {
                OutlinedTextField(
                    value = selectedFormatOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormatDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedFormatDropdown,
                    onDismissRequest = { expandedFormatDropdown = false }
                ) {
                    formatOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedFormatOption = option
                                expandedFormatDropdown = false
                            }
                        )
                    }
                }
            }

            // Scoring System Dropdown
            Text(
                text = "Scoring System",
                style = MaterialTheme.typography.bodyMedium
            )
            ExposedDropdownMenuBox(
                expanded = expandedScoringDropdown,
                onExpandedChange = { expandedScoringDropdown = !expandedScoringDropdown }
            ) {
                OutlinedTextField(
                    value = selectedScoringOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedScoringDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedScoringDropdown,
                    onDismissRequest = { expandedScoringDropdown = false }
                ) {
                    scoringOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedScoringOption = option
                                expandedScoringDropdown = false
                            }
                        )
                    }
                }
            }

            // Tie Break Rules
            OutlinedTextField(
                value = tieBreakRules,
                onValueChange = { tieBreakRules = it },
                label = { Text("Tie Break Rules") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Custom Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tournament Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = "Allow Self-Registration",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Participants can register themselves for the tournament",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = allowSelfRegistration,
                            onCheckedChange = { allowSelfRegistration = it }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = "Public Visibility",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Tournament is visible in public listings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = publicVisibility,
                            onCheckedChange = { publicVisibility = it }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        //validations for our fields

                        if (tournamentName.isBlank()) {
                            errorMessage = "Tournament name cannot be empty."
                            isLoading = false
                            return@launch
                        }
                        if(tieBreakRules.isBlank()){
                            errorMessage = "Tie Breaker Rules cannot be empty."
                            isLoading = false
                            return@launch
                        }
                        val tournament = Tournament(
                            uid = "", // Firebase will generate this
                            tournamentOwner =  auth.currentUser?.uid ?: "",
                            name = tournamentName,
                            tournamentFormat = selectedFormatOption,
                            scoringSystem = selectedScoringOption,
                            tieBreakRules = tieBreakRules,
                            allowSelfRegister = allowSelfRegistration,
                            publicVisibility = publicVisibility
                        )
                        try {
                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                errorMessage = "User not authenticated."
                                isLoading = false
                                return@launch
                            }

                        } catch (e: Exception) {
                            errorMessage = "User not logged in: ${e.message}"
                        }
                        try {
                            val documentRef = db.collection("users")
                                .document(auth.currentUser?.uid ?: throw Exception("User not authenticated"))
                                .collection("tournaments")
                                .add(tournament)
                                .addOnSuccessListener { documentReference ->
                                    Log.d("CreateTournament", "Tournament created with ID: ${documentReference.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("CreateTournament", "Error creating tournament", e)
                                    errorMessage = "${e.message}"
                                }
                                .await()


                            documentRef.update("uid", documentRef.id).await()

                            if (publicVisibility){
                                    // Also add to global tournaments collection
                                try {
                                    db.collection("publicTournaments")
                                        .document(documentRef.id)
                                        .set(tournament.copy(uid = documentRef.id,             // tournament ID
                                            tournamentOwner= auth.currentUser!!.uid,
                                            tournamentCode = tournament.tournamentCode ))
                                        .await()
                                    Log.d("CreateTournament", "Tournament added to public collection")
                                } catch (e: Exception) {
                                    Log.w("CreateTournament", "Error adding to public collection", e)
                                    errorMessage = "Tournament created but failed to make public: ${e.message}"
                                }
                            }
                                onCreateTournamentClick()

                        }catch (e: Exception) {
                            errorMessage = "Error creating tournament: ${e.message}"
                        }finally {
                            isLoading = false
                        }


                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Create Tournament")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTournamentScreenPreview() {
    BeybladeXTournamentManagerTheme {
        CreateTournamentScreen()
    }
}