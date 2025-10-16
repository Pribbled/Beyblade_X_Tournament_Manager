*Note: Aesthetic of buttons, fields, etc. are temporary. Design will be finalized before demo but the current theme colors of the app will be followed.*

**MainActivity.kt**
- Splash screen will show before going to HomeScreen
- Splash screen temporarily goes to HomeScreen but it should go to login or register first (temporary ito kasi we'll have logic for sessions, if logged in naman na, straight to home screen)

**HomeScreen.kt**
- Dummy data for tournament list
- Buttons should be set to the assigned screen after clicking
- Profile icon on top right should go to ProfileScreen

**TournamentDashboardScreen.kt**
- There are tabs currently blank (Bracket and Metrics)
- Just create a new tab nalang under the tabs folder then integrate it here on this file
- Yung View button for each match, it will go to the Match Details screen

**MatchRecordingScreen.kt**
- Black background because it's a placeholder for the actual camera view
- There are no other related views here mostly logic nalang
- When the back button (from phone) is pressed, it will go back to the Match Details Screen

**EditProfileScreen.kt**
- From Edit Profile button in ProfileScreen, it will go here.
- Nothing much to fix here but the logic in the future.
