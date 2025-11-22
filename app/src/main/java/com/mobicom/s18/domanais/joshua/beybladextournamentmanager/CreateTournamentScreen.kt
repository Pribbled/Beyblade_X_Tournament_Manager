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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    onBackClick: () -> Unit = {},
    onCreateTournamentClick: (String) -> Unit = {}
) {
    // --- State Variables ---
    var tournamentName by remember { mutableStateOf("") }
    var tournamentStartDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Structure
    var stageCount by remember { mutableStateOf(1) } // 1 or 2

    // Format Options
    val groupStageOptions = listOf("Round Robin", "Swiss System", "Snake Draft", "Single Elimination", "Double Elimination")
    val finalStageOptions = listOf("Single Elimination", "Double Elimination", "Page Playoff") // Usually cut to top X

    var selectedStage1Format by remember { mutableStateOf(groupStageOptions[0]) }
    var selectedStage2Format by remember { mutableStateOf(finalStageOptions[0]) }

    // Battle Rules
    val battleTypeOptions = listOf("3on3 Deck", "1on1 Standard", "5G Battle")
    var selectedBattleType by remember { mutableStateOf("3on3 Deck") } // Default per requirement

    val scoringOptions = listOf("Standard (1-2-2-3)", "WBO (1-2-2-3)", "Custom")
    var selectedScoringOption by remember { mutableStateOf(scoringOptions[0]) }

    // Tie Breakers
    val rankingSystems = listOf("Match Wins", "Points Scored")
    var selectedRankingSystem by remember { mutableStateOf("Match Wins") }

    val tieBreakerOptions = listOf("Points Scored", "Points Difference", "Wins vs Tied Participants", "Median-Buchholz System", "None")
    var tieBreaker1 by remember { mutableStateOf("Points Scored") }
    var tieBreaker2 by remember { mutableStateOf("Points Difference") }
    var tieBreaker3 by remember { mutableStateOf("Wins vs Tied Participants") }

    // Toggles
    var allowSelfRegistration by remember { mutableStateOf(true) }
    var publicVisibility by remember { mutableStateOf(true) }

    // Backend State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Tournament") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            // --- SECTION 1: BASIC INFO ---
            ConfigSection(title = "Basic Information") {
                OutlinedTextField(
                    value = tournamentName,
                    onValueChange = { tournamentName = it },
                    label = { Text("Tournament Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text(tournamentStartDate ?: "Select Start Date")
                }
            }

            ConfigSection(title = "Structure & Format") {
                Text("Number of Stages", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = stageCount == 1, onClick = { stageCount = 1 })
                    Text("1 Stage (Standard)", modifier = Modifier.padding(end = 16.dp))

                    RadioButton(selected = stageCount == 2, onClick = { stageCount = 2 })
                    Text("2 Stages (Group -> Final)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (stageCount == 1) {
                    DropdownSelector(
                        label = "Tournament Format",
                        options = groupStageOptions,
                        selectedOption = selectedStage1Format,
                        onOptionSelected = { selectedStage1Format = it }
                    )
                } else {
                    DropdownSelector(
                        label = "Group Stage Format",
                        options = groupStageOptions,
                        selectedOption = selectedStage1Format,
                        onOptionSelected = { selectedStage1Format = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownSelector(
                        label = "Final Stage Format",
                        options = finalStageOptions,
                        selectedOption = selectedStage2Format,
                        onOptionSelected = { selectedStage2Format = it }
                    )
                }
            }

            ConfigSection(title = "Battle Rules") {
                DropdownSelector(
                    label = "Battle Type",
                    options = battleTypeOptions,
                    selectedOption = selectedBattleType,
                    onOptionSelected = { selectedBattleType = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    label = "Scoring System",
                    options = scoringOptions,
                    selectedOption = selectedScoringOption,
                    onOptionSelected = { selectedScoringOption = it }
                )
            }

            ConfigSection(title = "Ranking & Tie Breakers") {
                DropdownSelector(
                    label = "Primary Ranking System",
                    options = rankingSystems,
                    selectedOption = selectedRankingSystem,
                    onOptionSelected = { selectedRankingSystem = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Tie Breaker Priority", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                DropdownSelector(label = "1. First Priority", options = tieBreakerOptions, selectedOption = tieBreaker1, onOptionSelected = { tieBreaker1 = it })
                Spacer(modifier = Modifier.height(4.dp))
                DropdownSelector(label = "2. Second Priority", options = tieBreakerOptions, selectedOption = tieBreaker2, onOptionSelected = { tieBreaker2 = it })
                Spacer(modifier = Modifier.height(4.dp))
                DropdownSelector(label = "3. Third Priority", options = tieBreakerOptions, selectedOption = tieBreaker3, onOptionSelected = { tieBreaker3 = it })
            }

            ConfigSection(title = "Registration") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow Self-Registration")
                    Switch(checked = allowSelfRegistration, onCheckedChange = { allowSelfRegistration = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Public Visibility")
                    Switch(checked = publicVisibility, onCheckedChange = { publicVisibility = it })
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        if (tournamentName.isBlank()) {
                            errorMessage = "Tournament name cannot be empty."
                            isLoading = false
                            return@launch
                        }
                        if(tournamentStartDate.isNullOrBlank()){
                            errorMessage = "Please select a start date."
                            isLoading = false
                            return@launch
                        }

                        val tournament = Tournament(
                            uid = "",
                            tournamentOwner = auth.currentUser?.uid ?: "",
                            name = tournamentName,
                            startDate = tournamentStartDate ?: "",

                            // New Structure Fields
                            stageCount = stageCount,
                            stage1Format = selectedStage1Format,
                            stage2Format = if (stageCount == 2) selectedStage2Format else "",

                            // Battle Rules
                            battleType = selectedBattleType,
                            scoringSystem = selectedScoringOption,

                            // Tie Breakers
                            rankingSystem = selectedRankingSystem,
                            tieBreaker1 = tieBreaker1,
                            tieBreaker2 = tieBreaker2,
                            tieBreaker3 = tieBreaker3,

                            allowSelfRegister = allowSelfRegistration,
                            publicVisibility = publicVisibility
                        )

                        try {
                            val documentRef = db.collection("tournaments").add(tournament).await()

                            // Link to User Profile
                            db.collection("users")
                                .document(auth.currentUser?.uid ?: "")
                                .update("pastTournaments", com.google.firebase.firestore.FieldValue.arrayUnion(documentRef.id))
                                .await()

                            // Update tournament UID
                            documentRef.update("uid", documentRef.id).await()

                            onCreateTournamentClick(documentRef.id)

                        } catch (e: Exception) {
                            errorMessage = "Error creating tournament: ${e.message}"
                            Log.e("CreateTournament", "Error", e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Tournament")
                }
            }
        }
    }

    // Date Picker Dialog Logic
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        tournamentStartDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { Button(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// --- Helper Composables ---

@Composable
fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
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