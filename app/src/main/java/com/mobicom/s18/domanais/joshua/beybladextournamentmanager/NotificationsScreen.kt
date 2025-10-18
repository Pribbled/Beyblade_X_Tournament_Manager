package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    MATCH_UPDATE, TOURNAMENT_RESULT, INVITATION, ANNOUNCEMENT, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit = {}
) {
    // Sample notifications
    val notifications = remember {
        listOf(
            Notification(
                id = "1",
                title = "Match Update",
                message = "Your match against Blader_X is scheduled to start in 15 minutes",
                timestamp = "Just now",
                type = NotificationType.MATCH_UPDATE
            ),
            Notification(
                id = "2",
                title = "Tournament Result",
                message = "Congratulations! You've advanced to the next round in Summer Bey Championship",
                timestamp = "5 minutes ago",
                type = NotificationType.TOURNAMENT_RESULT,
                isRead = true
            ),
            Notification(
                id = "3",
                title = "Invitation",
                message = "Blader_Pro invited you to join 'Weekly Beyblade Battles'",
                timestamp = "1 hour ago",
                type = NotificationType.INVITATION
            ),
            Notification(
                id = "4",
                title = "Tournament Announcement",
                message = "The finals of 'World Beyblader Championship' will begin tomorrow at 3:00 PM",
                timestamp = "3 hours ago",
                type = NotificationType.ANNOUNCEMENT,
                isRead = true
            ),
            Notification(
                id = "5",
                title = "System Update",
                message = "App update available: Version 2.0 with new tournament features",
                timestamp = "Yesterday",
                type = NotificationType.SYSTEM
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Notifications Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val icon = when (notification.type) {
        NotificationType.MATCH_UPDATE -> Icons.Default.Timer
        NotificationType.TOURNAMENT_RESULT -> Icons.Default.EmojiEvents
        NotificationType.INVITATION -> Icons.Default.PersonAdd
        NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
        NotificationType.SYSTEM -> Icons.Default.Settings
    }

    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!notification.isRead)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    BeybladeXTournamentManagerTheme {
        NotificationsScreen()
    }
}