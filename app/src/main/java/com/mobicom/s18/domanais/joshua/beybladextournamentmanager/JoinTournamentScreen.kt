// kotlin
package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTournamentScreen(
    onBackClick: () -> Unit = {},
    onScanQRCodeClick: () -> Unit = {},
    onJoinAsPlayerClick: () -> Unit = {},
    onJoinAsJudgeClick: () -> Unit = {}
) {
    var tournamentCode by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Join Tournament Screen",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tropy_cup),
                contentDescription = "App logo",
                modifier = Modifier
                    .size(120.dp)
            )
            Text(
                text = "Join Tournament",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(34.dp))
            OutlinedTextField(
                value = tournamentCode,
                onValueChange = { tournamentCode = it },
                label = { Text("Enter Tournament Code") },
                modifier = Modifier.width(300.dp),
                textStyle = TextStyle(color = Color.Black),
            )
            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onScanQRCodeClick,
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),

                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Scan QR Code!")
            }
            Spacer(modifier = Modifier.height(34.dp))
            Row(
                modifier = Modifier.width(300.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Button(
                    onClick = onJoinAsPlayerClick,
                    modifier = Modifier.weight(1f).width(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF645DD7),
                        contentColor = Color.White
                    ),
                ) {
                    Text("Join as Player")
                }

                Button(
                    onClick = onJoinAsJudgeClick,
                    modifier = Modifier.weight(1f).width(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF840032),
                        contentColor = Color.White
                    ),
                ) {
                    Text("Join as Judge")
                }

            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun JoinTournamentScreenPreview() {
    BeybladeXTournamentManagerTheme {
        JoinTournamentScreen()
    }
}
