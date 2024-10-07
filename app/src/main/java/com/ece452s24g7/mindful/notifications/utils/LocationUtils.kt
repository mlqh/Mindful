package com.ece452s24g7.mindful.notifications.utils

import android.location.Location
import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    // Haversine formula (great circle distance)
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun isSignificantChange(prevLocation: Location, newLocation: Location, thresholdKm: Double = 50.0): Boolean {
        val distance = calculateDistance(
            prevLocation.latitude,
            prevLocation.longitude,
            newLocation.latitude,
            newLocation.longitude
        )
        return distance > thresholdKm
    }
}