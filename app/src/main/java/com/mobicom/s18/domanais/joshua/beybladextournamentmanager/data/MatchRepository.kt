package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Match-related Firestore operations.
 * Provides methods to read, write, and listen to match data.
 */
class MatchRepository(private val db: FirebaseFirestore = FirebaseModule.db) {

    companion object {
        private const val TOURNAMENTS_COLLECTION = "tournaments"
        private const val MATCHES_COLLECTION = "matches"
    }

    /**
     * Fetch a single match document by ID (one-time read)
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match to fetch
     * @return Result containing the Match object or an error
     */
    suspend fun getMatch(tournamentId: String, matchId: String): Result<Match?> {
        return try {
            val document = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .get()
                .await()

            val match = document.toObject(Match::class.java)
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen for real-time updates to a single match document
     * Returns a Flow that emits Match objects whenever the document changes
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match to listen to
     * @return Flow of Result<Match?> that emits on every update
     */
    fun listenToMatch(tournamentId: String, matchId: String): Flow<Result<Match?>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Send error through the flow
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val match = snapshot.toObject(Match::class.java)
                        trySend(Result.success(match))
                    } else {
                        trySend(Result.success(null))
                    }
                }

            // Wait for the flow to be closed
            awaitClose {
                listenerRegistration?.remove()
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            awaitClose {
                listenerRegistration?.remove()
            }
        }
    }

    /**
     * Update match score and player stats
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match
     * @param updates Map of field names to new values
     * @return Result indicating success or failure
     */
    suspend fun updateMatch(
        tournamentId: String,
        matchId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a new round result to the match
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match
     * @param roundDetail The round details to add
     * @return Result indicating success or failure
     */
    suspend fun addRoundResult(
        tournamentId: String,
        matchId: String,
        roundDetail: RoundDetail
    ): Result<Unit> {
        return try {
            // Get current match to append to rounds array
            val matchDoc = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)

            val currentMatch = matchDoc.get().await().toObject(Match::class.java)
            val updatedRounds = currentMatch?.rounds.orEmpty() + roundDetail

            matchDoc.update(
                mapOf(
                    "rounds" to updatedRounds,
                    "currentRound" to roundDetail.roundNumber + 1,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update match status (e.g., from "upcoming" to "in_progress")
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match
     * @param status The new status
     * @return Result indicating success or failure
     */
    suspend fun updateMatchStatus(
        tournamentId: String,
        matchId: String,
        status: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to status,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            if (status == "in_progress") {
                updates.plus("startTime" to com.google.firebase.Timestamp.now())
            } else if (status == "completed") {
                updates.plus("endTime" to com.google.firebase.Timestamp.now())
            }

            db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all matches for a tournament
     *
     * @param tournamentId The ID of the tournament
     * @return Result containing list of matches
     */
    suspend fun getMatchesForTournament(tournamentId: String): Result<List<Match>> {
        return try {
            val querySnapshot = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .get()
                .await()

            val matches = querySnapshot.documents.mapNotNull {
                it.toObject(Match::class.java)
            }

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

