package com.ece452s24g7.mindful.notifications.workers

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import com.ece452s24g7.mindful.notifications.utils.LocationUtils

class LocationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

    override suspend fun doWork(): Result {
        try {
            val location = getLastLocation()
            if (location != null) {
                val prevLocation = getPreviousLocation()
                if (prevLocation == null || LocationUtils.isSignificantChange(
                        prevLocation,
                        location
                    )
                ) {
                    notifyLocationChange()
                    saveLocation(location)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    // Most recent location of the user (since we are running a background service, we aren't
    // formally requesting a *new* location from the device)
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { cont ->
        // Although this doesn't get scheduled without permissions, they could be retroactively revoked
        if (ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@suspendCancellableCoroutine
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            cont.resume(location)
        }.addOnFailureListener { exception ->
            cont.resumeWithException(exception)
        }
    }

    // Previous location that was recorded by Mindful
    private fun getPreviousLocation(): Location? {
        val lat = sharedPreferences.getFloat(PREV_LOCATION_LAT, Float.MIN_VALUE)
        val lon = sharedPreferences.getFloat(PREV_LOCATION_LON, Float.MIN_VALUE)

        return if (lat != Float.MIN_VALUE && lon != Float.MIN_VALUE) {
            Location("").apply {
                latitude = lat.toDouble()
                longitude = lon.toDouble()
            }
        } else {
            null
        }
    }

    // If the user is in a new place, save it
    private fun saveLocation(location: Location) {
        sharedPreferences.edit().apply {
            putFloat(PREV_LOCATION_LAT, location.latitude.toFloat())
            putFloat(PREV_LOCATION_LON, location.longitude.toFloat())
            apply()
        }
    }

    private fun notifyLocationChange() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Mindful: You've moved!")
            .setContentText("Would you like to write a journal entry about this place?")
            .build()

        notificationManager.notify(2, notification)
    }

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        private const val PREV_LOCATION_LAT = "prev_location_lat"
        private const val PREV_LOCATION_LON = "prev_location_lon"
    }
}

