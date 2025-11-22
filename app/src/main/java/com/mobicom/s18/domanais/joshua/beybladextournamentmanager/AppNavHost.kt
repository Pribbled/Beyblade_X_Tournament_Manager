package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*

@Composable
fun AppNavHost(){
    val navController = rememberNavController()

    val auth = FirebaseModule.auth
    val startDestination = if (auth.currentUser != null) {
        "home"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("registerScreenCredentials") {
            RegisterScreenCredentials(
                onRegisterScreenInfo = {
                    navController.navigate("registerScreenInfo")
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("registerScreenCredentials") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("registerScreenInfo") {
            RegisterScreenInfo(
                onRegistrationComplete = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                onNewUser = {
                    // Google Sign-In was a new user, send to Register Info screen
                    navController.navigate("registerScreenInfo") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {navController.navigate("registerScreenCredentials")}
            )
        }


        composable("home") {
            HomeScreen(
                onProfileClick = {
                    navController.navigate("profile")
                },
                onTournamentClick = { tournamentId ->
                    navController.navigate("tournament/$tournamentId")
                },
                onJoinTournamentClick = {
                    navController.navigate("joinTournament")
                },
                onCreateTournamentClick = {
                    navController.navigate("createTournament")
                }
            )
        }

        composable("joinTournament") {
            JoinTournamentScreen(
                onBackClick = { navController.popBackStack()},
                onJoinAsJudgeClick = { tournamentId ->
                    navController.navigate("tournament/$tournamentId")
                },
                onJoinAsPlayerClick = { tournamentId ->
                    navController.navigate("tournament/$tournamentId")
                }
            )
        }

        composable ("createTournament") {
            CreateTournamentScreen(
                onBackClick = { navController.popBackStack()},
                onCreateTournamentClick = { tournamentId: String ->
                    navController.navigate("tournament/$tournamentId")
                }
            )
        }

        composable(
            route = "tournament/{tournamentId}",
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            TournamentDashboardScreen(
                tournamentId = tournamentId,
                onBackClick = { navController.popBackStack() },
                onViewMatchClick = { match ->
                    // Navigate to match details with proper parameters
                    navController.navigate("match_details/${match.tournamentId}/${match.matchId}")
                }
            )
        }

        /**
         * Match Details Screen Route
         *
         * Displays detailed information about a specific match with real-time Firestore updates.
         * Integrated with MatchDetailsViewModel for state management.
         *
         * Route: match_details/{tournamentId}/{matchId}
         * Parameters:
         *   - tournamentId: ID of the tournament
         *   - matchId: ID of the specific match
         *
         * Features:
         *   - Real-time score updates from Firestore
         *   - Player details and statistics
         *   - Match timer with start/stop controls
         *   - Navigation to match recording
         *   - Navigation to final round build submission
         *
         * Note: SimpleMatchDetailsScreen.kt has been DELETED.
         * This route now uses the original MatchDetailsScreen.kt with full backend integration.
         */
        composable(
            route = "match_details/{tournamentId}/{matchId}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""

            MatchDetailsScreen(
                tournamentId = tournamentId,
                matchId = matchId,
                onBackClick = { navController.popBackStack() },
                onRecord = {
                    // Navigate to match recording screen
                    navController.navigate("match_recording/$tournamentId/$matchId")
                },
                onBuildSubmit = { playerId, playerName ->
                    // Navigate to build submission with player info
                    navController.navigate("BuildSubmit/$tournamentId/$playerId/$playerName")
                }
            )
        }

        composable(
            route = "match_recording/{tournamentId}/{matchId}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""

            MatchRecordingScreen(
                tournamentId = tournamentId,
                matchId = matchId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "BuildSubmit/{tournamentId}/{playerId}/{playerName}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType },
                navArgument("playerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""

            FinalRoundBuildSubmissionScreen(
                tournamentId = tournamentId,
                playerId = playerId,
                playerName = playerName,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        composable("profile"){
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = {
                    navController.navigate("editProfile")
                },
                onSettingsClick = {navController.navigate("settings")},
                onNotificationsClick = {navController.navigate("notifs")}
            )
        }

        composable ("editProfile"){
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable ("settings"){
            SettingsScreen(
                onBackClick = {navController.popBackStack()},
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                onAboutClick = {navController.navigate("About")}
            )
        }

        composable ("notifs"){
            NotificationsScreen(
                onBackClick = {navController.popBackStack()}
            )
        }

        composable("About"){
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

}