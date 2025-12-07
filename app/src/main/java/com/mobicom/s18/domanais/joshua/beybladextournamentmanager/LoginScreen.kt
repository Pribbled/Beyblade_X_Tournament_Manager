// kotlin
package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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

// Imports for Firebase, coroutines, and icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNewUser: () -> Unit = {}, // New callback for Google Sign-In
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    // --- UI State ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- Backend State ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseModule.auth

    // --- Google Sign-In Logic ---
    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }
    val webClientId = stringResource(id = R.string.default_web_client_id)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                isLoading = true
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data!!)
                    val googleIdToken = credential.googleIdToken
                    if (googleIdToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()

                        // Check if this is a new user
                        val isNewUser = authResult.additionalUserInfo?.isNewUser == true
                        val user = authResult.user!!

                        if (isNewUser) {
                            // User is new! Need to create their profile.
                            // We'll navigate to the RegisterScreenInfo page.
                            Log.d("LoginScreen", "Google Sign-In: New user, UID: ${user.uid}")
                            onNewUser()
                        } else {
                            // User already exists, just log them in.
                            Log.d("LoginScreen", "Google Sign-In: Existing user, UID: ${user.uid}")
                            onLoginSuccess()
                        }
                    }
                } catch (e: ApiException) {
                    errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
                    Log.w("LoginScreen", "Google Sign-In failed", e)
                } finally {
                    isLoading = false
                }
            }
        } else {
            Log.w("LoginScreen", "Google Sign-In flow cancelled or failed.")
        }
    }

    val onGoogleLoginClick: () -> Unit = {
        scope.launch {
            isLoading = true
            errorMessage = null

            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()

            try {
                val result = oneTapClient.beginSignIn(signInRequest).await()
                googleSignInLauncher.launch(
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                )
            } catch (e: Exception) {
                errorMessage = "Google Sign-In failed to start: ${e.localizedMessage}"
                Log.w("LoginScreen", "beginSignIn:failure", e)
            } finally {
                isLoading = false
            }
        }
    }
    // --- End Google Sign-In Logic ---


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
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
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Forgot Password?")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and Password cannot be blank."
                        isLoading = false
                        return@launch
                    }
                    try {
                        auth.signInWithEmailAndPassword(email.trim(), password).await()
                        isLoading = false
                        onLoginSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Login failed: ${e.localizedMessage}"
                        Log.w("LoginScreen", "signIn:failure", e)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Or continue with",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SocialLoginButton(
                text = "Google",
                onClick = onGoogleLoginClick, // This now calls our new logic
                iconResId = R.drawable.ic_google,
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
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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