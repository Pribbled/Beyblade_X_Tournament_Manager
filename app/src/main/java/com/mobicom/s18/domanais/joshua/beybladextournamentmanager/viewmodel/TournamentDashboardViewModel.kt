package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Tournament Dashboard Screen.
 * Manages the state of matches for a specific tournament with real-time updates from Firestore.
 */
class TournamentDashboardViewModel(
    private val matchRepository: MatchRepository = MatchRepository()
) : ViewModel() {

    // Private mutable state for matches
    private val _matches = MutableStateFlow<List<Match>>(emptyList())

    /**
     * Public immutable state flow of matches.
     * Emits the list of matches for the current tournament, updated in real-time.
     */
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    // Private mutable state for loading indicator
    private val _isLoading = MutableStateFlow(false)

    /**
     * Public state flow indicating whether data is being loaded.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Private mutable state for errors
    private val _error = MutableStateFlow<String?>(null)

    /**
     * Public state flow for error messages.
     */
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load matches for a specific tournament with real-time updates.
     * This function sets up a Firestore listener that will automatically
     * update the matches StateFlow whenever the data changes in Firestore.
     *
     * @param tournamentId The ID of the tournament to load matches for
     */
    fun loadMatches(tournamentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Collect from the Flow returned by repository
                // This will automatically update _matches whenever Firestore data changes
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
     * Get completed matches from the current list.
     * @return List of matches with status "completed"
     */
    fun getCompletedMatches(): List<Match> {
        return _matches.value.filter { it.status == "completed" }
    }

    /**
     * Get scheduled/in-progress matches from the current list.
     * @return List of matches with status "scheduled" or "in_progress"
     */
    fun getUpcomingMatches(): List<Match> {
        return _matches.value.filter {
            it.status == "scheduled" || it.status == "in_progress"
        }
    }
}

