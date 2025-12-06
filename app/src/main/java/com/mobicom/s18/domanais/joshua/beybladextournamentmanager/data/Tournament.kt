package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data

/**
 * Data class representing a Tournament with advanced configuration options.
 */
data class Tournament(
    val uid: String = "",
    val tournamentOwner: String = "",
    val name: String = "",
    val tournamentFormat: String = "",

    // --- Structure Settings ---
    val stageCount: Int = 1, // 1 or 2
    val stage1Format: String = "", // e.g., "Round Robin", "Swiss System"
    val stage2Format: String = "", // e.g., "Single Elimination" (Only used if stageCount == 2)
    val roundsToPlay: Int = 1,
    val topXQualifiers: Int = 4,

    // --- Battle Rules ---
    val battleType: String = "3on3 Deck",
    val scoringSystem: String = "standard",
    val scoringValueExtreme: Int = 3,
    val scoringValueBurst: Int = 2,
    val scoringValueOver: Int = 1,
    val scoringValueSpin: Int = 1,
    val lockMatchScoring: Boolean = true,

    // --- Ranking & Tie Breakers ---
    val rankingSystem: String = "Match Wins", // Default ranking metric
    val tieBreaker1: String = "", // Priority 1
    val tieBreaker2: String = "", // Priority 2
    val tieBreaker3: String = "", // Priority 3

    // --- General ---
    val tieBreakRules: String = "", // Keeping for manual text notes if needed
    val allowSelfRegister: Boolean = false,
    val publicVisibility: Boolean = false,
    val tournamentPlayers: List<String> = emptyList(),
    val tournamentJudges: List<String> = emptyList(),
    val tournamentJudgesAlsoPlay: Boolean = false,
    val tournamentCode: String = generateTournamentCode(),
    val status: String = "upcoming",
    val startDate: String = "",
    val currentStage: Int = 1, // 1 = Stage 1 (Group/Main), 2 = Stage 2 (Finals)
    val isStage1Complete: Boolean = false
)

fun generateTournamentCode(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6)
        .map { characters.random() }
        .joinToString("")
}