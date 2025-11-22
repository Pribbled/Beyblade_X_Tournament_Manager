# Match Management Backend Implementation Guide

This guide explains how to implement the **Match Management** feature for reading match details from Firestore in your Beyblade X Tournament Manager app.

## Overview

The implementation consists of:
1. **Data Model** - `Match.kt` and `RoundDetail` data classes
2. **Repository** - `MatchRepository.kt` for Firestore operations
3. **ViewModel** - `MatchDetailsViewModel.kt` for state management
4. **UI Screen** - `MatchDetailsScreen.kt` with real-time updates

## Architecture

```
UI Layer (MatchDetailsScreen)
    ↓
ViewModel (MatchDetailsViewModel)
    ↓
Repository (MatchRepository)
    ↓
Firebase Firestore
```

## 1. Data Model (`Match.kt`)

### Match Data Structure
The `Match` data class represents a match document in Firestore:

```kotlin
data class Match(
    val matchId: String,
    val tournamentId: String,
    val matchNumber: Int,
    val round: String,              // e.g., "Qualifier", "Semi-Finals"
    val format: String,             // e.g., "First to Four"
    
    // Player 1 details
    val player1Id: String,
    val player1Name: String,
    val player1Score: Int,
    val player1Wins: Int,
    val player1Losses: Int,
    
    // Player 2 details
    val player2Id: String,
    val player2Name: String,
    val player2Score: Int,
    val player2Wins: Int,
    val player2Losses: Int,
    
    // Match state
    val status: String,             // "upcoming", "in_progress", "completed"
    val currentRound: Int,
    val winnerId: String?,
    val winnerName: String?,
    
    // Timing
    val startTime: Timestamp?,
    val endTime: Timestamp?,
    val elapsedSeconds: Long,
    
    // Round history
    val rounds: List<RoundDetail>,
    
    // Metadata
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
```

### Firestore Document Path
```
tournaments/{tournamentId}/matches/{matchId}
```

## 2. Repository (`MatchRepository.kt`)

### Key Methods

#### a) **One-Time Read** - `getMatch()`
Fetches a match document once:

```kotlin
suspend fun getMatch(tournamentId: String, matchId: String): Result<Match?>
```

**Usage:**
```kotlin
val result = matchRepository.getMatch("tournament123", "match456")
result.fold(
    onSuccess = { match -> /* Handle match data */ },
    onFailure = { error -> /* Handle error */ }
)
```

#### b) **Real-Time Listener** - `listenToMatch()`
Listens for real-time updates to a match:

```kotlin
fun listenToMatch(tournamentId: String, matchId: String): Flow<Result<Match?>>
```

**Usage:**
```kotlin
matchRepository.listenToMatch("tournament123", "match456")
    .collect { result ->
        result.fold(
            onSuccess = { match -> /* Update UI with match */ },
            onFailure = { error -> /* Handle error */ }
        )
    }
```

#### c) **Update Match** - `updateMatch()`
Updates match fields:

```kotlin
suspend fun updateMatch(
    tournamentId: String,
    matchId: String,
    updates: Map<String, Any>
): Result<Unit>
```

**Usage:**
```kotlin
val updates = mapOf(
    "player1Wins" to 3,
    "player2Wins" to 1,
    "status" to "in_progress"
)
matchRepository.updateMatch("tournament123", "match456", updates)
```

#### d) **Add Round Result** - `addRoundResult()`
Adds a new round to the match history:

```kotlin
suspend fun addRoundResult(
    tournamentId: String,
    matchId: String,
    roundDetail: RoundDetail
): Result<Unit>
```

**Usage:**
```kotlin
val round = RoundDetail(
    roundNumber = 3,
    winnerId = "player1",
    winnerName = "Blader_ACE",
    videoLink = "https://example.com/video3"
)
matchRepository.addRoundResult("tournament123", "match456", round)
```

## 3. ViewModel (`MatchDetailsViewModel.kt`)

### State Management

The ViewModel manages three key states:

1. **Match State** - Loading, Success, or Error
2. **Timer State** - Elapsed time and running status
3. **UI Updates** - Real-time match updates from Firestore

### Key Methods

#### Load Match (One-Time)
```kotlin
viewModel.loadMatch("tournament123", "match456")
```

#### Listen to Match (Real-Time)
```kotlin
viewModel.listenToMatch("tournament123", "match456")
```

This automatically updates the UI whenever the Firestore document changes.

#### Update Score
```kotlin
viewModel.updateScore(
    tournamentId = "tournament123",
    matchId = "match456",
    player1Wins = 3,
    player1Losses = 1,
    player2Wins = 1,
    player2Losses = 3
)
```

