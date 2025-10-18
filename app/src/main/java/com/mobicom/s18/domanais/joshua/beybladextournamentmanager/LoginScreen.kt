// kotlin
package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleLoginClick: () -> Unit = {},
    onFacebookLoginClick: () -> Unit = {},
    onTwitterLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email / Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Forgot Password?")
        }

        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Or continue with",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stack social buttons and make each full width
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SocialLoginButton(
                text = "Google",
                onClick = onGoogleLoginClick,
                iconResId = R.drawable.ic_google,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            SocialLoginButton(
                text = "Facebook",
                onClick = onFacebookLoginClick,
                iconResId = R.drawable.ic_facebook,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            SocialLoginButton(
                text = "Twitter",
                onClick = onTwitterLoginClick,
                iconResId = R.drawable.ic_twitter,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
        }

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    iconResId: Int,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier // <- parent can still override width/height
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), // ✅ takes full width
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // ✅ centers content horizontally
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = "$text login",
                modifier = Modifier.size(18.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color.Black)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BeybladeXTournamentManagerTheme {
        LoginScreen()
    }
}