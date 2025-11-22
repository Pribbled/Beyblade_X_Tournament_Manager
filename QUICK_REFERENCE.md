# Match Management Quick Reference

## Quick Start

### 1. Fetch Match Once (One-Time Read)
```kotlin
// In your ViewModel or Composable
viewModelScope.launch {
    val result = matchRepository.getMatch("tournamentId", "matchId")
    result.fold(
        onSuccess = { match -> /* Use match */ },
        onFailure = { error -> /* Handle error */ }
    )
}
```

### 2. Listen to Real-Time Updates (Recommended)
```kotlin
// In ViewModel
fun listenToMatch(tournamentId: String, matchId: String) {
    viewModelScope.launch {
        repository.listenToMatch(tournamentId, matchId).collect { result ->
            result.fold(
                onSuccess = { match -> _matchState.value = MatchUiState.Success(match) },
                onFailure = { error -> _matchState.value = MatchUiState.Error(error.message) }
            )
        }
    }
}

// In Composable
val matchState by viewModel.matchState.collectAsState()
LaunchedEffect(tournamentId, matchId) {
    viewModel.listenToMatch(tournamentId, matchId)
}
```

### 3. Update Match Screen Navigation
```kotlin
// In your NavHost
composable("matchDetails/{tournamentId}/{matchId}") { backStackEntry ->
    MatchDetailsScreen(
        tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: "",
        matchId = backStackEntry.arguments?.getString("matchId") ?: "",
        onBackClick = { navController.popBackStack() },
        onRecord = { /* Navigate to recording */ },
        onBuildSubmit = { /* Navigate to build submission */ }
    )
}

// Navigate to match details
navController.navigate("matchDetails/$tournamentId/$matchId")
```

## Firestore Structure

```
tournaments/
  {tournamentId}/
    matches/
      {matchId}/
        - matchId: String
        - tournamentId: String
        - matchNumber: Int
        - round: String
        - format: String
        - player1Id: String
        - player1Name: String
        - player1Wins: Int
        - player1Losses: Int
        - player2Id: String
        - player2Name: String
        - player2Wins: Int
        - player2Losses: Int
        - status: String
        - currentRound: Int
        - elapsedSeconds: Long
        - rounds: Array<RoundDetail>
        - createdAt: Timestamp
        - updatedAt: Timestamp
```

## Common Operations

### Update Match Score
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

### Add Round Result
```kotlin
val round = RoundDetail(
    roundNumber = 3,
    winnerId = "player1",
    winnerName = "Blader_ACE",
    videoLink = "https://example.com/video"
)
viewModel.addRoundResult("tournament123", "match456", round)
```

### Update Match Status
```kotlin
viewModel.updateMatchStatus("tournament123", "match456", "in_progress")
// Possible values: "upcoming", "in_progress", "completed"
```

### Timer Controls
```kotlin
viewModel.startTimer()  // Start
viewModel.stopTimer()   // Stop
viewModel.resetTimer()  // Reset to 0
```

## Files Created

1. **`data/Match.kt`** - Data model for match and round details
2. **`data/MatchRepository.kt`** - Firestore operations (CRUD + real-time)
3. **`viewmodel/MatchDetailsViewModel.kt`** - State management
4. **`MatchDetailsScreen.kt`** - Updated UI with real-time data

## Key Features

✅ Real-time Firestore updates  
✅ One-time fetch option  
✅ Match timer (start/stop/persist)  
✅ Loading & error states  
✅ Player statistics display  
✅ Round history tracking  
✅ Type-safe data models  

## Testing Checklist

- [ ] Create test match in Firestore Console
- [ ] Navigate to match details screen
- [ ] Verify match data displays correctly
- [ ] Test real-time updates (edit in Firestore)
- [ ] Test timer start/stop
- [ ] Test loading state
- [ ] Test error state (invalid ID)
- [ ] Check navigation back button

## Sample Test Data

Create this in Firestore Console at:  
`tournaments/test-tournament-123/matches/match-001`

```json
{
  "matchId": "match-001",
  "tournamentId": "test-tournament-123",
  "matchNumber": 1,
  "round": "Qualifier",
  "format": "First to Four",
  "player1Id": "user1",
  "player1Name": "Blader_ACE",
  "player1Wins": 2,
  "player1Losses": 1,
  "player2Id": "user2",
  "player2Name": "X-Treme",
  "player2Wins": 1,
  "player2Losses": 2,
  "status": "in_progress",
  "currentRound": 4,
  "elapsedSeconds": 180,
  "rounds": [
    {
      "roundNumber": 1,
      "winnerId": "user1",
      "winnerName": "Blader_ACE",
      "videoLink": "https://example.com/video1"
    }
  ],
  "createdAt": {"_seconds": 1700000000, "_nanoseconds": 0},
  "updatedAt": {"_seconds": 1700000000, "_nanoseconds": 0}
}
```

Then navigate: `navController.navigate("matchDetails/test-tournament-123/match-001")`

