package com.ecoloop.greaternoida.data.datasource

import com.ecoloop.greaternoida.domain.model.Partner

object SeedPartnersData {
    val partners: List<Partner> = listOf(
        Partner(
            id = "partner_pari_01",
            name = "Pari Chowk Metro EcoPoint",
            address = "Opposite Gate 2, Pari Chowk Metro Station, Pari Chowk",
            lat = 28.4673,
            lng = 77.5140,
            wasteTypesAccepted = listOf("e-waste", "plastic", "paper"),
            verified = true,
            qrCodeId = "ECO_PARI_001"
        ),
        Partner(
            id = "partner_pari_02",
            name = "Crystal Plaza Green Hub",
            address = "Basement B1, Crystal Plaza, Commercial Belt, Pari Chowk",
            lat = 28.4658,
            lng = 77.5122,
            wasteTypesAccepted = listOf("plastic", "paper", "metal"),
            verified = true,
            qrCodeId = "ECO_PARI_002"
        ),
        Partner(
            id = "partner_pari_03",
            name = "Ansal Plaza Recycle Corner",
            address = "Ground Floor Service Lane, Ansal Plaza Mall, Pari Chowk",
            lat = 28.4691,
            lng = 77.5165,
            wasteTypesAccepted = listOf("e-waste", "plastic"),
            verified = false,
            qrCodeId = "ECO_PARI_003"
        ),
        Partner(
            id = "partner_alpha_01",
            name = "Alpha-1 Community Recyclers",
            address = "Near Community Center, C-Block Market, Sector Alpha 1",
            lat = 28.4735,
            lng = 77.5105,
            wasteTypesAccepted = listOf("plastic", "paper"),
            verified = true,
            qrCodeId = "ECO_ALPHA_001"
        ),
        Partner(
            id = "partner_alpha_02",
            name = "Alpha-2 CleanTech Depot",
            address = "Shop 12, Main Shopping Complex, Sector Alpha 2",
            lat = 28.4782,
            lng = 77.5050,
            wasteTypesAccepted = listOf("e-waste"),
            verified = true,
            qrCodeId = "ECO_ALPHA_002"
        ),
        Partner(
            id = "partner_alpha_03",
            name = "Nav Bharat Scrap & Circular Hub",
            address = "Plot 45, Pocket E, Sector Alpha 1",
            lat = 28.4710,
            lng = 77.5130,
            wasteTypesAccepted = listOf("glass", "metal", "e-waste", "plastic", "paper"),
            verified = false,
            qrCodeId = "ECO_ALPHA_003"
        ),
        Partner(
            id = "partner_beta_01",
            name = "Beta-1 Green Center",
            address = "Central Park Market, Near Gate 3, Sector Beta 1",
            lat = 28.4830,
            lng = 77.5085,
            wasteTypesAccepted = listOf("glass", "plastic", "paper"),
            verified = true,
            qrCodeId = "ECO_BETA_001"
        ),
        Partner(
            id = "partner_beta_02",
            name = "Beta-2 TechCycle Station",
            address = "Adjacent to City Park Post Office, Sector Beta 2",
            lat = 28.4875,
            lng = 77.5020,
            wasteTypesAccepted = listOf("e-waste", "paper"),
            verified = true,
            qrCodeId = "ECO_BETA_002"
        ),
        Partner(
            id = "partner_beta_03",
            name = "Pragati Eco Collectors",
            address = "Shop 7, B-Block Commercial Complex, Sector Beta 1",
            lat = 28.4815,
            lng = 77.5120,
            wasteTypesAccepted = listOf("plastic"),
            verified = false,
            qrCodeId = "ECO_BETA_003"
        ),
        Partner(
            id = "partner_gamma_01",
            name = "Jagat Farm Circular Point",
            address = "Kiosk 18, Central Roundabout, Jagat Farm Market, Sector Gamma 1",
            lat = 28.4915,
            lng = 77.5125,
            wasteTypesAccepted = listOf("e-waste", "plastic", "paper"),
            verified = true,
            qrCodeId = "ECO_GAMMA_001"
        ),
        Partner(
            id = "partner_gamma_02",
            name = "Gamma-1 Paper & Plastic Bank",
            address = "Near Union Bank, Jagat Farm Market, Sector Gamma 1",
            lat = 28.4930,
            lng = 77.5140,
            wasteTypesAccepted = listOf("paper", "plastic"),
            verified = true,
            qrCodeId = "ECO_GAMMA_002"
        ),
        Partner(
            id = "partner_gamma_03",
            name = "Gamma-2 Green Bin Facility",
            address = "Near Community Club, Sector Gamma 2",
            lat = 28.4965,
            lng = 77.5070,
            wasteTypesAccepted = listOf("e-waste"),
            verified = true,
            qrCodeId = "ECO_GAMMA_003"
        ),
        Partner(
            id = "partner_suraj_01",
            name = "Surajpur Industrial Green Exchange",
            address = "Plot 18, UPSIDC Site B Industrial Area, Surajpur",
            lat = 28.5150,
            lng = 77.4980,
            wasteTypesAccepted = listOf("metal", "glass", "e-waste", "plastic"),
            verified = true,
            qrCodeId = "ECO_SURAJ_001"
        ),
        Partner(
            id = "partner_suraj_02",
            name = "Surajpur Wetland Eco Center",
            address = "Main Entrance Road, Near Bird Sanctuary, Surajpur",
            lat = 28.5240,
            lng = 77.4920,
            wasteTypesAccepted = listOf("plastic", "paper"),
            verified = true,
            qrCodeId = "ECO_SURAJ_002"
        ),
        Partner(
            id = "partner_suraj_03",
            name = "Vikas Scrap & Metal Works",
            address = "Surajpur Main Market Road, Near Police Chowki, Surajpur",
            lat = 28.5080,
            lng = 77.4890,
            wasteTypesAccepted = listOf("metal", "glass", "e-waste", "plastic", "paper"),
            verified = false,
            qrCodeId = "ECO_SURAJ_003"
        )
    )
}
