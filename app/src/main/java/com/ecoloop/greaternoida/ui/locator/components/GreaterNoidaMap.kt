package com.ecoloop.greaternoida.ui.locator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoloop.greaternoida.BuildConfig
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun GreaterNoidaMap(
    partners: List<Partner>,
    selectedPartner: Partner?,
    onPartnerSelected: (Partner) -> Unit,
    userLat: Double?,
    userLng: Double?,
    isLocationPermissionGranted: Boolean,
    onSwitchToSectorMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultCenter = LatLng(
        EcoConstants.GREATER_NOIDA_CENTER_LAT,
        EcoConstants.GREATER_NOIDA_CENTER_LNG
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, EcoConstants.DEFAULT_MAP_ZOOM)
    }

    val coroutineScope = rememberCoroutineScope()
    val isPlaceholderKey = BuildConfig.MAPS_API_KEY.isBlank() || BuildConfig.MAPS_API_KEY.contains("PLACEHOLDER")

    // Smoothly animate camera when a partner is selected
    LaunchedEffect(selectedPartner) {
        selectedPartner?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.lat, it.lng),
                    15.0f
                ),
                durationMs = 600
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = isLocationPermissionGranted
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true
            )
        ) {
            partners.forEach { partner ->
                // Choose marker hue based on accepted materials or verification
                val markerHue = when {
                    partner.verified -> BitmapDescriptorFactory.HUE_AZURE
                    partner.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_E_WASTE) -> BitmapDescriptorFactory.HUE_ORANGE
                    partner.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_PLASTIC) -> BitmapDescriptorFactory.HUE_CYAN
                    else -> BitmapDescriptorFactory.HUE_GREEN
                }

                Marker(
                    state = MarkerState(position = LatLng(partner.lat, partner.lng)),
                    title = partner.name,
                    snippet = "${partner.address} • Accepts: ${partner.wasteTypesAccepted.joinToString(", ")}",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        onPartnerSelected(partner)
                        true
                    }
                )
            }
        }

        // Informative Banner if running with default placeholder API key
        if (isPlaceholderKey) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Maps Key Not Configured",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "local.properties has a placeholder key, causing Google tiles to show blank. Switch to Sector Map to view all drop-off points.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSwitchToSectorMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Sector Map", fontSize = 11.sp)
                    }
                }
            }
        }

        // Floating Action Button to center map on user or Greater Noida hub
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val target = if (userLat != null && userLng != null) {
                        LatLng(userLat, userLng)
                    } else {
                        defaultCenter
                    }
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(target, 14.0f)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedPartner != null) 140.dp else 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Center Location"
            )
        }
    }
}
