package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data

/**
 * Data class representing a user's profile stored in Firestore.
 *
 * @property uid The user's unique ID from Firebase Authentication. This will be the document ID.
 * @property email The user's email address.
 * @property bladerName The user's in-game name or username.
 * @property birthday The user's date of birth.
 * @property contactInfo The user's contact number.
 * @property rank A default rank given to new users.
 * @property xp Experience points, starting at 0.
 * @property pastTournaments A list of tournament IDs the user has participated in (starts empty).
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val bladerName: String = "",
    val birthday: String = "",
    val contactInfo: String = "",
    val rank: String = "Newbie Blader",
    val xp: Int = 0,
    val pastTournaments: List<String> = emptyList()
)