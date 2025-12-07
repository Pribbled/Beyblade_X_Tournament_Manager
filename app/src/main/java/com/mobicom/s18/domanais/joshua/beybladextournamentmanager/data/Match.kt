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
    val round: String = "",
    val format: String = "",

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
    val status: String = "scheduled",
    val currentRound: Int = 1,
    val winnerId: String? = null,
    val winnerName: String? = null,

    // Timing
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val elapsedSeconds: Long = 0L,

    // Round details
    val rounds: List<RoundDetail> = emptyList(),

    // Media
    val videoUrl: String? = null,

    // Metadata
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),

    // Metrics
    val extremeFinishes: Int = 0,
    val burstFinishes: Int = 0,
    val overFinishes: Int = 0,
    val spinFinishes: Int = 0
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

/**
 * Data class representing a Beyblade build for the final round.
 * Stored in Firestore under: tournaments/{tournamentId}/final_builds/{buildId}
 */
data class BeybladeBuild(
    val buildId: String = "",
    val tournamentId: String = "",
    val playerId: String = "",
    val playerName: String = "",

    // Beyblade parts
    val layer: String = "",
    val disc: String = "",
    val bit: String = "",

    // Metadata
    val submittedBy: String = "",
    val submittedAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

fun BeybladeBuild.belongsToPlayer(playerId: String): Boolean {
    if (playerId.isBlank()) return false
    if (this.playerId == playerId) return true
    return this.playerId.startsWith("${playerId}_")
}
