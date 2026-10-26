package com.ecoloop.greaternoida.ui.aiscanner

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.ui.theme.AccentAmber
import com.ecoloop.greaternoida.ui.theme.EWasteColor
import com.ecoloop.greaternoida.ui.theme.GlassColor
import com.ecoloop.greaternoida.ui.theme.MetalColor
import com.ecoloop.greaternoida.ui.theme.PaperColor
import com.ecoloop.greaternoida.ui.theme.PlasticColor
import com.ecoloop.greaternoida.ui.theme.VerifiedBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AiScannerScreen(
    viewModel: AiScannerViewModel,
    onBack: () -> Unit,
    onProceedToDropOff: (Partner, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Check GPS coordinates if permission granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.onLocationUpdated(loc.latitude, loc.longitude, true)
                    } else {
                        viewModel.onLocationUpdated(null, null, true)
                    }
                }
            } catch (_: SecurityException) {
                viewModel.onLocationUpdated(null, null, false)
            }
        } else {
            viewModel.onLocationUpdated(null, null, false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Waste Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LiteRT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Identify waste & recommend nearest centre",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!uiState.isInCaptureMode && !uiState.isCropping) {
                        IconButton(onClick = { viewModel.onStartCropping() }) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "Crop Image",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = { viewModel.onRetakePhoto() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retake",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isInCaptureMode) {
                // Step 1: Camera capture view
                if (cameraPermissionState.status.isGranted) {
                    CameraCaptureView(
                        isModelAvailable = uiState.isModelAvailable,
                        modelStatusMessage = uiState.modelStatusMessage,
                        onPhotoCaptured = { bitmap -> viewModel.onPhotoCaptured(bitmap) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CameraPermissionRationale(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        onSimulatePhoto = { bitmap -> viewModel.onPhotoCaptured(bitmap) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (uiState.isCropping && uiState.uncroppedOriginalBitmap != null) {
                // Step 2 (Optional): Image Cropper to isolate single object
                CropImageView(
                    originalBitmap = uiState.uncroppedOriginalBitmap!!,
                    onApplyCrop = { cropped -> viewModel.onApplyCrop(cropped) },
                    onCancel = { viewModel.onCancelCropping() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Step 3: Classification Result & Recommended Centres View
                ClassificationAndRecommendationsView(
                    uiState = uiState,
                    onRetake = { viewModel.onRetakePhoto() },
                    onStartCrop = { viewModel.onStartCropping() },
                    onResetCrop = { viewModel.onResetCrop() },
                    onThresholdChanged = { viewModel.onConfidenceThresholdChanged(it) },
                    onConfirmCategory = { slug, name -> viewModel.onCategoryConfirmed(slug, name) },
                    onRequestManualCategory = { viewModel.onManualCategoryRequested() },
                    onLocalitySelected = { locality -> viewModel.onLocalitySelected(locality) },
                    onRequestLocationPermission = { locationPermissionsState.launchMultiplePermissionRequest() },
                    onProceedToDropOff = onProceedToDropOff,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CameraCaptureView(
    isModelAvailable: Boolean,
    modelStatusMessage: String,
    onPhotoCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var showSimulationMenu by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        // CameraX Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Status Header (Model availability & Torch)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = if (isModelAvailable) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isModelAvailable) Icons.Default.Psychology else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isModelAvailable) Color(0xFF00E676) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isModelAvailable) "On-Device AI Classifier Ready" else "Model Warning",
                        color = if (isModelAvailable) Color.White else MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isModelAvailable) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = modelStatusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Viewfinder Guide Target
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(2.dp, Color(0xFF81C784), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Center waste item in frame",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom Controls (Torch, Capture Shutter, Simulation Helper)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Torch toggle
                camera?.cameraInfo?.hasFlashUnit()?.let { hasFlash ->
                    if (hasFlash) {
                        IconButton(
                            onClick = {
                                isTorchOn = !isTorchOn
                                camera?.cameraControl?.enableTorch(isTorchOn)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Torch",
                                tint = if (isTorchOn) Color.Yellow else Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                } ?: Spacer(modifier = Modifier.size(48.dp))

                // Circular Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color.Transparent)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else Color.White)
                        .clickable(enabled = !isCapturing && imageCapture != null) {
                            isCapturing = true
                            imageCapture?.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(imageProxy)
                                        imageProxy.close()
                                        isCapturing = false
                                        if (bitmap != null) {
                                            ContextCompat.getMainExecutor(context).execute {
                                                onPhotoCaptured(bitmap)
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                    } else {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                }

                // Test Simulation Helper button
                Box {
                    IconButton(
                        onClick = { showSimulationMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Simulate", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showSimulationMenu,
                        onDismissRequest = { showSimulationMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Water Bottle (Plastic)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color.Cyan, "Plastic Bottle"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cardboard Box (Paper)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color(0xFF8D6E63), "Cardboard Carton"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Beverage Can (Metal)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color.LightGray, "Tin Can"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Wine / Beer Bottle (Glass)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color(0xFF00695C), "Glass Bottle"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cellular Phone (E-Waste)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color.DarkGray, "Cellular Phone"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unrecognized Object (Manual Pick)") },
                            onClick = {
                                showSimulationMenu = false
                                onPhotoCaptured(createSyntheticTestBitmap(Color.Magenta, "Non-Waste Item"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap circle to capture photograph for AI classification",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Image Cropper control allowing the user to zoom and isolate a single waste item from cluttered scenes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CropImageView(
    originalBitmap: Bitmap,
    onApplyCrop: (Bitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.33f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedPreset by remember { mutableStateOf("Center 75%") }

    val croppedPreview = remember(zoomScale, offsetX, offsetY, originalBitmap) {
        cropBitmapRegion(originalBitmap, zoomScale, offsetX, offsetY)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crop & Focus on Waste Item",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Text(
            text = "Isolate one object (e.g. mouse or laptop) to remove cables and cluttered desk items.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Cropped live preview box
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = croppedPreview.asImageBitmap(),
                contentDescription = "Cropped focus preview",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Preset Focus Buttons
        Text(
            text = "Quick Focus Presets:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "Center 75%" to { zoomScale = 1.33f; offsetX = 0f; offsetY = 0f },
                "Focus Center (50%)" to { zoomScale = 2.0f; offsetX = 0f; offsetY = 0f },
                "Top Focus" to { zoomScale = 1.5f; offsetX = 0f; offsetY = -0.2f },
                "Bottom Focus" to { zoomScale = 1.5f; offsetX = 0f; offsetY = 0.2f },
                "Left Focus" to { zoomScale = 1.5f; offsetX = -0.2f; offsetY = 0f },
                "Right Focus" to { zoomScale = 1.5f; offsetX = 0.2f; offsetY = 0f },
                "Full View (1:1)" to { zoomScale = 1.0f; offsetX = 0f; offsetY = 0f }
            ).forEach { (label, action) ->
                FilterChip(
                    selected = selectedPreset == label,
                    onClick = {
                        selectedPreset = label
                        action()
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Zoom Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zoom:", style = MaterialTheme.typography.labelSmall, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = zoomScale,
                onValueChange = {
                    zoomScale = it
                    selectedPreset = "Custom"
                },
                valueRange = 1.0f..3.0f,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${String.format("%.1f", zoomScale)}x",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onApplyCrop(croppedPreview) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Crop")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClassificationAndRecommendationsView(
    uiState: AiScannerUiState,
    onRetake: () -> Unit,
    onStartCrop: () -> Unit,
    onResetCrop: () -> Unit,
    onThresholdChanged: (Float) -> Unit,
    onConfirmCategory: (String, String) -> Unit,
    onRequestManualCategory: () -> Unit,
    onLocalitySelected: (String) -> Unit,
    onRequestLocationPermission: () -> Unit,
    onProceedToDropOff: (Partner, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var localityDropdownExpanded by remember { mutableStateOf(false) }

    val isCropped = uiState.capturedBitmap != null &&
        uiState.uncroppedOriginalBitmap != null &&
        uiState.capturedBitmap != uiState.uncroppedOriginalBitmap

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Photo Preview & Analysis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with Crop and Retake actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Classification Result",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onStartCrop,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Crop & Focus", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onRetake,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retake", fontSize = 11.sp)
                        }
                    }
                }

                // If image is cropped, indicate with reset option
                if (isCropped) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cropped focus applied",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = onResetCrop,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text("Reset to Full Photo", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Photo Thumbnail
                    uiState.capturedBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured waste item",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Result Details
                    Column(modifier = Modifier.weight(1f)) {
                        if (uiState.isAnalyzing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Running on-device LiteRT model...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else if (uiState.classificationResult != null) {
                            val result = uiState.classificationResult

                            // Detected Category Chip
                            val (catColor, catIcon) = getCategoryColorAndIcon(result.categorySlug)

                            Surface(
                                color = catColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(imageVector = catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = result.categoryDisplayName,
                                        color = catColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Confidence Progress
                            Text(
                                text = "Confidence: ${result.confidencePercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { result.confidence },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (result.isConfident) catColor else AccentAmber
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Top raw class: \"${result.rawLabel}\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            // Show Top-3 predictions if available
                            if (result.topCandidates.size > 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Detected candidates:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    result.topCandidates.take(3).forEach { cand ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${cand.rawLabel} (${cand.confidencePercent}%)",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Configurable Confidence Threshold Selector
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Threshold:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                0.40f to "40% Relaxed",
                                0.50f to "50% Standard",
                                0.65f to "65% Strict"
                            ).forEach { (thresh, label) ->
                                val isSelected = Math.abs(uiState.confidenceThreshold - thresh) < 0.05f
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onThresholdChanged(thresh) },
                                    label = { Text(label, fontSize = 9.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }

                // Scene Clutter / Low Confidence / Unrecognized Alert
                val result = uiState.classificationResult
                if (!uiState.isAnalyzing && result != null && (!result.isConfident || result.isMultiObjectOrCluttered)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AccentAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (result.isMultiObjectOrCluttered) {
                                        "Scene Clutter / Multiple Objects Detected"
                                    } else {
                                        "Low Confidence Prediction (${result.confidencePercent}%)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = result.explanationMessage
                                    ?: "Confidence is below the ${(uiState.confidenceThreshold * 100).toInt()}% threshold. Results may be uncertain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onStartCrop,
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crop to Isolate Item", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Manual Category Selection Chips (Always available)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.confirmedCategorySlug != null) {
                            "Confirmed Category (Tap to change):"
                        } else {
                            "Select Waste Category Manually:"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.confirmedCategorySlug != null) {
                        TextButton(
                            onClick = onRequestManualCategory,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Change", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        EcoConstants.WASTE_TYPE_PLASTIC to "Plastic",
                        EcoConstants.WASTE_TYPE_PAPER to "Paper & Cardboard",
                        EcoConstants.WASTE_TYPE_METAL to "Metal",
                        EcoConstants.WASTE_TYPE_GLASS to "Glass",
                        EcoConstants.WASTE_TYPE_E_WASTE to "E-Waste"
                    ).forEach { (slug, name) ->
                        val isSelected = uiState.confirmedCategorySlug == slug
                        FilterChip(
                            selected = isSelected,
                            onClick = { onConfirmCategory(slug, name) },
                            label = { Text(name, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Nearest Recycling Centres Section
        if (uiState.confirmedCategorySlug != null) {
            val confirmedSlug = uiState.confirmedCategorySlug
            val categoryName = uiState.confirmedCategoryDisplayName ?: confirmedSlug

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Nearest Centres for $categoryName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Centres strictly accepting $categoryName in Greater Noida",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proximity & Locality Filter Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (uiState.isLocationPermissionGranted) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (uiState.isLocationPermissionGranted) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isLocationPermissionGranted && uiState.userLat != null) {
                                "GPS Proximity Enabled • Nearest First"
                            } else {
                                "Near: ${uiState.selectedLocality}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Locality Selector Dropdown & GPS request
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!uiState.isLocationPermissionGranted) {
                            TextButton(onClick = onRequestLocationPermission) {
                                Text("Enable GPS", fontSize = 11.sp)
                            }
                        }

                        Box {
                            TextButton(onClick = { localityDropdownExpanded = true }) {
                                Text("Locality", fontSize = 11.sp)
                            }

                            DropdownMenu(
                                expanded = localityDropdownExpanded,
                                onDismissRequest = { localityDropdownExpanded = false }
                            ) {
                                EcoConstants.GREATER_NOIDA_SECTORS.forEach { sector ->
                                    DropdownMenuItem(
                                        text = { Text(sector) },
                                        onClick = {
                                            localityDropdownExpanded = false
                                            onLocalitySelected(sector)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Recommended Centres
            if (uiState.isLoadingRecommendations) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else if (uiState.recommendedPartners.isEmpty()) {
                // Empty state: No centre accepts this waste
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Centres Accepting $categoryName",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "None of the registered Greater Noida recycling centres currently accept $categoryName in this sector. Try selecting another locality or waste type.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Display nearest up to 5 eligible centres
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.recommendedPartners.forEachIndexed { index, partner ->
                        RecommendedPartnerCard(
                            partner = partner,
                            targetCategorySlug = confirmedSlug,
                            rank = index + 1,
                            onProceedToDropOff = {
                                onProceedToDropOff(partner, confirmedSlug)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendedPartnerCard(
    partner: Partner,
    targetCategorySlug: String,
    rank: Int,
    onProceedToDropOff: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$rank",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = partner.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (partner.distanceKm != null) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NearMe, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${partner.distanceKm} km",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Address & Verification
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = partner.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (partner.verified) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = VerifiedBlue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Verified EcoLoop Partner", color = VerifiedBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Materials accepted chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                partner.wasteTypesAccepted.forEach { type ->
                    val isTarget = type.equals(targetCategorySlug, ignoreCase = true)
                    val (badgeColor, _) = getCategoryColorAndIcon(type)

                    Surface(
                        color = if (isTarget) badgeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        border = if (isTarget) BorderStroke(1.dp, badgeColor) else null
                    ) {
                        Text(
                            text = type.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = if (isTarget) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isTarget) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connect to Drop-off workflow button
            Button(
                onClick = onProceedToDropOff,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Proceed to Drop-Off at this Station", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CameraPermissionRationale(
    onRequestPermission: () -> Unit,
    onSimulatePhoto: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "EcoLoop requires camera access to photograph waste items for on-device AI material classification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Camera Permission")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onSimulatePhoto(createSyntheticTestBitmap(Color.Cyan, "Plastic Bottle"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simulate Sample Item (For Testing)")
                }
            }
        }
    }
}

private fun getCategoryColorAndIcon(categorySlug: String?): Pair<Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (categorySlug?.lowercase()) {
        EcoConstants.WASTE_TYPE_PLASTIC -> PlasticColor to Icons.Default.Recycling
        EcoConstants.WASTE_TYPE_PAPER -> PaperColor to Icons.Default.Recycling
        EcoConstants.WASTE_TYPE_METAL -> MetalColor to Icons.Default.Hardware
        EcoConstants.WASTE_TYPE_GLASS -> GlassColor to Icons.Default.WineBar
        EcoConstants.WASTE_TYPE_E_WASTE -> EWasteColor to Icons.Default.Devices
        else -> Color.Gray to Icons.AutoMirrored.Filled.HelpOutline
    }
}

/**
 * Crops a region from the source bitmap based on zoom and offset fractions.
 */
private fun cropBitmapRegion(
    source: Bitmap,
    scale: Float,
    offsetXFraction: Float,
    offsetYFraction: Float
): Bitmap {
    val srcWidth = source.width
    val srcHeight = source.height
    val minDim = minOf(srcWidth, srcHeight)

    val cropSize = (minDim / scale.coerceAtLeast(1.0f)).toInt().coerceIn(20, minDim)

    val centerX = srcWidth / 2 + (offsetXFraction * srcWidth).toInt()
    val centerY = srcHeight / 2 + (offsetYFraction * srcHeight).toInt()

    val left = (centerX - cropSize / 2).coerceIn(0, (srcWidth - cropSize).coerceAtLeast(0))
    val top = (centerY - cropSize / 2).coerceIn(0, (srcHeight - cropSize).coerceAtLeast(0))
    val actualWidth = minOf(cropSize, srcWidth - left)
    val actualHeight = minOf(cropSize, srcHeight - top)

    return Bitmap.createBitmap(source, left, top, actualWidth, actualHeight)
}

/**
 * Converts a CameraX ImageProxy to an Android Bitmap rotated appropriately.
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        if (bitmap != null && imageProxy.imageInfo.rotationDegrees != 0) {
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Creates a synthetic 224x224 bitmap for fast testing without needing physical objects.
 */
private fun createSyntheticTestBitmap(color: Color, label: String): Bitmap {
    val bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.rgb(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }
    canvas.drawRect(0f, 0f, 224f, 224f, paint)

    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(label, 112f, 112f, textPaint)
    return bitmap
}
