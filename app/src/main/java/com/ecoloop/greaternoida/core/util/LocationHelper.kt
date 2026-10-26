package com.ecoloop.greaternoida.core.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates great-circle distance between two coordinates in kilometers using Haversine formula.
     * Returns rounded to 1 decimal place (e.g. 1.4 km).
     */
    fun calculateDistanceKm(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_KM * c

        return round(distance * 10) / 10.0
    }
}
