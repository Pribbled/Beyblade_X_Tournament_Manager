package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.UserProfile
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
                val db = FirebaseModule.db
                val profiles = mutableListOf<UserProfile>()

                playerIds.forEach { uid ->
                    val doc = db.collection("users").document(uid).get().await()
                    val profile = doc.toObject(UserProfile::class.java)
                    if (profile != null) {
                        profiles.add(profile)
                    }
                }

                _participants.value = profiles
            } catch (e: Exception) {
                println("Error fetching participants: ${e.message}")
            }
        }
    }
}