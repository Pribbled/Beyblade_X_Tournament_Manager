# Match Management - Visual Flow Diagram

## 🔄 Real-Time Data Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                        FIREBASE FIRESTORE                        │
│                                                                  │
│  Path: tournaments/{tournamentId}/matches/{matchId}              │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  Document: match-001                                   │     │
│  │  {                                                     │     │
│  │    matchId: "match-001",                              │     │
│  │    tournamentId: "tournament-123",                    │     │
│  │    player1Name: "Blader_ACE",                         │     │
│  │    player1Wins: 2,                                    │     │
│  │    player2Name: "X-Treme",                            │     │
│  │    player2Wins: 1,                                    │     │
│  │    status: "in_progress",                             │     │
│  │    ...                                                │     │
│  │  }                                                     │     │
│  └────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ addSnapshotListener()
                              │ (Real-time updates)
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                       MATCH REPOSITORY                           │
│                    (MatchRepository.kt)                          │
│                                                                  │
│  fun listenToMatch(tournamentId, matchId): Flow<Result<Match>>  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  • Subscribes to Firestore document                   │     │
│  │  • Converts snapshot to Match data class              │     │
│  │  • Emits updates through Kotlin Flow                  │     │
│  │  • Handles errors gracefully                          │     │
│  └────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ Flow<Result<Match?>>
                              │ (Reactive stream)
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                    MATCH DETAILS VIEWMODEL                       │
│                 (MatchDetailsViewModel.kt)                       │
│                                                                  │
│  val matchState: StateFlow<MatchUiState>                         │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  Collects Flow from Repository                        │     │
│  │                                                        │     │
│  │  repository.listenToMatch()                            │     │
│  │      .collect { result ->                              │     │
│  │          _matchState.value = when {                    │     │
│  │              result.isSuccess -> Success(match)        │     │
│  │              result.isFailure -> Error(message)        │     │
│  │          }                                             │     │
│  │      }                                                  │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                  │
│  Also manages:                                                   │
│  • Timer state (elapsedTime, isTimerRunning)                    │
│  • Update operations (updateScore, addRound, etc.)              │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ StateFlow (observed by UI)
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                     MATCH DETAILS SCREEN                         │
│                   (MatchDetailsScreen.kt)                        │
│                                                                  │
│  val matchState by viewModel.matchState.collectAsState()         │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  LaunchedEffect(tournamentId, matchId) {              │     │
│  │      viewModel.listenToMatch(tournamentId, matchId)    │     │
│  │  }                                                     │     │
│  │                                                        │     │
│  │  when (matchState) {                                   │     │
│  │      Loading -> Show CircularProgressIndicator         │     │
│  │      Success(match) -> Display match details           │     │
│  │      Error(msg) -> Show error message                  │     │
│  │  }                                                     │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                  │
│  UI Components:                                                  │
│  • Player cards with scores                                     │
│  • Timer with start/stop buttons                                │
│  • Round history list                                           │
│  • Action buttons (Record, Update Score, Submit Build)          │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ User Interaction
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                         USER SEES                                │
│                                                                  │
│  ┌───────────────────────────────────────────────────────┐      │
│  │  Qualifier: Match 1                                   │      │
│  │  Format: First to Four                                │      │
│  │                                                        │      │
│  │  Round: 4                                             │      │
│  │                                                        │      │
│  │  ┌──────────────┐        ┌──────────────┐            │      │
│  │  │ Blader_ACE   │        │   X-Treme    │            │      │
│  │  │ Score: 2     │        │   Score: 1   │            │      │
│  │  └──────────────┘        └──────────────┘            │      │
│  │                                                        │      │
│  │  Timer: 03:45                                         │      │
│  │  [Start] [Stop]                                       │      │
│  │                                                        │      │
│  │  Round 1: Winner - Blader_ACE                         │      │
│  │  Round 2: Winner - X-Treme                            │      │
│  │  Round 3: Winner - Blader_ACE                         │      │
│  └───────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────────┘
```

## 📱 Navigation Flow

```
Any Screen in App
       │
       │ User clicks "View Match" button
       │
       ▼
navController.navigate("View Match/$tournamentId/$matchId")
       │
       │ Navigation system extracts parameters
       │
       ▼
AppNavHost.kt
  composable("View Match/{tournamentId}/{matchId}")
       │
       │ Creates MatchDetailsScreen with params
       │
       ▼
MatchDetailsScreen(
  tournamentId = "tournament-123",
  matchId = "match-001",
  ...
)
       │
       │ Initializes ViewModel and starts listening
       │
       ▼
Real-time data flows from Firestore
       │
       │ UI updates automatically
       │
       ▼
User sees live match data!
```

## 🔄 Update Flow (Writing Data)

```
User Action (e.g., Update Score)
       │
       ▼
UI calls ViewModel method
  viewModel.updateScore(...)
       │
       ▼
ViewModel calls Repository method
  repository.updateMatch(tournamentId, matchId, updates)
       │
       ▼
Repository updates Firestore
  db.collection("tournaments")
    .document(tournamentId)
    .collection("matches")
    .document(matchId)
    .update(updates)
       │
       ▼
Firestore triggers snapshot listener
       │
       ▼
Repository emits new data through Flow
       │
       ▼
ViewModel updates StateFlow
       │
       ▼
UI automatically re-composes with new data
       │
       ▼
User sees updated match info instantly!
```

## 🎯 Key Concepts

### 1. **Reactive Data Flow**
- Firestore → Flow → StateFlow → Compose State
- Changes propagate automatically
- No manual refresh needed

### 2. **Separation of Concerns**
- **Repository**: Handles Firestore operations
- **ViewModel**: Manages business logic & UI state
- **Screen**: Only displays data and handles user input

### 3. **Real-time Updates**
```kotlin
// One-time fetch
suspend fun getMatch() // Fetches once

// Real-time listener
fun listenToMatch(): Flow // Updates continuously
```

### 4. **State Management**
```kotlin
sealed class MatchUiState {
    object Loading          // Show spinner
    data class Success      // Show match data
    data class Error        // Show error message
}
```

## 📊 Data Structure

```
Match Document
├── matchId: String
├── tournamentId: String
├── matchNumber: Int
├── round: String
├── format: String
├── Player 1 Data
│   ├── player1Id: String
│   ├── player1Name: String
│   ├── player1Wins: Int
│   └── player1Losses: Int
├── Player 2 Data
│   ├── player2Id: String
│   ├── player2Name: String
│   ├── player2Wins: Int
│   └── player2Losses: Int
├── Match State
│   ├── status: String
│   ├── currentRound: Int
│   ├── winnerId: String?
│   └── winnerName: String?
├── Timing
│   ├── startTime: Timestamp?
│   ├── endTime: Timestamp?
│   └── elapsedSeconds: Long
├── Round History
│   └── rounds: List<RoundDetail>
│       ├── roundNumber: Int
│       ├── winnerId: String
│       ├── winnerName: String
│       └── videoLink: String
└── Metadata
    ├── createdAt: Timestamp
    └── updatedAt: Timestamp
```

## 🚀 Quick Test Steps

1. **Create test match in Firestore**
   - Path: `tournaments/test-123/matches/match-001`
   
2. **Navigate from app**
   ```kotlin
   navController.navigate("View Match/test-123/match-001")
   ```

3. **Watch it work!**
   - Data loads automatically
   - Edit in Firebase Console
   - See instant updates in app

4. **Test interactions**
   - Start/Stop timer
   - Navigate back
   - Try invalid IDs (see error handling)

---

**This diagram shows the complete end-to-end flow of your Match Management system!** 🎉

