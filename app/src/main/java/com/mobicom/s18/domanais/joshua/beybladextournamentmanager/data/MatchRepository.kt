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
        private const val FINAL_BUILDS_COLLECTION = "final_builds"
    }

    /**
     * Get all matches for a tournament with real-time updates.
     * Returns a Flow that emits the updated list whenever matches change in Firestore.
     * The list is automatically sorted by matchNumber for consistent display.
     *
     * @param tournamentId The ID of the tournament
     * @return Flow emitting lists of Match objects, sorted by matchNumber
     */
    fun getMatches(tournamentId: String): Flow<List<Match>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Log error but don't close the flow - keep listening
                        println("Error listening to matches: ${error.message}")
                        trySend(emptyList()) // Send empty list on error
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // Map documents to Match objects and sort by matchNumber
                        val matches = snapshot.documents
                            .mapNotNull { it.toObject(Match::class.java) }
                            .sortedBy { it.matchNumber }

                        trySend(matches)
                    } else {
                        trySend(emptyList())
                    }
                }

            // Wait for the flow to be closed
            awaitClose {
                listenerRegistration?.remove()
            }
        } catch (e: Exception) {
            trySend(emptyList())
            awaitClose {
                listenerRegistration?.remove()
            }
        }
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
     * Get match details with real-time updates.
     * Returns a Flow that emits Match objects whenever the document changes in Firestore.
     *
     * This is the primary function for the Match Details screen to get live updates
     * when scores, status, or other fields are modified.
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match to fetch
     * @return Flow emitting Match objects on every update, or null if not found
     */
    fun getMatchDetails(tournamentId: String, matchId: String): Flow<Match?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error listening to match: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val match = snapshot.toObject(Match::class.java)
                        trySend(match)
                    } else {
                        trySend(null)
                    }
                }

            awaitClose {
                listenerRegistration?.remove()
            }
        } catch (e: Exception) {
            trySend(null)
            awaitClose {
                listenerRegistration?.remove()
            }
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
     * Update match scores with automatic winner detection and status update.
     *
     * This function updates the player scores and automatically:
     * - Determines the winner if a player reaches the winning threshold (4 points)
     * - Updates match status to "completed" when there's a winner
     * - Sets winnerId and winnerName fields
     * - Updates both score and wins fields (UI displays wins)
     *
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match
     * @param player1Score New score for player 1
     * @param player2Score New score for player 2
     * @return Result indicating success or failure
     */
    suspend fun updateMatchScore(
        tournamentId: String,
        matchId: String,
        player1Score: Int,
        player2Score: Int
    ): Result<Unit> {
        return try {
            // First, get the current match to access player IDs and names
            val matchDoc = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(MATCHES_COLLECTION)
                .document(matchId)
                .get()
                .await()

            val currentMatch = matchDoc.toObject(Match::class.java)
                ?: return Result.failure(Exception("Match not found"))

            // Determine if there's a winner (first to 4 points)
            val winningScore = 4
            val hasWinner = player1Score >= winningScore || player2Score >= winningScore

            val updates = mutableMapOf<String, Any>(
                "player1Score" to player1Score,
                "player2Score" to player2Score,
                "player1Wins" to player1Score,  // UI displays wins field
                "player2Wins" to player2Score,  // UI displays wins field
                "player1Losses" to player2Score,  // Losses = opponent's score
                "player2Losses" to player1Score,  // Losses = opponent's score
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            // If there's a winner, update status and winner fields
            if (hasWinner) {
                val isPlayer1Winner = player1Score > player2Score

                updates["status"] = "completed"
                updates["winnerId"] = if (isPlayer1Winner) currentMatch.player1Id else currentMatch.player2Id
                updates["winnerName"] = if (isPlayer1Winner) currentMatch.player1Name else currentMatch.player2Name
                updates["endTime"] = com.google.firebase.Timestamp.now()
            } else if (currentMatch.status == "scheduled") {
                // If match is starting (first score update), set to in_progress
                updates["status"] = "in_progress"
                updates["startTime"] = com.google.firebase.Timestamp.now()
            }

            // Update the match document
            matchDoc.reference.update(updates).await()

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

    /**
     * Generate bracket for a tournament with initial Round 1 matches.
     * This creates match pairings for all participants.
     *
     * TEMPORARY: Uses mock participant data since Join Tournament feature is not implemented yet.
     *
     * @param tournamentId The ID of the tournament
     * @param tournamentFormat The format of the tournament (e.g., "First to Four", "Best of 5")
     * @return Result indicating success or failure with match count
     */
    suspend fun generateBracket(
        tournamentId: String,
        tournamentFormat: String = "First to Four"
    ): Result<Int> {
        return try {
            // MOCK DATA - Replace this when Join Tournament feature is implemented
            val mockParticipants = listOf(
                "user_001" to "Alice",
                "user_002" to "Bob",
                "user_003" to "Charlie",
                "user_004" to "Diana",
                "user_005" to "Eve",
                "user_006" to "Frank",
                "user_007" to "Grace",
                "user_008" to "Henry"
            )

            // Shuffle participants for random pairings
            val shuffledParticipants = mockParticipants.shuffled()

            // Create match pairings
            val batch = db.batch()
            var matchNumber = 1
            val matches = mutableListOf<Match>()

            // Pair up players (2 at a time)
            for (i in shuffledParticipants.indices step 2) {
                if (i + 1 < shuffledParticipants.size) {
                    // Normal pairing: two players
                    val player1 = shuffledParticipants[i]
                    val player2 = shuffledParticipants[i + 1]

                    val matchId = "match_${tournamentId}_round1_$matchNumber"
                    val matchRef = db.collection(TOURNAMENTS_COLLECTION)
                        .document(tournamentId)
                        .collection(MATCHES_COLLECTION)
                        .document(matchId)

                    val match = Match(
                        matchId = matchId,
                        tournamentId = tournamentId,
                        matchNumber = matchNumber,
                        round = "Round 1",
                        format = tournamentFormat,
                        player1Id = player1.first,
                        player1Name = player1.second,
                        player1Score = 0,
                        player1Wins = 0,
                        player1Losses = 0,
                        player2Id = player2.first,
                        player2Name = player2.second,
                        player2Score = 0,
                        player2Wins = 0,
                        player2Losses = 0,
                        status = "scheduled",
                        currentRound = 1,
                        winnerId = null,
                        winnerName = null,
                        startTime = null,
                        endTime = null,
                        elapsedSeconds = 0L,
                        rounds = emptyList(),
                        createdAt = com.google.firebase.Timestamp.now(),
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    batch.set(matchRef, match)
                    matches.add(match)
                    matchNumber++
                } else {
                    // Odd number of players: last player gets a BYE
                    val player1 = shuffledParticipants[i]

                    val matchId = "match_${tournamentId}_round1_$matchNumber"
                    val matchRef = db.collection(TOURNAMENTS_COLLECTION)
                        .document(tournamentId)
                        .collection(MATCHES_COLLECTION)
                        .document(matchId)

                    val match = Match(
                        matchId = matchId,
                        tournamentId = tournamentId,
                        matchNumber = matchNumber,
                        round = "Round 1",
                        format = tournamentFormat,
                        player1Id = player1.first,
                        player1Name = player1.second,
                        player1Score = 0,
                        player1Wins = 0,
                        player1Losses = 0,
                        player2Id = "BYE",
                        player2Name = "BYE",
                        player2Score = 0,
                        player2Wins = 0,
                        player2Losses = 0,
                        status = "completed", // BYE matches are auto-completed
                        currentRound = 1,
                        winnerId = player1.first,
                        winnerName = player1.second,
                        startTime = com.google.firebase.Timestamp.now(),
                        endTime = com.google.firebase.Timestamp.now(),
                        elapsedSeconds = 0L,
                        rounds = emptyList(),
                        createdAt = com.google.firebase.Timestamp.now(),
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    batch.set(matchRef, match)
                    matches.add(match)
                    matchNumber++
                }
            }

            // Commit all matches atomically
            batch.commit().await()

            Result.success(matches.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate bracket with custom participant list.
     * Use this when the Join Tournament feature is implemented.
     *
     * @param tournamentId The ID of the tournament
     * @param participants List of pairs (userId, userName)
     * @param tournamentFormat The format of the tournament
     * @return Result indicating success or failure with match count
     */
    suspend fun generateBracketWithParticipants(
        tournamentId: String,
        participants: List<Pair<String, String>>,
        tournamentFormat: String = "First to Four"
    ): Result<Int> {
        return try {
            if (participants.isEmpty()) {
                return Result.failure(IllegalArgumentException("No participants provided"))
            }

            if (participants.size < 2) {
                return Result.failure(IllegalArgumentException("At least 2 participants required"))
            }

            // Shuffle participants for random pairings
            val shuffledParticipants = participants.shuffled()

            // Create match pairings
            val batch = db.batch()
            var matchNumber = 1
            val matches = mutableListOf<Match>()

            // Pair up players (2 at a time)
            for (i in shuffledParticipants.indices step 2) {
                if (i + 1 < shuffledParticipants.size) {
                    // Normal pairing: two players
                    val player1 = shuffledParticipants[i]
                    val player2 = shuffledParticipants[i + 1]

                    val matchId = "match_${tournamentId}_round1_$matchNumber"
                    val matchRef = db.collection(TOURNAMENTS_COLLECTION)
                        .document(tournamentId)
                        .collection(MATCHES_COLLECTION)
                        .document(matchId)

                    val match = Match(
                        matchId = matchId,
                        tournamentId = tournamentId,
                        matchNumber = matchNumber,
                        round = "Round 1",
                        format = tournamentFormat,
                        player1Id = player1.first,
                        player1Name = player1.second,
                        player1Score = 0,
                        player1Wins = 0,
                        player1Losses = 0,
                        player2Id = player2.first,
                        player2Name = player2.second,
                        player2Score = 0,
                        player2Wins = 0,
                        player2Losses = 0,
                        status = "scheduled",
                        currentRound = 1,
                        winnerId = null,
                        winnerName = null,
                        startTime = null,
                        endTime = null,
                        elapsedSeconds = 0L,
                        rounds = emptyList(),
                        createdAt = com.google.firebase.Timestamp.now(),
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    batch.set(matchRef, match)
                    matches.add(match)
                    matchNumber++
                } else {
                    // Odd number of players: last player gets a BYE
                    val player1 = shuffledParticipants[i]

                    val matchId = "match_${tournamentId}_round1_$matchNumber"
                    val matchRef = db.collection(TOURNAMENTS_COLLECTION)
                        .document(tournamentId)
                        .collection(MATCHES_COLLECTION)
                        .document(matchId)

                    val match = Match(
                        matchId = matchId,
                        tournamentId = tournamentId,
                        matchNumber = matchNumber,
                        round = "Round 1",
                        format = tournamentFormat,
                        player1Id = player1.first,
                        player1Name = player1.second,
                        player1Score = 0,
                        player1Wins = 0,
                        player1Losses = 0,
                        player2Id = "BYE",
                        player2Name = "BYE",
                        player2Score = 0,
                        player2Wins = 0,
                        player2Losses = 0,
                        status = "completed", // BYE matches are auto-completed
                        currentRound = 1,
                        winnerId = player1.first,
                        winnerName = player1.second,
                        startTime = com.google.firebase.Timestamp.now(),
                        endTime = com.google.firebase.Timestamp.now(),
                        elapsedSeconds = 0L,
                        rounds = emptyList(),
                        createdAt = com.google.firebase.Timestamp.now(),
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    batch.set(matchRef, match)
                    matches.add(match)
                    matchNumber++
                }
            }

            // Commit all matches atomically
            batch.commit().await()

            Result.success(matches.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== FINAL ROUND BUILD SUBMISSION ==========

    /**
     * Submit a Beyblade build for the final round.
     * Creates or updates a build document in the final_builds subcollection.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @param playerName The name of the player
     * @param layer The Layer component of the Beyblade
     * @param disc The Disc component of the Beyblade
     * @param bit The Bit component of the Beyblade
     * @param submittedBy The ID of the judge/host submitting the build
     * @return Result containing the buildId on success, or an error
     */
    suspend fun submitFinalBuild(
        tournamentId: String,
        playerId: String,
        playerName: String,
        layer: String,
        disc: String,
        bit: String,
        submittedBy: String
    ): Result<String> {
        return try {
            // Validate inputs
            if (layer.isBlank() || disc.isBlank() || bit.isBlank()) {
                return Result.failure(IllegalArgumentException("All Beyblade parts (Layer, Disc, Bit) must be specified"))
            }

            // Generate build ID based on player and tournament
            val buildId = "build_${tournamentId}_${playerId}"

            val buildRef = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .document(buildId)

            // Check if build already exists to determine if this is an update
            val existingBuild = buildRef.get().await()
            val isUpdate = existingBuild.exists()

            val build = BeybladeBuild(
                buildId = buildId,
                tournamentId = tournamentId,
                playerId = playerId,
                playerName = playerName,
                layer = layer,
                disc = disc,
                bit = bit,
                submittedBy = submittedBy,
                submittedAt = if (isUpdate) {
                    existingBuild.toObject(BeybladeBuild::class.java)?.submittedAt ?: com.google.firebase.Timestamp.now()
                } else {
                    com.google.firebase.Timestamp.now()
                },
                updatedAt = com.google.firebase.Timestamp.now()
            )

            // Save to Firestore
            buildRef.set(build).await()

            Result.success(buildId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a specific player's final build (one-time read).
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @return Result containing the BeybladeBuild or null if not found
     */
    suspend fun getFinalBuild(tournamentId: String, playerId: String): Result<BeybladeBuild?> {
        return try {
            val buildId = "build_${tournamentId}_${playerId}"

            val document = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .document(buildId)
                .get()
                .await()

            val build = document.toObject(BeybladeBuild::class.java)
            Result.success(build)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all final builds for a tournament (one-time read).
     *
     * @param tournamentId The ID of the tournament
     * @return Result containing a list of all BeybladeBuild objects
     */
    suspend fun getAllFinalBuilds(tournamentId: String): Result<List<BeybladeBuild>> {
        return try {
            val querySnapshot = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .get()
                .await()

            val builds = querySnapshot.documents.mapNotNull {
                it.toObject(BeybladeBuild::class.java)
            }.sortedBy { it.playerName }

            Result.success(builds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen for real-time updates to all final builds in a tournament.
     * Returns a Flow that emits the updated list whenever any build changes.
     *
     * @param tournamentId The ID of the tournament
     * @return Flow emitting lists of BeybladeBuild objects on every update
     */
    fun listenToFinalBuilds(tournamentId: String): Flow<List<BeybladeBuild>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error listening to final builds: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val builds = snapshot.documents
                            .mapNotNull { it.toObject(BeybladeBuild::class.java) }
                            .sortedBy { it.playerName }

                        trySend(builds)
                    } else {
                        trySend(emptyList())
                    }
                }

            awaitClose {
                listenerRegistration?.remove()
            }
        } catch (e: Exception) {
            trySend(emptyList())
            awaitClose {
                listenerRegistration?.remove()
            }
        }
    }

    /**
     * Listen for real-time updates to a specific player's final build.
     * Returns a Flow that emits the BeybladeBuild whenever it changes.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @return Flow emitting BeybladeBuild objects on every update, or null if not found
     */
    fun listenToPlayerFinalBuild(tournamentId: String, playerId: String): Flow<BeybladeBuild?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            val buildId = "build_${tournamentId}_${playerId}"

            listenerRegistration = db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .document(buildId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error listening to player build: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val build = snapshot.toObject(BeybladeBuild::class.java)
                        trySend(build)
                    } else {
                        trySend(null)
                    }
                }

            awaitClose {
                listenerRegistration?.remove()
            }
        } catch (e: Exception) {
            trySend(null)
            awaitClose {
                listenerRegistration?.remove()
            }
        }
    }

    /**
     * Update an existing final build.
     * Note: You can also use submitFinalBuild() which handles both create and update.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @param layer The new Layer component (optional)
     * @param disc The new Disc component (optional)
     * @param bit The new Bit component (optional)
     * @return Result indicating success or failure
     */
    suspend fun updateFinalBuild(
        tournamentId: String,
        playerId: String,
        layer: String? = null,
        disc: String? = null,
        bit: String? = null
    ): Result<Unit> {
        return try {
            val buildId = "build_${tournamentId}_${playerId}"

            val updates = mutableMapOf<String, Any>(
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            layer?.let { updates["layer"] = it }
            disc?.let { updates["disc"] = it }
            bit?.let { updates["bit"] = it }

            if (updates.size == 1) {
                return Result.failure(IllegalArgumentException("No updates provided"))
            }

            db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .document(buildId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a player's final build.
     *
     * @param tournamentId The ID of the tournament
     * @param playerId The ID of the player
     * @return Result indicating success or failure
     */
    suspend fun deleteFinalBuild(tournamentId: String, playerId: String): Result<Unit> {
        return try {
            val buildId = "build_${tournamentId}_${playerId}"

            db.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(FINAL_BUILDS_COLLECTION)
                .document(buildId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

