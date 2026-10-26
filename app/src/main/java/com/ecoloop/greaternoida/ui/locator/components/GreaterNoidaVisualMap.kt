package com.ecoloop.greaternoida.ui.locator.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.ui.theme.EWasteColor
import com.ecoloop.greaternoida.ui.theme.PaperColor
import com.ecoloop.greaternoida.ui.theme.PlasticColor
import com.ecoloop.greaternoida.ui.theme.VerifiedBlue

// Bounding box coordinates for Greater Noida urban sectors
private const val MIN_LAT = 28.4580
private const val MAX_LAT = 28.5320
private const val MIN_LNG = 77.4820
private const val MAX_LNG = 77.5260

@Composable
fun GreaterNoidaVisualMap(
    partners: List<Partner>,
    selectedPartner: Partner?,
    onPartnerSelected: (Partner) -> Unit,
    userLat: Double?,
    userLng: Double?,
    isLocationPermissionGranted: Boolean,
    modifier: Modifier = Modifier
) {
    // Zoom and Pan states
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.85f, 3.5f)
        offset += offsetChange
    }

    // Pulsing animation for selected partner and user location
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8EFE6))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Function to map (lat, lng) to pixel offsets on canvas
            fun project(lat: Double, lng: Double): Offset {
                val clampedLat = lat.coerceIn(MIN_LAT, MAX_LAT)
                val clampedLng = lng.coerceIn(MIN_LNG, MAX_LNG)

                val xNorm = (clampedLng - MIN_LNG) / (MAX_LNG - MIN_LNG)
                val yNorm = 1.0 - ((clampedLat - MIN_LAT) / (MAX_LAT - MIN_LAT))

                val paddingX = canvasWidth * 0.08f
                val paddingY = canvasHeight * 0.08f

                val usableWidth = canvasWidth - (paddingX * 2)
                val usableHeight = canvasHeight - (paddingY * 2)

                return Offset(
                    x = paddingX + (xNorm.toFloat() * usableWidth),
                    y = paddingY + (yNorm.toFloat() * usableHeight)
                )
            }

            // Canvas drawing Greater Noida sectors, expressways and roads
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformState)
                    .pointerInput(partners, scale, offset) {
                        detectTapGestures { tapOffset ->
                            // Transform tap location back to canvas coordinates
                            val adjustedTap = (tapOffset - offset) / scale
                            // Find closest partner within 48dp tap radius
                            val hitRadiusPx = 54.dp.toPx()
                            val hit = partners.minByOrNull { partner ->
                                val pt = project(partner.lat, partner.lng)
                                (pt - adjustedTap).getDistance()
                            }
                            if (hit != null) {
                                val pt = project(hit.lat, hit.lng)
                                if ((pt - adjustedTap).getDistance() <= hitRadiusPx) {
                                    onPartnerSelected(hit)
                                }
                            }
                        }
                    }
            ) {
                // 1. Draw stylized sector zones
                drawSectorZones(this, ::project)

                // 2. Draw arterial roads connecting sectors
                drawArterialRoads(this, ::project)

                // 3. Draw Partner Pins
                partners.forEach { partner ->
                    val pos = project(partner.lat, partner.lng)
                    val isSelected = partner.id == selectedPartner?.id

                    val pinColor = when {
                        partner.verified -> VerifiedBlue
                        partner.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_E_WASTE) -> EWasteColor
                        partner.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_PLASTIC) -> PlasticColor
                        else -> PaperColor
                    }

                    // Pulsing highlight for selected partner
                    if (isSelected) {
                        drawCircle(
                            color = pinColor.copy(alpha = 0.25f),
                            radius = pulseAnim * 1.5f,
                            center = pos
                        )
                        drawCircle(
                            color = pinColor.copy(alpha = 0.4f),
                            radius = pulseAnim,
                            center = pos
                        )
                    }

                    // Shadow & outer ring
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.18f),
                        radius = 16f,
                        center = pos + Offset(0f, 4f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 17f else 13f,
                        center = pos
                    )

                    // Inner color circle
                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 13f else 10f,
                        center = pos
                    )

                    // Small center dot
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 5f else 4f,
                        center = pos
                    )
                }

                // 4. Draw User GPS Location if available
                if (userLat != null && userLng != null && isLocationPermissionGranted) {
                    val userPos = project(userLat, userLng)
                    drawCircle(
                        color = Color(0xFF2979FF).copy(alpha = 0.25f),
                        radius = pulseAnim * 1.8f,
                        center = userPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 11f,
                        center = userPos
                    )
                    drawCircle(
                        color = Color(0xFF2979FF),
                        radius = 8f,
                        center = userPos
                    )
                }
            }
        }

        // Sector Legend & Compass Pill at Top-Left
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Greater Noida Hub",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${partners.size} drop-off points active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Floating Action Buttons (Recenter / Reset Zoom)
        FloatingActionButton(
            onClick = {
                scale = 1.0f
                offset = Offset.Zero
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedPartner != null) 140.dp else 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Recenter Map"
            )
        }
    }
}

