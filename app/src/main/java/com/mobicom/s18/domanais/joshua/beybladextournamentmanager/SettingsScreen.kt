package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(false) }
    var privacyEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings toggles section
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSectionTitle(title = "Preferences")

                    SettingsToggleItem(
                        title = "Notifications",
                        description = "Enable push notifications for match updates",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsToggleItem(
                        title = "Dark Theme",
                        description = "Toggle between light and dark app theme",
                        checked = darkThemeEnabled,
                        onCheckedChange = { darkThemeEnabled = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsToggleItem(
                        title = "Privacy Mode",
                        description = "Hide your profile information from other users",
                        checked = privacyEnabled,
                        onCheckedChange = { privacyEnabled = it }
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(), onClick = {onAboutClick()}
            ) {
                Box(
                    Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Text("About App")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BeybladeXTournamentManagerTheme {
        SettingsScreen()
    }
}