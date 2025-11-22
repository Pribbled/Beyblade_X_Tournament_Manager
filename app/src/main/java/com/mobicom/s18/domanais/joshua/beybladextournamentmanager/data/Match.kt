package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data

import com.google.firebase.Timestamp

/**
 * Data class representing a match in a tournament.
 * Stored in Firestore under: tournaments/{tournamentId}/matches/{matchId}
 */
data class Match(
    val matchId: String = "",
    val tournamentId: String = "",
    val matchNumber: Int = 0,
    val round: String = "", // e.g., "Qualifier", "Semi-Finals", "Finals"
    val format: String = "", // e.g., "First to Four", "Best of 5"

    // Players
    val player1Id: String = "",
    val player1Name: String = "",
    val player1Score: Int = 0,
    val player1Wins: Int = 0,
    val player1Losses: Int = 0,

    val player2Id: String = "",
    val player2Name: String = "",
    val player2Score: Int = 0,
    val player2Wins: Int = 0,
    val player2Losses: Int = 0,

    // Match state
    val status: String = "scheduled", // "scheduled", "in_progress", "completed"
    val currentRound: Int = 1,
    val winnerId: String? = null,
    val winnerName: String? = null,

    // Timing
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val elapsedSeconds: Long = 0L,

    // Round details
    val rounds: List<RoundDetail> = emptyList(),

    // Metadata
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Details for each round within a match
 */
data class RoundDetail(
    val roundNumber: Int = 0,
    val winnerId: String = "",
    val winnerName: String = "",
    val videoLink: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