#### Timer Controls
```kotlin
viewModel.startTimer()  // Start the match timer
viewModel.stopTimer()   // Stop the match timer
viewModel.resetTimer()  // Reset to 0
```

## 4. UI Screen (`MatchDetailsScreen.kt`)

### Usage

Update your navigation to pass the required parameters:

```kotlin
// In your NavHost
composable("matchDetails/{tournamentId}/{matchId}") { backStackEntry ->
    val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
    
    MatchDetailsScreen(
        tournamentId = tournamentId,
        matchId = matchId,
        onBackClick = { navController.popBackStack() },
        onRecord = { /* Navigate to recording screen */ },
        onBuildSubmit = { /* Navigate to build submission */ }
    )
}
```

### Features

✅ **Real-time updates** - Automatically reflects Firestore changes  
✅ **Loading state** - Shows spinner while loading  
✅ **Error handling** - Displays error messages  
✅ **Timer** - Match timer with start/stop controls  
✅ **Player stats** - Live score and wins/losses display  
✅ **Round history** - Shows all completed rounds  

## 5. Firestore Setup

### Security Rules

Add these security rules to your Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Tournament matches
    match /tournaments/{tournamentId}/matches/{matchId} {
      // Allow read if user is part of the tournament
      allow read: if request.auth != null;
      
      // Allow write if user is the tournament owner or a participant
      allow write: if request.auth != null && (
        get(/databases/$(database)/documents/tournaments/$(tournamentId)).data.tournamentOwner == request.auth.uid ||
        request.auth.uid in get(/databases/$(database)/documents/tournaments/$(tournamentId)).data.tournamentPlayers
      );
    }
  }
}
```

### Sample Firestore Document

Create a test match document in Firestore:

**Path:** `tournaments/test-tournament-123/matches/match-001`

```json
{
  "matchId": "match-001",
  "tournamentId": "test-tournament-123",
  "matchNumber": 1,
  "round": "Qualifier",
  "format": "First to Four",
  
  "player1Id": "user123",
  "player1Name": "Blader_ACE",
  "player1Score": 0,
  "player1Wins": 0,
  "player1Losses": 0,
  
  "player2Id": "user456",
  "player2Name": "X-Treme",
  "player2Score": 0,
  "player2Wins": 0,
  "player2Losses": 0,
  
  "status": "upcoming",
  "currentRound": 1,
  "winnerId": null,
  "winnerName": null,
  
  "startTime": null,
  "endTime": null,
  "elapsedSeconds": 0,
  
  "rounds": [],
  
  "createdAt": {"_seconds": 1700000000, "_nanoseconds": 0},
  "updatedAt": {"_seconds": 1700000000, "_nanoseconds": 0}
}
```

## 6. Testing

### Test the Implementation

1. **Create a test match in Firestore** using the Firebase Console
2. **Run the app** and navigate to Match Details
3. **Pass the correct tournamentId and matchId** through navigation
4. **Verify real-time updates** by editing the document in Firebase Console
5. **Test timer controls** (Start/Stop)
6. **Check loading and error states** by using invalid IDs

### Example Test Navigation

```kotlin
navController.navigate("matchDetails/test-tournament-123/match-001")
```

## 7. Common Issues & Solutions

### Issue: "Match not found"
**Solution:** Verify the document exists at the correct path in Firestore

### Issue: No real-time updates
**Solution:** Check that you're using `listenToMatch()` not `getMatch()`

### Issue: Permission denied
**Solution:** Update Firestore security rules to allow read access

### Issue: ViewModel not persisting
**Solution:** Use `viewModel()` from `androidx.lifecycle.viewmodel.compose`

## 8. Next Steps

Now that you have match reading implemented, you can extend it with:

1. **Update Score Dialog** - Allow users to update match scores
2. **Video Recording Integration** - Link round results with video URLs
3. **Build Submission** - Connect to final build submission feature
4. **Match Notifications** - Notify users when match status changes
5. **Match History** - List all matches in a tournament

## Dependencies Required

The following dependencies are already added to your project:

```kotlin
// In libs.versions.toml
androidx-lifecycle-viewmodel-compose = "2.9.3"

// In app/build.gradle.kts
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
implementation("com.google.firebase:firebase-firestore-ktx")
```

## Summary

You now have a complete Match Management backend implementation with:

- ✅ Data models for matches and rounds
- ✅ Repository with read and real-time listening
- ✅ ViewModel for state management
- ✅ Updated UI screen with real data integration
- ✅ Timer functionality
- ✅ Error handling and loading states

The implementation follows Android best practices with separation of concerns and reactive state management using Kotlin Flow and Compose State.

