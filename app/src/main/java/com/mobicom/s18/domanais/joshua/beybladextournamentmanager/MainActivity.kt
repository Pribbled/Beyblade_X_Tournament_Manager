package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            BeybladeXTournamentManagerTheme {
                var showSplashScreen by remember { mutableStateOf(true) }
                if (showSplashScreen) {
                    SplashScreen(onTimeout = { showSplashScreen = false })
                } else {
                    HomeScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BeybladeXTournamentManagerTheme {
        SplashScreen(onTimeout = {})
    }
}