// Draws realistic sector cards/zones representing Greater Noida layout
private fun drawSectorZones(
    scope: DrawScope,
    project: (Double, Double) -> Offset
) {
    // 1. Pari Chowk Zone (South-East Roundabout)
    val pariCenter = project(28.4673, 77.5140)
    scope.drawCircle(
        color = Color(0xFFD4E7D0),
        radius = 75f,
        center = pariCenter
    )
    scope.drawCircle(
        color = Color(0xFFB8DCB1),
        radius = 50f,
        center = pariCenter
    )

    // 2. Sector Alpha 1 & 2 Zone
    val alphaTopLeft = project(28.4800, 77.5020)
    val alphaBottomRight = project(28.4680, 77.5160)
    scope.drawRoundRect(
        color = Color(0xFFDEEBDC).copy(alpha = 0.8f),
        topLeft = Offset(alphaTopLeft.x, alphaTopLeft.y),
        size = Size(
            width = (alphaBottomRight.x - alphaTopLeft.x).coerceAtLeast(80f),
            height = (alphaBottomRight.y - alphaTopLeft.y).coerceAtLeast(70f)
        ),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 3. Sector Beta 1 & 2 Zone
    val betaTopLeft = project(28.4900, 77.4980)
    val betaBottomRight = project(28.4800, 77.5140)
    scope.drawRoundRect(
        color = Color(0xFFD7E7D4).copy(alpha = 0.85f),
        topLeft = Offset(betaTopLeft.x, betaTopLeft.y),
        size = Size(
            width = (betaBottomRight.x - betaTopLeft.x).coerceAtLeast(85f),
            height = (betaBottomRight.y - betaTopLeft.y).coerceAtLeast(70f)
        ),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 4. Sector Gamma 1 & 2 (Jagat Farm) Zone
    val gammaTopLeft = project(28.4980, 77.5040)
    val gammaBottomRight = project(28.4890, 77.5180)
    scope.drawRoundRect(
        color = Color(0xFFE2EEDF).copy(alpha = 0.85f),
        topLeft = Offset(gammaTopLeft.x, gammaTopLeft.y),
        size = Size(
            width = (gammaBottomRight.x - gammaTopLeft.x).coerceAtLeast(85f),
            height = (gammaBottomRight.y - gammaTopLeft.y).coerceAtLeast(70f)
        ),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // 5. Surajpur Industrial Area & Wetland Zone (North)
    val surajTopLeft = project(28.5280, 77.4860)
    val surajBottomRight = project(28.5040, 77.5020)
    scope.drawRoundRect(
        color = Color(0xFFCFE5CC).copy(alpha = 0.9f),
        topLeft = Offset(surajTopLeft.x, surajTopLeft.y),
        size = Size(
            width = (surajBottomRight.x - surajTopLeft.x).coerceAtLeast(95f),
            height = (surajBottomRight.y - surajTopLeft.y).coerceAtLeast(80f)
        ),
        cornerRadius = CornerRadius(18f, 18f)
    )
}

// Draws arterial roads linking Greater Noida sectors
private fun drawArterialRoads(
    scope: DrawScope,
    project: (Double, Double) -> Offset
) {
    val roadColor = Color(0xFFC7D3C4)
    val expresswayColor = Color(0xFFB5C4B2)

    val pPari = project(28.4673, 77.5140)
    val pAlpha = project(28.4740, 77.5100)
    val pBeta = project(28.4840, 77.5060)
    val pGamma = project(28.4930, 77.5120)
    val pSuraj = project(28.5150, 77.4950)

    // Main Sector Arterial Road: Pari Chowk -> Alpha -> Beta -> Gamma -> Surajpur
    val mainRoadPath = Path().apply {
        moveTo(pPari.x, pPari.y)
        lineTo(pAlpha.x, pAlpha.y)
        lineTo(pBeta.x, pBeta.y)
        lineTo(pGamma.x, pGamma.y)
        lineTo(pSuraj.x, pSuraj.y)
    }

    scope.drawPath(
        path = mainRoadPath,
        color = expresswayColor,
        style = Stroke(width = 16f, pathEffect = PathEffect.cornerPathEffect(30f))
    )
    scope.drawPath(
        path = mainRoadPath,
        color = Color.White,
        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
    )

    // Surajpur Wetland road branch
    val pWetland = project(28.5240, 77.4920)
    scope.drawLine(
        color = roadColor,
        start = pSuraj,
        end = pWetland,
        strokeWidth = 10f
    )
}
