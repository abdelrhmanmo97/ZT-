package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {

    // Default Company Workplace Coordinates (Cairo Central Business District / Head Office)
    const val DEFAULT_WORK_LAT = 30.0444
    const val DEFAULT_WORK_LNG = 31.2357
    const val DEFAULT_GEOFENCE_RADIUS_METERS = 250.0

    /**
     * Calculates distance between two GPS coordinates in meters using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Checks if current coordinates fall within company geofence radius.
     */
    fun isWithinGeofence(
        currentLat: Double,
        currentLng: Double,
        workLat: Double,
        workLng: Double,
        radiusMeters: Double
    ): Boolean {
        val distance = calculateDistanceMeters(currentLat, currentLng, workLat, workLng)
        return distance <= radiusMeters
    }

    /**
     * Formats distance in Arabic for UI display.
     */
    fun formatDistanceArabic(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} متر"
        } else {
            String.format("%.1f كم", meters / 1000.0)
        }
    }

    /**
     * Attempts to read last known location from LocationManager.
     * Returns fallback coordinates if location permission is not granted or hardware is unavailable.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(
        context: Context,
        fallbackLat: Double = DEFAULT_WORK_LAT,
        fallbackLng: Double = DEFAULT_WORK_LNG
    ): Pair<Double, Double> {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager?.isProviderEnabled(provider) == true) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            }
            if (bestLocation != null) {
                Pair(bestLocation.latitude, bestLocation.longitude)
            } else {
                Pair(fallbackLat, fallbackLng)
            }
        } catch (_: Exception) {
            Pair(fallbackLat, fallbackLng)
        }
    }
}
