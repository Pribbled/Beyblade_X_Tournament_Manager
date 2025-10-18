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
                }
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

        composable("home") {
            HomeScreen(
                //onProfileClick
            onTournamentClick = {
                navController.navigate("tournament")
            },
           onJoinTournamentClick = {
               navController.navigate("joinTournament")
           }
            //onCreateTournamentClick
            )
        }

        composable("joinTournament") {
            JoinTournamentScreen(
                onBackClick = { navController.popBackStack()}
                //onScanQRCodeClick: () -> Unit = {},
               // onJoinAsPlayerClick: () -> Unit = {},
               // onJoinAsJudgeClick: () -> Unit = {}
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
                onBackClick = { navController.popBackStack() }
            )
        }
    }

}