package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.BracketTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MatchesTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.MetricsTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs.OverviewTab
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel.TournamentDashboardViewModel
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.util.QRCodeUtils
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.FirebaseModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    tournamentId: String = "preview1",
    onBackClick: () -> Unit = {} ,
    onViewMatchClick: (Match) -> Unit = {},
    viewModel: TournamentDashboardViewModel = viewModel()
) {
    val db = FirebaseFirestore.getInstance()
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Overview", "Matches", "Bracket", "Metrics")

    // QR Code Dialog State
    var showShareDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val currentUser = FirebaseModule.auth.currentUser
    val isHost = tournament != null && currentUser != null && tournament!!.tournamentOwner == currentUser.uid

    LaunchedEffect(tournamentId) {
        if (tournamentId == "preview1") {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("tournaments").document(tournamentId).get()
            .addOnSuccessListener { document ->
                val fetchedTournament = document.toObject(Tournament::class.java)
                tournament = fetchedTournament

                if (fetchedTournament != null) {
                    viewModel.loadParticipants(fetchedTournament.tournamentPlayers)
                    // Generate QR Code
                    qrBitmap = QRCodeUtils.generateQRCode(fetchedTournament.tournamentCode)
                }

                isLoading = false
            }
            .addOnFailureListener { isLoading = false }

        viewModel.loadMatches(tournamentId)
    }

    val matches by viewModel.matches.collectAsState()
    val participants by viewModel.participants.collectAsState()

    // QR Code Dialog
    if (showShareDialog && tournament != null) {
        ShareTournamentDialog(
            tournamentName = tournament!!.name,
            tournamentCode = tournament!!.tournamentCode,
            qrBitmap = qrBitmap,
            onDismiss = { showShareDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament?.name ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share Button
                    if (tournament != null) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Tournament Code")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) })
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tournament != null) {
                when (selectedTabIndex) {
                    0 -> OverviewTab(tournament = tournament!!, participants = participants)
                    1 -> MatchesTab(
                        tournament = tournament!!,
                        matches = matches,
                        isHost = isHost,
                        onViewMatchClick = onViewMatchClick,
                        onGenerateMatches = {
                            val shouldAdvanceToFinals = tournament!!.stageCount == 2 && tournament!!.currentStage == 1 &&
                                matches.isNotEmpty() && matches.all { it.status == "completed" }
                            viewModel.generateMatches(tournament!!, advanceToFinals = shouldAdvanceToFinals)
                        }
                    )
                    2 -> BracketTab(
                        tournament = tournament!!,
                        matches = matches,
                        onViewMatchClick = onViewMatchClick
                    )
                    3 -> MetricsTab(matches = matches)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tournament not found")
                }
            }
        }
    }
}

@Composable
fun ShareTournamentDialog(
    tournamentName: String,
    tournamentCode: String,
    qrBitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Join Tournament", fontWeight = FontWeight.Bold)
                Text(tournamentName, style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // QR Code Image
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Tournament QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                        Text("Generating QR...")
                    }
                }

                Text("Scan to join via App", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)

                HorizontalDivider()

                Text("OR USE CODE", fontWeight = FontWeight.Bold)

                // Code + Copy Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(
                        text = tournamentCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Tournament Code", tournamentCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TournamentDashboardScreenPreview() {
    BeybladeXTournamentManagerTheme {
        TournamentDashboardScreen()
    }
}