package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.RoundDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing match details and real-time updates
 */
class MatchDetailsViewModel(
    private val repository: MatchRepository = MatchRepository()
) : ViewModel() {

    // UI State
    private val _matchState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val matchState: StateFlow<MatchUiState> = _matchState.asStateFlow()

    // Timer state
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    /**
     * Load match details (one-time fetch)
     */
    fun loadMatch(tournamentId: String, matchId: String) {
        viewModelScope.launch {
            _matchState.value = MatchUiState.Loading

            val result = repository.getMatch(tournamentId, matchId)

            result.fold(
                onSuccess = { match ->
                    if (match != null) {
                        _matchState.value = MatchUiState.Success(match)
                        _elapsedTime.value = match.elapsedSeconds
                    } else {
                        _matchState.value = MatchUiState.Error("Match not found")
                    }
                },
                onFailure = { exception ->
                    _matchState.value = MatchUiState.Error(
                        exception.message ?: "Failed to load match"
                    )
                }
            )
        }
    }

    /**
     * Listen to real-time match updates
     */
    fun listenToMatch(tournamentId: String, matchId: String) {
        viewModelScope.launch {
            _matchState.value = MatchUiState.Loading

            repository.listenToMatch(tournamentId, matchId).collect { result ->
                result.fold(
                    onSuccess = { match ->
                        if (match != null) {
                            _matchState.value = MatchUiState.Success(match)
                            _elapsedTime.value = match.elapsedSeconds
                        } else {
                            _matchState.value = MatchUiState.Error("Match not found")
                        }
                    },
                    onFailure = { exception ->
                        _matchState.value = MatchUiState.Error(
                            exception.message ?: "Failed to listen to match updates"
                        )
                    }
                )
            }
        }
    }

    /**
     * Update match scores
     */
    fun updateScore(
        tournamentId: String,
        matchId: String,
        player1Wins: Int,
        player1Losses: Int,
        player2Wins: Int,
        player2Losses: Int
    ) {
        viewModelScope.launch {
            val updates = mapOf(
                "player1Wins" to player1Wins,
                "player1Losses" to player1Losses,
                "player2Wins" to player2Wins,
                "player2Losses" to player2Losses,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            repository.updateMatch(tournamentId, matchId, updates)
        }
    }

    /**
     * Add a round result
     */
    fun addRoundResult(
        tournamentId: String,
        matchId: String,
        roundDetail: RoundDetail
    ) {
        viewModelScope.launch {
            repository.addRoundResult(tournamentId, matchId, roundDetail)
        }
    }

    /**
     * Update match status
     */
    fun updateMatchStatus(tournamentId: String, matchId: String, status: String) {
        viewModelScope.launch {
            repository.updateMatchStatus(tournamentId, matchId, status)
        }
    }

    /**
     * Start the timer
     */
    fun startTimer() {
        _isTimerRunning.value = true
    }

    /**
     * Stop the timer
     */
    fun stopTimer() {
        _isTimerRunning.value = false
    }

    /**
     * Update elapsed time (called from UI)
     */
    fun updateElapsedTime(seconds: Long, tournamentId: String, matchId: String) {
        _elapsedTime.value = seconds

        // Optionally save to Firestore periodically
        viewModelScope.launch {
            repository.updateMatch(
                tournamentId,
                matchId,
                mapOf("elapsedSeconds" to seconds)
            )
        }
    }

    /**
     * Reset timer
     */
    fun resetTimer() {
        _elapsedTime.value = 0L
        _isTimerRunning.value = false
    }
}

/**
 * UI State for match details
 */
sealed class MatchUiState {
    object Loading : MatchUiState()
    data class Success(val match: Match) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

