package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.BuildSubmissionState
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.FinalRoundBuildViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalRoundBuildSubmissionScreen(
    tournamentId: String = "tournament_001",
    playerId: String = "player_001",
    playerName: String = "Tyson Granger",
    viewModel: FinalRoundBuildViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    // State for player's builds (3 builds per player)
    var playerBuilds by remember { mutableStateOf(generateInitialBuilds()) }

    // Observe submission state
    val submissionState by viewModel.submissionState.collectAsState()

    // Show toast/snackbar for submission state
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle submission state changes
    LaunchedEffect(submissionState) {
        when (submissionState) {
            is BuildSubmissionState.Success -> {
                snackbarHostState.showSnackbar(
                    message = (submissionState as BuildSubmissionState.Success).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetSubmissionState()
                // Navigate back on success
                onSubmitSuccess()
            }
            is BuildSubmissionState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (submissionState as BuildSubmissionState.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetSubmissionState()
            }
            else -> { /* Do nothing */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Final Round Build Submission") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player name header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Three build cards
            playerBuilds.forEachIndexed { index, build ->
                BuildConfigCard(
                    buildNumber = index + 1,
                    buildConfig = build,
                    onBuildChange = { updatedBuild ->
                        playerBuilds = playerBuilds.toMutableList().apply {
                            set(index, updatedBuild)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    // Convert BuildConfig list to Triple list for ViewModel
                    val buildsToSubmit = playerBuilds.map { build ->
                        Triple(build.layer, build.disc, build.bit)
                    }

                    // Call ViewModel to submit builds
                    viewModel.submitMultipleBuilds(
                        tournamentId = tournamentId,
                        playerId = playerId,
                        playerName = playerName,
                        builds = buildsToSubmit
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = submissionState !is BuildSubmissionState.Loading
            ) {
                if (submissionState is BuildSubmissionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Builds")
                }
            }
        }
    }
}

@Composable
fun BuildConfigCard(
    buildNumber: Int,
    buildConfig: BuildConfig,
    onBuildChange: (BuildConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Build $buildNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            PartDropdown(
                label = "Layer",
                options = layerOptions,
                selectedOption = buildConfig.layer,
                onOptionSelected = { layer ->
                    onBuildChange(buildConfig.copy(layer = layer))
                }
            )

            PartDropdown(
                label = "Disc",
                options = discOptions,
                selectedOption = buildConfig.disc,
                onOptionSelected = { disc ->
                    onBuildChange(buildConfig.copy(disc = disc))
                }
            )

            PartDropdown(
                label = "Bit/Driver",
                options = bitOptions,
                selectedOption = buildConfig.bit,
                onOptionSelected = { bit ->
                    onBuildChange(buildConfig.copy(bit = bit))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
}

// Data class for build configuration
data class BuildConfig(
    val buildId: String,
    val layer: String,
    val disc: String,
    val bit: String
)

// Dummy data for 3 builds
private fun generateInitialBuilds(): List<BuildConfig> {
    return listOf(
        BuildConfig("b1", "Dragoon", "Wing", "Slash"),
        BuildConfig("b2", "Dranzer", "Weight", "Spiral"),
        BuildConfig("b3", "Driger", "Attack", "Claw")
    )
}

// Sample part options
val layerOptions = listOf(
    "Dragoon", "Dranzer", "Draciel", "Driger", "Galaxy", "Cosmic", "Union", "Prominence", "Rage"
)

val discOptions = listOf(
    "Wing", "Weight", "Defense", "Balance", "Attack", "Stamina", "Force", "Nexus", "Over"
)

val bitOptions = listOf(
    "Slash", "Spiral", "Shield", "Claw", "Destroy", "Ultimate", "Metal", "Evolution", "Drift"
)

@Preview(showBackground = true)
@Composable
fun FinalRoundBuildSubmissionScreenPreview() {
    BeybladeXTournamentManagerTheme {
        FinalRoundBuildSubmissionScreen()
    }
}