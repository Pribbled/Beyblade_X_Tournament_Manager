package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data

/**
 * Data class representing a user's profile stored in Firestore.
 *

 */
data class Tournament(
    val uid: String = "",
    val tournamentOwner : String = "",
    val name: String = "",
    val tournamentFormat: String = "",
    val scoringSystem: String = "",
    val tieBreakRules: String = "",
    val allowSelfRegister: Boolean = false,
    val publicVisibility: Boolean = false,
    val tournamentPlayers : List<String> = emptyList(),
    val tournamentJudges : List<String> = emptyList(),
    val tournamentCode : String = generateTournamentCode()
)

fun generateTournamentCode(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6)
        .map { characters.random() }
        .joinToString("")
}