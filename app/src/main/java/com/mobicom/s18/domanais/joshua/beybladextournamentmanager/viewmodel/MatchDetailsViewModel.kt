package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.RoundDetail
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing match details and real-time updates
 */
class MatchDetailsViewModel(
    private val repository: MatchRepository = MatchRepository(),
    private val db: FirebaseFirestore = Firebase.firestore
) : ViewModel() {

    // UI State
    private val _matchState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val matchState: StateFlow<MatchUiState> = _matchState.asStateFlow()

    // Timer state
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _tournamentState = MutableStateFlow<Tournament?>(null)
    val tournamentState: StateFlow<Tournament?> = _tournamentState.asStateFlow()


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
     * Listen to real-time match updates using the new getMatchDetails function.
     * This provides live updates whenever the match document changes in Firestore.
     */
    fun listenToMatch(tournamentId: String, matchId: String) {
        viewModelScope.launch {
            _matchState.value = MatchUiState.Loading
            loadTournament(tournamentId)

            try {
                repository.getMatchDetails(tournamentId, matchId).collect { match ->
                    if (match != null) {
                        _matchState.value = MatchUiState.Success(match)
                        _elapsedTime.value = match.elapsedSeconds
                    } else {
                        _matchState.value = MatchUiState.Error("Match not found")
                    }
                }
            } catch (e: Exception) {
                _matchState.value = MatchUiState.Error(
                    e.message ?: "Failed to listen to match updates"
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

    /**
     * Submit match result from the recording screen.
     * Updates scores in Firestore with automatic winner detection.
     *
     * @param tournamentId The tournament ID
     * @param matchId The match ID
     * @param player1Score Final score for player 1
     * @param player2Score Final score for player 2
     * @return Result indicating success or failure
     */
    suspend fun submitMatchResult(
        tournamentId: String,
        matchId: String,
        player1Score: Int,
        player2Score: Int
    ): Result<Unit> {
        return repository.updateMatchScore(
            tournamentId = tournamentId,
            matchId = matchId,
            player1Score = player1Score,
            player2Score = player2Score
        )
    }

    fun overrideScore(tournamentId: String, matchId: String, player1Score: Int, player2Score: Int) {
        viewModelScope.launch {
            repository.updateMatchScore(
                tournamentId = tournamentId,
                matchId = matchId,
                player1Score = player1Score,
                player2Score = player2Score
            )
        }
    }

    private fun loadTournament(tournamentId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("tournaments").document(tournamentId).get().await()
                _tournamentState.value = snapshot.toObject(Tournament::class.java)
            } catch (_: Exception) {
                _tournamentState.value = null
            }
        }
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
