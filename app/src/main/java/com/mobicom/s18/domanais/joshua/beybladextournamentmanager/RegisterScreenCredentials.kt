package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenCredentials(
    onRegisterScreenInfo: () -> Unit = { }
) {
    var email by remember { mutableStateOf("") }
    var password by remember{mutableStateOf("")}
    var confirmPassword by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Create an Account",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(34.dp))
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .size(120.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                )
                Spacer(modifier = Modifier.height(34.dp))
                Button(
                    //Extra to do validation before proceeding to next screen
                    onClick = onRegisterScreenInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Next")
                }
        }
    }



@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    BeybladeXTournamentManagerTheme {
        RegisterScreenCredentials()
    }
}