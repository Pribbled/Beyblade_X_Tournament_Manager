# First-Time Setup Tutorial

## 🎯 Goal
Get your Match Management feature working with real Firestore data in 5 minutes!

---

## Step 1: Sync Gradle Dependencies ⚙️

The dependencies are already added, just sync your project:

1. Open Android Studio
2. Click **"Sync Now"** in the notification bar, OR
3. Go to **File → Sync Project with Gradle Files**
4. Wait for sync to complete

**Dependencies added:**
- ✅ `androidx.lifecycle.viewmodel.compose`
- ✅ `firebase-firestore-ktx` (already in project)

---

## Step 2: Create Test Data in Firestore 🔥

### Option A: Using Firebase Console (Recommended)

1. **Open Firebase Console**
   - Go to [https://console.firebase.google.com](https://console.firebase.google.com)
   - Select your project: "Beyblade X Tournament Manager"

2. **Navigate to Firestore Database**
   - Click **"Firestore Database"** in the left sidebar
   - If prompted, enable Firestore (choose production mode)

3. **Create Tournament Collection**
   - Click **"Start collection"**
   - Collection ID: `tournaments`
   - Click **"Next"**
   
4. **Create Tournament Document**
   - Document ID: `test-tournament-123` (or click "Auto-ID")
   - Add fields:
     - `uid`: (string) `test-uid`
     - `name`: (string) `Test Tournament`
     - `tournamentOwner`: (string) `test-owner`
   - Click **"Save"**

5. **Create Matches Subcollection**
   - Click on the tournament document you just created
   - Click **"Start collection"**
   - Collection ID: `matches`
   - Click **"Next"**

6. **Create Match Document**
   - Document ID: `match-001`
   - Add these fields (copy and paste):

   | Field Name | Type | Value |
   |------------|------|-------|
   | matchId | string | `match-001` |
   | tournamentId | string | `test-tournament-123` |
   | matchNumber | number | `1` |
   | round | string | `Qualifier` |
   | format | string | `First to Four` |
   | player1Id | string | `user1` |
   | player1Name | string | `Blader_ACE` |
   | player1Score | number | `0` |
   | player1Wins | number | `2` |
   | player1Losses | number | `1` |
   | player2Id | string | `user2` |
   | player2Name | string | `X-Treme` |
   | player2Score | number | `0` |
   | player2Wins | number | `1` |
   | player2Losses | number | `2` |
   | status | string | `in_progress` |
   | currentRound | number | `4` |
   | winnerId | null | |
   | winnerName | null | |
   | startTime | null | |
   | endTime | null | |
   | elapsedSeconds | number | `0` |
   | rounds | array | `[]` (empty array) |
   | createdAt | timestamp | (click clock icon, select now) |
   | updatedAt | timestamp | (click clock icon, select now) |

   - Click **"Save"**

7. **Verify Structure**
   Your Firestore should now look like:
   ```
   tournaments
     └── test-tournament-123
           └── matches
                 └── match-001
   ```

### Option B: Using Firestore REST API (Advanced)

Run this in your terminal:
```bash
curl -X POST \
  'https://firestore.googleapis.com/v1/projects/YOUR_PROJECT_ID/databases/(default)/documents/tournaments/test-tournament-123/matches' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -d '{
    "fields": {
      "matchId": {"stringValue": "match-001"},
      "tournamentId": {"stringValue": "test-tournament-123"},
      "matchNumber": {"integerValue": 1},
      "round": {"stringValue": "Qualifier"},
      "format": {"stringValue": "First to Four"},
      "player1Name": {"stringValue": "Blader_ACE"},
      "player1Wins": {"integerValue": 2},
      "player1Losses": {"integerValue": 1},
      "player2Name": {"stringValue": "X-Treme"},
      "player2Wins": {"integerValue": 1},
      "player2Losses": {"integerValue": 2},
      "status": {"stringValue": "in_progress"},
      "currentRound": {"integerValue": 4},
      "elapsedSeconds": {"integerValue": 0}
    }
  }'
```

---

## Step 3: Update Firestore Security Rules 🔐

1. In Firebase Console, go to **Firestore Database → Rules**

2. Replace the rules with:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow all reads for authenticated users (temporary for testing)
    match /{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

3. Click **"Publish"**

**Note:** These are permissive rules for testing. In production, use more restrictive rules from `MATCH_MANAGEMENT_GUIDE.md`.

---

## Step 4: Test Navigation in Your App 📱

### Option A: Add a Test Button (Quick Test)

In your `HomeScreen.kt` or any screen, add a test button:

```kotlin
Button(
    onClick = { 
        navController.navigate("View Match/test-tournament-123/match-001") 
    }
) {
    Text("Test Match Details")
}
```

### Option B: Navigate from Tournament Dashboard

If you have a tournament dashboard, update the match click handler:

```kotlin
// When user clicks on a match
onClick = {
    navController.navigate("View Match/${tournament.tournamentId}/${match.matchId}")
}
```

---

## Step 5: Run and Test! 🚀

1. **Build and Run** your app on emulator or device

2. **Sign in** (authentication required for Firestore)

3. **Navigate to Match Details**
   - Click your test button, OR
   - Navigate to the match from your app flow

4. **You should see:**
   - ✅ Loading spinner briefly
   - ✅ Match title: "Qualifier: Match 1"
   - ✅ Format: "First to Four"
   - ✅ Player 1: "Blader_ACE" with 2 wins
   - ✅ Player 2: "X-Treme" with 1 win
   - ✅ Timer at 00:00
   - ✅ Start/Stop buttons

---

## Step 6: Test Real-Time Updates 🔄

1. **Keep your app open** on the Match Details screen

2. **Open Firebase Console** in your browser
   - Navigate to the match document

3. **Edit a field**
   - Change `player1Wins` from `2` to `3`
   - Click **"Update"**

4. **Watch your app** 
   - The UI should update **instantly** without refreshing!
   - Player 1's wins should change from 2 to 3

5. **Test the timer**
   - Click "Start" button
   - Watch the timer count up
   - Click "Stop" button

---

## 🎉 Success Checklist

- [ ] Gradle sync completed without errors
- [ ] Test tournament created in Firestore
- [ ] Test match document created with all fields
- [ ] Security rules updated and published
- [ ] App builds and runs successfully
- [ ] Can navigate to Match Details screen
- [ ] Match data displays correctly
- [ ] Real-time updates work (edit in Firebase, see in app)
- [ ] Timer starts and stops
- [ ] Back button works

---

## 🐛 Troubleshooting

### Problem: "Match not found" error

**Solutions:**
- ✅ Verify document path: `tournaments/test-tournament-123/matches/match-001`
- ✅ Check that document exists in Firestore Console
- ✅ Ensure IDs match exactly (case-sensitive)

### Problem: "Permission denied" error

**Solutions:**
- ✅ Check Firestore security rules allow read access
- ✅ Ensure user is authenticated (signed in)
- ✅ Verify Firebase is properly configured in `google-services.json`

### Problem: Loading forever (never finishes)

**Solutions:**
- ✅ Check internet connection
- ✅ Verify Firebase project is active
- ✅ Check Android Studio Logcat for Firestore errors

### Problem: No real-time updates

**Solutions:**
- ✅ Verify you're using `listenToMatch()` not `getMatch()`
- ✅ Check that `LaunchedEffect` is being called
- ✅ Ensure ViewModel is not being recreated

### Problem: Gradle sync fails

**Solutions:**
- ✅ Check `libs.versions.toml` for syntax errors
- ✅ Verify internet connection
- ✅ Try **File → Invalidate Caches → Restart**
- ✅ Update Gradle if needed

### Problem: App crashes on navigation

**Solutions:**
- ✅ Check Logcat for error messages
- ✅ Verify navigation route matches: `"View Match/{tournamentId}/{matchId}"`
- ✅ Ensure parameters are not null or empty

---

## 📚 Next Steps After Setup

Once everything works:

1. **Create more test matches** with different data
2. **Test error handling** (use invalid IDs)
3. **Integrate with your tournament creation** flow
4. **Add score update dialog**
5. **Connect video recording** to rounds
6. **Add push notifications** for match updates

---

## 🆘 Still Having Issues?

Check these files for detailed help:
- `MATCH_MANAGEMENT_GUIDE.md` - Complete implementation guide
- `QUICK_REFERENCE.md` - Code snippets and examples
- `VISUAL_FLOW_DIAGRAM.md` - Understand the data flow
- `IMPLEMENTATION_SUMMARY.md` - Overview and checklist

Or check the Android Studio Logcat for error messages.

---

## ✨ You're All Set!

Your Match Management system is now fully functional with real-time Firestore integration! 

**What you've accomplished:**
- ✅ Set up Firestore backend
- ✅ Created test data
- ✅ Integrated real-time updates
- ✅ Implemented clean architecture (MVVM)
- ✅ Built a production-ready feature

**Enjoy your Beyblade X Tournament Manager!** 🎮⚡

---

**Setup completed:** November 22, 2025

