package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FieldValue
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Tournament
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTournamentScreen(
    onBackClick: () -> Unit = {},
    onJoinAsPlayerClick: (String) -> Unit = {},
    onJoinAsJudgeClick: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseModule.auth
    val db = FirebaseModule.db
    val context = LocalContext.current

    var tournamentCode by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isScanning = true
        } else {
            Toast.makeText(context, "Camera permission needed to scan QR code", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // --- Logic to Join Tournament ---
    val joinTournamentLogic = fun(code: String) {
        if (code.isBlank()) return

        scope.launch {
            isLoading = true
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Toast.makeText(context, "You must be logged in.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }

                val result = db.collection("tournaments")
                    .whereEqualTo("tournamentCode", code)
                    .get()
                    .await()

                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    val tournamentId = doc.id
                    val tournament = doc.toObject(Tournament::class.java)!!

                    val isJudge = tournament.tournamentJudges.contains(userId)
                    val isAlreadyPlayer = tournament.tournamentPlayers.contains(userId)
                    val judgesAlsoPlay = tournament.tournamentJudgesAlsoPlay
                    val isJoiningAsJudge = !isJudge && judgesAlsoPlay

                    if (isJudge) {
                        Toast.makeText(context, "Joining as Judge...", Toast.LENGTH_SHORT).show()

                        db.collection("users").document(userId)
                            .update("pastTournaments", FieldValue.arrayUnion(tournamentId))
                            .await()

                        onJoinAsJudgeClick(tournamentId)
                    } else {
                        if (isAlreadyPlayer) {
                            Toast.makeText(
                                context,
                                "Welcome back to the tournament!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val judgeIdsToAdd = if (judgesAlsoPlay) tournament.tournamentJudges else emptyList()
                            val playerUpdate = FieldValue.arrayUnion(*(listOf(userId) + judgeIdsToAdd).toTypedArray())
                            doc.reference.update("tournamentPlayers", playerUpdate)
                            if (!isJudge && isJoiningAsJudge) {
                                doc.reference.update("tournamentJudges", FieldValue.arrayUnion(userId))
                            }

                            db.collection("users").document(userId)
                                .update("pastTournaments", FieldValue.arrayUnion(tournamentId))
                                .await()

                            Toast.makeText(context, "Joined as Player!", Toast.LENGTH_SHORT).show()
                        }
                        onJoinAsPlayerClick(tournamentId)
                    }
                } else {
                    Toast.makeText(context, "Tournament not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JoinTournament", "Error joining", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                isScanning = false
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Tournament") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        if (isScanning) {
            QRScannerView(
                onCodeScanned = { scannedCode ->
                    tournamentCode = scannedCode
                    joinTournamentLogic(scannedCode)
                },
                onClose = { isScanning = false }
            )
        } else {
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
                    contentDescription = "Trophy",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Enter Tournament Code",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tournamentCode,
                    onValueChange = {
                        tournamentCode = it.uppercase()
                    },
                    label = { Text("Code (e.g. A1B2C3)") },
                    modifier = Modifier.width(300.dp),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { joinTournamentLogic(tournamentCode) },
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Join Tournament")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("OR", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            isScanning = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan QR Code")
                }
            }
        }
    }
}

@Composable
fun QRScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(localContext) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val executor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(imageProxy, onCodeScanned)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QRScanner", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text("Cancel Scanning")
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun processImageProxy(imageProxy: ImageProxy, onCodeScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        onCodeScanned(rawValue)
                        // Only scan once
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QRScanner", "Scan failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Preview(showBackground = true)
@Composable
fun JoinTournamentScreenPreview() {
    BeybladeXTournamentManagerTheme {
        JoinTournamentScreen()
    }
}