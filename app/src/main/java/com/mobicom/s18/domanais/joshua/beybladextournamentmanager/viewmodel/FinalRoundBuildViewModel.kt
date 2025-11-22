package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.BeybladeBuild
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Final Round Build Submission Screen.
 * Manages submission of Beyblade builds for players in the final round.
 */
class FinalRoundBuildViewModel(
    private val matchRepository: MatchRepository = MatchRepository()
) : ViewModel() {

    // Submission state
    private val _submissionState = MutableStateFlow<BuildSubmissionState>(BuildSubmissionState.Idle)
    val submissionState: StateFlow<BuildSubmissionState> = _submissionState.asStateFlow()

    // List of final builds for the tournament
    private val _finalBuilds = MutableStateFlow<List<BeybladeBuild>>(emptyList())
    val finalBuilds: StateFlow<List<BeybladeBuild>> = _finalBuilds.asStateFlow()

    /**
     * Submit a single Beyblade build for a player.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @param playerName The name of the player
     * @param layer The Layer component
     * @param disc The Disc component
     * @param bit The Bit component
     */
    fun submitBuild(
        tournamentId: String,
        playerId: String,
        playerName: String,
        layer: String,
        disc: String,
        bit: String
    ) {
        viewModelScope.launch {
            _submissionState.value = BuildSubmissionState.Loading

            // Get current user ID as submitter
            val submittedBy = FirebaseModule.auth.currentUser?.uid ?: "unknown"

            val result = matchRepository.submitFinalBuild(
                tournamentId = tournamentId,
                playerId = playerId,
                playerName = playerName,
                layer = layer,
                disc = disc,
                bit = bit,
                submittedBy = submittedBy
            )

            result.fold(
                onSuccess = { buildId ->
                    _submissionState.value = BuildSubmissionState.Success(
                        "Build submitted successfully for $playerName!"
                    )
                },
                onFailure = { exception ->
                    _submissionState.value = BuildSubmissionState.Error(
                        exception.message ?: "Failed to submit build"
                    )
                }
            )
        }
    }

    /**
     * Submit multiple builds for a single player (for tournaments with multiple builds per player).
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @param playerName The name of the player
     * @param builds List of (layer, disc, bit) triples
     */
    fun submitMultipleBuilds(
        tournamentId: String,
        playerId: String,
        playerName: String,
        builds: List<Triple<String, String, String>>
    ) {
        viewModelScope.launch {
            _submissionState.value = BuildSubmissionState.Loading

            val submittedBy = FirebaseModule.auth.currentUser?.uid ?: "unknown"
            var successCount = 0
            var errorMessage: String? = null

            // Submit each build sequentially
            for ((index, build) in builds.withIndex()) {
                val (layer, disc, bit) = build

                // Create unique player ID for each build (e.g., player_001_build1)
                val buildPlayerId = "${playerId}_build${index + 1}"

                val result = matchRepository.submitFinalBuild(
                    tournamentId = tournamentId,
                    playerId = buildPlayerId,
                    playerName = "$playerName (Build ${index + 1})",
                    layer = layer,
                    disc = disc,
                    bit = bit,
                    submittedBy = submittedBy
                )

                result.fold(
                    onSuccess = { successCount++ },
                    onFailure = { exception ->
                        if (errorMessage == null) {
                            errorMessage = exception.message
                        }
                    }
                )
            }

            // Set final state based on results
            _submissionState.value = when {
                successCount == builds.size -> BuildSubmissionState.Success(
                    "All $successCount builds submitted successfully for $playerName!"
                )
                successCount > 0 -> BuildSubmissionState.Success(
                    "$successCount of ${builds.size} builds submitted. Some failed: $errorMessage"
                )
                else -> BuildSubmissionState.Error(
                    errorMessage ?: "Failed to submit builds"
                )
            }
        }
    }

    /**
     * Load all final builds for a tournament with real-time updates.
     *
     * @param tournamentId The ID of the tournament
     */
    fun loadFinalBuilds(tournamentId: String) {
        viewModelScope.launch {
            try {
                matchRepository.listenToFinalBuilds(tournamentId).collect { builds ->
                    _finalBuilds.value = builds
                }
            } catch (e: Exception) {
                // Handle error silently or emit to a separate error state
                _finalBuilds.value = emptyList()
            }
        }
    }

    /**
     * Get a player's build (one-time fetch).
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     */
    fun getPlayerBuild(tournamentId: String, playerId: String) {
        viewModelScope.launch {
            val result = matchRepository.getFinalBuild(tournamentId, playerId)

            result.fold(
                onSuccess = { build ->
                    // Handle success if needed
                },
                onFailure = { exception ->
                    // Handle error if needed
                }
            )
        }
    }

    /**
     * Reset submission state back to Idle.
     * Call this after showing success/error message.
     */
    fun resetSubmissionState() {
        _submissionState.value = BuildSubmissionState.Idle
    }

    /**
     * Delete a player's build.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     */
    fun deleteBuild(tournamentId: String, playerId: String) {
        viewModelScope.launch {
            _submissionState.value = BuildSubmissionState.Loading

            val result = matchRepository.deleteFinalBuild(tournamentId, playerId)

            result.fold(
                onSuccess = {
                    _submissionState.value = BuildSubmissionState.Success("Build deleted successfully!")
                },
                onFailure = { exception ->
                    _submissionState.value = BuildSubmissionState.Error(
                        exception.message ?: "Failed to delete build"
                    )
                }
            )
        }
    }
}

/**
 * UI state for build submission operations.
 */
sealed class BuildSubmissionState {
    object Idle : BuildSubmissionState()
    object Loading : BuildSubmissionState()
    data class Success(val message: String) : BuildSubmissionState()
    data class Error(val message: String) : BuildSubmissionState()
}

