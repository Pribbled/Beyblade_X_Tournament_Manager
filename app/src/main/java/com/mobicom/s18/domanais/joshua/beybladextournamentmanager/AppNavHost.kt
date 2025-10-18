package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*


@Composable
fun AppNavHost(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "registerScreenCredentials") {

        composable("registerScreenCredentials") {
            RegisterScreenCredentials(
                onRegisterScreenInfo = {
                    navController.navigate("registerScreenInfo")
                },
                onLoginClick = {navController.navigate("login")}
            )
        }

        composable("registerScreenInfo") {
            RegisterScreenInfo(
                onRegistrationComplete = {
                    //If there is login page go to login page
                    navController.navigate("home")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginClick = {navController.navigate("home")},
                onRegisterClick = {navController.navigate("registerScreenCredentials")}
            )
        }


        composable("home") {
            HomeScreen(
                onProfileClick = {
                    navController.navigate("profile")
                },
            onTournamentClick = {
                navController.navigate("tournament")
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
                onJoinAsJudgeClick = {navController.navigate("tournament")},
                onJoinAsPlayerClick = {navController.navigate("tournament")}
                //onScanQRCodeClick: () -> Unit = {},
               // onJoinAsPlayerClick: () -> Unit = {},
               // onJoinAsJudgeClick: () -> Unit = {}
            )
        }

        composable ("createTournament") {
            CreateTournamentScreen(
                onBackClick = { navController.popBackStack()},
                onCreateTournamentClick = {navController.navigate("tournament")}
            )
        }

        composable ("tournament") {
            TournamentDashboardScreen (
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
                onLogoutClick = {navController.navigate("login")},
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

            )
        }

        composable("About"){
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

}