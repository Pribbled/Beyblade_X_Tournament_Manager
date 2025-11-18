package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch

// Imports for Firebase and coroutines
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenCredentials(
    onRegisterScreenInfo: () -> Unit = { },
    onLoginClick: () -> Unit = { }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // --- Backend State ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope() // For launching backend tasks
    val auth = FirebaseModule.auth

    // --- UX State for Password Visibility ---
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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

        // --- Email Field ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // --- Password Field ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )

        // --- Confirm Password Field ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Error Message Display ---
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        TextButton(onClick = onLoginClick) {
            Text("Login instead")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Next Button ---
        Button(
            onClick = {
                // Launch the coroutine for the Firebase call
                scope.launch {
                    isLoading = true
                    errorMessage = null // Clear previous errors

                    // --- 1. Validation ---
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and Password cannot be blank."
                        isLoading = false
                        return@launch
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        isLoading = false
                        return@launch
                    }
                    // Firebase requires 6 chars, this check is good practice
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        isLoading = false
                        return@launch
                    }

                    // --- 2. Firebase Call ---
                    try {
                        // Create the user
                        auth.createUserWithEmailAndPassword(email.trim(), password).await()

                        // Success! User is created and auto-logged in.
                        isLoading = false
                        onRegisterScreenInfo() // Navigate to the next step

                    } catch (e: Exception) {
                        // --- 3. Handle Errors ---
                        isLoading = false
                        errorMessage = e.localizedMessage ?: "An unknown error occurred."
                        Log.w("RegisterScreen", "createUserWithEmail:failure", e)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading // Disable button while loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Next")
            }
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