package com.ecoloop.greaternoida.ui.locator

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ecoloop.greaternoida.data.datasource.SeedPartnersData
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.ui.locator.components.GreaterNoidaMap
import com.ecoloop.greaternoida.ui.locator.components.GreaterNoidaVisualMap
import com.ecoloop.greaternoida.ui.locator.components.PartnerCard
import com.ecoloop.greaternoida.ui.locator.components.PartnerDetailSheet
import com.ecoloop.greaternoida.ui.locator.components.WasteFilterChips
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocatorScreen(
    viewModel: LocatorViewModel,
    modifier: Modifier = Modifier,
    onInitiateDropOff: ((Partner) -> Unit)? = null,
    onScanQrCode: (() -> Unit)? = null,
    onViewQrCode: ((Partner) -> Unit)? = null,
    onOpenAiScanner: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDetailSheet by remember { mutableStateOf(false) }

    // Runtime Location Permission Management
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        viewModel.onLocationPermissionResult(locationPermissionsState.allPermissionsGranted)
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EcoLoop Greater Noida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drop-off Points Locator",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    // AI Waste Scanner Action
                    if (onOpenAiScanner != null) {
                        IconButton(onClick = onOpenAiScanner) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Waste Scanner",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    // View Station QR Codes (For testing & printing)
                    if (onViewQrCode != null) {
                        IconButton(onClick = {
                            val targetPartner = uiState.selectedPartner
                                ?: uiState.filteredPartners.firstOrNull()
                                ?: SeedPartnersData.partners.first()
                            onViewQrCode(targetPartner)
                        }) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Station QR Codes",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    // View Toggle (Map <-> List)
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isMapView) Icons.AutoMirrored.Filled.List else Icons.Default.Map,
                            contentDescription = if (uiState.isMapView) "Switch to List View" else "Switch to Map View",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Refresh action
                    IconButton(onClick = { viewModel.retry() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (onScanQrCode != null) {
                ExtendedFloatingActionButton(
                    onClick = onScanQrCode,
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    text = { Text("Scan Station QR", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    // AI Scanner Quick Action Banner
                    if (onOpenAiScanner != null) {
                        Card(
                            onClick = onOpenAiScanner,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "AI Waste Scanner",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Scan waste to identify material & find nearest centre",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search Alpha, Beta, Pari Chowk, Surajpur...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Waste Type Filter Chips (All, E-Waste, Plastic, Paper)
                    WasteFilterChips(
                        selectedWasteType = uiState.selectedWasteType,
                        partners = uiState.partners,
                        onSelectWasteType = { viewModel.selectWasteType(it) }
                    )
                }
            }

            // Body Area
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Loading State
                    uiState.isLoading -> {
                        LoadingStateView()
                    }

                    // Error State
                    uiState.errorMessage != null -> {
                        ErrorStateView(
                            errorMessage = uiState.errorMessage ?: "Failed to load drop-off points",
                            onRetry = { viewModel.retry() }
                        )
                    }

                    // Empty State
                    uiState.isEmpty -> {
                        EmptyStateView(
                            selectedFilter = uiState.selectedWasteType,
                            searchQuery = uiState.searchQuery,
                            onClearFilters = {
                                viewModel.selectWasteType(null)
                                viewModel.onSearchQueryChanged("")
                            }
                        )
                    }

                    // Main Content: Map or List
                    else -> {
                        if (uiState.isMapView) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (uiState.useGoogleMaps) {
                                    GreaterNoidaMap(
                                        partners = uiState.filteredPartners,
                                        selectedPartner = uiState.selectedPartner,
                                        onPartnerSelected = { partner ->
                                            viewModel.selectPartner(partner)
                                        },
                                        userLat = uiState.userLat,
                                        userLng = uiState.userLng,
                                        isLocationPermissionGranted = uiState.isLocationPermissionGranted,
                                        onSwitchToSectorMap = { viewModel.setUseGoogleMaps(false) }
                                    )
                                } else {
                                    GreaterNoidaVisualMap(
                                        partners = uiState.filteredPartners,
                                        selectedPartner = uiState.selectedPartner,
                                        onPartnerSelected = { partner ->
                                            viewModel.selectPartner(partner)
                                        },
                                        userLat = uiState.userLat,
                                        userLng = uiState.userLng,
                                        isLocationPermissionGranted = uiState.isLocationPermissionGranted
                                    )
                                }

                                // Engine Switcher (Sector Map <-> Google Maps)
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 12.dp, top = 8.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilterChip(
                                            selected = !uiState.useGoogleMaps,
                                            onClick = { viewModel.setUseGoogleMaps(false) },
                                            label = { Text("Sector Map", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        FilterChip(
                                            selected = uiState.useGoogleMaps,
                                            onClick = { viewModel.setUseGoogleMaps(true) },
                                            label = { Text("Google Map", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                }

                                // Floating selected partner card overlay
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = uiState.selectedPartner != null,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                ) {
                                    uiState.selectedPartner?.let { partner ->
                                        PartnerCard(
                                            partner = partner,
                                            isSelected = true,
                                            onClick = { showDetailSheet = true }
                                        )
                                    }
                                }
                            }
                        } else {
                            // List View
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.filteredPartners,
                                    key = { it.id }
                                ) { partner ->
                                    PartnerCard(
                                        partner = partner,
                                        isSelected = partner.id == uiState.selectedPartner?.id,
                                        onClick = {
                                            viewModel.selectPartner(partner)
                                            showDetailSheet = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Partner Details Bottom Sheet
    if (showDetailSheet && uiState.selectedPartner != null) {
        PartnerDetailSheet(
            partner = uiState.selectedPartner!!,
            onDismiss = { showDetailSheet = false },
            onInitiateDropOff = onInitiateDropOff,
            onViewQrCode = onViewQrCode,
            onScanQrCode = onScanQrCode
        )
    }
}

@Composable
private fun LoadingStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Finding drop-off points in Greater Noida...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorStateView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Unable to connect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    selectedFilter: String?,
    searchQuery: String,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationSearching,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Drop-off Points Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val reason = when {
                searchQuery.isNotBlank() -> "No locations in Greater Noida match \"$searchQuery\"."
                selectedFilter != null -> "No drop-off points found currently accepting $selectedFilter."
                else -> "No recycling points available in this sector."
            }
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearFilters) {
                Text("Reset Filters")
            }
        }
    }
}
