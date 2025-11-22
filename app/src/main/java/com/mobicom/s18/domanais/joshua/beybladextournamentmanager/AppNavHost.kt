package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
                    navController.navigate("View Match")
                }
            )

        }


        composable("View Match"){
            MatchDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onRecord = {
                    navController.navigate("Record Match")
                },
                onBuildSubmit = {navController.navigate("BuildSubmit")}
            )
        }

        composable("BuildSubmit"){
            FinalRoundBuildSubmissionScreen(
                onBackClick = {navController.popBackStack()},
                onSubmitClick = {navController.popBackStack()}
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

        composable ("Record Match"){
            MatchRecordingScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("About"){
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

}