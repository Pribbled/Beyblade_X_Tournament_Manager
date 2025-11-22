# Beyblade X Tournament Manager

Android application for managing Beyblade X tournaments with real-time match tracking and Firebase integration.

## 📱 Features

- ✅ User Authentication (Firebase Auth)
- ✅ Tournament Creation and Management
- ✅ **Match Management with Real-time Updates** ⭐ NEW!
- ✅ Match Recording
- ✅ Player Profiles
- ✅ Live Match Tracking
- ✅ Timer Functionality

## 🆕 Match Management Implementation

The Match Management feature has been fully implemented with Firestore backend integration!

### What's Included:

1. **Data Models** (`data/Match.kt`)
   - Type-safe Kotlin data classes
   - Support for match state, player stats, and round history

2. **Repository Layer** (`data/MatchRepository.kt`)
   - One-time fetch: `getMatch()`
   - Real-time listener: `listenToMatch()`
   - Update operations: `updateMatch()`, `addRoundResult()`, etc.

3. **ViewModel** (`viewmodel/MatchDetailsViewModel.kt`)
   - MVVM architecture
   - State management with Kotlin Flow
   - Timer controls

4. **UI Screen** (`MatchDetailsScreen.kt`)
   - Real-time data display
   - Loading and error states
   - Match timer with start/stop
   - Player statistics cards
   - Round history display

### Quick Start:

1. **Read the setup guide:**
   ```
   FIRST_TIME_SETUP.md
   ```

2. **Navigate to a match:**
   ```kotlin
   navController.navigate("View Match/$tournamentId/$matchId")
   ```

3. **See it update in real-time!** 🔄

### Documentation Files:

| File | Description |
|------|-------------|
| `FIRST_TIME_SETUP.md` | ⭐ **START HERE** - Step-by-step tutorial |
| `IMPLEMENTATION_SUMMARY.md` | Overview and checklist |
| `MATCH_MANAGEMENT_GUIDE.md` | Complete technical documentation |
| `QUICK_REFERENCE.md` | Code snippets and examples |
| `VISUAL_FLOW_DIAGRAM.md` | Architecture and data flow diagrams |

## 🏗️ Architecture

```
UI Layer (Jetpack Compose)
    ↓
ViewModel Layer (State Management)
    ↓
Repository Layer (Data Access)
    ↓
Firebase Firestore (Backend)
```

## 🛠️ Technologies Used

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Firestore, Auth, Storage, Messaging)
- **Reactive Programming:** Kotlin Flow & StateFlow
- **Navigation:** Jetpack Navigation Compose
- **Charts:** Vico Charts Library

## 📦 Dependencies

- AndroidX Core KTX
- Lifecycle & ViewModel Compose
- Firebase BOM 33.1.0
  - Firestore
  - Authentication
  - Storage
  - Cloud Messaging
- Jetpack Compose BOM 2024.09.00
- Material 3
- Navigation Compose
- Vico Charts

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Firebase project configured
- `google-services.json` in `app/` directory

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Follow **FIRST_TIME_SETUP.md** for Match Management setup
5. Run on emulator or device

## 📝 Firestore Structure

```
tournaments/
  {tournamentId}/
    - Tournament data...
    matches/
      {matchId}/
        - matchId: String
        - player1Name, player1Wins, player1Losses
        - player2Name, player2Wins, player2Losses
        - status: "upcoming" | "in_progress" | "completed"
        - rounds: Array<RoundDetail>
        - Real-time sync enabled
```

## 🧪 Testing

To test the Match Management feature:
1. Create a test match in Firebase Console
2. Navigate to `View Match/test-tournament-123/match-001`
3. Edit fields in Firebase Console and watch real-time updates!

See **FIRST_TIME_SETUP.md** for detailed testing instructions.

## 👥 Authors

**Joshua Domanais**
- Course: MOBICOM
- Term: Third Year, Term 1
- School: DLSU

## 📄 License

This project is part of an academic coursework for DLSU MOBICOM.

---

**Status:** ✅ Match Management Feature Fully Implemented (November 22, 2025)

For questions about Match Management, refer to the documentation files listed above.

