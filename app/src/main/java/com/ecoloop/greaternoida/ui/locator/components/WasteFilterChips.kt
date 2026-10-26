package com.ecoloop.greaternoida.ui.locator.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner

@Composable
fun WasteFilterChips(
    selectedWasteType: String?,
    partners: List<Partner>,
    onSelectWasteType: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val totalCount = partners.size
    val eWasteCount = partners.count { it.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_E_WASTE) }
    val plasticCount = partners.count { it.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_PLASTIC) }
    val paperCount = partners.count { it.wasteTypesAccepted.contains(EcoConstants.WASTE_TYPE_PAPER) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All Filter Chip
        FilterChip(
            selected = selectedWasteType == null,
            onClick = { onSelectWasteType(null) },
            label = { Text("All ($totalCount)") },
            leadingIcon = {
                if (selectedWasteType == null) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                } else {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // E-Waste Chip
        FilterChip(
            selected = selectedWasteType == EcoConstants.WASTE_TYPE_E_WASTE,
            onClick = { onSelectWasteType(EcoConstants.WASTE_TYPE_E_WASTE) },
            label = { Text("E-Waste ($eWasteCount)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Devices, contentDescription = null)
            }
        )

        // Plastic Chip
        FilterChip(
            selected = selectedWasteType == EcoConstants.WASTE_TYPE_PLASTIC,
            onClick = { onSelectWasteType(EcoConstants.WASTE_TYPE_PLASTIC) },
            label = { Text("Plastic ($plasticCount)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Recycling, contentDescription = null)
            }
        )

        // Paper Chip
        FilterChip(
            selected = selectedWasteType == EcoConstants.WASTE_TYPE_PAPER,
            onClick = { onSelectWasteType(EcoConstants.WASTE_TYPE_PAPER) },
            label = { Text("Paper ($paperCount)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Description, contentDescription = null)
            }
        )
    }
}
