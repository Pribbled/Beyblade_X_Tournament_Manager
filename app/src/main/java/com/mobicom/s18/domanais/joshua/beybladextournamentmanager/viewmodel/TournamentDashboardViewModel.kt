package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.BeybladeBuild
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the Tournament Dashboard Screen.
 * Manages matches and participants.
 */
class TournamentDashboardViewModel(
    private val matchRepository: MatchRepository = MatchRepository()
) : ViewModel() {

    // --- Matches State ---
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    // --- Participants State ---
    private val _participants = MutableStateFlow<List<UserProfile>>(emptyList())
    val participants: StateFlow<List<UserProfile>> = _participants.asStateFlow()

    // --- Final Builds State ---
    private val _finalBuilds = MutableStateFlow<List<BeybladeBuild>>(emptyList())
    val finalBuilds: StateFlow<List<BeybladeBuild>> = _finalBuilds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load matches for a specific tournament with real-time updates.
     */
    fun loadMatches(tournamentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                matchRepository.getMatches(tournamentId).collect { matchList ->
                    _matches.value = matchList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load matches: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load participant details given a list of UIDs.
     * This fetches the UserProfile for each ID in the tournamentPlayers list.
     */
    fun loadParticipants(playerIds: List<String>) {
        if (playerIds.isEmpty()) {
            _participants.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                Log.d("DashboardVM", "Fetching profiles for IDs: $playerIds")
                val db = FirebaseModule.db

                // Create a list of Task<DocumentSnapshot>
                val tasks = playerIds.map { uid ->
                    db.collection("users").document(uid).get()
                }

                // Wait for ALL tasks to complete in parallel
                val snapshots = Tasks.whenAllSuccess<com.google.firebase.firestore.DocumentSnapshot>(tasks).await()

                // Convert snapshots to UserProfile objects
                val profiles = snapshots.mapNotNull { doc ->
                    try {
                        doc.toObject(UserProfile::class.java)
                    } catch (e: Exception) {
                        Log.e("DashboardVM", "Error parsing profile for ${doc.id}", e)
                        null
                    }
                }

                Log.d("DashboardVM", "Successfully loaded ${profiles.size} profiles")
                _participants.value = profiles
            } catch (e: Exception) {
                Log.e("DashboardVM", "Error fetching participants", e)
            }
        }
    }

    /**
     * Observe final builds for a specific tournament.
     */
    fun observeFinalBuilds(tournamentId: String) {
        viewModelScope.launch {
            matchRepository.listenToFinalBuilds(tournamentId).collect { builds ->
                _finalBuilds.value = builds
            }
        }
    }

    fun generateMatches(tournament: Tournament, advanceToFinals: Boolean = false) {
        val currentParticipants = _participants.value
        if (currentParticipants.isEmpty()) {
            _error.value = "No participants loaded. Cannot generate matches."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val shouldAdvanceToFinals = advanceToFinals && tournament.stageCount == 2 && tournament.currentStage == 1

            if (shouldAdvanceToFinals) {
                val db = FirebaseModule.db
                db.collection("tournaments").document(tournament.uid)
                    .update("currentStage", 2)
                    .await()
                val updatedTournament = tournament.copy(currentStage = 2)
                matchRepository.generateMatchesForTournament(updatedTournament, currentParticipants)
            } else {
                matchRepository.generateMatchesForTournament(tournament, currentParticipants)
            }

            _isLoading.value = false
        }
    }
}