package eu.mcomputing.mobv.zadanie.workers

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.mcomputing.mobv.zadanie.data.GeofenceManager
import eu.mcomputing.mobv.zadanie.data.LocationManager
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.ui.utils.GeofenceUtils

class GeofenceWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val dataRepository = DataRepository.getInstance(context)
    private val locationManager = LocationManager(context)
    private val geofenceManager = GeofenceManager(context)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun doWork(): Result {
        val action = inputData.getString("ACTION") ?: return Result.failure()
        Log.d("GeofenceWorker", "Geofence action $action")

        return try {
            when(action) {
                "CREATE" -> {
                    val location = locationManager.getCurrentLocationSuspend()
                    if (location == null) {
                        return Result.failure()
                    }

                    geofenceManager.addGeofence(
                        location.latitude,
                        location.longitude,
                        GeofenceUtils.RADIUS_SIZE.toDouble(),
                        "geofence")
                    dataRepository.apiCreateGeofence(location.latitude,
                        location.longitude, GeofenceUtils.RADIUS_SIZE)
                    dataRepository.apiGetGeofence()
                }
                "REMOVE" -> {
                    Log.d("Geofence", "Removed geofence")
                    geofenceManager.removeGeofence("geofence")
                    dataRepository.clearLocation();
                    dataRepository.apiDeleteGeofence();
                }
                else -> return Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
